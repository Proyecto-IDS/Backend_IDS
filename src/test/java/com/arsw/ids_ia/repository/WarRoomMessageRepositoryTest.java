package com.arsw.ids_ia.repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.arsw.ids_ia.model.Meeting;
import com.arsw.ids_ia.model.User;
import com.arsw.ids_ia.model.chat.WarRoomMessage;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
@ExtendWith(SpringExtension.class)
@DisplayName("WarRoomMessageRepository - Integration Test")
class WarRoomMessageRepositoryTest {
    @Autowired
    private WarRoomMessageRepository repository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    void findByMeetingIdOrderByCreatedAtAsc_returnsMessages() {
        Meeting meeting = new Meeting();
        meeting.setCode("test-code");
        meeting.setTitle("Test Meeting");
        meeting.setDescription("desc");
        meeting = entityManager.merge(meeting);

        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("pass");
        user.setRole(com.arsw.ids_ia.utils.enums.Role.USER);
        user = entityManager.merge(user);

        WarRoomMessage msg1 = WarRoomMessage.builder()
                .meeting(meeting)
                .sender(user)
                .content("msg1")
                .role("user")
                .createdAt(LocalDateTime.now().minusMinutes(2))
                .build();
        WarRoomMessage msg2 = WarRoomMessage.builder()
                .meeting(meeting)
                .sender(user)
                .content("msg2")
                .role("user")
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(msg1);
        repository.save(msg2);
        List<WarRoomMessage> result = repository.findByMeetingIdOrderByCreatedAtAsc(meeting.getId());
        assertEquals(2, result.size());
        assertEquals("msg1", result.get(0).getContent());
        assertEquals("msg2", result.get(1).getContent());
    }
}
