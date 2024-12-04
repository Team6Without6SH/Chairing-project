package com.sparta.chairingproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
public class ChairingProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChairingProjectApplication.class, args);
	}

}
