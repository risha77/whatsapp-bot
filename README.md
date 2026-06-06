# 🏠 WhatsApp AI Chatbot — MVP

A production-ready AI-powered WhatsApp chatbot for room booking business.

---

## 🏗️ Architecture

```
Customer
    ↓
WhatsApp
    ↓
Meta Cloud API
    ↓
Spring Boot Backend  (Render)
    ↓
OpenAI GPT-4o-mini
    ↓
MongoDB Atlas  +  CSV (local leads file)  +  Email Notification
```

---

## 🚀 Features

| Feature | Status |
|---|---|
| WhatsApp AI chatbot (GPT-4o-mini) | ✅ |
| Lead collection & MongoDB storage | ✅ |
| Room photos & pricing via WhatsApp | ✅ |
| CSV leads storage (local) | ✅ |
| Human handover (email + WA alert) | ✅ |
| Conversation state tracking | ✅ |
| Sample room data seeder | ✅ |
| Deployment config (Render) | ✅ |

---

## 📋 Prerequisites

- Java 21
- Maven 3.8+
- MongoDB Atlas account (free tier works)
- Meta Developer account + WhatsApp Business app
- OpenAI API key
- Gmail account (for email notifications)

---

## ⚙️ Setup

### 1. Clone & Configure
```bash
git clone https://github.com/your-org/whatsapp-bot
cd whatsapp-bot
```

Edit `src/main/resources/application.properties`:
```properties
whatsapp.phone-number-id=YOUR_PHONE_NUMBER_ID
whatsapp.access-token=YOUR_META_ACCESS_TOKEN
whatsapp.verify-token=verify_token_2024
openai.api-key=sk-...
# Optional: configure CSV path for leads (default: leads.csv)
# leads.csv.path=leads.csv
spring.mail.username=your@gmail.com
spring.mail.password=YOUR_APP_PASSWORD
```

### 2. Leads storage (CSV)

This project appends lead rows to a local CSV file instead of Google Sheets.

Default CSV path: `leads.csv` (in the working directory). You can configure this in `application.properties`:

```properties
leads.csv.path=leads.csv
```

CSV columns (header row created automatically if file doesn't exist):
```
Date,Name,Phone,City,Check-In,Check-Out,Guests,Budget,Status
```

### 3. Meta WhatsApp Setup
1. Go to [developers.facebook.com](https://developers.facebook.com)
2. Create an app → Add WhatsApp product
3. Get your **Phone Number ID** and **Access Token**
4. Set Webhook URL: `https://your-render-url.onrender.com/webhook`
5. Subscribe to `messages` event
6. Use verify token from your config

### 4. Build & Run
```bash
mvn clean package -DskipTests
java -jar target/chatbot-0.0.1-SNAPSHOT.jar
```

### 5. Deploy to Render
```bash
# Push to GitHub, then connect repo in Render dashboard
# Add all env vars from render.yaml
```

---

## 📁 Project Structure

```
src/main/java/com/chatbot/
├── controller/
│   ├── WebhookController.java      # Meta webhook (GET verify + POST messages)
│   └── LeadController.java         # REST API for leads
├── service/
│   ├── WhatsAppService.java        # Core bot logic & state machine
│   ├── LeadService.java            # Lead CRUD + CSV append
│   ├── OpenAIService.java          # AI reply generation
│   ├── GoogleSheetService.java     # (legacy) previously used Google Sheets; replaced by CSV client
│   └── HumanHandoverService.java   # Email + WA alert to sales
├── repository/
│   ├── LeadRepository.java
│   ├── RoomRepository.java
│   └── ConversationRepository.java
├── model/
│   ├── Lead.java                   # MongoDB document
│   ├── Room.java                   # MongoDB document
│   └── Conversation.java           # MongoDB document + state enum
├── config/
│   ├── WhatsAppConfig.java
│   ├── OpenAIConfig.java
│   ├── JacksonConfig.java
│   └── DataInitializer.java        # Seeds sample rooms
├── dto/
│   ├── WhatsAppWebhookDTO.java     # Meta payload mapping
│   └── LeadDTO.java
├── helper/
│   └── PromptBuilder.java          # AI system prompt + handover detection
└── integration/
    ├── WhatsAppClient.java         # Sends text, images, buttons via Meta API
    ├── OpenAIClient.java           # GPT-4o-mini API calls
    └── GoogleSheetClient.java      # Appends rows to CSV (local leads.csv)
```

---

## 💬 Conversation Flow

```
User: "Hi"
Bot:  "Hey! 👋 Welcome! Looking for a room?
       Which city? (Goa / Mumbai / ...)"

User: "Goa"
Bot:  "Great choice! 🌊 When's your check-in date?"

User: "10 June"
Bot:  "How many guests?"

User: "2"
Bot:  "What's your budget per night? (₹1000-5000)"

User: "around 2500"
Bot:  [Shows Deluxe room with image + pricing]
      "Would you like to book this? Or want to see more options?"

User: "I want to talk to someone"
Bot:  "Connecting you with our team now! 🙋"
      → Email + WhatsApp alert sent to sales team
```

---


## 📞 Human Handover Keywords

Bot triggers handover for: `human`, `agent`, `support`, `call me`, `talk to someone`, `real person`, `mujhe call karo`, `baat karni hai`
