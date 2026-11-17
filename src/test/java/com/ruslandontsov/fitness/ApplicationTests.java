package com.ruslandontsov.fitness;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

// Starts the whole application, so it needs the PostgreSQL database from docker-compose.
@EnabledIfEnvironmentVariable(named = "DB_PASSWORD", matches = ".+")
@SpringBootTest
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
