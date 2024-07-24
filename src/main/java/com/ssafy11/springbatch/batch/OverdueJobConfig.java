package com.ssafy11.springbatch.batch;

import com.ssafy11.springbatch.domain.rental.Rental;
import com.ssafy11.springbatch.domain.user.User;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class OverdueJobConfig {

    private final JobRepository jobRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final JavaMailSender mailSender;
    private int chunkSize;

    @Value("${chunkSize:1000}")
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Bean
    public Job overdueJob(PlatformTransactionManager transactionManager) {
        return new JobBuilder("overdueJob", jobRepository)
                .start(overdueStep(transactionManager))
                .build();
    }

    @Bean
    public Step overdueStep(PlatformTransactionManager transactionManager) {
        return new StepBuilder("overdueStep", jobRepository)
                .<Rental, User>chunk(chunkSize, transactionManager)
                .reader(jpaCursorItemReader())
                .processor(overdueProcessor(null))
                .writer(jpaItemWriter())
                .listener(chunkListener(mailSender))
                .build();
    }

    @Bean
    public JpaCursorItemReader<Rental> jpaCursorItemReader() {
        return new JpaCursorItemReaderBuilder<Rental>()
                .name("jpaCursorItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT r FROM Rental r WHERE r.endDate < CURRENT_TIMESTAMP AND r.rentalStatus != 'COMPLETED'")
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Rental, User> overdueProcessor(@Value("#{jobParameters['currentTime']}") String currentTime) {
        Thread thread = Thread.currentThread();
        log.info("processor Thread : {}", thread.getName());
        return rental -> {
            LocalDate today = LocalDate.now();
            LocalDate endDate = rental.getEndDate().toLocalDate();
            long daysOverdue = ChronoUnit.DAYS.between(endDate, today);

            User user = rental.getUser();
            user.overdueCharge(daysOverdue);

            ExecutionContext executionContext = StepSynchronizationManager.getContext().getStepExecution().getExecutionContext();
            List<User> users = (List<User>) executionContext.get("users");
            if (users == null) {
                users = new ArrayList<>();
                executionContext.put("users", users);
            }
            users.add(user);

            return user;
        };
    }

    @Bean
    public JpaItemWriter<User> jpaItemWriter() {
        return new JpaItemWriterBuilder<User>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public ChunkListener chunkListener(JavaMailSender mailSender) {
        return new ChunkListener() {
            @Override
            public void afterChunk(ChunkContext context) {
                StepExecution stepExecution = context.getStepContext().getStepExecution();
                ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
                List<User> users = (List<User>) stepExecutionContext.get("users");
                if (users != null) {
                    sendEmails(users, mailSender);
                    stepExecutionContext.remove("users"); // 메일 전송 후 사용자 목록 초기화
                }
            }
        };
    }

    @Async
    protected void sendEmails(List<User> users, JavaMailSender mailSender) {
        Thread thread = Thread.currentThread();
        log.info("send Mail Thread : {}", thread.getName());
        for (User user : users) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("우주도서 연체 알림");
            message.setText("포인트와 경험치가 차감되었습니다.");
            mailSender.send(message);
        }
    }
}
