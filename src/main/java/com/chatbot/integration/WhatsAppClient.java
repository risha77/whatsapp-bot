package com.chatbot.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.chatbot.config.WhatsAppConfig;
import com.chatbot.model.Room;
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
public class WhatsAppClient {

    private final WhatsAppConfig config;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient = new OkHttpClient();

    private static final MediaType JSON = MediaType.parse("application/json");

    // ── Send plain text message ─────────────────────────────
    public void sendTextMessage(String to, String text) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("recipient_type", "individual");
        payload.put("to", to);
        payload.put("type", "text");
        payload.put("text", Map.of("preview_url", false, "body", text));

        post(payload);
    }

    // ── Send image + caption ────────────────────────────────
    public void sendImageMessage(String to, String imageUrl, String caption) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("recipient_type", "individual");
        payload.put("to", to);
        payload.put("type", "image");
        payload.put("image", Map.of("link", imageUrl, "caption", caption));

        post(payload);
    }

    // ── Send interactive button message ─────────────────────
    public void sendButtonMessage(String to, String bodyText, List<String> buttonLabels) {
        List<Map<String, Object>> buttons = new ArrayList<>();
        for (int i = 0; i < Math.min(buttonLabels.size(), 3); i++) {
            buttons.add(Map.of(
                "type", "reply",
                "reply", Map.of("id", "btn_" + i, "title", buttonLabels.get(i))
            ));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("recipient_type", "individual");
        payload.put("to", to);
        payload.put("type", "interactive");
        payload.put("interactive", Map.of(
            "type", "button",
            "body", Map.of("text", bodyText),
            "action", Map.of("buttons", buttons)
        ));

        post(payload);
    }

    // ── Send room details with image ─────────────────────────
    public void sendRoomDetails(String to, Room room) {
        // Send first image if available
        if (room.getImages() != null && !room.getImages().isEmpty()) {
            String caption = String.format("🏠 *%s* - %s\n💰 ₹%.0f/night\n👥 Up to %d guests\n\n%s",
                    room.getRoomType(),
                    room.getCity(),
                    room.getPricePerNight(),
                    room.getMaxGuests(),
                    room.getDescription() != null ? room.getDescription() : "");
            sendImageMessage(to, room.getImages().get(0), caption);
        } else {
            String text = String.format("🏠 *%s* - %s\n💰 ₹%.0f/night\n👥 Up to %d guests\n\n%s",
                    room.getRoomType(),
                    room.getCity(),
                    room.getPricePerNight(),
                    room.getMaxGuests(),
                    room.getDescription() != null ? room.getDescription() : "");
            sendTextMessage(to, text);
        }
    }

    // ── Internal HTTP POST ───────────────────────────────────
    private void post(Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            String url = config.getApiUrl() + "/" + config.getPhoneNumberId() + "/messages";

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + config.getAccessToken())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json, JSON))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("WhatsApp API error: {} - {}", response.code(), response.body().string());
                } else {
                    log.debug("WhatsApp message sent successfully");
                }
            }
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message: {}", e.getMessage(), e);
        }
    }
}
