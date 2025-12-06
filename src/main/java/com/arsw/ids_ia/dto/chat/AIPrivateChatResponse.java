package com.arsw.ids_ia.dto.chat;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AIPrivateChatResponse {
    private Long id;
    private Long meetingId;
    private Long userId;
    private String userEmail;
    private String content;
    private String role;
    private LocalDateTime createdAt;
}