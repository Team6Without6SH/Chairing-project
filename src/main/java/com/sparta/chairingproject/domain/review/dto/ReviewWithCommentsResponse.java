package com.sparta.chairingproject.domain.review.dto;

import java.util.List;

import com.sparta.chairingproject.domain.comment.dto.CommentResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewWithCommentsResponse {
	private ReviewResponse review;
	private List<CommentResponse> comments;
}
