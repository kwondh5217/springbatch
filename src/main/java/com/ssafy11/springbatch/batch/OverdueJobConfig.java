package com.ssafy11.springbatch.batch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.springframework.util.Assert;

import com.ssafy11.springbatch.batch.dto.UserDelete;
import com.ssafy11.springbatch.batch.dto.UserPersonal;
import com.ssafy11.springbatch.batch.writer.UserDeleteItemWriter;
import com.ssafy11.springbatch.batch.writer.UserPersonalItemWriter;
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

@Slf4j
@RequiredArgsConstructor
@Configuration
public class OverdueJobConfig {

	private final JobRepository jobRepository;
	private final EntityManagerFactory entityManagerFactory;
	private final EmailSender emailSender;
	private final PlatformTransactionManager transactionManager;
	private int chunkSize;
	private static final String PENALTY_SUBJECT = "우주도서 연체 알림";
	private static final String PENALTY_TEXT = "포인트와 경험치가 차감되었습니다.";
	private static final String USER_DELETE_SUBJECT = "우주도서 회원 탈퇴 처리 안내";
	private static final String USER_DELETE_TEXT = "이용 규칙 위반으로 회원탈퇴 처리 되었습니다.";

	@Value("${chunkSize:1000}")
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	@Bean
	public Job overdueJob() {
		return new JobBuilder("overdueJob", jobRepository)
			.start(overduePersonalStep())
			.next(overdueDeleteUserStep())
			.build();
	}

	@Bean
	public Step overduePersonalStep() {
		return new StepBuilder("overduePersonalStep", jobRepository)
			.<Rental, UserPersonal>chunk(chunkSize, transactionManager)
			.reader(jpaRentalCursorItemReader())
			.processor(overdueProcessor(null))
			.writer(userPersonalItemWriter())
			.listener(chunkListener(PENALTY_SUBJECT, PENALTY_TEXT))
			.startLimit(5)
			.build();
	}

	@Bean
	public Step overdueDeleteUserStep() {
		return new StepBuilder("overdueDeleteUserStep", jobRepository)
			.<User, UserDelete>chunk(chunkSize, transactionManager)
			.reader(jpaUserCursorItemReader())
			.processor(userDeleteProcessor())
			.writer(userDeleteItemWriter())
			.listener(chunkListener(USER_DELETE_SUBJECT, USER_DELETE_TEXT))
			.startLimit(5)
			.build();
	}

	@Bean
	public JpaCursorItemReader<User> jpaUserCursorItemReader() {
		return new JpaCursorItemReaderBuilder<User>()
			.name("jpaUserCursorItemReader")
			.entityManagerFactory(entityManagerFactory)
			.queryString(
				"SELECT u FROM User u WHERE u.id IN (SELECT u.id FROM User u JOIN u.rentals r WHERE r.endDate < :threeDaysAgo AND r.rentalStatus = 'IN_PROGRESS' GROUP BY u.id) OR u.id IN (SELECT p.user.id FROM Point p GROUP BY p.user.id HAVING SUM(p.amount) < 0)")
			.parameterValues(Collections.singletonMap("threeDaysAgo", LocalDateTime.now().minusDays(3)))
			.build();
	}

	@Bean
	public JpaCursorItemReader<Rental> jpaRentalCursorItemReader() {
		return new JpaCursorItemReaderBuilder<Rental>()
			.name("jpaRentalCursorItemReader")
			.entityManagerFactory(entityManagerFactory)
			.queryString(
				"SELECT r FROM Rental r WHERE r.endDate < CURRENT_TIMESTAMP AND r.rentalStatus = 'IN_PROGRESS'")
			.build();
	}

	@Bean
	public ItemProcessor<User, UserDelete> userDeleteProcessor() {
		return user -> {
			Assert.notNull(user, "user is null");

			List<Userbook> userbooks = user.getUserbooks();
			List<Rental> rentals = user.getRentals();
			List<WishBook> wishBooks = user.getWishBooks();

			ExecutionContext executionContext = StepSynchronizationManager.getContext()
				.getStepExecution()
				.getExecutionContext();
			List<User> users = (List<User>)executionContext.get("users");
			if (users == null) {
				users = new ArrayList<>();
				executionContext.put("users", users);
			}
			users.add(user);

			return new UserDelete(user, userbooks, wishBooks, rentals);
		};
	}

	@Bean
	@StepScope
	public ItemProcessor<Rental, UserPersonal> overdueProcessor(
		@Value("#{jobParameters['currentDate']}") String currentDate) {
		return rental -> {
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
	public ItemWriter<UserDelete> userDeleteItemWriter() {
		return new UserDeleteItemWriter(entityManagerFactory);
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
