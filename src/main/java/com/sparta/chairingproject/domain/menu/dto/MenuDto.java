package com.sparta.chairingproject.domain.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MenuDto {
	private String name;  // 메뉴 이름
	private int price;    // 메뉴 가격
}

