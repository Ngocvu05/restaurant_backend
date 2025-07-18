package com.management.chat_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIPromptDTO {
    private String roomId;
    private String message;
    private String context;
    private Boolean includeHistory;
}
