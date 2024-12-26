package com.sparta.chairingproject;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableJpaAuditing
@EnableScheduling
@EnableCaching
public class ChairingProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChairingProjectApplication.class, args);
	}

	//github actions 테스트용 주석
}
