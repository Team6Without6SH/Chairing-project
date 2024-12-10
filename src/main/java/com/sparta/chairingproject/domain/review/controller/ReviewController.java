package com.sparta.chairingproject.domain.review.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.review.dto.ReviewRequest;
import com.sparta.chairingproject.domain.review.dto.ReviewWithCommentsResponse;
import com.sparta.chairingproject.domain.review.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReviewController {
	private final ReviewService reviewService;

	@Secured("ROLE_USER")
	@PostMapping("/stores/{storeId}/orders/{orderId}/reviews")
	public ResponseEntity<Void> createReview(
		@PathVariable Long storeId,
		@PathVariable Long orderId,
		@RequestBody @Valid ReviewRequest request,
		@AuthenticationPrincipal UserDetailsImpl authMember
	) {
		reviewService.createReview(storeId, orderId, request, authMember.getMember());
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping("/stores/{storeId}/reviews")
	public ResponseEntity<Page<ReviewWithCommentsResponse>> getReviewsByStore(
		@PathVariable Long storeId,
		@PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Page<ReviewWithCommentsResponse> response = reviewService.getReviewsByStore(storeId, pageable);
		return ResponseEntity.ok(response);
	}
}
