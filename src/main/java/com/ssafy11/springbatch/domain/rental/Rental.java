package com.ssafy11.springbatch.domain.rental;

import com.ssafy11.springbatch.domain.user.User;
import com.ssafy11.springbatch.domain.userbook.Userbook;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Rental {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	private User user;
	@ManyToOne
	private Userbook userbook;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	@Enumerated(EnumType.STRING)
	private RentalStatus rentalStatus;
	private int extensionCount;

	@Builder
	public Rental(User user, Userbook userbook, LocalDateTime startDate, LocalDateTime endDate, RentalStatus rentalStatus, int extensionCount) {
		this.user = user;
		this.userbook = userbook;
		this.startDate = startDate;
		this.endDate = endDate;
		this.rentalStatus = rentalStatus;
		this.extensionCount = extensionCount;
	}

	public void respond(boolean isApproved) {
		if (isApproved) {
			this.startDate = LocalDateTime.now();
			this.endDate = LocalDateTime.now().plusDays(7);
			this.rentalStatus = RentalStatus.IN_PROGRESS;
		} else {
			this.rentalStatus = RentalStatus.REJECTED;
		}
	}

	public void giveBack() {
		this.endDate = LocalDateTime.now();
		this.rentalStatus = RentalStatus.COMPLETED;
	}

	public void extension(boolean isApproved) {
		if (isApproved) {
			this.endDate = endDate.plusDays(7);
			this.extensionCount++;
		}
	}
}
