package com.sparta.chairingproject.domain.review.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.comment.entity.Comment;
import com.sparta.chairingproject.domain.comment.repository.CommentRepository;
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
	private ReviewRequest request;
	private Member member;
	private Store store;
	private List<Menu> menus;
	private Order order;
	private Pageable pageable;

	@BeforeEach
	void setUp() {
		storeId = 1L;
		orderId = 1L;
		request = new ReviewRequest("좋은 가게였습니다.", 5);
		member = new Member("Test user", "test@example.com", "1234", MemberRole.USER);
		store = new Store(1L, "Test name", "Test image", "Test description", member);
		store.updateStoreStatus(StoreStatus.OPEN);
		menus = new ArrayList<>();
		order = order.createOf(member, store, menus, OrderStatus.COMPLETED, 10000);
		pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
	}

	@Test
	@DisplayName("리뷰 작성 성공 - 가게 OPEN 상태 및 주문 완료 상태")
	void createReview_success() {
		// Given
		Order order = Order.createOf(member, store, menus, OrderStatus.COMPLETED, 10000);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findByIdAndMemberAndStore(orderId, member, store)).thenReturn(Optional.of(order));
		when(reviewRepository.existsByOrderIdAndMemberId(orderId, member.getId())).thenReturn(false);
		when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
		Order order = Order.createOf(member, store, menus, OrderStatus.COMPLETED, 10000);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findByIdAndMemberAndStore(orderId, member, store)).thenReturn(Optional.of(order));
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
		Order order = Order.createOf(member, store, menus, OrderStatus.WAITING, 10000);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findByIdAndMemberAndStore(orderId, member, store)).thenReturn(Optional.of(order));

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
		store = new Store(1L, "Pending Store", "Pending Image", "Pending Description", member);
		Order order = Order.createOf(member, store, menus, OrderStatus.COMPLETED, 10000);

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
	@DisplayName("리뷰 조회 성공 - 댓글 존재")
	void getReviewsByStore_success_withComment() {
		// Given
		Review review = new Review("Good service", 5, store, member, order);
		Comment comment = new Comment("Thank you for your review!", review);

		Page<Review> reviews = new PageImpl<>(List.of(review), pageable, 1);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(reviewRepository.findReviewsByStore(store, pageable)).thenReturn(reviews);
		when(commentRepository.findByReview(review)).thenReturn(Optional.of(comment));

		// When
		Page<ReviewWithCommentResponse> result = reviewService.getReviewsByStore(storeId, pageable);

		// Then
		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		assertEquals("Good service", result.getContent().get(0).getReview().getContent());
		assertEquals("Thank you for your review!", result.getContent().get(0).getComment().getContent());

		verify(storeRepository, times(1)).findById(storeId);
		verify(reviewRepository, times(1)).findReviewsByStore(store, pageable);
		verify(commentRepository, times(1)).findByReview(review);
	}

	@Test
	@DisplayName("리뷰 조회 성공 - 댓글 없음")
	void getReviewsByStore_success_withoutComment() {
		// Given
		Review review = new Review("Good service", 5, store, member, order);

		Page<Review> reviews = new PageImpl<>(List.of(review), pageable, 1);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(reviewRepository.findReviewsByStore(store, pageable)).thenReturn(reviews);
		when(commentRepository.findByReview(review)).thenReturn(Optional.empty());

		// When
		Page<ReviewWithCommentResponse> result = reviewService.getReviewsByStore(storeId, pageable);

		// Then
		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		assertEquals("Good service", result.getContent().get(0).getReview().getContent());
		assertNull(result.getContent().get(0).getComment());

		verify(storeRepository, times(1)).findById(storeId);
		verify(reviewRepository, times(1)).findReviewsByStore(store, pageable);
		verify(commentRepository, times(1)).findByReview(review);
	}

	@Test
	@DisplayName("리뷰 조회 실패 - 가게 없음")
	void getReviewsByStore_fail_storeNotFound() {
		// Given
		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> reviewService.getReviewsByStore(storeId, pageable));

		assertEquals(NOT_FOUND_STORE.getMessage(), exception.getMessage());

		verify(storeRepository, times(1)).findById(storeId);
		verify(reviewRepository, never()).findReviewsByStore(any(), any());
		verify(commentRepository, never()).findByReview(any());
	}
}
