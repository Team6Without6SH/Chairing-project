package com.sparta.chairingproject.domain.store.entity;

public enum StoreRequestStatus {
	PENDING,       // 승인 대기
	APPROVED,      // 승인 완료
	REJECTED,      // 승인 거절
	INACTIVE       // 삭제
}
