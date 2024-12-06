package com.sparta.chairingproject.domain.member.entity;

import com.sparta.chairingproject.domain.Issuance.entity.Issuance;
import com.sparta.chairingproject.domain.common.entity.Timestamped;
import com.sparta.chairingproject.domain.order.entity.Order;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
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
	@Enumerated(EnumType.STRING)
	private MemberRole memberRole;

	public Member(String name, @NotBlank @Email String email, String password,
		MemberRole memberRole) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.memberRole = memberRole;
	}

	public Member(Long id, String name, @NotBlank @Email String email, String password,
		MemberRole memberRole) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
		this.memberRole = memberRole;
	}

	// TEST
	public Member(Long id, String name,@NotBlank @Email String email, String password, MemberRole memberRole) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
		this.memberRole = memberRole;
	}

	public void updatePassword(String updatePassword) {
		this.password = updatePassword;
	}
}
