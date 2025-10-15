package com.ads.mygateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MygatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(MygatewayApplication.class, args);
	}

}
