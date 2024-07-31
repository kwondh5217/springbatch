package com.ssafy11.springbatch.batch.writer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import com.ssafy11.springbatch.batch.dto.UserPersonal;
import com.ssafy11.springbatch.domain.user.experience.Experience;
import com.ssafy11.springbatch.domain.user.point.Point;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserPersonalItemWriter implements ItemWriter<UserPersonal> {

	private final ItemWriter<Point> pointItemWriter;
	private final ItemWriter<Experience> experienceItemWriter;

	@Override
	public void write(Chunk<? extends UserPersonal> chunk) throws Exception {
		List<Point> points = new ArrayList<>();
		List<Experience> experiences = new ArrayList<>();

		for (UserPersonal userPersonal : chunk) {
			points.add(userPersonal.getPoint());
			experiences.add(userPersonal.getExperience());
		}

		pointItemWriter.write(new Chunk<>(points));
		experienceItemWriter.write(new Chunk<>(experiences));
	}
}
