package com.sparta.chairingproject.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PopularStoreResponse {
	private Long storeId;
	private String storeName;
	private String image;
	private String description;
	private double averageScore;
	private int orderCount;
}
