package com.sparta.chairingproject.domain.review.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	public Review createReview(Long storeId, Long orderId, ReviewRequest request, Member member) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

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

	public Page<ReviewWithCommentsResponse> getReviewsByStore(Long storeId, Pageable pageable) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

		Page<Review> reviews = reviewRepository.findReviewsByStore(store, pageable);
		// 조회한 리뷰 목록에 해당하는 댓글들을 한 번의 쿼리로 가져옴
		List<Comment> comments = commentRepository.findCommentsByReviews(reviews.getContent());
		// 댓글 목록을 각 리뷰를 기준으로 그룹화하여 Map 으로 변환.
		Map<Review, List<Comment>> commentsByReview = comments.stream()
			.collect(Collectors.groupingBy(Comment::getReview));

		return reviews.map(
			review -> ReviewWithCommentsResponse.from(review, commentsByReview.getOrDefault(review, List.of())));
	}
}
