package com.sparta.chairingproject.domain.menu.dto.request;

import com.sparta.chairingproject.domain.menu.entity.MenuStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuUpdateRequest {
	private String name;
	private Integer price;
	private MenuStatus status;
}
