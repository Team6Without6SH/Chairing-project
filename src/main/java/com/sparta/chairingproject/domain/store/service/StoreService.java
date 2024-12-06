package com.sparta.chairingproject.domain.store.service;

import java.util.List;

import org.springframework.stereotype.Service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;
import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.menu.dto.MenuDto;
import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
import com.sparta.chairingproject.domain.review.dto.ReviewDto;
import com.sparta.chairingproject.domain.review.repository.ReviewRepository;
import com.sparta.chairingproject.domain.store.dto.StoreDetailsResponse;
import com.sparta.chairingproject.domain.store.dto.StoreResponse;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sparta.chairingproject.domain.store.dto.StoreRequest;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;
import com.sparta.chairingproject.domain.store.mapper.StoreMapper;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

	private final StoreRepository storeRepository;
	private final MemberRepository memberRepository;
	private final MenuRepository menuRepository;
	private final ReviewRepository reviewRepository;

	public void registerStore(StoreRequest request, UserDetailsImpl authMember) {
		// 사장님 정보 확인
		Member owner = memberRepository.findById(authMember.getMember().getId())
			.orElseThrow(() -> new GlobalException(NOT_FOUND_USER));

		// 이미 등록된 가게 여부 확인
		int exitingStoreCount = storeRepository.countByOwner(owner);
		if (exitingStoreCount >= 3) {
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
		return StoreMapper.toStoreResponseList(stores);
	}


	public StoreDetailsResponse getStoreDetails(Long storeId) {
		// 1. 가게 조회
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

		// 2. 메뉴 리스트 조회
		List<MenuDto> menus = menuRepository.findByStoreId(storeId)
			.stream()
			.map(menu -> new MenuDto(menu.getName(), menu.getPrice()))
			.toList();

		// 3. 리뷰 리스트 조회
		List<ReviewDto> reviews = reviewRepository.findByStoreId(storeId)
			.stream()
			.map(review -> new ReviewDto(review.getMember().getName(), review.getContent(), review.getRating()))
			.toList();

		// 4. 현재 대기자 수 계산 (예: DB에 대기자 수 저장된 경우)
		int waitingCount = store.getReservations().size(); // 가게 예약 리스트 크기 사용

		// 5. 응답 객체 생성 및 반환
		return new StoreDetailsResponse(
			store.getName(),
			store.getImage(),
			store.getDescription(),
			store.getAddress(),
			menus,
			reviews,
			waitingCount
		);
	}
}
