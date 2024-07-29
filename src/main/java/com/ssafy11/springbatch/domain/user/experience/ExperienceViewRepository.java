package com.ssafy11.springbatch.domain.user.experience;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceViewRepository extends JpaRepository<ExperienceView, Long> {
	Optional<ExperienceView> findByUserId(Long userId);
}
