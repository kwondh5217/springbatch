package com.ssafy11.springbatch.batch;

import com.ssafy11.springbatch.domain.user.User;
import com.ssafy11.springbatch.domain.user.experience.Experience;
import com.ssafy11.springbatch.domain.user.point.Point;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class UserPersonal {
	private Point point;
	private Experience experience;
	private User user;

	public UserPersonal(Point point, Experience experience) {
		this.point = point;
		this.experience = experience;
	}
}
