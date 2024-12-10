package com.sparta.chairingproject.domain.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreRequest {
	@NotBlank(message = "매장 이름은 필수 입력 항목입니다.")
	private String name;

	@NotBlank(message = "매장 주소는 필수 입력 항목입니다.")
	private String address;

	@NotBlank(message = "매장 번호는 필수 입력 항목입니다.")
	private String phone;

	private String openTime;

	private String closeTime;

	private String category;

	@NotBlank(message = "매장 설명은 필수 입력 항목입니다.")
	@Size(max = 500, message = "설명은 500자를 초과할 수 없습니다.")
	private String description;

	private String image;

	// storeRequest TEST 용
	public StoreRequest(String testStore, String testAddress, String s, String description, String testImage) {
		this.name = testStore;
		this.address = testAddress;
		this.phone = s;
		this.openTime = s;
		this.closeTime = s;
		this.category = s;
		this.description = description;
		this.image = testImage;
	}
}