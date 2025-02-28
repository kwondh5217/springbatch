package com.ssafy11.springbatch.domain.book;

import java.io.Serializable;

import com.ssafy11.springbatch.domain.user.User;
import com.ssafy11.springbatch.domain.userbook.Userbook;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class WishBook implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	@ManyToOne(fetch = FetchType.LAZY)
	private Userbook userbook;

	@Builder
	public WishBook(User user, Userbook userbook) {
		this.user = user;
		this.userbook = userbook;
	}

	public void removeUser() {
		this.user = null;
	}
}
