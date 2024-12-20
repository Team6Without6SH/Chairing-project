package com.sparta.chairingproject.domain.store.service;

import static com.sparta.chairingproject.config.exception.enums.ExceptionCode.*;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.chairingproject.config.exception.customException.GlobalException;
import com.sparta.chairingproject.domain.order.repository.OrderRepository;
import com.sparta.chairingproject.domain.review.repository.ReviewRepository;
import com.sparta.chairingproject.domain.store.dto.PopularStoreResponse;
import com.sparta.chairingproject.domain.store.entity.Store;
import com.sparta.chairingproject.domain.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PopularStoreService {

	private static final String POPULAR_STORES_KEY = "popularStores";
	private final StoreRepository storeRepository;
	private final ReviewRepository reviewRepository;
	private final RedisTemplate<String, Object> redisTemplate;
	private final OrderRepository orderRepository;

	@Transactional
	public void updatePopularStoresCache() {
		Pageable top100 = PageRequest.of(0, 200); // 상위 200개
		List<Store> topStores = storeRepository.findTopStoresByOrderCount(top100);

		// 필터링된 가게에서 리뷰 데이터 로드
		List<PopularStoreResponse> popularStores = topStores.stream().map(store -> {
				double averageScore = store.calculateAverageScore(); // 리뷰 평균 점수 계산
				int orderCount = store.getOrders().size(); // 주문 수 계산

				return new PopularStoreResponse(
					store.getId(),
					store.getName(),
					store.getImage(),
					store.getDescription(),
					averageScore,
					orderCount
				);
			}).sorted(Comparator.comparing(PopularStoreResponse::getOrderCount).reversed())
			.limit(25) // 최종 캐싱할 탑 25
			.toList();

		// Redis에 캐싱
		redisTemplate.opsForValue().set("popularStores", popularStores, Duration.ofHours(1));
	}

	// 인기 가게 데이터를 조회
	@Transactional(readOnly = true)
	public List<PopularStoreResponse> getPopularStores() {
		List<PopularStoreResponse> popularStores =
			(List<PopularStoreResponse>)redisTemplate.opsForValue().get(POPULAR_STORES_KEY);

		if (popularStores == null) {
			throw new GlobalException(NOT_FOUND_DATA);
		}
		return popularStores;
	}
}

