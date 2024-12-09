package com.sparta.chairingproject.domain.review.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.comment.entity.Comment;
import com.sparta.chairingproject.domain.comment.repository.CommentRepository;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.review.dto.ReviewRequest;
import com.sparta.chairingproject.domain.review.dto.ReviewWithCommentsResponse;
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

	public Review createReview(Long storeId, ReviewRequest request, Member member) {
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

	public Page<ReviewWithCommentsResponse> getReviewsByStore(Long storeId, Pageable pageable) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

		return reviewRepository.findByStore(store, pageable)
			.map(review -> {
				List<Comment> comments = commentRepository.findByReview(review);
				return ReviewWithCommentsResponse.from(review, comments);
			});
	}
}
