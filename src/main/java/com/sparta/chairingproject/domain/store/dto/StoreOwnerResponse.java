package com.sparta.chairingproject.domain.store.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StoreOwnerResponse {
	private String name;           // 가게 이름
	private String address;        // 가게 주소
	private String phone;          // 가게 전화번호
	private String openTime;       // 영업 시작 시간
	private String closeTime;      // 영업 종료 시간
	private String category; // 카테고리 목록
	private String description;    // 가게 설명
	private List<String> images;   // 이미지 목록
	private boolean approved;

}
