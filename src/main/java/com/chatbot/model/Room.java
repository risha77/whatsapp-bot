package com.chatbot.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    private String id;

    private String roomType;       // e.g. Deluxe, Standard, Suite
    private String city;           // e.g. Goa, Mumbai
    private Double pricePerNight;
    private Integer maxGuests;
    private List<String> images;   // Cloudinary URLs
    private List<String> amenities;
    private String description;

    @Builder.Default
    private boolean available = true;
}
