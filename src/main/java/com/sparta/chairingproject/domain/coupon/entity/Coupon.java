package com.sparta.chairingproject.domain.coupon.entity;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import java.util.ArrayList;
import java.util.List;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import com.sparta.chairingproject.domain.common.entity.Timestamped;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
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
	@Builder.Default
	private List<Issuance> issuances = new ArrayList<>();

	@Version
	private Long version; // 낙관적 락을 위한 버전 컬럼

	public void validateQuantity() {
		if (this.quantity <= 0) {
			throw new GlobalException(COUPON_OUT_OF_STOCK);
		}
	}

	public void decreaseQuantity() {
		this.quantity -= 1;
	}
}
