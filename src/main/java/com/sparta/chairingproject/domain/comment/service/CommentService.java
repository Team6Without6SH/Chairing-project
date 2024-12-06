package com.sparta.chairingproject.domain.comment.service;

import org.springframework.stereotype.Service;

import com.sparta.chairingproject.domain.comment.dto.CommentRequest;
import com.sparta.chairingproject.domain.comment.repository.CommentRepository;
import com.sparta.chairingproject.domain.review.repository.ReviewRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final ReviewRepository reviewRepository;
	private final CommentRepository commentRepository;

	public void createComment(Long storeId, Long reviewId, @Valid CommentRequest commentRequest) {

	}
}
