package com.example.Vi_vu_Viet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ViVuVietApplication {

	public static void main(String[] args) {
		System.out.println("Hello world");
		SpringApplication.run(ViVuVietApplication.class, args);
	}

}
