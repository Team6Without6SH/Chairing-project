package com.sparta.chairingproject.domain.store.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.sparta.chairingproject.domain.common.service.S3Uploader;
import java.io.IOException;
import java.util.List;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.config.security.UserDetailsImpl;
import com.sparta.chairingproject.domain.member.entity.Member;
import com.sparta.chairingproject.domain.member.repository.MemberRepository;
import com.sparta.chairingproject.domain.menu.dto.response.MenuSummaryResponse;
import com.sparta.chairingproject.domain.menu.repository.MenuRepository;
import com.sparta.chairingproject.domain.review.dto.ReviewResponse;
import com.sparta.chairingproject.domain.review.repository.ReviewRepository;
import com.sparta.chairingproject.domain.store.dto.StoreDetailsResponse;
import com.sparta.chairingproject.domain.store.dto.StoreOpenCloseRequest;
import com.sparta.chairingproject.domain.store.dto.StoreOpenCloseResponse;
import com.sparta.chairingproject.domain.store.dto.StoreOwnerResponse;
import com.sparta.chairingproject.domain.store.dto.StoreRequest;
import com.sparta.chairingproject.domain.store.dto.StoreResponse;
import com.sparta.chairingproject.domain.store.dto.UpdateStoreRequest;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.entity.StoreRequestStatus;
import com.sparta.chairingproject.domain.store.entity.StoreStatus;
import com.sparta.chairingproject.domain.store.mapper.StoreMapper;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class StoreService {

	private final StoreRepository storeRepository;
	private final MemberRepository memberRepository;
	private final MenuRepository menuRepository;
	private final ReviewRepository reviewRepository;
	private final S3Uploader s3Uploader;

	@Transactional
	public StoreResponse registerStore(StoreRequest request, UserDetailsImpl authMember,
		MultipartFile file) {

		Member owner = memberRepository.findById(authMember.getMember().getId())
			.orElseThrow(() -> new GlobalException(NOT_FOUND_USER));

		int exitingStoreCount = storeRepository.countByOwner(owner);
		if (exitingStoreCount >= 3) {
			throw new GlobalException(CANNOT_EXCEED_STORE_LIMIT);
		}

		if ((request.getOpenTime() == null) != (request.getCloseTime() == null)) {
			throw new GlobalException(STORE_CLOSED);
		}

		// 이미지 업로드 로직 추가 필요 (예: S3)
		String fileName = s3Uploader.upload(file);

		Store store = new Store( // request 에 맞게 생성자 추가하고 -> pending 값을 여기다 담아두고 ->save  하기 ->
			request.getName(), request.getAddress(), fileName, request.getDescription(), owner);
		store.setTableCount(request.getTableCount());
		storeRepository.save(store);

		return StoreMapper.toStoreResponse(store);
	}

	public List<StoreResponse> getAllOpenedStores() {

		List<Store> stores = storeRepository.findAllByRequestStatusAndStatus(
			StoreRequestStatus.APPROVED,
			StoreStatus.OPEN);

		if (stores.isEmpty()) {
			throw new GlobalException(NOT_FOUND_STORE);
		}

		return StoreMapper.toStoreResponseList(stores);
	}

	@Cacheable(value = "storeDetails", key = "'store:' + #storeId + ':details'")
	public StoreDetailsResponse getStoreDetails(Long storeId) {
		System.out.println("DB 조회 실행: storeId = " + storeId);

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

		List<MenuSummaryResponse> menus = menuRepository.findByStoreId(storeId)
			.stream()
			.map(menu -> new MenuSummaryResponse(menu.getName(), menu.getPrice()))
			.toList();

		List<ReviewResponse> reviews = reviewRepository.findByStoreId(storeId)
			.stream()
			.map(review -> new ReviewResponse(review.getMember().getName(), review.getContent(),
				review.getScore()))
			.toList();

		int waitingCount =
			store.getReservations() == null ? 0 : store.getReservations().size(); // 가게 예약 리스트 크기 사용

		return new StoreDetailsResponse(store.getName(), store.getImage(), store.getDescription(),
			store.getAddress(),
			menus, reviews, waitingCount);
	}

	@Transactional
	@CacheEvict(value = "storeDetails", key = "'store:' + #storeId + ':details'")
	public StoreDetailsResponse updateStore(Long storeId, UpdateStoreRequest req,
		UserDetailsImpl authUser, MultipartFile file) {
		Member owner = memberRepository.findById(authUser.getMember().getId())
			.orElseThrow(() -> new GlobalException(NOT_FOUND_USER));

		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

		String fileName = s3Uploader.update(store.getImage(), file);
		req.setImage(fileName);
		store.updateStore(req);
		storeRepository.save(store);

		List<MenuSummaryResponse> menus = menuRepository.findByStoreId(storeId)
			.stream()
			.map(menu -> new MenuSummaryResponse(menu.getName(), menu.getPrice()))
			.toList();

		List<ReviewResponse> reviews = reviewRepository.findByStoreId(storeId)
			.stream()
			.map(review -> new ReviewResponse(review.getMember().getName(), review.getContent(),
				review.getScore()))
			.toList();

		int waitingCount =
			store.getReservations() == null ? 0 : store.getReservations().size();// 가게 예약 리스트 크기 사용

		return new StoreDetailsResponse(store.getName(), store.getImage(), store.getDescription(),
			store.getAddress(),
			menus, reviews, waitingCount);
	}

	@Transactional
	public StoreOwnerResponse getStoreById(Long storeId, Long ownerId) {

		Store store = storeRepository.findByIdAndOwnerId(storeId, ownerId)
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

		if (store.getRequestStatus() != StoreRequestStatus.APPROVED) {
			if (store.getStatus() == StoreStatus.PENDING) {
				throw new GlobalException(APPROVAL_PENDING);
			}
			throw new GlobalException(STORE_OUT_OF_BUSINESS);
		}

		return new StoreOwnerResponse(
			store.getName(),
			store.getAddress(),
			store.getPhone(),
			store.getOpenTime(),
			store.getCloseTime(),
			store.getCategory(),
			store.getDescription(),
			store.getImage(),
			store.getTableCount(),
			store.getOwner().getId()
		);
	}

	@Transactional
	public void requestDeleteStore(Long storeId, UserDetailsImpl authUser) {
		Member owner = memberRepository.findById(authUser.getMember().getId())
			.orElseThrow(() -> new GlobalException(NOT_FOUND_USER));

		Store store = storeRepository.findByIdAndOwnerId(storeId, owner.getId())
			.orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

		if (store.getRequestStatus() == StoreRequestStatus.DELETE_REQUESTED) {
			throw new GlobalException(STORE_ALREADY_DELETED);
		}

		store.setRequestStatus(StoreRequestStatus.DELETE_REQUESTED);
		storeRepository.save(store);

		// 알림 정도 추가 가능할듯?
	}

	public StoreOpenCloseResponse storeOpenClose(Long storeId, Member member, StoreOpenCloseRequest request) {
		Store store = storeRepository.findById(storeId).orElseThrow(() -> new GlobalException(NOT_FOUND_STORE));

		if (!store.getOwner().getId().equals(member.getId())) {
			throw new GlobalException(ONLY_OWNER_ALLOWED);
		}
		StoreStatus newStatus = StoreStatus.valueOf(request.getStatus().toUpperCase());

		store.storeOpenClose(newStatus);

		return StoreOpenCloseResponse.builder()
			.storeId(store.getId())
			.status(newStatus.name())
			.build();
	}
}
