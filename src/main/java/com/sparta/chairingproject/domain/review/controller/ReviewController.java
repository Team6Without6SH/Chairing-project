package com.sparta.chairingproject.domain.review.controller;

import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.review.dto.ReviewRequest;
import com.sparta.chairingproject.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @Secured("ROLE_USER")
    @PostMapping("stores/{storeId}/reviews")
    public ResponseEntity<Void> createReview(
            @PathVariable Long storeId,
            @RequestBody @Valid ReviewRequest request,
            @AuthenticationPrincipal UserDetailsImpl authMember
            ) {
        reviewService.createReview(storeId,request,authMember.getMember());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
