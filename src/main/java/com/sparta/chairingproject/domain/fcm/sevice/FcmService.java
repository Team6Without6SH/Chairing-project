package com.sparta.chairingproject.domain.fcm.sevice;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.sparta.chairingproject.domain.fcm.dto.request.FcmMessageRequest;

@Service
public interface FcmService {

	int sendMessageTo(FcmMessageRequest req) throws IOException;

}