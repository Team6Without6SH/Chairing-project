package com.sparta.chairingproject.domain.member.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import com.sparta.chairingproject.domain.common.entity.Timestamped;
import com.sparta.chairingproject.domain.order.entity.Order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "members")
public class Member extends Timestamped {

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<Order> orders = new ArrayList<>();
	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<Issuance> issuances = new ArrayList<>();
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String name;
	@Column(nullable = false, unique = true)
	private String email;
	@Column(nullable = false)
	private String password;
	@Column
	private String image;
	@Enumerated(EnumType.STRING)
	private MemberRole memberRole;

	public Member(String name, @NotBlank @Email String email, String password, String image,
		MemberRole memberRole) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.image = image;
		this.memberRole = memberRole;
	}

	public void updatePassword(String updatePassword) {
		this.password = updatePassword;
	}

	public void delete() {
		deletedAt = LocalDateTime.now();
	}

	public void updateImage(String fileName) {
		this.image = fileName;
	}
}
