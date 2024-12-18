package com.sparta.chairingproject.domain.fcm.sevice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.sparta.chairingproject.domain.fcm.dto.response.FcmMessageResponse;
import com.sparta.chairingproject.domain.fcm.dto.request.FcmMessageRequest;

@Service
public class FcmServiceImpl implements FcmService {

	/**
	 * 푸시 메시지 처리를 수행하는 비즈니스 로직
	 *
	 * @param req 모바일에서 전달받은 Object
	 * @return 성공(1), 실패(0)
	 */
	@Override
	public int sendMessageTo(FcmMessageRequest req) throws IOException {

		String message = makeMessage(req);
		RestTemplate restTemplate = new RestTemplate();
		/**
		 * 추가된 사항 : RestTemplate 이용중 클라이언트의 한글 깨짐 증상에 대한 수정
		 * @refernece : https://stackoverflow.com/questions/29392422/how-can-i-tell-resttemplate-to-post-with-utf-8-encoding
		 */
		restTemplate.getMessageConverters()
			.add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + getAccessToken());

		HttpEntity entity = new HttpEntity<>(message, headers);

		String API_URL = "https://fcm.googleapis.com/v1/projects/chairing-33cfe/messages:send";
		ResponseEntity response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

		System.out.println(response.getStatusCode());

		return response.getStatusCode() == HttpStatus.OK ? 1 : 0;
	}

	/**
	 * Firebase Admin SDK의 비공개 키를 참조하여 Bearer 토큰을 발급 받습니다.
	 *
	 * @return Bearer token
	 */
	private String getAccessToken() throws IOException {
		String firebaseConfigPath = "firebase/chairing-33cfe-firebase-adminsdk-rw63h-690386f487.json";

		// Firebase 서비스 계정 키 파일 로드
		GoogleCredentials googleCredentials = GoogleCredentials
			.fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
			.createScoped(List.of("https://www.googleapis.com/auth/firebase.messaging"));

		System.out.println("GoogleCredentials loaded successfully.");

		// 액세스 토큰 갱신
		googleCredentials.refreshIfExpired();
		String tokenValue = googleCredentials.getAccessToken().getTokenValue();

		System.out.println("Access Token: " + tokenValue);

		return tokenValue;
	}


	/**
	 * FCM 전송 정보를 기반으로 메시지를 구성합니다. (Object -> String)
	 *
	 * @param req FcmSendDto
	 * @return String
	 */
	private String makeMessage(FcmMessageRequest req) throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		FcmMessageResponse fcmMessageResponse = FcmMessageResponse.builder()
			.message(FcmMessageResponse.Message.builder()
				.token(req.getToken())
				.notification(FcmMessageResponse.Notification.builder()
					.title(req.getTitle())
					.body(req.getBody())
					.image(null)
					.build()
				).build()).validateOnly(false).build();

		return om.writeValueAsString(fcmMessageResponse);
	}
}