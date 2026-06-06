package com.chatbot.controller;

import com.chatbot.dto.LeadDTO;
import com.chatbot.model.Lead;
import com.chatbot.service.LeadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal REST API for lead management.
 * Useful for admin dashboard (Phase 2) or direct CRM integration.
 */
@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @GetMapping
    public ResponseEntity<List<Lead>> getAllLeads() {
        return ResponseEntity.ok(leadService.getAllLeads());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Lead>> getByStatus(@PathVariable Lead.LeadStatus status) {
        return ResponseEntity.ok(leadService.getLeadsByStatus(status));
    }

    @PostMapping
    public ResponseEntity<Lead> createLead(@RequestBody LeadDTO dto) {
        return ResponseEntity.ok(leadService.saveLead(dto));
    }
}
