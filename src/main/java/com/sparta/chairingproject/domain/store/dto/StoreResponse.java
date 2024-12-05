package com.sparta.chairingproject.domain.store.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StoreResponse {
	private Long id;                  // 가게 ID
	private String name;              // 가게 이름
	private String address;           // 가게 주소
	private String phone;             // 연락처
	private String openTime;          // 영업 시작 시간
	private String closeTime;         // 영업 종료 시간
	private List<String> categories;  // 카테고리 리스트
	private String description;       // 가게 설명
	private List<String> images;      // 이미지 리스트
	private String ownerName;         // 사장님 이름

}