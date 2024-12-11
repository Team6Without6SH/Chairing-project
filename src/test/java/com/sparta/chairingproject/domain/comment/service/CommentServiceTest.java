package com.sparta.chairingproject.domain.comment.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.exception.enums.ExceptionCode;
import com.sparta.chairingproject.domain.comment.dto.CommentRequest;
import com.sparta.chairingproject.domain.comment.entity.Comment;
import com.sparta.chairingproject.domain.comment.repository.CommentRepository;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.review.entity.Review;
import com.sparta.chairingproject.domain.review.repository.ReviewRepository;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;

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
	private Long commentId;
	private Member owner;
	private Store store;
	private Review review;
	private Order order;
	private CommentRequest request;
	private RequestDto requestDto;

	@BeforeEach
	void setUp() {
		storeId = 1L;
		reviewId = 1L;
		commentId = 1L;
		owner = new Member("Test Owner", "owner@example.com", "password", MemberRole.OWNER); // id
		ReflectionTestUtils.setField(owner, "id", 1L);
		store = new Store(1L, "Test Store", "storeImage", "description", owner, StoreRequestStatus.APPROVED,
			StoreStatus.OPEN);
		store.approveRequest();
		order = Order.createOf(owner, store, List.of(), OrderStatus.COMPLETED, 10000);
		review = new Review("Good service", 5, store, owner, order);
		// review의 id 값 설정
		ReflectionTestUtils.setField(review, "id", reviewId);
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
		Store anotherStore = new Store(2L, "Another Store", "storeImage", "description", owner,
			StoreRequestStatus.APPROVED, StoreStatus.OPEN);
		Order invalidOrder = Order.createOf(owner, anotherStore, List.of(), OrderStatus.COMPLETED, 10000);
		Review differentReview = new Review("Good service", 5, anotherStore, owner, invalidOrder); // 잘못된 Order

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(differentReview));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.createComment(storeId, reviewId, request, owner));

		assertEquals(NOT_MATCHING_STORE_AND_ORDER.getMessage(), exception.getMessage());

		verify(reviewRepository, times(1)).findById(reviewId);
		verify(commentRepository, never()).existsByReview(any());
		verify(commentRepository, never()).save(any(Comment.class));
	}

	@Test
	@DisplayName("댓글 작성 실패 - 권한 없는 OWNER")
	void createComment_fail_unauthorizedOwner() {
		// Given
		Member differentOwner = new Member("Another Owner", "different@example.com", "password", MemberRole.OWNER);
		ReflectionTestUtils.setField(differentOwner, "id", 2L);

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.createComment(storeId, reviewId, request, differentOwner));
		assertEquals(UNAUTHORIZED_OWNER.getMessage(), exception.getMessage());

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
		assertEquals(NOT_FOUND_REVIEW.getMessage(), exception.getMessage());

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
		assertEquals(COMMENT_ALREADY_EXISTS.getMessage(), exception.getMessage());

		verify(reviewRepository, times(1)).findById(reviewId);
		verify(commentRepository, times(1)).existsByReview(review);
		verify(commentRepository, never()).save(any(Comment.class));
	}

	@Test
	@DisplayName("댓글 수정 성공")
	void updateComment_success() {
		// Given
		Comment comment = new Comment("Original content", review);
		when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

		// When
		commentService.updateComment(reviewId, commentId, request, owner);

		// Then
		verify(commentRepository, times(1)).findById(commentId);
		assertEquals("Thank you for your review!", comment.getContent());
	}

	@Test
	@DisplayName("댓글 수정 실패 - 이미 삭제된 댓글")
	void updateComment_fail_alreadyDeleted() {
		// Given
		Comment comment = new Comment("Original content", review);
		comment.softDelete();
		when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.updateComment(reviewId, commentId, request, owner));
		assertEquals(COMMENT_ALREADY_DELETED.getMessage(), exception.getMessage());

		verify(commentRepository, times(1)).findById(commentId);
	}

	@Test
	@DisplayName("댓글 수정 실패 - 권한 없는 OWNER")
	void updateComment_fail_unauthorizedOwner() {
		// Given
		Member differentOwner = new Member("Different Owner", "different@example.com", "password", MemberRole.OWNER);
		ReflectionTestUtils.setField(differentOwner, "id", 2L);
		Comment comment = new Comment("Original content", review);
		when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.updateComment(reviewId, commentId, request, differentOwner));
		assertEquals(NOT_AUTHOR_OF_COMMENT.getMessage(), exception.getMessage());

		verify(commentRepository, times(1)).findById(commentId);
	}

	@Test
	@DisplayName("댓글 수정 실패 - 존재하지 않는 댓글")
	void updateComment_fail_commentNotFound() {
		// Given
		when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.updateComment(reviewId, commentId, request, owner));
		assertEquals(NOT_FOUND_COMMENT.getMessage(), exception.getMessage());

		verify(commentRepository, times(1)).findById(commentId);
	}

	@Test
	@DisplayName("댓글 수정 실패 - 댓글이 리뷰와 연결되지 않음")
	void updateComment_fail_notMatchingCommentAndReview() {
		// Given
		Long mismatchedReviewId = 999L; // 리뷰 ID가 댓글의 리뷰와 다르게 설정
		Review mismatchedReview = new Review("Mismatched review content", 4, store, owner, order);
		ReflectionTestUtils.setField(mismatchedReview, "id", mismatchedReviewId);

		Comment comment = new Comment("Original comment content", review); // 댓글은 기존 리뷰와 연결
		ReflectionTestUtils.setField(comment, "id", 1L);

		when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.updateComment(mismatchedReviewId, comment.getId(), request, owner));

		assertEquals(ExceptionCode.NOT_MATCHING_COMMENT_AND_REVIEW, exception.getExceptionCode());

		verify(commentRepository, times(1)).findById(comment.getId());
		verify(commentRepository, never()).save(any());
	}

	@Test
	@DisplayName("댓글 삭제 성공")
	void deleteComment_success() {
		// Given
		Comment comment = new Comment("Content", review);
		when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

		// When
		commentService.deleteComment(reviewId, commentId, request, owner);

		// Then
		verify(commentRepository, times(1)).findById(commentId);
		assertNotNull(comment.getDeletedAt());
	}

	@Test
	@DisplayName("댓글 삭제 실패 - 이미 삭제된 댓글")
	void deleteComment_fail_alreadyDeleted() {
		// Given
		Comment comment = new Comment("Content", review);
		comment.softDelete();
		when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.deleteComment(reviewId, commentId, request, owner));
		assertEquals(COMMENT_ALREADY_DELETED.getMessage(), exception.getMessage());

		verify(commentRepository, times(1)).findById(commentId);
	}

	@Test
	@DisplayName("댓글 삭제 실패 - 권한 없는 OWNER")
	void deleteComment_fail_unauthorizedOwner() {
		// Given
		Member differentOwner = new Member("Different Owner", "different@example.com", "password", MemberRole.OWNER);
		ReflectionTestUtils.setField(differentOwner, "id", 2L);
		Comment comment = new Comment("Content", review);
		when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.deleteComment(reviewId, commentId, request, differentOwner));
		assertEquals(NOT_AUTHOR_OF_COMMENT.getMessage(), exception.getMessage());

		verify(commentRepository, times(1)).findById(commentId);
	}

	@Test
	@DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글")
	void deleteComment_fail_commentNotFound() {
		// Given
		when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.deleteComment(reviewId, commentId, request, owner));
		assertEquals(NOT_FOUND_COMMENT.getMessage(), exception.getMessage());

		verify(commentRepository, times(1)).findById(commentId);
	}

	@Test
	@DisplayName("댓글 삭제 실패 - 댓글이 리뷰와 연결되지 않음")
	void deleteComment_fail_notMatchingCommentAndReview() {
		// Given
		Long mismatchedReviewId = 999L; // 리뷰 ID가 댓글의 리뷰와 다르게 설정
		Review mismatchedReview = new Review("Mismatched review content", 4, store, owner, order);
		ReflectionTestUtils.setField(mismatchedReview, "id", mismatchedReviewId);

		Comment comment = new Comment("Original comment content", review); // 댓글은 기존 리뷰와 연결
		ReflectionTestUtils.setField(comment, "id", commentId);

		when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> commentService.deleteComment(mismatchedReviewId, commentId, requestDto, owner));

		assertEquals(ExceptionCode.NOT_MATCHING_COMMENT_AND_REVIEW.getMessage(), exception.getMessage());

		verify(commentRepository, times(1)).findById(commentId);
		verify(commentRepository, never()).save(any());
	}
}