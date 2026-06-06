package com.chatbot.controller;

import com.chatbot.model.Room;
import com.chatbot.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for room management.
 * Admin can add/update/delete rooms from here (or Phase 2 admin panel).
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepository;

    @GetMapping
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @GetMapping("/city/{city}")
    public List<Room> getRoomsByCity(@PathVariable String city) {
        return roomRepository.findByCityIgnoreCaseAndAvailableTrue(city);
    }

    @PostMapping
    public ResponseEntity<Room> addRoom(@RequestBody Room room) {
        return ResponseEntity.ok(roomRepository.save(room));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable String id, @RequestBody Room room) {
        room.setId(id);
        return ResponseEntity.ok(roomRepository.save(room));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String id) {
        roomRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<Room> toggleAvailability(@PathVariable String id,
                                                    @RequestParam boolean available) {
        return roomRepository.findById(id).map(room -> {
            room.setAvailable(available);
            return ResponseEntity.ok(roomRepository.save(room));
        }).orElse(ResponseEntity.notFound().build());
    }
}
