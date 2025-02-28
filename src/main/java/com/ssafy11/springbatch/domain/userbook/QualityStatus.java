package com.ssafy11.springbatch.domain.userbook;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QualityStatus {
	VERY_BAD(1), BAD(2), NORMAL(3), GOOD(4), VERY_GOOD(5);

	private final int value;
}