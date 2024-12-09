package com.sparta.chairingproject.domain.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.exception.enums.ExceptionCode;
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

	@Test
	@DisplayName("댓글 작성 실패 - 리뷰가 가게와 연결되지 않음")
	void createComment_fail_notMatchingStoreAndReview() {
		// Given
		Long storeId = 1L;
		Long reviewId = 1L;
		Member owner = new Member("Test Owner", "owner@example.com", "password", MemberRole.OWNER);
		// Store와 다른 Store를 생성하며 명확하게 ID를 설정
		Store store = new Store(1L, "Test Store", "storeImage", "description", owner);
		Store differentStore = new Store(2L, "Another Store", "anotherImage", "anotherDescription", owner);
		// Review는 다른 Store에 속하도록 설정
		Review review = new Review("Good service", 5, differentStore, owner);

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		CommentRequest request = new CommentRequest("Thank you for your review!");

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.createComment(storeId, reviewId, request, owner));
		assertEquals(ExceptionCode.NOT_MATCHING_STORE_AND_REVIEW.getMessage(), exception.getMessage());

		verify(reviewRepository, times(1)).findById(reviewId);
		verify(commentRepository, never()).save(any(Comment.class));
	}

	@Test
	@DisplayName("댓글 작성 실패 - 권한 없는 OWNER")
	void createComment_fail_unauthorizedOwner() {
		// Given
		Long storeId = 1L;
		Long reviewId = 1L;
		Member owner = new Member("Test Owner", "owner@example.com", "password", MemberRole.OWNER);
		Member differentOwner = new Member("Another Owner", "different@example.com", "password", MemberRole.OWNER);
		Store store = new Store(1L, "Test Store", "storeImage", "description", owner);
		Review review = new Review("Good service", 5, store, owner);

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		CommentRequest request = new CommentRequest("Thank you for your review!");

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.createComment(storeId, reviewId, request, differentOwner));
		assertEquals(ExceptionCode.UNAUTHORIZED_OWNER.getMessage(), exception.getMessage());

		verify(reviewRepository, times(1)).findById(reviewId);
		verify(commentRepository, never()).save(any(Comment.class));
	}

	@Test
	@DisplayName("댓글 작성 실패 - 리뷰 없음")
	void createComment_fail_reviewNotFound() {
		// Given
		Long storeId = 1L;
		Long reviewId = 1L;
		Member owner = new Member("Test Owner", "owner@example.com", "password", MemberRole.OWNER);

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

		CommentRequest request = new CommentRequest("Thank you for your review!");

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.createComment(storeId, reviewId, request, owner));
		assertEquals(ExceptionCode.NOT_FOUND_REVIEW.getMessage(), exception.getMessage());

		verify(reviewRepository, times(1)).findById(reviewId);
		verify(commentRepository, never()).save(any(Comment.class));
	}
}
