package com.sparta.chairingproject.domain.review.service;

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
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.entity.MemberRole;
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

	@InjectMocks
	private ReviewService reviewService;

	@Test
	@DisplayName("리뷰 작성 성공")
	void createReview_success() {
		//Given
		Long storeId = 1L;
		ReviewRequest request = new ReviewRequest("좋은 가게였습니다.", 5);
		Member member = new Member("Test user", "test@example.com", "1234", MemberRole.USER);
		Store store = new Store("Test name", "Test image", "Test description", member);
		store.updateStoreStatus(StoreStatus.OPEN);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
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
		verify(reviewRepository, times(1)).save(any(Review.class));
	}

	@Test
	@DisplayName("리뷰 작성 실패 - 팬딩 상태 가게")
	void createReview_fail_storePending() {
		// Given
		Long storeId = 1L;
		ReviewRequest request = new ReviewRequest("좋은 가게였습니다.", 5);
		Member member = new Member("Test User", "test@example.com", "test-password", MemberRole.USER);

		// default 로 pending 상태
		Store store = new Store("Test name", "Test image", "Test description", member);

		when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

		// When & Then
		GlobalException exception = assertThrows(GlobalException.class,
			() -> reviewService.createReview(storeId, request, member));

		assertEquals(ExceptionCode.STORE_PENDING.getMessage(), exception.getMessage());

		verify(storeRepository, times(1)).findById(storeId);
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
		verify(reviewRepository, never()).save(any(Review.class));
	}
}
