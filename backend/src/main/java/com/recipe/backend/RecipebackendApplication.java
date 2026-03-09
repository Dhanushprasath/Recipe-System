package com.recipe.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.recipe.backend.model")
public class RecipebackendApplication {


	public static void main(String[] args) {
		SpringApplication.run(RecipebackendApplication.class, args);
	}

}
