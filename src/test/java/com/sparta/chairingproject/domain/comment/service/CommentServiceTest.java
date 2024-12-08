package com.sparta.chairingproject.domain.comment.service;

import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparta.chairingproject.domain.comment.dto.CommentRequest;
import com.sparta.chairingproject.domain.comment.entity.Comment;
import com.sparta.chairingproject.domain.comment.repository.CommentRepository;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.review.entity.Review;
import com.sparta.chairingproject.domain.review.repository.ReviewRepository;
import com.sparta.chairingproject.domain.store.entity.Store;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {
	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private CommentRepository commentRepository;

	@InjectMocks
	private CommentService commentService;

	@Test
	@DisplayName("댓글 작성 성공")
	void createComment_success() {
		// Given
		Long storeId = 1L;
		Long reviewId = 1L;
		Member owner = new Member("Test Owner", "owner@example.com", "password", MemberRole.OWNER);
		Store store = new Store(1L, "Test Store", "storeImage", "description", owner);
		Review review = new Review("Good service", 5, store, owner);

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
		when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CommentRequest request = new CommentRequest("Thank you for your review!");

		// When
		commentService.createComment(storeId, reviewId, request, owner);

		// Then
		verify(reviewRepository, times(1)).findById(reviewId);
		verify(commentRepository, times(1)).save(any(Comment.class));
	}
}
