package com.example.JourneyHub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@EnableCaching
public class JourneyHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(JourneyHubApplication.class, args);
	}

}
