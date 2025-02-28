package com.ssafy11.springbatch.domain.user.experience;


import java.io.Serializable;

import com.ssafy11.springbatch.domain.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Experience implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	private User user;
	@Enumerated(EnumType.STRING)
	private ExperienceHistory history;
	private int amount;

	@Builder
	public Experience(User user, ExperienceHistory history, int amount) {
		this.user = user;
		this.history = history;
		this.amount = amount;
	}
}
