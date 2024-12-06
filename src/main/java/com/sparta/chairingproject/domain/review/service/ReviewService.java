package com.sparta.chairingproject.domain.review.service;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.review.dto.ReviewRequest;
import com.sparta.chairingproject.domain.review.entity.Review;
import com.sparta.chairingproject.domain.review.repository.ReviewRepository;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.NOT_FOUND_STORE;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;

    public void createReview(Long storeId, @Valid ReviewRequest request, Member member) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

        Review review = Review.builder()
                .content(request.getContent())
                .score(request.getScore())
                .store(store)
                .member(member)
                .build();

        reviewRepository.save(review);
    }
}
