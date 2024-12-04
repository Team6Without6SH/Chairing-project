package com.sparta.chairingproject.domain.order.entity;

import java.util.List;

import com.sparta.chairingproject.domain.common.entity.Timestamped;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.menu.entity.Menu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor
public class Order extends Timestamped {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	// 주문 하나에 많은 메뉴를 받을 수 있게 하기 위해 중간테이블 설정 없이 다대다 관계로
	@ManyToMany
	@JoinTable(
		name = "order_menu",
		joinColumns = @JoinColumn(name = "order_id"),
		inverseJoinColumns = @JoinColumn(name = "menu_id")
	)
	private List<Menu> menus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	@Column(nullable = false)
	private int price;

	private Order(Member member, List<Menu> menus, OrderStatus status, int price) {
		this.member = member;
		this.menus = menus;
		this.status = status;
		this.price = price;
	}

	public static Order createOf(Member member, List<Menu> menus, OrderStatus status, int price) {
		return new Order(member, menus, status, price);
	}
}
