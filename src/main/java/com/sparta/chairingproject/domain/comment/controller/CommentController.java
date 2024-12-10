package com.sparta.chairingproject.domain.comment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.comment.dto.CommentRequest;
import com.sparta.chairingproject.domain.comment.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Secured("ROLE_OWNER")
@RestController
@RequiredArgsConstructor
public class CommentController {
	private final CommentService commentService;

	@PostMapping("/owners/stores/{storeId}/reviews/{reviewId}/comments")
	public ResponseEntity<Void> createComment(
		@PathVariable Long storeId,
		@PathVariable Long reviewId,
		@RequestBody @Valid CommentRequest commentRequest,
		@AuthenticationPrincipal UserDetailsImpl authMember
	) {
		commentService.createComment(storeId, reviewId, commentRequest, authMember.getMember());
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PatchMapping("/reviews/{reviewId}/comments/{commentId}")
	public ResponseEntity<Void> updateComment(
		@PathVariable Long reviewId,
		@PathVariable Long commentId,
		@RequestBody @Valid CommentRequest request,
		@AuthenticationPrincipal UserDetailsImpl authMember
	) {
		commentService.updateComment(reviewId, commentId, request, authMember.getMember());
		return ResponseEntity.ok().build();
	}
}
