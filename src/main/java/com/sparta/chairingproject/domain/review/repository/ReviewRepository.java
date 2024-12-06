package com.sparta.chairingproject.domain.review.repository;

import com.sparta.chairingproject.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
