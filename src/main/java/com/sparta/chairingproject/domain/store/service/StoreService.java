package com.sparta.chairingproject.domain.store.service;

import org.springframework.stereotype.Service;
import com.sparta.chairingproject.config.JwtUtil;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sparta.chairingproject.domain.store.dto.StoreRequest;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

	private final StoreRepository storeRepository;
	private final MemberRepository memberRepository;
	private final JwtUtil jwtUtil;

	public void registerStore(StoreRequest request, UserDetailsImpl authMember) {
		// 사장님 정보 확인
		Member owner = memberRepository.findById(authMember.getMember().getId())
			.orElseThrow(() -> new IllegalArgumentException("해당 사장님 정보를 찾을 수 없습니다."));

		if (storeRepository.existsByOwner(owner)) {
			throw new IllegalStateException("이미 매장이 등록되어 있습니다.");
		}

		if ((request.getOpenTime() == null) != (request.getCloseTime() == null)) {
			throw new IllegalArgumentException("영업 시작 시간과 종료 시간을 모두 입력해야 합니다.");
		}

		// Store 엔터티 생성 및 저장
		Store store = new Store(
			request.getName(),
			request.getImages().isEmpty() ? null : request.getImages().get(0), // 첫 번째 이미지 선택
			request.getDescription(),
			authMember.getMember()
		);

		storeRepository.save(store); // 저장

	}
}


