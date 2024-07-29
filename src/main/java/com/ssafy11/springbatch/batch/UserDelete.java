package com.ssafy11.springbatch.batch;

import java.util.List;
import java.util.Set;

import com.ssafy11.springbatch.domain.book.WishBook;
import com.ssafy11.springbatch.domain.rental.Rental;
import com.ssafy11.springbatch.domain.user.User;
import com.ssafy11.springbatch.domain.userbook.Userbook;

import lombok.Getter;

@Getter
public class UserDelete {
	private User user;
	private Set<Userbook> userbooks;
	private Set<WishBook> wishBooks;
	private Set<Rental> rentals;

	public UserDelete(User user, Set<Userbook> userbooks, Set<WishBook> wishBooks, Set<Rental> rentals) {
		this.user = user;
		this.userbooks = userbooks;
		this.wishBooks = wishBooks;
		this.rentals = rentals;
	}
}
