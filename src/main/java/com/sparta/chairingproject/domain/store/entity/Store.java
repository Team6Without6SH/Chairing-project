package com.sparta.chairingproject.domain.store.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.sparta.chairingproject.domain.common.entity.Timestamped;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.order.entity.Order;
import com.sparta.chairingproject.domain.reservation.entity.Reservation;
import com.sparta.chairingproject.domain.review.entity.Review;
import com.sparta.chairingproject.domain.store.dto.UpdateStoreRequest;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Builder
@AllArgsConstructor
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

	@Column(nullable = true)
	private String address;

	@Column(nullable = true)
	private String phone;

	@Column(nullable = true)
	private String openTime;

	@Column(nullable = true)
	private String closeTime;

	private String category;

	@Column(nullable = false)
	private int tableCount;

	@Column
	private LocalDateTime deletedAt = null;

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
	@Setter
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StoreStatus status = StoreStatus.PENDING;
	@Setter
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StoreRequestStatus requestStatus = StoreRequestStatus.PENDING;

	// 매개변수 생성자 (서비스)
	public Store(String name, String address, String image, String description, Member owner) {
		this.name = name;
		this.image = image;
		this.description = description;
		this.address = address;
		this.owner = owner;
	}

	// 상태 업데이트 메서드
	public void approveRequest() {
		this.requestStatus = StoreRequestStatus.APPROVED;
		this.status = StoreStatus.OPEN;
	}

	public void rejectRequest() {
		this.requestStatus = StoreRequestStatus.REJECTED;
	}

	public void updateStore(UpdateStoreRequest req) {
		this.name = req.getName();
		this.address = req.getAddress();
		this.phone = req.getPhone();
		this.openTime = req.getOpenTime();
		this.closeTime = req.getCloseTime();
		this.category = req.getCategory();
		this.description = req.getDescription();
		this.image = req.getImage();
	}

	public void approveDelete() {
		this.status = StoreStatus.CLOSED;
		this.requestStatus = StoreRequestStatus.DELETED;
		this.deletedAt = LocalDateTime.now();
	}

	public boolean isDeleted() {
		return this.requestStatus == StoreRequestStatus.DELETED;
	}

	public void rejectDeleteRequest() {
		this.requestStatus = StoreRequestStatus.DELETE_REJECTED;
	}

	public void setTableCount(int tableCount) {
		this.tableCount = tableCount;
	}
}
