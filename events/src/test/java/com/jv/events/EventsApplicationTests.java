package com.jv.events;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"de.flapdoodle.mongodb.embedded.version=6.0.8"})
class EventsApplicationTests {

	@Test
	void contextLoads() {
	}

}
