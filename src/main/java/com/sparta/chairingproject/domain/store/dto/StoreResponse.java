package com.sparta.chairingproject.domain.store.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StoreResponse {
	private Long id;
	private String name;
	private String address;
	private String phone;
	private String openTime;
	private String closeTime;
	private List<String> categories;
	private String description;
	private List<String> images;
	private String ownerName;
}
