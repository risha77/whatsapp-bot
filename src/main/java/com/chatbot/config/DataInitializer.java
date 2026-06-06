package com.chatbot.config;

import com.chatbot.model.Room;
import com.chatbot.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Seeds sample room data on first startup if collection is empty.
 * In production, rooms are managed via an Admin Panel.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner seedRooms(RoomRepository roomRepository) {
        return args -> {
            if (roomRepository.count() == 0) {
                log.info("🌱 Seeding sample room data...");

                roomRepository.saveAll(List.of(
                    Room.builder()
                        .roomType("Standard")
                        .city("Goa")
                        .pricePerNight(1500.0)
                        .maxGuests(2)
                        .description("Cozy room with garden view, AC, WiFi, hot water")
                        .amenities(List.of("AC", "WiFi", "Hot Water", "TV"))
                        .images(List.of(
                            "https://res.cloudinary.com/example/image/upload/v1/rooms/goa-standard-1.jpg"
                        ))
                        .build(),

                    Room.builder()
                        .roomType("Deluxe")
                        .city("Goa")
                        .pricePerNight(2500.0)
                        .maxGuests(3)
                        .description("Spacious room with sea-facing balcony, premium amenities")
                        .amenities(List.of("AC", "WiFi", "Balcony", "Mini Bar", "TV", "Hot Water"))
                        .images(List.of(
                            "https://res.cloudinary.com/example/image/upload/v1/rooms/goa-deluxe-1.jpg"
                        ))
                        .build(),

                    Room.builder()
                        .roomType("Suite")
                        .city("Goa")
                        .pricePerNight(4500.0)
                        .maxGuests(4)
                        .description("Luxury suite with private pool access, living room & kitchen")
                        .amenities(List.of("AC", "WiFi", "Pool", "Kitchen", "Mini Bar", "Balcony"))
                        .images(List.of(
                            "https://res.cloudinary.com/example/image/upload/v1/rooms/goa-suite-1.jpg"
                        ))
                        .build(),

                    Room.builder()
                        .roomType("Standard")
                        .city("Mumbai")
                        .pricePerNight(2000.0)
                        .maxGuests(2)
                        .description("City-view room near BKC, ideal for business travelers")
                        .amenities(List.of("AC", "WiFi", "Workspace", "TV"))
                        .images(List.of(
                            "https://res.cloudinary.com/example/image/upload/v1/rooms/mumbai-standard-1.jpg"
                        ))
                        .build()
                ));

                log.info("✅ Sample rooms seeded successfully");
            }
        };
    }
}
