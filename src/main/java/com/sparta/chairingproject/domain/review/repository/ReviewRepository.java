package com.sparta.chairingproject.domain.review.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.chairingproject.domain.review.entity.Review;
import com.sparta.chairingproject.domain.store.entity.Store;

public interface ReviewRepository extends JpaRepository<Review, Long> {
	List<Review> findByStoreId(Long storeId);

	Page<Review> findReviewsByStore(Store store, Pageable pageable);
}
