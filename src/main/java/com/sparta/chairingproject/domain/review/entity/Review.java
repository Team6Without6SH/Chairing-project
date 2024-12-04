package com.sparta.chairingproject.domain.review.entity;

import com.sparta.chairingproject.domain.common.entity.Timestamped;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.store.entity.Store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name = "reviews")
public class Review extends Timestamped {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String content;

	@Column(nullable = false)
	private int score;

	@ManyToOne
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;
}
