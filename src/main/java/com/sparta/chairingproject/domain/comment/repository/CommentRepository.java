package com.sparta.chairingproject.domain.comment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.chairingproject.domain.comment.entity.Comment;
import com.sparta.chairingproject.domain.review.entity.Review;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	// 댓글을 한 번의 쿼리로 가져오는 방식. N+1문제 해결
	@Query("SELECT c FROM Comment c WHERE c.review IN :reviews")
	List<Comment> findCommentsByReviews(@Param("reviews") List<Review> reviews);
}
