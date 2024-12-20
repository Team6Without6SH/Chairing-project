package com.sparta.chairingproject.domain.common.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sparta.chairingproject.domain.store.service.PopularStoreService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PopularStoreBatch {

	private final PopularStoreService popularStoreService;

	// 1시간 간격으로 실행
	@Scheduled(cron = "0 0 0 * * * ")
	public void updatePopularStores() {
		popularStoreService.updatePopularStoresCache();
		System.out.println("인기 가게 데이터 갱신 완료");
	}
}