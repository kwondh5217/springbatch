package com.ssafy11.springbatch.domain.userbook;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserbookRepository extends JpaRepository<Userbook, Long> {

}
