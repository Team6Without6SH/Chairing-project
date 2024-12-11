package com.sparta.chairingproject.domain.order.entity;

import com.sparta.chairingproject.domain.common.entity.Timestamped;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.store.entity.Store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
public class Order extends Timestamped {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store; // 가게 정보 추가

	// 주문 하나에 많은 메뉴를 받을 수 있게 하기 위해 중간테이블 설정 없이 다대다 관계로
	@ManyToMany
	@JoinTable(
		name = "order_menu",
		joinColumns = @JoinColumn(name = "order_id"),
		inverseJoinColumns = @JoinColumn(name = "menu_id")
	)
	private List<Menu> menus = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	@Column(nullable = false)
	private int price;

	private Order(Member member, Store store, List<Menu> menus, OrderStatus status, int price) {
		this.member = member;
		this.store = store;
		this.menus = menus;
		this.status = status;
		this.price = price;
	}

	public static Order createOf(Member member, Store store, List<Menu> menus, OrderStatus status,
		int price) {
		return new Order(member, store, menus, status, price);
	}

	public void changeStatus(OrderStatus status) {
		this.status = status;
	}
}
