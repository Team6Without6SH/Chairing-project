package com.sparta.chairingproject.domain.review.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.comment.entity.Comment;
import com.sparta.chairingproject.domain.comment.repository.CommentRepository;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.review.dto.ReviewRequest;
import com.sparta.chairingproject.domain.review.dto.ReviewWithCommentResponse;
import com.sparta.chairingproject.domain.review.entity.Review;
import com.sparta.chairingproject.domain.review.repository.ReviewRepository;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private CommentRepository commentRepository;

	@InjectMocks
	private ReviewService reviewService;

	private Long storeId;
	private Long orderId;
	private Long reviewId;
	private ReviewRequest request;
	private Member member;
	private Store store;
	private List<Menu> menus;
	private Order order;
	private Pageable pageable;
	private RequestDto requestDto;
	private Review review;

	@BeforeEach
	void setUp() {
		storeId = 1L;
		orderId = 1L;
		reviewId = 1L;
		request = new ReviewRequest("좋은 가게였습니다.", 5);
		member = new Member("Test user", "test@example.com", "1234", "image", MemberRole.USER);
		ReflectionTestUtils.setField(member, "id", 1L);
		store = new Store("Test name", "Test image", "Test description", "Test address", member);
		store.approveRequest();
		menus = new ArrayList<>();
		order = Order.createOf(member, store, menus, OrderStatus.COMPLETED, 10000);
		pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
		review = new Review("Original content", 4, store, member, order);
	}

	@Test
	@DisplayName("리뷰 작성 성공 - 가게 OPEN 상태 및 주문 완료 상태")
	void createReview_success() {
		// Given
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findByIdAndMemberAndStore(orderId, member, store)).thenReturn(
			Optional.of(order));
		when(reviewRepository.existsByOrderIdAndMemberId(orderId, member.getId())).thenReturn(
			false);
		when(reviewRepository.save(any(Review.class))).thenAnswer(
			invocation -> invocation.getArgument(0));

		// When
		Review savedReview = reviewService.createReview(storeId, orderId, request, member);

		// Then
		assertNotNull(savedReview);
		assertEquals("좋은 가게였습니다.", savedReview.getContent());
		assertEquals(5, savedReview.getScore());
		assertEquals(store, savedReview.getStore());
		assertEquals(member, savedReview.getMember());

		verify(storeRepository, times(1)).findById(storeId);
		verify(orderRepository, times(1)).findByIdAndMemberAndStore(orderId, member, store);
		verify(reviewRepository, times(1)).existsByOrderIdAndMemberId(orderId, member.getId());
		verify(reviewRepository, times(1)).save(any(Review.class));
	}

	@Test
	@DisplayName("리뷰 작성 실패 - 이미 해당 주문에 리뷰 존재")
	void createReview_fail_reviewAlreadyExists() {
		// Given
		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findByIdAndMemberAndStore(orderId, member, store)).thenReturn(
			Optional.of(order));
		when(reviewRepository.existsByOrderIdAndMemberId(orderId, member.getId())).thenReturn(true);

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> reviewService.createReview(storeId, orderId, request, member));

		assertEquals(REVIEW_ALREADY_EXISTS.getMessage(), exception.getMessage());

		verify(storeRepository, times(1)).findById(storeId);
		verify(orderRepository, times(1)).findByIdAndMemberAndStore(orderId, member, store);
		verify(reviewRepository, times(1)).existsByOrderIdAndMemberId(orderId, member.getId());
		verify(reviewRepository, never()).save(any(Review.class));
	}

	@Test
	@DisplayName("리뷰 작성 실패 - 가게 OPEN 상태지만 주문 미완료 상태")
	void createReview_fail_orderNotCompleted() {
		// Given
		ReflectionTestUtils.setField(order, "status", OrderStatus.WAITING);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findByIdAndMemberAndStore(orderId, member, store)).thenReturn(
			Optional.of(order));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> reviewService.createReview(storeId, orderId, request, member));

		assertEquals(ORDER_NOT_COMPLETED_CANNOT_REVIEW.getMessage(), exception.getMessage());

		verify(storeRepository, times(1)).findById(storeId);
		verify(orderRepository, times(1)).findByIdAndMemberAndStore(orderId, member, store);
		verify(reviewRepository, never()).save(any(Review.class));
	}

	@Test
	@DisplayName("리뷰 작성 실패 - 팬딩 상태 가게")
	void createReview_fail_storePending() {
		// Given
		ReflectionTestUtils.setField(store, "status", StoreStatus.PENDING);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> reviewService.createReview(storeId, orderId, request, member));

		assertEquals(STORE_PENDING_CANNOT_REVIEW.getMessage(), exception.getMessage());

		verify(storeRepository, times(1)).findById(storeId);
		verify(orderRepository, never()).findByIdAndMemberAndStore(any(), any(), any());
		verify(reviewRepository, never()).save(any(Review.class));
	}

	@Test
	@DisplayName("리뷰 작성 실패 - 이미 삭제된 가게")
	void createReview_fail_storeDeleted() {
		// Given
		ReflectionTestUtils.setField(store, "deletedAt", LocalDateTime.now());
		when(storeRepository.findById((storeId))).thenReturn(Optional.of(store));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> reviewService.createReview(storeId, orderId, request, member));

		assertEquals(STORE_ALREADY_DELETED.getMessage(), exception.getMessage());

		verify(storeRepository, times(1)).findById(storeId);
		verify(orderRepository, never()).findByIdAndMemberAndStore(any(), any(), any());
		verify(reviewRepository, never()).save(any(Review.class));
	}

	@Test
	@DisplayName("리뷰 조회 성공 - 댓글 존재")
	void getReviewsByStore_success_withComment() {
		// Given
		Review review = new Review("Good service", 5, store, member, order);
		Comment comment = new Comment("Thank you for your review!", review);

		Page<Review> reviews = new PageImpl<>(List.of(review), pageable, 1);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(reviewRepository.findByStoreIdAndDeletedAtIsNull(storeId, pageable)).thenReturn(
			reviews);
		when(commentRepository.findByReviewAndDeletedAtIsNull(review)).thenReturn(
			Optional.of(comment));

		// When
		Page<ReviewWithCommentResponse> result = reviewService.getReviewsByStore(storeId,
			requestDto, pageable);

		// Then
		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		assertEquals("Good service", result.getContent().get(0).getReview().getContent());
		assertEquals("Thank you for your review!",
			result.getContent().get(0).getComment().getContent());

		verify(storeRepository, times(1)).findById(storeId);
		verify(reviewRepository, times(1)).findByStoreIdAndDeletedAtIsNull(storeId, pageable);
		verify(commentRepository, times(1)).findByReviewAndDeletedAtIsNull(review);
	}

	@Test
	@DisplayName("리뷰 조회 성공 - 댓글 없음")
	void getReviewsByStore_success_withoutComment() {
		// Given
		Review review = new Review("Good service", 5, store, member, order);

		Page<Review> reviews = new PageImpl<>(List.of(review), pageable, 1);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(reviewRepository.findByStoreIdAndDeletedAtIsNull(storeId, pageable)).thenReturn(
			reviews);
		when(commentRepository.findByReviewAndDeletedAtIsNull(review)).thenReturn(Optional.empty());

		// When
		Page<ReviewWithCommentResponse> result = reviewService.getReviewsByStore(storeId,
			requestDto, pageable);

		// Then
		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		assertEquals("Good service", result.getContent().get(0).getReview().getContent());
		assertNull(result.getContent().get(0).getComment());

		verify(storeRepository, times(1)).findById(storeId);
		verify(reviewRepository, times(1)).findByStoreIdAndDeletedAtIsNull(storeId, pageable);
		verify(commentRepository, times(1)).findByReviewAndDeletedAtIsNull(review);
	}

	@Test
	@DisplayName("리뷰 조회 실패 - 가게 없음")
	void getReviewsByStore_fail_storeNotFound() {
		// Given
		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> reviewService.getReviewsByStore(storeId, requestDto, pageable));

		assertEquals(NOT_FOUND_STORE.getMessage(), exception.getMessage());

		verify(storeRepository, times(1)).findById(storeId);
		verify(reviewRepository, never()).findByStoreIdAndDeletedAtIsNull(any(), any());
		verify(commentRepository, never()).findByReviewAndDeletedAtIsNull(any());
	}

	@Test
	@DisplayName("리뷰 수정 성공")
	void updateReview_success() {
		// Given
		when(reviewRepository.findById(orderId)).thenReturn(Optional.of(review));

		ReviewRequest updatedRequest = new ReviewRequest("Updated content", 5);

		// When
		reviewService.updateReview(orderId, updatedRequest, member);

		// Then
		assertEquals("Updated content", review.getContent());
		assertEquals(5, review.getScore());

		verify(reviewRepository, times(1)).findById(orderId);
	}

	@Test
	@DisplayName("리뷰 수정 실패 - 작성자가 아닌 경우")
	void updateReview_fail_notAuthor() {
		// Given
		Member anotherMember = new Member("Another user", "another@example.com", "password",
			"image", MemberRole.USER);

		when(reviewRepository.findById(orderId)).thenReturn(Optional.of(review));

		ReviewRequest updatedRequest = new ReviewRequest("Updated content", 5);

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> reviewService.updateReview(orderId, updatedRequest, anotherMember));

		assertEquals(NOT_AUTHOR_OF_REVIEW.getMessage(), exception.getMessage());
		verify(reviewRepository, times(1)).findById(orderId);
	}

	@Test
	@DisplayName("리뷰 수정 실패 - 이미 삭제된 리뷰")
	void updateReview_fail_alreadyDeleted() {
		// Given
		review.delete();

		when(reviewRepository.findById(orderId)).thenReturn(Optional.of(review));

		ReviewRequest updatedRequest = new ReviewRequest("Updated content", 5);

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> reviewService.updateReview(orderId, updatedRequest, member));

		assertEquals(REVIEW_ALREADY_DELETED.getMessage(), exception.getMessage());
		verify(reviewRepository, times(1)).findById(orderId);
	}

	@Test
	@DisplayName("리뷰 삭제 성공")
	void deleteReview_success() {
		// Given
		when(reviewRepository.findById(orderId)).thenReturn(Optional.of(review));

		// When
		reviewService.deleteReview(orderId, requestDto, member);

		// Then
		assertNotNull(review.getDeletedAt());
		verify(reviewRepository, times(1)).findById(orderId);
	}

	@Test
	@DisplayName("리뷰 삭제 실패 - 작성자가 아닌 경우")
	void deleteReview_fail_notAuthor() {
		// Given
		Member anotherMember = new Member("Another user", "another@example.com", "password",
			"image", MemberRole.USER);

		when(reviewRepository.findById(orderId)).thenReturn(Optional.of(review));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> reviewService.deleteReview(orderId, requestDto, anotherMember));

		assertEquals(NOT_AUTHOR_OF_REVIEW.getMessage(), exception.getMessage());
		verify(reviewRepository, times(1)).findById(orderId);
	}

	@Test
	@DisplayName("리뷰 삭제 실패 - 이미 삭제된 리뷰")
	void deleteReview_fail_alreadyDeleted() {
		// Given
		review.delete(); // 리뷰를 삭제 상태로 설정

		when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> reviewService.deleteReview(reviewId, requestDto, member));

		assertEquals(REVIEW_ALREADY_DELETED.getMessage(), exception.getMessage());

		verify(reviewRepository, times(1)).findById(reviewId);
		verify(reviewRepository, never()).save(any(Review.class));
	}
}
