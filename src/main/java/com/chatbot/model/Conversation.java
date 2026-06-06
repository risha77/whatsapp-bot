package com.chatbot.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "conversations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    private String id;

    private String userPhone;
    private String userName;

    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    @Builder.Default
    private ConversationState state = ConversationState.GREETING;

    @Builder.Default
    private boolean humanHandoverRequested = false;

    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime lastMessageAt;

    // ── Nested Message ──────────────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;       // "user" | "assistant"
        private String content;
        private LocalDateTime timestamp;
    }

    // ── Conversation State Machine ────────────────────────
    public enum ConversationState {
        GREETING,
        COLLECTING_CITY,
        COLLECTING_CHECKIN,
        COLLECTING_GUESTS,
        COLLECTING_BUDGET,
        SHOWING_ROOMS,
        COLLECTING_NAME,
        COLLECTING_PHONE_CONFIRM,
        LEAD_CAPTURED,
        HUMAN_HANDOVER,
        CLOSED
    }
}
