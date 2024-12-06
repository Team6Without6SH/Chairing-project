package com.sparta.chairingproject.domain.review.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import org.springframework.stereotype.Service;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.review.dto.ReviewRequest;
import com.sparta.chairingproject.domain.review.entity.Review;
import com.sparta.chairingproject.domain.review.repository.ReviewRepository;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final StoreRepository storeRepository;
	private final OrderRepository orderRepository;

	public Review createReview(Long storeId, @Valid ReviewRequest request, Member member) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

		if (store.getStatus().equals(StoreStatus.PENDING)) {
			throw new GlobalException(STORE_PENDING_CANNOT_REVIEW);
		}

		Order order = orderRepository.findByMemberAndStore(member, store)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_ORDER));

		if (order.getStatus() != OrderStatus.COMPLETED) {
			throw new GlobalException(ORDER_NOT_COMPLETED_CANNOT_REVIEW);
		}

		Review review = Review.builder()
			.content(request.getContent())
			.score(request.getScore())
			.store(store)
			.member(member)
			.build();

		return reviewRepository.save(review);
	}
}
