package com.sparta.chairingproject.domain.store.service;

import org.springframework.stereotype.Service;
import com.sparta.chairingproject.config.JwtUtil;
import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.exception.enums.ExceptionCode;
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
			.orElseThrow(() -> new GlobalException(ExceptionCode.NOT_FOUND_USER));

		// 이미 등록된 가게 여부 확인
		if (storeRepository.existsByOwner(owner)) {
			throw new GlobalException(ExceptionCode.CANNOT_EXCEED_STORE_LIMIT);
		}

		// 영업 시간 검증
		if ((request.getOpenTime() == null) != (request.getCloseTime() == null)) {
			throw new GlobalException(ExceptionCode.STORE_CLOSED);
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


