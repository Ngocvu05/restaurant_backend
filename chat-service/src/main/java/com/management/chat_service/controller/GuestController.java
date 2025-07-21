package com.management.chat_service.controller;

import com.management.chat_service.dto.GuestChatMessageDTO;
import com.management.chat_service.service.IGuestChatService;
import com.management.chat_service.status.SenderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/guest")
public class GuestController {
    private final IGuestChatService guestChatService;

    @PostMapping("/send")
    public ResponseEntity<?> sendChat(@RequestBody GuestChatMessageDTO request) {
        log.info(" ChatController - Received message: {}", request);
        try {
            // For guest users, userId can be null or 0
            request.setSenderType(SenderType.USER);

            guestChatService.handleGuestMessage(request);
            log.info(">>> ChatController - Guest message sent to queue successfully");

            return ResponseEntity.ok().body("{\"status\":\"success\",\"message\":\"Guest message sent\"}");

        } catch (Exception e) {
            log.error(">>> ChatController - Error sending guest message: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"status\":\"error\",\"message\":\"Failed to send guest message\"}");
        }
    }

    @PostMapping("/migrate")
    public ResponseEntity<Void> migrateMessages(
            @RequestParam String sessionId,
            @RequestParam Long userId
    ) {
        guestChatService.migrateToDatabase(sessionId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/messages/{sessionId}")
    public ResponseEntity<List<GuestChatMessageDTO>> getGuestMessages(@PathVariable String sessionId) {
        List<GuestChatMessageDTO> messages = guestChatService.getMessages(sessionId);
        return ResponseEntity.ok(messages);
    }
}
