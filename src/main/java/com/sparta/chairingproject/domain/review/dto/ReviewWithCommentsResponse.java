package com.sparta.chairingproject.domain.review.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.sparta.chairingproject.domain.comment.dto.CommentResponse;
import com.sparta.chairingproject.domain.comment.entity.Comment;
import com.sparta.chairingproject.domain.review.entity.Review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewWithCommentsResponse {
	private ReviewResponse review;
	private List<CommentResponse> comments;

	public static ReviewWithCommentsResponse from(Review review, List<Comment> comments) {
		ReviewResponse reviewResponse = new ReviewResponse(
			review.getMember().getName(),
			review.getContent(),
			review.getScore()
		);

		List<CommentResponse> commentResponses = comments.stream()
			.map(comment -> new CommentResponse(comment.getContent()))
			.collect(Collectors.toList());

		return new ReviewWithCommentsResponse(reviewResponse, commentResponses);
	}
}
