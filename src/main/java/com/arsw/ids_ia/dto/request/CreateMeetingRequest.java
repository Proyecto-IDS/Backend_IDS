package com.arsw.ids_ia.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMeetingRequest {
    @NotBlank
    private String title;
    
    private String description;
    
    private String incidentId;
}