package com.tradingbot.backend;
// main backend application class to bootstrap the Spring Boot application
// it contains the main method which runs the application
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
