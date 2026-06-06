package com.chatbot.repository;

import com.chatbot.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends MongoRepository<Room, String> {
    List<Room> findByCityIgnoreCaseAndAvailableTrue(String city);
    List<Room> findByPricePerNightLessThanEqualAndAvailableTrue(Double maxPrice);
    List<Room> findByCityIgnoreCaseAndPricePerNightLessThanEqualAndAvailableTrue(String city, Double maxPrice);
}
