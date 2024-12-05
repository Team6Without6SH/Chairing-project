package com.sparta.chairingproject.domain.store.service;

import java.util.List;

import org.springframework.stereotype.Service;
import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;
import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.store.dto.StoreResponse;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sparta.chairingproject.domain.store.dto.StoreRequest;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

	private final StoreRepository storeRepository;
	private final MemberRepository memberRepository;

	public void registerStore(StoreRequest request, UserDetailsImpl authMember) {
		// 사장님 정보 확인
		Member owner = memberRepository.findById(authMember.getMember().getId())
			.orElseThrow(() -> new GlobalException(NOT_FOUND_USER));

		// 이미 등록된 가게 여부 확인
		if (storeRepository.existsByOwner(owner)) {
			throw new GlobalException(CANNOT_EXCEED_STORE_LIMIT);
		}

		// 영업 시간 검증
		if ((request.getOpenTime() == null) != (request.getCloseTime() == null)) {
			throw new GlobalException(STORE_CLOSED);
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

	public List<StoreResponse> getAllOpenedStores() {
		// Approved 상태 및 Open 상태의 가게만 조회
		List<Store> stores = storeRepository.findAllByRequestStatusAndStatus(
			StoreRequestStatus.APPROVED, StoreStatus.OPEN
		);

		//조회된 가게가 없는 경우
		if (stores.isEmpty()) {
			throw new GlobalException(NOT_FOUND_STORE);
		}

		// Store 데이터를 StoreResponse DTO로 변환하여 반환
		return stores.stream()
			.map(store -> new StoreResponse(
				store.getName(),
				store.getImage(),
				store.getDescription()
			))
			.toList();
	}

	public List<StoreResponse> getAdminStores(StoreStatus status) {
		// 상태와 승인된 요청 상태를 기준으로 가게 조회
		List<Store> stores = storeRepository.findByStatusAndRequestStatus(
			status, StoreRequestStatus.APPROVED
		);

		// 조회된 가게가 없는 경우 예외 처리
		if (stores.isEmpty()) {
			throw new GlobalException(NOT_FOUND_STORE);
		}

		// Store 엔티티를 StoreResponse로 변환
		return stores.stream()
			.map(store -> new StoreResponse(
				store.getId(),
				store.getName(),
				store.getAddress() != null ? store.getAddress() : "기본 주소 없음", // 기본값 설정
				store.getPhone() != null ? store.getPhone() : "전화번호 없음",       // 기본값 설정
				store.getOpenTime() != null ? store.getOpenTime() : "09:00",     // 기본값 설정
				store.getCloseTime() != null ? store.getCloseTime() : "18:00",   // 기본값 설정
				null, // 카테고리 정보는 필요 없음
				store.getDescription(),
				List.of(store.getImage() != null ? store.getImage() : "default.jpg"), // 기본 이미지 설정
				store.getOwner().getName()
			))
			.toList();
	}
}
