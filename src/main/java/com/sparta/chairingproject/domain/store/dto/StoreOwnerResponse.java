package com.sparta.chairingproject.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StoreOwnerResponse {
	private String name;
	private String address;
	private String phone;
	private String openTime;
	private String closeTime;
	private String category;
	private String description;
	private String image;
	private int tableCount;
	private Long ownerId;

}
