package com.arsw.ids_ia.dto.chat;

public class WarRoomMessageRequest {
    private Long meetingId;
    private String content;
    private String role;

    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
