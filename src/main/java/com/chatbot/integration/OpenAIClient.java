package com.chatbot.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.chatbot.config.OpenAIConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAIClient {

    private final OpenAIConfig config;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient = new OkHttpClient();

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.parse("application/json");

    /**
     * Sends a full conversation (system + history + new user message) to GPT.
     *
     * @param systemPrompt  The bot persona/instructions
     * @param messageHistory  List of {role, content} maps (past turns + current user msg)
     * @return AI response text
     */
    public String chat(String systemPrompt, List<Map<String, String>> messageHistory) {
        try {
            // Build messages array: system first, then history
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.addAll(messageHistory);

            Map<String, Object> body = new HashMap<>();
            body.put("model", config.getModel());
            body.put("messages", messages);
            body.put("max_tokens", config.getMaxTokens());
            body.put("temperature", config.getTemperature());

            String json = objectMapper.writeValueAsString(body);

            Request request = new Request.Builder()
                    .url(OPENAI_URL)
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json, JSON))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("OpenAI API error: {} {}", response.code(), response.body().string());
                    return "I'm having trouble right now. Please try again in a moment. 🙏";
                }

                JsonNode root = objectMapper.readTree(response.body().string());
                return root.path("choices").get(0)
                        .path("message").path("content").asText();
            }

        } catch (Exception e) {
            log.error("OpenAI call failed: {}", e.getMessage(), e);
            return "Sorry, I encountered an error. Please try again. 🙏";
        }
    }
}
