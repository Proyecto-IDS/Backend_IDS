package com.arsw.ids_ia.service;

import com.arsw.ids_ia.dto.request.CreateMeetingRequest;
import com.arsw.ids_ia.dto.request.JoinMeetingRequest;
import com.arsw.ids_ia.model.Meeting;

public interface MeetingService {
    Meeting createMeeting(CreateMeetingRequest request, String creatorEmail);
    Meeting joinMeeting(JoinMeetingRequest request, String participantEmail);
    Meeting leaveMeeting(Long meetingId, String participantEmail);
    Meeting getMeetingById(Long meetingId);
}