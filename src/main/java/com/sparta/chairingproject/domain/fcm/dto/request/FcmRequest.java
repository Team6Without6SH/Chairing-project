package com.sparta.chairingproject.domain.fcm.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 모바일에서 전달받은 객체
 *
 * @author : lee
 * @fileName : FcmSendDto
 * @since : 2/21/24
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmRequest {
	private String token;

	private String title;

	private String body;

	@Builder(toBuilder = true)
	public FcmRequest(String token, String title, String body) {
		this.token = token;
		this.title = title;
		this.body = body;
	}
}