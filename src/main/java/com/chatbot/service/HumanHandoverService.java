package com.chatbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.chatbot.integration.WhatsAppClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class HumanHandoverService {

    private final JavaMailSender mailSender;
    private final WhatsAppClient whatsAppClient;

    @Value("${notification.sales-email}")
    private String salesEmail;

    @Value("${notification.whatsapp-number}")
    private String salesWhatsApp;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends email + WhatsApp alert to sales team when user requests human agent.
     * Runs asynchronously.
     */
    @Async
    public void notifySalesTeam(String userPhone, String userName) {
        sendEmail(userPhone, userName);
        sendWhatsAppAlert(userPhone, userName);
    }

    private void sendEmail(String userPhone, String userName) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromEmail);
            mail.setTo(salesEmail);
            mail.setSubject("🔔 Human Handover Requested - Chatbot");
            mail.setText(String.format("""
                    A customer has requested to speak with a human agent.
                    
                    Customer Details:
                    ------------------
                    Name   : %s
                    Phone  : %s
                    
                    Action Required:
                    Please contact this customer on WhatsApp or phone as soon as possible.
                    
                    -- Booking Bot
                    """, userName != null ? userName : "Unknown", userPhone));

            mailSender.send(mail);
            log.info("📧 Handover email sent for {}", userPhone);

        } catch (Exception e) {
            log.error("Failed to send handover email: {}", e.getMessage(), e);
        }
    }

    private void sendWhatsAppAlert(String userPhone, String userName) {
        try {
            String alertMsg = String.format(
                "🔔 *Human Handover Alert*\n\n" +
                "Customer: %s\n" +
                "Phone: %s\n\n" +
                "Please contact them immediately! 🏃",
                userName != null ? userName : "Unknown", userPhone
            );
            whatsAppClient.sendTextMessage(salesWhatsApp, alertMsg);
            log.info("📱 WhatsApp alert sent to sales team for {}", userPhone);

        } catch (Exception e) {
            log.error("Failed to send WhatsApp alert: {}", e.getMessage(), e);
        }
    }
}
