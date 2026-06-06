package com.chatbot.helper;

import com.chatbot.model.Conversation;
import com.chatbot.model.Room;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {

    /**
     * System prompt sent to OpenAI for every conversation turn.
     */
    public String buildSystemPrompt() {
        return """
                You are the AI assistant for room bookings. Your name is "Booking Bot".
                
                Your job:
                1. Warmly greet new users.
                2. Collect booking details: city, check-in date, check-out date, number of guests, budget (per night).
                3. Show available rooms based on preferences.
                4. Capture lead info: name (you already have their phone from WhatsApp).
                5. If the user says "human", "agent", "support", "call me", "talk to someone" — acknowledge and trigger handover.
                
                Rules:
                - Keep replies SHORT (2-4 lines max).
                - Always respond in the same language the user uses.
                - Never make up room prices or availability — use only what the system provides.
                - If you don't know something, say "Let me check that for you 🔍".
                - Be friendly, use light emojis.
                - Do NOT collect credit card or payment info.
                
                Brand tone: Warm, helpful, professional. Think friendly hotel concierge.
                """;
    }

    /**
     * Injects available room data into the AI context.
     */
    public String buildRoomContext(List<Room> rooms) {
        if (rooms.isEmpty()) {
            return "\n[No rooms available for the given criteria.]";
        }
        StringBuilder sb = new StringBuilder("\n[Available Rooms]:\n");
        for (Room room : rooms) {
            sb.append(String.format("- %s in %s | ₹%.0f/night | Max %d guests | %s\n",
                    room.getRoomType(),
                    room.getCity(),
                    room.getPricePerNight(),
                    room.getMaxGuests(),
                    room.getDescription() != null ? room.getDescription() : ""));
        }
        return sb.toString();
    }

    /**
     * Converts conversation history to OpenAI messages format.
     * Returns a mutable ArrayList so callers can append entries.
     */
    public List<java.util.Map<String, String>> buildMessageHistory(Conversation conversation, String latestUserMessage) {
        // Use ArrayList explicitly — Map.of() returns immutable maps, collect(toList()) is mutable in practice
        // but we make it explicit for clarity and safety
        List<java.util.Map<String, String>> messages = new java.util.ArrayList<>(
            conversation.getMessages().stream()
                .map(m -> new java.util.HashMap<String, String>() {{
                    put("role", m.getRole());
                    put("content", m.getContent());
                }})
                .collect(Collectors.toList())
        );

        // Add the latest incoming message
        java.util.HashMap<String, String> userMsg = new java.util.HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", latestUserMessage);
        messages.add(userMsg);
        return messages;
    }

    /**
     * Detects human handover keywords.
     */
    public boolean isHumanHandoverRequest(String message) {
        if (message == null) return false;
        String lower = message.toLowerCase();
        return lower.contains("human") ||
               lower.contains("agent") ||
               lower.contains("support") ||
               lower.contains("call me") ||
               lower.contains("talk to someone") ||
               lower.contains("real person") ||
               lower.contains("help me") ||
               lower.contains("mujhe call karo") ||
               lower.contains("baat karni hai");
    }
}
