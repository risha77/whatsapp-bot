package com.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Maps the incoming Meta WhatsApp Cloud API webhook payload.
 * Supports text messages and interactive button replies.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppWebhookDTO {

    private String object;
    private List<Entry> entry;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        private String id;
        private List<Change> changes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Change {
        private Value value;
        private String field;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        @JsonProperty("messaging_product")
        private String messagingProduct;

        private Metadata metadata;
        private List<Contact> contacts;
        private List<Message> messages;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        @JsonProperty("display_phone_number")
        private String displayPhoneNumber;

        @JsonProperty("phone_number_id")
        private String phoneNumberId;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contact {
        @JsonProperty("wa_id")
        private String waId;
        private Profile profile;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String from;
        private String id;
        private String timestamp;
        private String type;

        // Text message
        private TextBody text;

        // Interactive button reply
        private Interactive interactive;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextBody {
        private String body;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Interactive {
        private String type;

        @JsonProperty("button_reply")
        private ButtonReply buttonReply;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ButtonReply {
        private String id;
        private String title;
    }
}
