package com.sparta.chairingproject.domain.menu.dto.response;

import com.sparta.chairingproject.domain.menu.entity.Menu;
import com.sparta.chairingproject.domain.menu.entity.MenuStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuDeleteResponse {
	private Long menuId;
	private String name;
	private MenuStatus status;

	public static MenuDeleteResponse from(Menu menu) {
		return new MenuDeleteResponse(menu.getId(), menu.getName(), menu.getStatus());
	}
}
