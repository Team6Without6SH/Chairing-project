package com.sparta.chairingproject.domain.fcm.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.chairingproject.domain.fcm.dto.request.FcmRequest;
import com.sparta.chairingproject.domain.fcm.sevice.FcmService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/fcm")
public class FcmController {

	private final FcmService fcmService;

	public FcmController(FcmService fcmService) {
		this.fcmService = fcmService;
	}

	@PostMapping("/send")
	public ResponseEntity<Object> pushMessage(@RequestBody @Validated FcmRequest req) throws
		IOException {
		log.debug("[+] 푸시 메시지를 전송합니다. ");
		int result = fcmService.sendMessageTo(req);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}