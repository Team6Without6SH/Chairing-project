package com.sparta.chairingproject.domain.store.dto;

import java.io.Serializable;
import java.util.List;

import com.sparta.chairingproject.domain.menu.dto.response.MenuSummaryResponse;
import com.sparta.chairingproject.domain.review.dto.ReviewResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StoreDetailsResponse implements Serializable {
	private String name;
	private String image;
	private String description;
	private String address;
	private List<MenuSummaryResponse> menus;
	private List<ReviewResponse> reviews;
	private int waitingCount;
}
