package com.example.JourneyHub;

import com.example.JourneyHub.service.route.RouteDataGenerator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@EnableCaching
public class JourneyHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(JourneyHubApplication.class, args);
	}
	@Bean
	public CommandLineRunner dataInitializer(RouteDataGenerator routeDataGenerator) {
		return args -> {
			System.out.println("Starting route generation...");
			routeDataGenerator.generateRoutes();
			System.out.println("Route generation completed.");
		};
	}

}
