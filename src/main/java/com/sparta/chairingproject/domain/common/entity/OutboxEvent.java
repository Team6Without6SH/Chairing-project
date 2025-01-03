package com.sparta.chairingproject.domain.common.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.chairingproject.domain.outbox.entity.Outbox;

public interface OutboxEvent {
	enum Type {
		MEMBER,
		COUPON,
		RESERVATION,
		ORDER
	}

	Outbox toOutbox();
}
