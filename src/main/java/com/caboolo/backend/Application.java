package com.caboolo.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@Slf4j
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class Application {

	public static void main(String[] args) {
		log.info("Starting Caboolo Backend Application...");
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		SpringApplication.run(Application.class, args);
		log.info("Caboolo Backend Application started successfully.");
	}

}
