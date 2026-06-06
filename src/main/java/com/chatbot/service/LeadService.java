package com.chatbot.service;

import com.chatbot.dto.LeadDTO;
import com.chatbot.integration.GoogleSheetClient;
import com.chatbot.model.Lead;
import com.chatbot.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadService {

    private final LeadRepository leadRepository;
    private final GoogleSheetClient googleSheetClient;

    /**
     * Creates a new lead or returns the existing one for this phone number.
     */
    public Lead getOrCreateLead(String phone, String name) {
        return leadRepository.findByPhone(phone).stream()
                .findFirst()
                .orElseGet(() -> {
                    Lead lead = Lead.builder()
                            .phone(phone)
                            .name(name)
                            .status(Lead.LeadStatus.NEW)
                            .build();
                    Lead saved = leadRepository.save(lead);
                    log.info("🆕 New lead created: {} - {}", phone, name);
                    return saved;
                });
    }

    /**
     * Saves a fully-qualified lead and appends it to the local CSV storage.
     */
    public Lead saveLead(LeadDTO dto) {
        Lead lead = Lead.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .checkIn(dto.getCheckIn())
                .checkOut(dto.getCheckOut())
                .guests(dto.getGuests())
                .budget(dto.getBudget())
                .city(dto.getCity())
                .roomPreference(dto.getRoomPreference())
                .status(Lead.LeadStatus.NEW)
                .build();

        Lead saved = leadRepository.save(lead);

        // Async append to CSV
        googleSheetClient.appendLead(saved);

        log.info("✅ Lead saved: {} [{}]", saved.getPhone(), saved.getId());
        return saved;
    }

    /**
     * Updates lead status by phone number.
     */
    public void updateLeadStatusByPhone(String phone, Lead.LeadStatus status) {
        leadRepository.findByPhone(phone).forEach(lead -> {
            lead.setStatus(status);
            lead.setUpdatedAt(LocalDateTime.now());
            leadRepository.save(lead);
        });
    }

    public List<Lead> getAllLeads() {
        return leadRepository.findAll();
    }

    public List<Lead> getLeadsByStatus(Lead.LeadStatus status) {
        return leadRepository.findByStatus(status);
    }
}
