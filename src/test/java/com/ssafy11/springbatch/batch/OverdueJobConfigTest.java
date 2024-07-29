package com.ssafy11.springbatch.batch;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.ssafy11.springbatch.domain.rental.Rental;
import com.ssafy11.springbatch.domain.rental.RentalRepository;
import com.ssafy11.springbatch.domain.rental.RentalStatus;
import com.ssafy11.springbatch.domain.user.User;
import com.ssafy11.springbatch.domain.user.UserRepository;
import com.ssafy11.springbatch.domain.user.experience.ExperienceHistory;
import com.ssafy11.springbatch.domain.user.experience.ExperienceView;
import com.ssafy11.springbatch.domain.user.experience.ExperienceViewRepository;
import com.ssafy11.springbatch.domain.user.point.PointHistory;
import com.ssafy11.springbatch.domain.user.point.PointView;
import com.ssafy11.springbatch.domain.user.point.PointViewRepository;

@SpringBootTest
@SpringBatchTest
class OverdueJobConfigTest {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RentalRepository rentalRepository;
	@Autowired
	private PointViewRepository pointViewRepository;
	@Autowired
	private ExperienceViewRepository experienceViewRepository;
	@MockBean
	private JavaMailSender mailSender;

	@DisplayName("연체가 발생하면 회원에게 알림을 주고, 회원의 경험치와 포인트를 차감한다")
	@Test
	void overdueJobTest() throws Exception {
		// given
		LocalDateTime startDate = LocalDateTime.of(2024, 07, 1, 12, 0, 0);
		LocalDateTime endDate = LocalDateTime.of(2024, 07, 2, 12, 0);

		User user = createUser();
		Rental rental = createRental(user, startDate, endDate);

		JobParameters jobParameters = new JobParametersBuilder()
			.addLocalDate("requestDate", LocalDate.of(2024, 07, 3))
			.toJobParameters();

		// when
		JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(jobParameters);

		// then
		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		verify(this.mailSender, atLeastOnce()).send(any(SimpleMailMessage.class));
		Optional<PointView> optionalPointView = this.pointViewRepository.findByUserId(user.getId());
		Optional<ExperienceView> optionalExperienceView = this.experienceViewRepository.findByUserId(user.getId());
		assertThat(optionalPointView.isPresent()).isTrue();
		assertThat(optionalExperienceView.isPresent()).isTrue();
		assertThat(optionalPointView.get().getTotalPoint()).isEqualTo(PointHistory.OVERDUE.getAmount());
		assertThat(optionalExperienceView.get().getTotalExperience()).isEqualTo(ExperienceHistory.OVERDUE.getAmount());
	}

	private Rental createRental(User user, LocalDateTime startDate, LocalDateTime endDate) {
		Rental rental = Rental.builder()
			.user(user)
			.startDate(startDate)
			.endDate(endDate)
			.rentalStatus(RentalStatus.IN_PROGRESS)
			.build();
		return this.rentalRepository.save(rental);
	}

	private User createUser() {
		User user = User.builder()
			.build();
		user = this.userRepository.save(user);
		return user;
	}
}