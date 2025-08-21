package com.mertkacar.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.mertkacar"})
@SpringBootApplication
public class KeycloakApplicationStarter {
	public static void main(String[] args) {
		SpringApplication.run(KeycloakApplicationStarter.class, args);
	}
}
