package com.ssafy11.springbatch.domain.book;

import java.time.LocalDate;

public record BookRequest(String isbn, String title, String author, String publisher, LocalDate publicationDate,
						  String thumbnail, String description) {
}
