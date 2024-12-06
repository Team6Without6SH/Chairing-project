package com.sparta.chairingproject.domain.store.service;

import java.util.List;

import org.springframework.stereotype.Service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.menu.dto.response.MenuSummaryResponse;
import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
import com.sparta.chairingproject.domain.review.dto.ReviewResponse;
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

		Member owner = memberRepository.findById(authMember.getMember().getId())
			.orElseThrow(() -> new GlobalException(NOT_FOUND_USER));
		
		int exitingStoreCount = storeRepository.countByOwner(owner);
		if (exitingStoreCount >= 3) {
			throw new GlobalException(CANNOT_EXCEED_STORE_LIMIT);
		}

		if ((request.getOpenTime() == null) != (request.getCloseTime() == null)) {
			throw new GlobalException(STORE_CLOSED);
		}

		Store store = new Store(
			request.getName(),
			request.getImages().isEmpty() ? null : request.getImages().get(0), // 첫 번째 이미지 선택
			request.getDescription(),
			authMember.getMember()
		);
		storeRepository.save(store); // 저장
	}

	public List<StoreResponse> getAllOpenedStores() {

		List<Store> stores = storeRepository.findAllByRequestStatusAndStatus(
			StoreRequestStatus.APPROVED, StoreStatus.OPEN
		);

		if (stores.isEmpty()) {
			throw new GlobalException(NOT_FOUND_STORE);
		}

		return StoreMapper.toStoreResponseList(stores);
	}

	public StoreDetailsResponse getStoreDetails(Long storeId) {

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

		List<MenuSummaryResponse> menus = menuRepository.findByStoreId(storeId)
			.stream()
			.map(menu -> new MenuSummaryResponse(menu.getName(), menu.getPrice()))
			.toList();

		List<ReviewResponse> reviews = reviewRepository.findByStoreId(storeId)
			.stream()
			.map(review -> new ReviewResponse(review.getMember().getName(), review.getContent(), review.getRating()))
			.toList();

		int waitingCount = store.getReservations().size(); // 가게 예약 리스트 크기 사용

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
