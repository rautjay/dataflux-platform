package com.dataflux;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DatafluxApiApplication {

	public static void main(String[] args) throws InterruptedException {

		SpringApplication.run(DatafluxApiApplication.class, args);
		System.out.println("Starting Dataflux API...");

	}

}
