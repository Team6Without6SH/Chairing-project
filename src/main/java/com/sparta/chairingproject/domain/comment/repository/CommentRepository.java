package com.sparta.chairingproject.domain.comment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.chairingproject.domain.comment.entity.Comment;
import com.sparta.chairingproject.domain.review.entity.Review;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	List<Comment> findByReview(Review review);
}
