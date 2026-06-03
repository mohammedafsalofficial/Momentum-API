package com.momentum.api;

import org.springframework.boot.SpringApplication;

public class MomentumAPIApplication {

	public static void main(String[] args) {
		SpringApplication.from(MomentumAPI::main).with(TestcontainersConfiguration.class).run(args);
	}

}
