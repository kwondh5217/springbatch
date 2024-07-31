package com.ssafy11.springbatch.batch.dto;

import java.util.List;

import com.ssafy11.springbatch.domain.book.WishBook;
import com.ssafy11.springbatch.domain.rental.Rental;
import com.ssafy11.springbatch.domain.user.User;
import com.ssafy11.springbatch.domain.userbook.Userbook;

import lombok.Getter;

@Getter
public class UserDelete {
	private User user;
	private List<Userbook> userbooks;
	private List<WishBook> wishBooks;
	private List<Rental> rentals;

	public UserDelete(User user, List<Userbook> userbooks, List<WishBook> wishBooks, List<Rental> rentals) {
		this.user = user;
		this.userbooks = userbooks;
		this.wishBooks = wishBooks;
		this.rentals = rentals;
	}
}
