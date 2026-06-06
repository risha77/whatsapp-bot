package com.chatbot.controller;

import com.chatbot.config.WhatsAppConfig;
import com.chatbot.dto.WhatsAppWebhookDTO;
import com.chatbot.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles all Meta WhatsApp webhook traffic.
 *
 * GET  /webhook  → Meta verification challenge
 * POST /webhook  → Incoming messages
 */
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WhatsAppService whatsAppService;
    private final WhatsAppConfig whatsAppConfig;

    /**
     * Meta webhook verification (one-time setup).
     * Meta sends: hub.mode=subscribe, hub.verify_token=<your_token>, hub.challenge=<challenge>
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge) {

        log.info("🔐 Webhook verification request received. Mode: {}", mode);

        if ("subscribe".equals(mode) && whatsAppConfig.getVerifyToken().equals(verifyToken)) {
            log.info("✅ Webhook verified successfully");
            return ResponseEntity.ok(challenge);
        }

        log.warn("❌ Webhook verification failed — token mismatch");
        return ResponseEntity.status(403).body("Forbidden");
    }

    /**
     * Receives incoming WhatsApp messages from Meta.
     * Must return 200 immediately; processing is handled asynchronously.
     */
    @PostMapping
    public ResponseEntity<String> receiveMessage(@RequestBody WhatsAppWebhookDTO payload) {
        log.debug("📨 Webhook payload received");

        // Process asynchronously (Meta expects 200 within 20s)
        try {
            whatsAppService.handleIncomingMessage(payload);
        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}
