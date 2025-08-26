package com.mertkacar.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan(basePackages = {"com.mertkacar"})
@SpringBootApplication
@EntityScan(basePackages = {"com.mertkacar.model"})
@EnableJpaRepositories(basePackages = {"com.mertkacar.repository"})
public class KeycloakApplicationStarter {
	public static void main(String[] args) {
		SpringApplication.run(KeycloakApplicationStarter.class, args);
	}
}
