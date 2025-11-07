package com.arsw.ids_ia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IdsIaApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdsIaApplication.class, args);
	}

}
