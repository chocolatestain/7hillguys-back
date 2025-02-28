package com.shinhan.peoch;

import org.springframework.boot.SpringApplication;

public class TestPeochApplication {

	public static void main(String[] args) {
		SpringApplication.from(PeochApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
