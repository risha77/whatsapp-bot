package com.chatbot.service;

import com.chatbot.helper.PromptBuilder;
import com.chatbot.integration.OpenAIClient;
import com.chatbot.model.Conversation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAIService {

    private final OpenAIClient openAIClient;
    private final PromptBuilder promptBuilder;

    /**
     * Generates a reply given a conversation object and new user message.
     */
    public String generateReply(Conversation conversation, String userMessage, String extraContext) {
        String systemPrompt = promptBuilder.buildSystemPrompt();
        List<Map<String, String>> history = promptBuilder.buildMessageHistory(
                conversation, userMessage + (extraContext != null ? "\n" + extraContext : "")
        );
        return openAIClient.chat(systemPrompt, history);
    }
}
