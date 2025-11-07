package com.arsw.ids_ia.dto.response;

import java.util.Set;

public record MeetingResponse(
    Long id,
    String code,
    String title,
    String description,
    String startTime,
    String endTime,
    String creatorEmail,
    Set<String> participantEmails,
    Integer currentParticipantCount,
    Long durationSeconds,
    String status
) {}
