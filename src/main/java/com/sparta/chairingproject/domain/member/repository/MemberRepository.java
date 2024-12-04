package com.sparta.chairingproject.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.chairingproject.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByEmail(String email);
	boolean existsByEmail(String email);
}
