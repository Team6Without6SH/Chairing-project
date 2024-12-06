package com.sparta.chairingproject.domain.menu.dto.request;

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
	private Boolean soldOut; //여기의 참조변수는 null 을 가능하게 하기 위해서입니다.
}
