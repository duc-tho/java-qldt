package com.nhom11.qldt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class QldtApplication {

	public static void main(String[] args) {
		SpringApplication.run(QldtApplication.class, args);
	}

}
