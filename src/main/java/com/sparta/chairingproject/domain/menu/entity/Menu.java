package com.sparta.chairingproject.domain.menu.entity;

import java.util.List;

import com.sparta.chairingproject.domain.common.entity.Timestamped;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.order.entity.OrderStatus;
import com.sparta.chairingproject.domain.store.entity.Store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
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

	@Column(nullable = false)
	private boolean inActive = false;

	@ManyToOne
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	private Menu(String name, int price, String image, Store store) {
		this.name = name;
		this.price = price;
		this.image = image;
		this.store = store;
	}

	public static Menu createOf(String name, int price, String image, Store store) {
		return new Menu(name, price, image, store);
	}

	public void delete() {
		this.inActive = true;
	}

}
