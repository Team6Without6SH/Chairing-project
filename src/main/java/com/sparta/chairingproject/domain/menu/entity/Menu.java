package com.sparta.chairingproject.domain.menu.entity;

import com.sparta.chairingproject.domain.common.entity.Timestamped;
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
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "menus")
public class Menu extends Timestamped {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private int price;

	@Column(nullable = true)
	private String image;

	@ManyToOne
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;
}
