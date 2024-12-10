package com.sparta.chairingproject.domain.menu.dto.request;

import com.sparta.chairingproject.domain.common.dto.RequestDto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MenuRequest extends RequestDto {
	@NotEmpty(message = "메뉴 이름은 필수 입력 항목입니다.")
	private String name;

	@Positive(message = "가격은 0보다 커야 합니다.")
	private int price;

	private String image;

	//테스트 용 생성자
	public MenuRequest(String name, int price, String image) {
		this.name = name;
		this.price = price;
		this.image = image;
	}
}
