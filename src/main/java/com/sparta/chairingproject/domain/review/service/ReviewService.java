package com.sparta.chairingproject.domain.review.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.comment.entity.Comment;
import com.sparta.chairingproject.domain.comment.repository.CommentRepository;
import com.sparta.chairingproject.domain.common.dto.RequestDto;
import com.sparta.chairingproject.domain.member.entity.Member;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final StoreRepository storeRepository;
	private final OrderRepository orderRepository;
	private final CommentRepository commentRepository;
	private final RedisTemplate<String, Object> redisTemplate;

	@Transactional
	@CacheEvict(value = "reviews", key = "'store:' + #storeId + ':reviews'")
	public Review createReview(Long storeId, Long orderId, ReviewRequest request, Member member) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

		if (store.getDeletedAt() != null) {
			throw new GlobalException(STORE_ALREADY_DELETED);
		}

		if (store.getStatus().equals(StoreStatus.PENDING)) {
			throw new GlobalException(STORE_PENDING_CANNOT_REVIEW);
		}

		Order order = orderRepository.findByIdAndMemberAndStore(orderId, member, store)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_ORDER));

		if (order.getStatus() != OrderStatus.COMPLETED) {
			throw new GlobalException(ORDER_NOT_COMPLETED_CANNOT_REVIEW);
		}

		// 동일 주문 ID에 리뷰가 존재하는지 확인
		if (reviewRepository.existsByOrderIdAndMemberId(orderId, member.getId())) {
			throw new GlobalException(REVIEW_ALREADY_EXISTS);
		}

		Review review = Review.builder()
			.content(request.getContent())
			.score(request.getScore())
			.store(store)
			.member(member)
			.order(order)
			.build();

		return reviewRepository.save(review);
	}

	@Cacheable(value = "reviews", key = "'store:' + #storeId + ':reviews'")
	public Page<ReviewWithCommentResponse> getReviewsByStore(Long storeId, RequestDto request, Pageable pageable) {
		storeRepository.findById(storeId).orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

		return reviewRepository.findByStoreIdAndDeletedAtIsNull(storeId, pageable)
			.map(review -> {
				Comment comment = commentRepository.findByReviewAndDeletedAtIsNull(review).orElse(null);
				return ReviewWithCommentResponse.from(review, comment);
			});
	}

	@Transactional
	@CacheEvict(value = "reviews", key = "'store:' + #review.store.id + ':reviews'")
	public void updateReview(Long reviewId, ReviewRequest request, Member member) {
		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_REVIEW));

		if (!review.getMember().getId().equals(member.getId())) {
			throw new GlobalException(NOT_AUTHOR_OF_REVIEW);
		}

		if (review.getDeletedAt() != null) {
			throw new GlobalException(REVIEW_ALREADY_DELETED);
		}

		review.update(request.getContent(), request.getScore());
	}

	@Transactional
	@CacheEvict(value = "reviews", key = "'store:' + #review.store.id + ':reviews'")
	public void deleteReview(Long reviewId, RequestDto request, Member member) {

		Review review = reviewRepository.findById(reviewId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_REVIEW));

		if (review.getDeletedAt() != null) {
			throw new GlobalException(REVIEW_ALREADY_DELETED);
		}

		if (!review.getMember().getId().equals(member.getId())) {
			throw new GlobalException(NOT_AUTHOR_OF_REVIEW);
		}

		review.delete();
	}
}
