package com.chatbot.repository;

import com.chatbot.model.Lead;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends MongoRepository<Lead, String> {
    List<Lead> findByPhone(String phone);
    List<Lead> findByStatus(Lead.LeadStatus status);
}
