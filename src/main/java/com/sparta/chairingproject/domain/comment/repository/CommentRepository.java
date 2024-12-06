package com.sparta.chairingproject.domain.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.chairingproject.domain.comment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
