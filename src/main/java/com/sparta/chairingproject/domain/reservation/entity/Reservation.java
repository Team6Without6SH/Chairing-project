package com.sparta.chairingproject.domain.reservation.entity;

import java.time.LocalDate;
import java.time.LocalTime;

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

@Entity
@Getter
@Table(name = "reservations")
public class Reservation extends Timestamped {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String holder;

	@Column(nullable = false)
	private LocalDate date;

	@Column(nullable = false)
	private LocalTime time; //ERD 따라 만들긴했는데 LocalDateTime 형식이면 시간도 포함이긴한데
	//내가 Time 타입 참조변수를 잘 모름

	@ManyToOne
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;
}
