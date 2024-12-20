package com.sparta.chairingproject.domain.common.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
@Service
public class S3Uploader {

	private final AmazonS3Client amazonS3Client;
	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	public String upload(MultipartFile file, String domain) {
		if (!file.isEmpty()) {
			try {
				String uuid = UUID.randomUUID().toString();
				String fileName = domain + file.getOriginalFilename() + "_" + uuid;
				ObjectMetadata metadata = new ObjectMetadata();
				metadata.setContentType(file.getContentType());
				metadata.setContentLength(file.getSize());
				amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);
				return fileName;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	public String update(String image, MultipartFile file, String domain) {
		if (image != null) {
			amazonS3Client.deleteObject(bucket, image);
		}
		return upload(file, domain);
	}

}
