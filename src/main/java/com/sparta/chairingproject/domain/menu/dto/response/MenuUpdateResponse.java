package com.sparta.chairingproject.domain.menu.dto.response;

import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.menu.entity.MenuStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MenuUpdateResponse {
	private Long id;
	private String name;
	private int price;
	private MenuStatus status;

	public static MenuUpdateResponse from(Menu menu) {
		return MenuUpdateResponse.builder()
			.id(menu.getId())
			.name(menu.getName())
			.price(menu.getPrice())
			.status(menu.getStatus())
			.build();
	}
}
