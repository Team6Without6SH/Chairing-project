package com.sparta.chairingproject.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreAdminResponse {
	private Long id;
	private String name;
	private String ownerName;
	private String status;
	private String requestStatus;

	public StoreAdminResponse(Long id, String name, String status, String requestStatus) {
		this.id = id;
		this.name = name;
		this.status = status;
		this.requestStatus = status;
	}
}
