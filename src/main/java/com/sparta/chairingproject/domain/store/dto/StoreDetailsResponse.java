package com.sparta.chairingproject.domain.store.dto;

import java.util.List;

import com.sparta.chairingproject.domain.menu.dto.MenuDto;
import com.sparta.chairingproject.domain.review.dto.ReviewDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StoreDetailsResponse {
	private String name;
	private String image;
	private String description;
	private String address;
	private List<MenuDto> menus;
	private List<ReviewDto> reviews;
	private int waitingCount;
}
