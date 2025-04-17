package com.university.utms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.university.utms")
public class UtmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(UtmsApplication.class, args);
	}

}
