package com.sparta.chairingproject.domain.order.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	private String message;
	private String sender;
	private String targetChannel;
}
