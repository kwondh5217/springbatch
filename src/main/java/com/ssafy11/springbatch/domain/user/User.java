package com.ssafy11.springbatch.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ssafy11.springbatch.domain.book.WishBook;
import com.ssafy11.springbatch.domain.rental.Rental;
import com.ssafy11.springbatch.domain.user.experience.Experience;
import com.ssafy11.springbatch.domain.user.point.Point;
import com.ssafy11.springbatch.domain.userbook.Userbook;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "users")
@Entity
public class User implements Serializable{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String email;
	private String password;
	private String nickname;
	private String areaCode;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Point> points = new ArrayList<>();
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Experience> experiences = new ArrayList<>();
	@OneToMany(fetch = FetchType.EAGER)
	private List<Userbook> userbooks = new ArrayList<>();
	@OneToMany(fetch = FetchType.EAGER)
	private List<WishBook> wishBooks = new ArrayList<>();
	@OneToMany(fetch = FetchType.EAGER)
	private List<Rental> rentals = new ArrayList<>();



	@Builder
	private User(String email, String password, String nickname, String areaCode) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.areaCode = areaCode;
	}

	public void update(String nickname, String areaCode) {
		this.nickname = nickname;
		this.areaCode = areaCode;
	}

	public void updatePassword(String password) {
		this.password = password;
	}

	public void removeMappingEntities() {
		this.wishBooks.forEach(WishBook::removeUser);
		this.rentals.forEach(Rental::removeUser);
		this.userbooks.forEach(Userbook::removeUser);
	}
}
