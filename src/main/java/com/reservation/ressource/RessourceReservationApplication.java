package com.reservation.ressource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan("com.reservation")
@EnableJpaRepositories(basePackages = "com.reservation.repository")
@EntityScan(basePackages = "com.reservation.entity")
public class RessourceReservationApplication {

	public static void main(String[] args) {
		SpringApplication.run(RessourceReservationApplication.class, args);
	}

}
