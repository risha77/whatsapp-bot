package com.chatbot.service;

import com.chatbot.dto.WhatsAppWebhookDTO;
import com.chatbot.helper.PromptBuilder;
import com.chatbot.integration.OpenAIClient;
import com.chatbot.integration.WhatsAppClient;
import com.chatbot.model.Conversation;
import com.chatbot.model.Lead;
import com.chatbot.model.Room;
import com.chatbot.repository.ConversationRepository;
import com.chatbot.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {

    private final WhatsAppClient whatsAppClient;
    private final OpenAIClient openAIClient;
    private final ConversationRepository conversationRepository;
    private final RoomRepository roomRepository;
    private final LeadService leadService;
    private final HumanHandoverService humanHandoverService;
    private final PromptBuilder promptBuilder;

    /**
     * Entry point — called by webhook controller for every incoming message.
     */
    public void handleIncomingMessage(WhatsAppWebhookDTO payload) {
        try {
            var entry = payload.getEntry();
            if (entry == null || entry.isEmpty()) return;

            var changes = entry.get(0).getChanges();
            if (changes == null || changes.isEmpty()) return;

            var value = changes.get(0).getValue();
            if (value.getMessages() == null || value.getMessages().isEmpty()) return;

            var message = value.getMessages().get(0);
            String userPhone = message.getFrom();
            String userName = extractName(value, userPhone);
            String userText = extractText(message);

            if (userText == null || userText.isBlank()) return;

            log.info("📩 Message from {}: {}", userPhone, userText);

            // Load or create conversation
            Conversation conversation = conversationRepository
                    .findByUserPhone(userPhone)
                    .orElseGet(() -> createNewConversation(userPhone, userName));

            // Check for human handover first
            if (promptBuilder.isHumanHandoverRequest(userText)) {
                handleHumanHandover(conversation, userPhone, userText);
                return;
            }

            // Already in human handover state
            if (conversation.getState() == Conversation.ConversationState.HUMAN_HANDOVER) {
                whatsAppClient.sendTextMessage(userPhone,
                        "Our team has been notified and will reach out to you shortly! 🙏\n" +
                        "If urgent, call us at +91-XXXXXXXXXX.");
                return;
            }

            // Process with AI
            processWithAI(conversation, userPhone, userText);

        } catch (Exception e) {
            log.error("Error handling incoming WhatsApp message: {}", e.getMessage(), e);
        }
    }

    // ── AI processing ────────────────────────────────────────
    private void processWithAI(Conversation conversation, String userPhone, String userText) {
        // Extract city hint from conversation messages (scan for known cities)
        String cityHint = extractCityFromHistory(conversation, userText);

        // Inject room context when collecting budget or about to show rooms
        String roomContext = "";
        if (!cityHint.isEmpty()) {
            List<Room> rooms = roomRepository.findByCityIgnoreCaseAndAvailableTrue(cityHint);
            if (!rooms.isEmpty()) {
                roomContext = promptBuilder.buildRoomContext(rooms);
            }
        }

        // Build message history (with room context appended to the user message for AI)
        String userMsgWithContext = userText + (roomContext.isEmpty() ? "" : "\n" + roomContext);
        var history = promptBuilder.buildMessageHistory(conversation, userMsgWithContext);
        String systemPrompt = promptBuilder.buildSystemPrompt();

        // Call OpenAI
        String aiReply = openAIClient.chat(systemPrompt, history);

        // Save messages to conversation (store plain userText, not context-injected version)
        addMessageToConversation(conversation, "user", userText);
        addMessageToConversation(conversation, "assistant", aiReply);

        // Ensure lead record exists
        leadService.getOrCreateLead(userPhone, conversation.getUserName());

        // Save conversation
        conversationRepository.save(conversation);

        // Send reply
        whatsAppClient.sendTextMessage(userPhone, aiReply);
    }

    /**
     * Scans conversation history + current message for a known city name.
     */
    private String extractCityFromHistory(Conversation conversation, String currentMessage) {
        List<String> knownCities = List.of("goa", "mumbai", "delhi", "bangalore", "hyderabad",
                "pune", "jaipur", "udaipur", "manali", "shimla", "ooty", "kerala");

        // Check current message first
        String lower = currentMessage.toLowerCase();
        for (String city : knownCities) {
            if (lower.contains(city)) return city;
        }

        // Scan recent history (last 10 messages)
        List<Conversation.Message> msgs = conversation.getMessages();
        int start = Math.max(0, msgs.size() - 10);
        for (int i = msgs.size() - 1; i >= start; i--) {
            String content = msgs.get(i).getContent().toLowerCase();
            for (String city : knownCities) {
                if (content.contains(city)) return city;
            }
        }
        return "";
    }

    // ── Human handover ───────────────────────────────────────
    private void handleHumanHandover(Conversation conversation, String userPhone, String userText) {
        conversation.setState(Conversation.ConversationState.HUMAN_HANDOVER);
        conversation.setHumanHandoverRequested(true);
        addMessageToConversation(conversation, "user", userText);
        conversationRepository.save(conversation);

        // Update lead status
        leadService.updateLeadStatusByPhone(userPhone, Lead.LeadStatus.HUMAN_REQUESTED);

        // Notify sales team
        humanHandoverService.notifySalesTeam(userPhone, conversation.getUserName());

        whatsAppClient.sendTextMessage(userPhone,
                "Got it! 🙋 I'm connecting you with our team right now.\n\n" +
                "A team member will contact you shortly. " +
                "Thank you for your patience! 🏠");
    }

    // ── Helpers ──────────────────────────────────────────────
    private Conversation createNewConversation(String phone, String name) {
        Conversation conv = Conversation.builder()
                .userPhone(phone)
                .userName(name)
                .lastMessageAt(LocalDateTime.now())
                .build();
        log.info("🆕 New conversation started for {}", phone);
        return conversationRepository.save(conv);
    }

    private void addMessageToConversation(Conversation conversation, String role, String content) {
        conversation.getMessages().add(
            Conversation.Message.builder()
                .role(role)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build()
        );
        conversation.setLastMessageAt(LocalDateTime.now());
    }

    private String extractText(WhatsAppWebhookDTO.Message message) {
        if ("text".equals(message.getType()) && message.getText() != null) {
            return message.getText().getBody();
        }
        if ("interactive".equals(message.getType()) && message.getInteractive() != null) {
            var btnReply = message.getInteractive().getButtonReply();
            return btnReply != null ? btnReply.getTitle() : null;
        }
        return null;
    }

    private String extractName(WhatsAppWebhookDTO.Value value, String phone) {
        if (value.getContacts() != null && !value.getContacts().isEmpty()) {
            var profile = value.getContacts().get(0).getProfile();
            if (profile != null && profile.getName() != null) {
                return profile.getName();
            }
        }
        return phone;
    }
}
