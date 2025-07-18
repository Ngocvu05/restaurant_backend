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

@Slf4j
@Service
public class ChatAIServiceImpl implements IChatAIService {
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    private final GroqProperties props;
    private final RestTemplate restTemplate;

    public ChatAIServiceImpl(GroqProperties props) {
        this.props = props;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String sendToAI(String userInput) {
        String token = props.getApiKey();
        String model = props.getModel();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 👉 Prompt: giữ nguyên ngôn ngữ khi phản hồi
        String prompt = """
            Bạn là trợ lý AI. Đây là tin nhắn từ người dùng:

            "%s"

            Hãy phản hồi một cách tự nhiên và giữ nguyên ngôn ngữ người dùng sử dụng (ví dụ: nếu là tiếng Việt thì trả lời tiếng Việt, nếu là tiếng Anh thì trả lời tiếng Anh).
        """.formatted(userInput.trim());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", props.getSystemPrompt()),
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", props.getTemperature());
        requestBody.put("max_tokens", props.getMaxTokens());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    GROQ_API_URL, request, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Prompt gửi AI:\n{}", prompt);
                return extractTextFromGroqResponse(response.getBody());
            } else {
                log.error("❌ Groq API trả về lỗi: {}", response.getStatusCode());
                return "Không thể phản hồi từ AI.";
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi gọi Groq API", e);
            return "Xin lỗi, AI đang bận. Vui lòng thử lại sau.";
        }
    }

    @Override
    public String ask(String userInput) {
        String token = props.getApiKey();
        String model = props.getModel();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 👉 Prompt: giữ nguyên ngôn ngữ khi phản hồi
        String prompt = """
            Bạn là trợ lý AI. Đây là tin nhắn từ người dùng:

            "%s"

            Hãy phản hồi một cách tự nhiên và giữ nguyên ngôn ngữ người dùng sử dụng (ví dụ: nếu là tiếng Việt thì trả lời tiếng Việt, nếu là tiếng Anh thì trả lời tiếng Anh).
        """.formatted(userInput.trim());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", props.getSystemPrompt()),
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", props.getTemperature());
        requestBody.put("max_tokens", props.getMaxTokens());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    GROQ_API_URL, request, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Prompt gửi AI:\n{}", prompt);
                return extractTextFromGroqResponse(response.getBody());
            } else {
                log.error("❌ Groq API trả về lỗi: {}", response.getStatusCode());
                return "Không thể phản hồi từ AI.";
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi gọi Groq API", e);
            return "Xin lỗi, AI đang bận. Vui lòng thử lại sau.";
        }
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
