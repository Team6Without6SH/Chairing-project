package com.sparta.chairingproject.domain.review.dto;

import com.sparta.chairingproject.domain.comment.dto.CommentResponse;
import com.sparta.chairingproject.domain.comment.entity.Comment;
import com.sparta.chairingproject.domain.review.entity.Review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewWithCommentResponse {
	private ReviewResponse review;
	private CommentResponse comment;

	public static ReviewWithCommentResponse from(Review review, Comment comments) {
		ReviewResponse reviewResponse = new ReviewResponse(
			review.getMember().getName(),
			review.getContent(),
			review.getScore()
		);

		CommentResponse commentResponses = (comments != null) ? new CommentResponse(comments.getContent()) : null;

		return new ReviewWithCommentResponse(reviewResponse, commentResponses);
	}
}
