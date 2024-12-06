package com.sparta.chairingproject.domain.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sparta.chairingproject.domain.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
	List<Review> findByStoreId(Long storeId);
}
