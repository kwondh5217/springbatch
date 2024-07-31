package com.ssafy11.springbatch.batch.dto;

import com.ssafy11.springbatch.domain.user.experience.Experience;
import com.ssafy11.springbatch.domain.user.point.Point;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class UserPersonal {
	private Point point;
	private Experience experience;

	public UserPersonal(Point point, Experience experience) {
		this.point = point;
		this.experience = experience;
	}
}
