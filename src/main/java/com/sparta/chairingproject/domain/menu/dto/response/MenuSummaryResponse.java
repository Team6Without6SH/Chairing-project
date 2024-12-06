package com.sparta.chairingproject.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MenuSummaryResponse {
	private String name;  // 메뉴 이름
	private int price;    // 메뉴 가격
}

