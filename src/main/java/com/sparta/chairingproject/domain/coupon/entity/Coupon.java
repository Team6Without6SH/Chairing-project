package com.sparta.chairingproject.domain.coupon.entity;

import java.util.ArrayList;
import java.util.List;

import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import com.sparta.chairingproject.domain.common.entity.Timestamped;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;

@Getter
@Entity
public class Coupon extends Timestamped {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false)
	private int discountPrice;

	@OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Issuance> issuances = new ArrayList<>();
}
