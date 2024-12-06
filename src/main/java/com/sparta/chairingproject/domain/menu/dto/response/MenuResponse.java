package com.sparta.chairingproject.domain.menu.dto.response;

import com.sparta.chairingproject.domain.menu.entity.Menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MenuResponse {
	private Long id;
	private String name;
	private int price;
	private String image;

	public static MenuResponse from(Menu menu) {
		return MenuResponse.builder()
			.id(menu.getId())
			.name(menu.getName())
			.price(menu.getPrice())
			.image(menu.getImage())
			.build();
	}
}