package com.ssafy11.springbatch.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "users")
@Entity
public class User implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String email;
	private String password;
	private String nickname;
	private String areaCode;
	private Long experience;
	private Long point;

	@Builder
	public User(String email, String password, String nickname, String areaCode, Long experience, Long point) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.areaCode = areaCode;
		this.experience = experience;
		this.point = point;
	}

	public void update(String nickname, String areaCode) {
		this.nickname = nickname;
		this.areaCode = areaCode;
	}

	public void updatePassword(String password) {
		this.password = password;
	}

	public void overdueCharge(long daysOverdue) {
		this.experience -= daysOverdue;
		this.point -= daysOverdue;
	}
}
