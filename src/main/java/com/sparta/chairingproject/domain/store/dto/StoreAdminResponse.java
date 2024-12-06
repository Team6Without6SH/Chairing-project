package com.sparta.chairingproject.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreAdminResponse {
	private Long id;                  // 가게 ID
	private String name;              // 가게 이름
	private String ownerName;         // 사장님 이름
	private String status;            // 가게 상태 (OPEN, CLOSED 등)
	private String requestStatus;

	public StoreAdminResponse(Long id, String name, String status) {
		this.id = id;
		this.name = name;
		this.status = status;
	}
}
