package com.ssafy11.springbatch.batch.writer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

import com.ssafy11.springbatch.batch.dto.UserDelete;
import com.ssafy11.springbatch.domain.book.WishBook;
import com.ssafy11.springbatch.domain.rental.Rental;
import com.ssafy11.springbatch.domain.user.User;
import com.ssafy11.springbatch.domain.userbook.Userbook;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UserDeleteItemWriter implements ItemWriter<UserDelete> {

	private final EntityManagerFactory entityManagerFactory;

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

		log.info("user count : {}", users.size());

		EntityManager entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
		if (entityManager == null) {
			throw new DataAccessResourceFailureException("Unable to obtain a transactional EntityManager");
		}
		doWrite(rentals, entityManager, wishBooks, userbooks);
		deleteUser(entityManager, users);

		entityManager.flush();
	}

	private void doWrite(List<Rental> rentals, EntityManager entityManager, List<WishBook> wishBooks,
		List<Userbook> userbooks) {
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
	}

	private void deleteUser(EntityManager entityManager, List<User> users) {
		users.forEach(user -> {
			if(entityManager.contains(user)) {
				entityManager.remove(user);
			} else {
				User merge = entityManager.merge(user);
				entityManager.remove(merge);
			}
		});
	}
}
