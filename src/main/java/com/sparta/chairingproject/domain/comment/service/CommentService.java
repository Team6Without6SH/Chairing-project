package com.sparta.chairingproject.domain.comment.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.comment.dto.CommentRequest;
import com.sparta.chairingproject.domain.comment.entity.Comment;
import com.sparta.chairingproject.domain.comment.repository.CommentRepository;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.review.entity.Review;
import com.sparta.chairingproject.domain.review.repository.ReviewRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final ReviewRepository reviewRepository;
	private final CommentRepository commentRepository;

	public void createComment(Long storeId, Long reviewId, CommentRequest request, Member owner) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_REVIEW));

		if (!review.getOrder().getStore().getId().equals(storeId)) {
			throw new GlobalException(NOT_MATCHING_STORE_AND_ORDER);
		}

		if (!review.getStore().getOwner().getId().equals(owner.getId())) {
			throw new GlobalException(UNAUTHORIZED_OWNER);
		}

		if (commentRepository.existsByReview(review)) {
			throw new GlobalException(COMMENT_ALREADY_EXISTS);
		}

		Comment comment = new Comment(request.getContent(), review);
		commentRepository.save(comment);
	}

	@Transactional
	public void updateComment(Long reviewId, Long commentId, @Valid CommentRequest request, Member owner) {
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_COMMENT));

		if (!comment.getReview().getId().equals(reviewId)) {
			throw new GlobalException(NOT_MATCHING_COMMENT_AND_REVIEW);
		}

		if (!comment.getReview().getStore().getOwner().getId().equals(owner.getId())) {
			throw new GlobalException(NOT_AUTHOR_OF_COMMENT);
		}

		comment.update(request.getContent());
		commentRepository.save(comment);
	}
}
