package com.ssafy11.springbatch.batch;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import com.ssafy11.springbatch.domain.book.WishBook;
import com.ssafy11.springbatch.domain.rental.Rental;
import com.ssafy11.springbatch.domain.user.User;
import com.ssafy11.springbatch.domain.userbook.Userbook;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UserDeleteItemWriter implements ItemWriter<UserDelete> {

	private final ItemWriter<User> userWriter;
	private final EntityManager entityManager;

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

		removeMappings(userbooks, wishBooks, rentals);

		log.info("users size: " + users.size());
		log.info("userbooks size: " + userbooks.size());
		log.info("wishBooks size: " + wishBooks.size());
		log.info("rentals size: " + rentals.size());

		userWriter.write(new Chunk<>(users));
	}

	public void removeMappings(List<Userbook> userbooks, List<WishBook> wishBooks, List<Rental> rentals) {
		entityManager.getTransaction().begin();

		rentals.forEach(rental -> {
			if (entityManager.contains(rental)) {
				rental.removeUser();
			} else {
				Rental merge = entityManager.merge(rental);
				merge.removeUser();
			}
		});

		wishBooks.forEach(wishBook -> {
			if (entityManager.contains(wishBook)) {
				wishBook.removeUser();
			} else {
				WishBook merge = entityManager.merge(wishBook);
				merge.removeUser();
			}
		});

		userbooks.forEach(userbook -> {
			if (entityManager.contains(userbook)) {
				userbook.removeUser();
			} else {
				Userbook merge = entityManager.merge(userbook);
				merge.removeUser();
			}
		});

		entityManager.flush();
		entityManager.getTransaction().commit();
	}
}
