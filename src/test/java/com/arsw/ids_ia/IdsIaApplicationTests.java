package com.arsw.ids_ia;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Integration test - requires database and full Spring context")
class IdsIaApplicationTests {

	@Test
	void contextLoads() {
	}

}
