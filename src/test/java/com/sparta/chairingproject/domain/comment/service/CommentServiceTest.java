package com.sparta.chairingproject.domain.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
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

	private Long storeId;
	private Long reviewId;
	private Member owner;
	private Store store;
	private Review review;
	private CommentRequest request;

	@BeforeEach
	void setUp() {
		storeId = 1L;
		reviewId = 1L;
		owner = new Member(1L, "Test Owner", "owner@example.com", "password", MemberRole.OWNER);
		store = new Store(1L, "Test Store", "storeImage", "description", owner);
		Order order = Order.createOf(owner, store, List.of(), OrderStatus.COMPLETED, 10000);
		review = new Review("Good service", 5, store, owner, order);
		request = new CommentRequest("Thank you for your review!");
	}

	@Test
	@DisplayName("댓글 작성 성공")
	void createComment_success() {
		// Given
		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
		when(commentRepository.existsByReview(review)).thenReturn(false);
		when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		commentService.createComment(storeId, reviewId, request, owner);

		// Then
		verify(reviewRepository, times(1)).findById(reviewId);
		verify(commentRepository, times(1)).existsByReview(review);
		verify(commentRepository, times(1)).save(any(Comment.class));
	}

	@Test
	@DisplayName("댓글 작성 실패 - 리뷰가 해당 가게의 주문과 연결되지 않음")
	void createComment_fail_notMatchingOrderAndReview() {
		// Given
		Store anotherStore = new Store(2L, "Another Store", "storeImage", "description", owner);
		Order invalidOrder = Order.createOf(owner, anotherStore, List.of(), OrderStatus.COMPLETED, 10000);
		Review differentReview = new Review("Good service", 5, anotherStore, owner, invalidOrder); // 잘못된 Order

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(differentReview));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.createComment(storeId, reviewId, request, owner));

		assertEquals(ExceptionCode.NOT_MATCHING_STORE_AND_ORDER.getMessage(), exception.getMessage());

		verify(reviewRepository, times(1)).findById(reviewId);
		verify(commentRepository, never()).existsByReview(any());
		verify(commentRepository, never()).save(any(Comment.class));
	}

	@Test
	@DisplayName("댓글 작성 실패 - 권한 없는 OWNER")
	void createComment_fail_unauthorizedOwner() {
		// Given
		Member differentOwner = new Member(2L, "Another Owner", "different@example.com", "password", MemberRole.OWNER);

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.createComment(storeId, reviewId, request, differentOwner));
		assertEquals(ExceptionCode.UNAUTHORIZED_OWNER.getMessage(), exception.getMessage());

		verify(reviewRepository, times(1)).findById(reviewId);
		verify(commentRepository, never()).existsByReview(any());
		verify(commentRepository, never()).save(any(Comment.class));
	}

	@Test
	@DisplayName("댓글 작성 실패 - 리뷰 없음")
	void createComment_fail_reviewNotFound() {
		// Given
		when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.createComment(storeId, reviewId, request, owner));
		assertEquals(ExceptionCode.NOT_FOUND_REVIEW.getMessage(), exception.getMessage());

		verify(reviewRepository, times(1)).findById(reviewId);
		verify(commentRepository, never()).existsByReview(any());
		verify(commentRepository, never()).save(any(Comment.class));
	}

	@Test
	@DisplayName("댓글 작성 실패 - 이미 댓글이 존재함")
	void createComment_fail_alreadyExists() {
		// Given
		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
		when(commentRepository.existsByReview(review)).thenReturn(true);

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.createComment(storeId, reviewId, request, owner));
		assertEquals(ExceptionCode.COMMENT_ALREADY_EXISTS.getMessage(), exception.getMessage());

		verify(reviewRepository, times(1)).findById(reviewId);
		verify(commentRepository, times(1)).existsByReview(review);
		verify(commentRepository, never()).save(any(Comment.class));
	}
}
