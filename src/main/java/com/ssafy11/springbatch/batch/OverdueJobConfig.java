package com.ssafy11.springbatch.batch;

import com.ssafy11.springbatch.domain.book.WishBook;
import com.ssafy11.springbatch.domain.rental.Rental;
import com.ssafy11.springbatch.domain.user.User;
import com.ssafy11.springbatch.domain.user.experience.Experience;
import com.ssafy11.springbatch.domain.user.experience.ExperienceHistory;
import com.ssafy11.springbatch.domain.user.point.Point;
import com.ssafy11.springbatch.domain.user.point.PointHistory;
import com.ssafy11.springbatch.domain.userbook.Userbook;

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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
	private final EmailSender emailSender;
	private int chunkSize;
	private static final String PENALTY_SUBJECT = "우주도서 연체 알림";
	private static final String PENALTY_TEXT = "포인트와 경험치가 차감되었습니다.";

	@Value("${chunkSize:1000}")
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	@Bean
	public Job overdueJob(PlatformTransactionManager transactionManager) {
		return new JobBuilder("overdueJob", jobRepository)
			.start(overduePersonalStep(transactionManager))
			.build();
	}

	// @Bean
	// public Step overduePersonalStep(PlatformTransactionManager transactionManager) {
	// 	return new StepBuilder("overduePersonalStep", jobRepository)
	// 		.<Rental, UserPersonal>chunk(chunkSize, transactionManager)
	// 		.reader(jpaCursorItemReader())
	// 		.processor(overdueProcessor(null))
	// 		.writer(userPersonalItemWriter())
	// 		.listener(chunkListener(PENALTY_SUBJECT, PENALTY_TEXT))
	// 		.build();
	// }

	@Bean
	public Step overduePersonalStep(PlatformTransactionManager transactionManager) {
		return new StepBuilder("overduePersonalStep", jobRepository)
			.<Rental, UserDelete>chunk(chunkSize, transactionManager)
			.reader(jpaCursorItemReader())
			.processor(userDeleteProcessor())
			.writer(userDeleteItemWriter())
			.listener(chunkListener(PENALTY_SUBJECT, PENALTY_TEXT))
			.build();
	}

	@Bean
	public JpaCursorItemReader<Rental> jpaCursorItemReader() {
		return new JpaCursorItemReaderBuilder<Rental>()
			.name("jpaCursorItemReader")
			.entityManagerFactory(entityManagerFactory)
			.queryString("SELECT r FROM Rental r WHERE r.endDate < CURRENT_TIMESTAMP AND r.rentalStatus = 'IN_PROGRESS'")
			.build();
	}

	@Bean
	public ItemProcessor<Rental, UserDelete> userDeleteProcessor() {
		return rental -> {
			User user = rental.getUser();
			List<Userbook> userbooks = user.getUserbooks();
			List<WishBook> wishBooks = user.getWishBooks();
			List<Rental> rentals = user.getRentals();

			userbooks.forEach(Userbook::removeUser);
			wishBooks.forEach(WishBook::removeUser);
			rentals.forEach(Rental::removeUser);

			return new UserDelete(user, userbooks, wishBooks, rentals);
		};
	}

	@Bean
	@StepScope
	public ItemProcessor<Rental, UserPersonal> overdueProcessor(
		@Value("#{jobParameters['currentTime']}") String currentTime) {
		Thread thread = Thread.currentThread();
		log.info("processor Thread : {}", thread.getName());
		return rental -> {
			LocalDate today = LocalDate.now();
			LocalDate endDate = rental.getEndDate().toLocalDate();
			long daysOverdue = ChronoUnit.DAYS.between(endDate, today);

			User user = rental.getUser();

			ExecutionContext executionContext = StepSynchronizationManager.getContext()
				.getStepExecution()
				.getExecutionContext();
			List<User> users = (List<User>)executionContext.get("users");
			if (users == null) {
				users = new ArrayList<>();
				executionContext.put("users", users);
			}
			users.add(user);

			Point point = Point.builder()
				.user(user)
				.history(PointHistory.OVERDUE)
				.amount(PointHistory.OVERDUE.getAmount())
				.build();

			Experience experience = Experience.builder()
				.user(user)
				.history(ExperienceHistory.OVERDUE)
				.amount(ExperienceHistory.OVERDUE.getAmount())
				.build();

			UserPersonal userPersonal = new UserPersonal(point, experience);
			log.info("userPersonal : {}", userPersonal);
			return userPersonal;
		};
	}

	@Bean
	public ItemWriter<UserPersonal> userPersonalItemWriter() {
		return new UserPersonalItemWriter(pointItemWriter(), experienceItemWriter());
	}

	@Bean
	public ItemWriter<UserDelete> userDeleteItemWriter () {
		return new UserDeleteItemWriter(userItemWriter(), userbookItemWriter(), wishBookItemWriter(), rentalItemWriter());
	}

	@Bean
	public JpaItemWriter<User> userItemWriter() {
		return new JpaItemWriterBuilder<User>()
			.entityManagerFactory(entityManagerFactory)
			.build();
	}

	@Bean
	public JpaItemWriter<Userbook> userbookItemWriter() {
		return new JpaItemWriterBuilder<Userbook>()
			.entityManagerFactory(entityManagerFactory)
			.build();
	}

	@Bean
	public JpaItemWriter<WishBook> wishBookItemWriter() {
		return new JpaItemWriterBuilder<WishBook>()
			.entityManagerFactory(entityManagerFactory)
			.build();
	}

	@Bean
	public JpaItemWriter<Rental> rentalItemWriter() {
		return new JpaItemWriterBuilder<Rental>()
			.entityManagerFactory(entityManagerFactory)
			.build();
	}

	@Bean
	public JpaItemWriter<Point> pointItemWriter() {
		return new JpaItemWriterBuilder<Point>()
			.entityManagerFactory(entityManagerFactory)
			.build();
	}

	@Bean
	public JpaItemWriter<Experience> experienceItemWriter() {
		return new JpaItemWriterBuilder<Experience>()
			.entityManagerFactory(entityManagerFactory)
			.build();
	}

	@Bean
	public ChunkListener chunkListener(String subject, String text) {
		return new ChunkListener() {
			@Override
			public void afterChunk(ChunkContext context) {
				StepExecution stepExecution = context.getStepContext().getStepExecution();
				ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
				List<User> users = (List<User>)stepExecutionContext.get("users");
				if (users != null) {
					emailSender.sendEmails(users, subject, text);
					stepExecutionContext.remove("users"); // 메일 전송 후 사용자 목록 초기화
				}
			}
		};
	}
}
