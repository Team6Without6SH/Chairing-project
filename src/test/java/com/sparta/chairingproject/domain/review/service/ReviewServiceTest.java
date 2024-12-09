package com.sparta.chairingproject.domain.review.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.exception.enums.ExceptionCode;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.review.dto.ReviewRequest;
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

	@InjectMocks
	private ReviewService reviewService;

	@Test
	@DisplayName("리뷰 작성 성공 - 가게 OPEN 상태 및 주문 완료 상태")
	void createReview_success() {
		//Given
		Long storeId = 1L;
		ReviewRequest request = new ReviewRequest("좋은 가게였습니다.", 5);
		Member member = new Member("Test user", "test@example.com", "1234", MemberRole.USER);
		Store store = new Store("Test name", "Test image", "Test description", "Test Address", member);
		store.updateStoreStatus(StoreStatus.OPEN);

		List<Menu> menu = new ArrayList<>();
		Order order = Order.createOf(member, store, menu, OrderStatus.COMPLETED, 10000);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findByMemberAndStore(member, store)).thenReturn(Optional.of(order));
		when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		Review savedReview = reviewService.createReview(storeId, request, member);

		// Then
		assertNotNull(savedReview);
		assertEquals("좋은 가게였습니다.", savedReview.getContent());
		assertEquals(5, savedReview.getScore());
		assertEquals(store, savedReview.getStore());
		assertEquals(member, savedReview.getMember());

		verify(storeRepository, times(1)).findById(storeId);
		verify(orderRepository, times(1)).findByMemberAndStore(member, store);
		verify(reviewRepository, times(1)).save(any(Review.class));
	}

	@Test
	@DisplayName("리뷰 작성 실패 - 가게 OPEN 상태지만 주문 미완료 상태")
	void createReview_fail_orderNotCompleted() {
		// Given
		Long storeId = 1L;
		ReviewRequest request = new ReviewRequest("좋은 가게였습니다.", 5);
		Member member = new Member("Test user", "test@example.com", "1234", MemberRole.USER);
		Store store = new Store("Test name", "Test image", "Test description", "Test Address", member);
		store.updateStoreStatus(StoreStatus.OPEN);

		List<Menu> menus = new ArrayList<>();
		Order order = Order.createOf(member, store, menus, OrderStatus.WAITING, 10000); // 주문 미완료 상태 생성

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
		when(orderRepository.findByMemberAndStore(member, store)).thenReturn(Optional.of(order));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> reviewService.createReview(storeId, request, member));

		assertEquals(ORDER_NOT_COMPLETED_CANNOT_REVIEW.getMessage(), exception.getMessage());

		verify(storeRepository, times(1)).findById(storeId);
		verify(orderRepository, times(1)).findByMemberAndStore(member, store);
		verify(reviewRepository, never()).save(any(Review.class));
	}

	@Test
	@DisplayName("리뷰 작성 실패 - 팬딩 상태 가게")
	void createReview_fail_storePending() {
		// Given
		Long storeId = 1L;
		ReviewRequest request = new ReviewRequest("좋은 가게였습니다.", 5);
		Member member = new Member("Test User", "test@example.com", "test-password", MemberRole.USER);

		// default 로 pending 상태
		Store store = new Store("Test name", "Test image", "Test description", "Test Address", member);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> reviewService.createReview(storeId, request, member));

		assertEquals(ExceptionCode.STORE_PENDING_CANNOT_REVIEW.getMessage(), exception.getMessage());

		verify(storeRepository, times(1)).findById(storeId);
		verify(orderRepository, never()).findByMemberAndStore(any(), any());
		verify(reviewRepository, never()).save(any(Review.class));
	}

	@Test
	@DisplayName("리뷰 작성 실패 - 가게 없음")
	void createReview_fail_storeNotFound() {
		// Given
		Long storeId = 1L;
		ReviewRequest request = new ReviewRequest("좋은 가게였습니다.", 5);
		Member member = new Member("Test User", "test@example.com", "test-password", MemberRole.USER);

		when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> reviewService.createReview(storeId, request, member));

		assertEquals(ExceptionCode.NOT_FOUND_STORE.getMessage(), exception.getMessage());

		verify(storeRepository, times(1)).findById(storeId);
		verify(orderRepository, never()).findByMemberAndStore(any(), any());
		verify(reviewRepository, never()).save(any(Review.class));
	}
}
