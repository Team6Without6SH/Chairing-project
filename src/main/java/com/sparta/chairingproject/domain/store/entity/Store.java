package com.sparta.chairingproject.domain.store.entity;

import java.util.ArrayList;
import java.util.List;

import com.sparta.chairingproject.domain.common.entity.Timestamped;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.reservation.entity.Reservation;
import com.sparta.chairingproject.domain.review.entity.Review;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "stores")
public class Store extends Timestamped {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = true)
	private String image;

	@Column(nullable = true)
	private String description;

	@NotBlank(message = "가게 주소는 필수 입력 항목입니다.")
	@Column(nullable = true)
	private String address;

	@Column(nullable = true)
	private String phone;

	@Column(nullable = true)
	private String openTime;

	@Column(nullable = true)
	private String closeTime;

	private String Category;

	@Column(nullable = false)
	private int tableCount;

	private boolean approved;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member owner;

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Menu> menus = new ArrayList<>();

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Review> reviews = new ArrayList<>();

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Reservation> reservations = new ArrayList<>();

	@OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Order> orders = new ArrayList<>();
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StoreStatus status = StoreStatus.PENDING;
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StoreRequestStatus requestStatus = StoreRequestStatus.PENDING;

	public Store(String name, String image, String description, Member owner) {
		this.name = name;
		this.image = image;
		this.description = description;
		this.owner = owner;
	}

	public Store(Long id, String name, String image, String description, Member member) {
		this.id = id;
		this.name = name;
		this.image = image;
		this.description = description;
		this.owner = member;
	}

	public void updateStoreStatus(StoreStatus status) {
		this.status = status;
	}

	public void updateRequestStatus(StoreRequestStatus storeRequestStatus) {
		this.requestStatus = storeRequestStatus;
	}

	// 상태 업데이트 메서드
	public void approveRequest() {
		this.requestStatus = StoreRequestStatus.APPROVED;
		this.status = StoreStatus.OPEN;
	}

	public void rejectRequest() {
		this.requestStatus = StoreRequestStatus.REJECTED;
	}

}
