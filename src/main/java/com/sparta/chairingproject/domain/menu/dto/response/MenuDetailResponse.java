package com.sparta.chairingproject.domain.menu.dto.response;

import com.sparta.chairingproject.domain.menu.entity.Menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MenuDetailResponse {
	private Long id;
	private String name;
	private int price;
	private String image;
	private boolean inActive;

	public static MenuDetailResponse from(Menu menu) {
		return MenuDetailResponse.builder()
			.id(menu.getId())
			.name(menu.getName())
			.price(menu.getPrice())
			.image(menu.getImage())
			.inActive(menu.isInActive())
			.build();
	}
}
