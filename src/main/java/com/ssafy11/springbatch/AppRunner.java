package com.ssafy11.springbatch;

import com.ssafy11.springbatch.domain.book.Book;
import com.ssafy11.springbatch.domain.book.BookRepository;
import com.ssafy11.springbatch.domain.book.WishBook;
import com.ssafy11.springbatch.domain.book.WishBookRepository;
import com.ssafy11.springbatch.domain.rental.Rental;
import com.ssafy11.springbatch.domain.rental.RentalRepository;
import com.ssafy11.springbatch.domain.rental.RentalStatus;
import com.ssafy11.springbatch.domain.user.User;
import com.ssafy11.springbatch.domain.user.UserRepository;
import com.ssafy11.springbatch.domain.user.experience.ExperienceRepository;
import com.ssafy11.springbatch.domain.user.point.Point;
import com.ssafy11.springbatch.domain.user.point.PointHistory;
import com.ssafy11.springbatch.domain.user.point.PointRepository;
import com.ssafy11.springbatch.domain.userbook.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class AppRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserbookRepository userbookRepository;
    private final RentalRepository rentalRepository;
    private final BookRepository bookRepository;
    private final PointRepository pointRepository;
    private final WishBookRepository wishBookRepository;
    private final ExperienceRepository experienceRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
//         initData();
    }

    private void initData() {
        // 소유자
        User owner = User.builder()
                .email("trewq231@naver.com")
                .nickname("owner")
                .password("password")
                .areaCode("1111")
                .build();
        owner = this.userRepository.save(owner);
        Point ownerPoint = Point.builder()
            .user(owner)
            .history(PointHistory.BOOK_RENTAL)
            .amount(PointHistory.BOOK_RENTAL.getAmount())
            .build();
        this.pointRepository.save(ownerPoint);

        // 대여자
        User rentalUser = User.builder()
                .email("kwondh5217@gmail.com")
                .nickname("rentalUser")
                .password("password")
                .areaCode("1111")
                .build();
        rentalUser = this.userRepository.save(rentalUser);

        // Book 객체 생성
        Book book = Book.builder()
                .isbn("978-3-16-148410-0")
                .title("Example Book Title")
                .author("Jane Doe")
                .publisher("Example Publisher")
                .publicationDate(LocalDate.of(2021, 1, 15))
                .thumbnail("http://example.com/thumbnail.jpg")
                .description("This is a detailed description of the example book.")
                .build();
        book = this.bookRepository.save(book);

        // 도서
        Userbook userbook = Userbook.builder()
                .book(book)
                .qualityStatus(QualityStatus.NORMAL)
                .registerType(RegisterType.RENTAL)
                .tradeStatus(TradeStatus.RENTAL_AVAILABLE)
                .user(owner)
                .build();
        userbook = this.userbookRepository.save(userbook);

        // 관심 도서
        WishBook wishBook = WishBook.builder()
            .user(rentalUser)
            .userbook(userbook)
            .build();
        wishBook = this.wishBookRepository.save(wishBook);

        // 대여 A
        Rental rentalA = Rental.builder()
                .user(rentalUser)
                .userbook(userbook)
                .rentalStatus(RentalStatus.IN_PROGRESS)
                .startDate(LocalDateTime.of(2024, 05, 01, 12, 00, 00))
                .endDate(LocalDateTime.of(2024, 05, 10, 12, 00, 00))
                .build();
        rentalA = this.rentalRepository.save(rentalA);

        // 대여 B
        Rental rentalB = Rental.builder()
            .user(rentalUser)
            .userbook(userbook)
            .rentalStatus(RentalStatus.IN_PROGRESS)
            .startDate(LocalDateTime.of(2024, 05, 01, 12, 00, 00))
            .endDate(LocalDateTime.of(2024, 05, 03, 12, 00, 00))
            .build();
        rentalB = this.rentalRepository.save(rentalB);

        // 대여 C
        Rental rentalC = Rental.builder()
            .user(rentalUser)
            .userbook(userbook)
            .rentalStatus(RentalStatus.IN_PROGRESS)
            .startDate(LocalDateTime.of(2024, 05, 01, 12, 00, 00))
            .endDate(LocalDateTime.of(2024, 07, 27, 12, 00, 00))
            .build();
        rentalC = this.rentalRepository.save(rentalC);

        // 대여 D
        Rental rentalD = Rental.builder()
            .user(rentalUser)
            .userbook(userbook)
            .rentalStatus(RentalStatus.IN_PROGRESS)
            .startDate(LocalDateTime.of(2024, 05, 01, 12, 00, 00))
            .endDate(LocalDateTime.of(2024, 8, 04, 12, 00, 00))
            .build();
        rentalD = this.rentalRepository.save(rentalD);
    }
}
