package com.sparta.chairingproject.domain.menu.dto.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MenuSummaryResponse implements Serializable {
	private String name;  // 메뉴 이름
	private int price;    // 메뉴 가격
}

