package com.sparta.chairingproject.domain.menu.dto.response;

import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.menu.entity.MenuStatus;

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
	private MenuStatus status;

	public static MenuDetailResponse from(Menu menu) {
		return MenuDetailResponse.builder()
			.id(menu.getId())
			.name(menu.getName())
			.price(menu.getPrice())
			.image(menu.getImage())
			.status(menu.getStatus())
			.build();
	}
}
