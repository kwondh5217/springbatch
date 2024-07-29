package com.ssafy11.springbatch.batch;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import com.ssafy11.springbatch.domain.book.WishBook;
import com.ssafy11.springbatch.domain.rental.Rental;
import com.ssafy11.springbatch.domain.user.User;
import com.ssafy11.springbatch.domain.userbook.Userbook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UserDeleteItemWriter implements ItemWriter<UserDelete> {

	private final ItemWriter<User> userWriter;
	private final ItemWriter<Userbook> userbookItemWriter;
	private final ItemWriter<WishBook> wishBookItemWriter;
	private final ItemWriter<Rental> rentalItemWriter;

	@Override
	public void write(Chunk<? extends UserDelete> chunk) throws Exception {
		List<User> users = new ArrayList<>();
		List<Userbook> userbooks = new ArrayList<>();
		List<WishBook> wishBooks = new ArrayList<>();
		List<Rental> rentals = new ArrayList<>();

		for (UserDelete userDelete : chunk) {
			users.add(userDelete.getUser());
			userbooks.addAll(userDelete.getUserbooks());
			wishBooks.addAll(userDelete.getWishBooks());
			rentals.addAll(userDelete.getRentals());
		}

		log.info("users size: " + users.size());
		log.info("userbooks size: " + userbooks.size());
		log.info("wishBooks size: " + wishBooks.size());
		log.info("rentals size: " + rentals.size());

		userWriter.write(new Chunk<>(users));
		userbookItemWriter.write(new Chunk<>(userbooks));
		wishBookItemWriter.write(new Chunk<>(wishBooks));
		rentalItemWriter.write(new Chunk<>(rentals));
	}
}
