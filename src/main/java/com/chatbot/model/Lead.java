package com.chatbot.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "leads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    @Id
    private String id;
    private String name;
    private String phone;
    private String checkIn;
    private String checkOut;
    private Integer guests;
    private String budget;
    private String roomPreference;
    private String city;

    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public enum LeadStatus {
        NEW, CONTACTED, QUALIFIED, CONVERTED, LOST, HUMAN_REQUESTED
    }
}
