package com.ssafy11.springbatch.domain.user.point;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PointViewRepository extends JpaRepository<PointView, Long> {
	Optional<PointView> findByUserId(Long pointId);

}
