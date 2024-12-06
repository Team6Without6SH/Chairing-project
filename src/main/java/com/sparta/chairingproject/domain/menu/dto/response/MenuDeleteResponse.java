package com.sparta.chairingproject.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuDeleteResponse {
	private Long menuId;
	private String name;
	private boolean inActive;
}
