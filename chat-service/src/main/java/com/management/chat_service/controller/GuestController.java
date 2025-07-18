package com.management.chat_service.controller;

import com.management.chat_service.dto.ChatMessageRequest;
import com.management.chat_service.service.IChatProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/guest")
public class GuestController {
    private final IChatProducerService chatProducerService;

    @PostMapping("/send")
    public ResponseEntity<?> sendChat(@RequestBody ChatMessageRequest request,
                                      @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        log.info(" ChatController - Received message: {}", request);

        if (userIdHeader != null) {
            request.setUserId(Long.parseLong(userIdHeader));
        }

        chatProducerService.sendMessageToChatQueue(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
