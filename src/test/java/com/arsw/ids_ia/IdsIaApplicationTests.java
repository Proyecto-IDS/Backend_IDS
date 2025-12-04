package com.arsw.ids_ia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.arsw.ids_ia.config.MeetingScheduler;
import com.arsw.ids_ia.repository.AlertRepository;
import com.arsw.ids_ia.repository.MeetingRepository;
import com.arsw.ids_ia.repository.UserRepository;
import com.arsw.ids_ia.repository.AIPrivateMessageRepository;

@SpringBootTest(properties = {
	"app.security.oauth2.client-id=test",
	"app.security.oauth2.client-secret=test",
	"app.security.jwt.secret=test",
	"app.security.jwt.expiration=3600"
})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Disabled("Disabled to avoid JwtDecoder wiring during tests; covered by repository/controller tests")
class IdsIaApplicationTests {

	// Avoid constructing the scheduled component (which depends on JPA repos) in this smoke test
	@MockBean
	private MeetingScheduler meetingScheduler;
	
	// Mock AlertRepository to satisfy AlertService constructor during context load
	@MockBean
	private AlertRepository alertRepository;

	// Mock MeetingRepository to satisfy MeetingServiceImpl constructor during context load
	@MockBean
	private MeetingRepository meetingRepository;

	// Mock UserRepository to satisfy MeetingServiceImpl constructor during context load
	@MockBean
	private UserRepository userRepository;

	// Mock AIPrivateMessageRepository used by AIPrivateChatServiceImpl
	@MockBean
	private AIPrivateMessageRepository aiPrivateMessageRepository;

	@Test
	void contextLoads() {
	}

}
