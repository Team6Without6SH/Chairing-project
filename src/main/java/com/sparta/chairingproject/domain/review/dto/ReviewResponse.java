package com.sparta.chairingproject.domain.review.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponse implements Serializable {
	private String userName;  // 작성자 이름
	private String content;   // 리뷰 내용
	private int rating;       // 평점
}
