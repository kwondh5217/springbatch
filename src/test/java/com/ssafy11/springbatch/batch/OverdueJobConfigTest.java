package com.ssafy11.springbatch.batch;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.mail.javamail.JavaMailSender;

import com.ssafy11.springbatch.domain.book.WishBook;
import com.ssafy11.springbatch.domain.book.WishBookRepository;
import com.ssafy11.springbatch.domain.rental.Rental;
import com.ssafy11.springbatch.domain.rental.RentalRepository;
import com.ssafy11.springbatch.domain.rental.RentalStatus;
import com.ssafy11.springbatch.domain.user.User;
import com.ssafy11.springbatch.domain.user.UserRepository;
import com.ssafy11.springbatch.domain.user.experience.ExperienceHistory;
import com.ssafy11.springbatch.domain.user.experience.ExperienceView;
import com.ssafy11.springbatch.domain.user.experience.ExperienceViewRepository;
import com.ssafy11.springbatch.domain.user.point.Point;
import com.ssafy11.springbatch.domain.user.point.PointHistory;
import com.ssafy11.springbatch.domain.user.point.PointRepository;
import com.ssafy11.springbatch.domain.user.point.PointView;
import com.ssafy11.springbatch.domain.user.point.PointViewRepository;
import com.ssafy11.springbatch.domain.userbook.Userbook;
import com.ssafy11.springbatch.domain.userbook.UserbookRepository;

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
	@Autowired
	private WishBookRepository wishBookRepository;
	@Autowired
	private UserbookRepository userbookRepository;
	@Autowired
	private PointRepository pointRepository;
	@MockBean
	private JavaMailSender mailSender;

	@AfterEach
	void tearDown() {
		rentalRepository.deleteAll();
		pointRepository.deleteAll();
		wishBookRepository.deleteAll();
		userbookRepository.deleteAll();
		userRepository.deleteAll();
	}

	@DisplayName("연체가 발생한 회원의 포인트와 경험치를 차감 후, 연체가 3일 경과 또는 포인트가 음수이면 회원은 탈퇴된다")
	@Test
	void overdueJobTest() throws Exception {
		// given
		LocalDateTime startDate = LocalDateTime.of(LocalDate.now(), LocalTime.now()).minusDays(10);
		LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.now()).minusDays(1);

		User owner = createOwner();
		Userbook userbook = createUserbook(owner);
		User willDeleteuser = createUser();
		User overdueUser = createOverdueUser();

		createRental(willDeleteuser, userbook, startDate, endDate);
		createRental(overdueUser, userbook, startDate, endDate);

		JobParameters jobParameters = new JobParametersBuilder()
			.addLocalDate("requestDate", LocalDate.now())
			.toJobParameters();

		// when
		JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(jobParameters);

		// then
		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(this.userRepository.findById(willDeleteuser.getId()).isEmpty()).isTrue();
		assertThat(this.userRepository.findById(overdueUser.getId()).isPresent()).isTrue();
		assertThat(this.pointViewRepository.findByUserId(overdueUser.getId()).isPresent()).isTrue();
		assertThat(this.pointViewRepository.findByUserId(willDeleteuser.getId()).isEmpty()).isTrue();
	}

	@DisplayName("연체가 발생한 대여를 조회해, 회원의 포인트와 경험치를 차감한다")
	@Test
	void overduePersonalStep()  {
		// given
		LocalDateTime startDate = LocalDateTime.of(2024, 07, 1, 12, 0);
		LocalDateTime endDate = LocalDateTime.of(2024, 07, 2, 12, 0);

		User owner = createOwner();
		Userbook userbook = createUserbook(owner);
		User user = createUser();
		createRental(user, userbook, startDate, endDate);

		// when
		JobExecution jobExecution = this.jobLauncherTestUtils.launchStep("overduePersonalStep");

		// then
		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		Optional<PointView> optionalPointView = this.pointViewRepository.findByUserId(user.getId());
		Optional<ExperienceView> optionalExperienceView = this.experienceViewRepository.findByUserId(user.getId());
		assertThat(optionalPointView.isPresent()).isTrue();
		assertThat(optionalExperienceView.isPresent()).isTrue();
		assertThat(optionalPointView.get().getTotalPoint()).isEqualTo(PointHistory.OVERDUE.getAmount());
		assertThat(optionalExperienceView.get().getTotalExperience()).isEqualTo(ExperienceHistory.OVERDUE.getAmount());

	}


	@DisplayName("연체가 3일 이상 발생하였거나, 포인트가 음수라면 회원은 탈퇴처리 된다")
	@Test
	void overdueDeleteUserStep() {
		// given
		User owner = createOwner();
		User user = createUser();

		Userbook ownerUserbook = createUserbook(owner);
		Userbook userbook = createUserbook(user);

		WishBook wishBook = createWishBook(user, ownerUserbook);

		LocalDateTime startDate = LocalDateTime.of(2024, 07, 1, 12, 0);
		LocalDateTime endDate = LocalDateTime.of(2024, 07, 2, 12, 0);
		createRental(user, ownerUserbook, startDate, endDate);

		Point point = Point.builder()
			.user(user)
			.history(PointHistory.OVERDUE)
			.amount(PointHistory.OVERDUE.getAmount())
			.build();
		pointRepository.save(point);

		Optional<PointView> optionalPointView = this.pointViewRepository.findByUserId(user.getId());
		assertThat(optionalPointView.isPresent()).isTrue();
		assertThat(optionalPointView.get().getTotalPoint()).isEqualTo(PointHistory.OVERDUE.getAmount());

		// when
		JobExecution jobExecution = this.jobLauncherTestUtils.launchStep("overdueDeleteUserStep");

		// then
		Userbook userbookById = this.userbookRepository.findById(userbook.getId()).get();
		WishBook wishBookById = this.wishBookRepository.findById(wishBook.getId()).get();
		Optional<User> userById = this.userRepository.findById(user.getId());

		assertThat(userById.isEmpty()).isTrue();
		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(userbookById.getUser()).isNull();
		assertThat(wishBookById.getUser()).isNull();
	}

	private Userbook createUserbook(User user) {
		Userbook userbook = Userbook.builder()
			.user(user)
			.build();
		return this.userbookRepository.save(userbook);
	}


	private WishBook createWishBook(User user, Userbook userbook) {
		WishBook wishBook = WishBook.builder()
			.user(user)
			.userbook(userbook)
			.build();
		return this.wishBookRepository.save(wishBook);
	}

	private Rental createRental(User user, Userbook userbook, LocalDateTime startDate, LocalDateTime endDate) {
		Rental rental = Rental.builder()
			.user(user)
			.userbook(userbook)
			.startDate(startDate)
			.endDate(endDate)
			.rentalStatus(RentalStatus.IN_PROGRESS)
			.build();
		return this.rentalRepository.save(rental);
	}

	private User createOwner() {
		User user = User.builder()
			.email("trewq231@naver.com")
			.build();
		return this.userRepository.save(user);
	}

	private User createUser() {
		User user = User.builder()
			.email("kwondh5217@gmail.com")
			.build();
		return this.userRepository.save(user);
	}

	private User createOverdueUser() {
		User user = User.builder()
			.email("2020215728@dongguk.ac.kr")
			.build();

		User save = this.userRepository.save(user);

		Point point = Point.builder()
			.user(user)
			.history(PointHistory.BOOK_REGISTER)
			.amount(PointHistory.BOOK_REGISTER.getAmount())
			.build();
		pointRepository.save(point);

		return save;
	}
}