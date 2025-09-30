package com.marbl.declarative_batct.spring_declarative_batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringDeclarativeBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringDeclarativeBatchApplication.class, args);
	}

}
