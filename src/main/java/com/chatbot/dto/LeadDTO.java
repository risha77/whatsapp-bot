package com.chatbot.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeadDTO {
    private String name;
    private String phone;
    private String checkIn;
    private String checkOut;
    private Integer guests;
    private String budget;
    private String city;
    private String roomPreference;
}
