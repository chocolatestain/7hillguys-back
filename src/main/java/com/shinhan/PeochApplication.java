package com.shinhan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PeochApplication {

	public static void main(String[] args) {
		SpringApplication.run(PeochApplication.class, args);
	}

}
