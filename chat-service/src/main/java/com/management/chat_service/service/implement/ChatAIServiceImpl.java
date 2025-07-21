package com.management.chat_service.service.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.management.chat_service.config.GroqProperties;
import com.management.chat_service.service.IChatAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class ChatAIServiceImpl implements IChatAIService {
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    private final GroqProperties props;
    private final RestTemplate restTemplate;

    public ChatAIServiceImpl(GroqProperties props, RestTemplate restTemplate) {
        this.props = props;
        this.restTemplate = restTemplate;
    }

    @Override
    public String sendToAI(String userInput) {
        try {
            return sendToAIAsync(userInput, false)
                    .get(30, TimeUnit.SECONDS); // Thêm timeout 30 giây
        } catch (TimeoutException e) {
            log.error("❌ AI response timeout", e);
            return "Xin lỗi, AI phản hồi quá chậm. Vui lòng thử lại sau.";
        } catch (Exception e) {
            log.error("❌ Error getting AI response", e);
            return "Xin lỗi, AI đang bận. Vui lòng thử lại sau.";
        }
    }

    @Override
    public void ask(String userInput) {
        sendToAIAsync(userInput, true)
                .thenAccept(response -> {
                    if (response == null) {
                        log.error("❌ Guest Chat - Không nhận được phản hồi từ AI");
                    }
                })
                .exceptionally(throwable -> {
                    log.error("❌ Guest Chat - Lỗi khi xử lý phản hồi từ AI", throwable);
                    return null;
                });
    }

    private CompletableFuture<String> sendToAIAsync(String userInput, boolean isGuestChat) {
        return CompletableFuture.supplyAsync(() -> sendGroqRequest(userInput, isGuestChat));
    }

    private String sendGroqRequest(String userInput, boolean isGuestChat) {
        HttpHeaders headers = createHeaders();
        String prompt = createPrompt(userInput);
        Map<String, Object> requestBody = createRequestBody(prompt);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    GROQ_API_URL, request, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String logPrefix = isGuestChat ? "✅Guest Chat - " : "✅";
                log.info("{} Prompt gửi AI:\n{}", logPrefix, prompt);
                String extractedText = extractTextFromGroqResponse(response.getBody());
                return isGuestChat ? null : extractedText;
            } else {
                String logPrefix = isGuestChat ? "❌ Guest Chat - " : "❌";
                log.error("{} Groq API trả về lỗi: {}", logPrefix, response.getStatusCode());
                return isGuestChat ? null : "Không thể phản hồi từ AI.";
            }
        } catch (Exception e) {
            String logPrefix = isGuestChat ? "❌ Guest Chat - " : "❌";
            log.error("{} Lỗi khi gọi Groq API", logPrefix, e);
            return null;
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(props.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String createPrompt(String userInput) {
        return """
            Bạn là trợ lý AI. Đây là tin nhắn từ người dùng:

            "%s"

            Hãy phản hồi một cách tự nhiên và giữ nguyên ngôn ngữ người dùng sử dụng (ví dụ: nếu là tiếng Việt thì trả lời tiếng Việt, nếu là tiếng Anh thì trả lời tiếng Anh).
        """.formatted(userInput.trim());
    }

    private Map<String, Object> createRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", props.getModel());
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", props.getSystemPrompt()),
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", props.getTemperature());
        requestBody.put("max_tokens", props.getMaxTokens());
        return requestBody;
    }

    private String extractTextFromGroqResponse(String responseJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseJson);
            return root
                    .path("choices").get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (Exception e) {
            log.error("❌ Lỗi khi parse phản hồi từ Groq API", e);
            return "Không thể hiểu câu trả lời từ AI.";
        }
    }
}
