package com.chatbot.service;

import com.chatbot.integration.GoogleSheetClient;
import com.chatbot.model.Lead;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleSheetService {

    private final GoogleSheetClient googleSheetClient;

    public void pushLeadToSheet(Lead lead) {
        googleSheetClient.appendLead(lead);
    }
}
