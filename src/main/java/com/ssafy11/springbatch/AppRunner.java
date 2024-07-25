package com.ssafy11.springbatch;

import com.ssafy11.springbatch.domain.book.Book;
import com.ssafy11.springbatch.domain.book.BookRepository;
import com.ssafy11.springbatch.domain.rental.Rental;
import com.ssafy11.springbatch.domain.rental.RentalRepository;
import com.ssafy11.springbatch.domain.rental.RentalStatus;
import com.ssafy11.springbatch.domain.user.User;
import com.ssafy11.springbatch.domain.user.UserRepository;
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

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // initData();
    }

    private void initData() {
        // 소유자
        User owner = User.builder()
                .email("trewq231@naver.com")
                .nickname("owner")
                .password("password")
                .areaCode("1111")
                .point(1000L)
                .experience(1000L)
                .build();
        owner = this.userRepository.save(owner);

        // 대여자
        User rentalUser = User.builder()
                .email("kwondh5217@gmail.com")
                .nickname("rentalUser")
                .password("password")
                .areaCode("1111")
                .point(1000L)
                .experience(1000L)
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

        // 대여
        Rental rental = Rental.builder()
                .user(rentalUser)
                .userbook(userbook)
                .rentalStatus(RentalStatus.IN_PROGRESS)
                .startDate(LocalDateTime.of(24, 05, 01, 12, 00, 00))
                .endDate(LocalDateTime.of(24, 05, 10, 12, 00, 00))
                .build();
        rental = this.rentalRepository.save(rental);
    }
}
