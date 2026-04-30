package com.example.aisupportchatbot.controller;

import com.example.aisupportchatbot.model.ChatMessage;
import com.example.aisupportchatbot.repository.ChatMessageRepository;
import com.example.aisupportchatbot.service.SentimentAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:5173", "${FRONTEND_URL:*}"})
public class ChatController {

    @Autowired
    private SentimentAnalysisService sentimentAnalysisService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @PostMapping
    public ResponseEntity<ChatMessage> chat(@RequestBody ChatRequest request) {
        System.out.println("Received chat request: " + request.getMessage());
        
        String userMessage = request.getMessage();
        String sentiment = "neutral";
        
        try {
            System.out.println("Analyzing sentiment...");
            sentiment = sentimentAnalysisService.analyzeSentiment(userMessage);
            System.out.println("Detected sentiment: " + sentiment);
        } catch (Exception e) {
            System.err.println("Sentiment analysis failed: " + e.getMessage());
        }

        String botResponse = generateBotResponse(userMessage, sentiment);
        System.out.println("Generated bot response: " + botResponse);

        ChatMessage chatMessage = new ChatMessage(userMessage, botResponse, sentiment);
        ChatMessage savedMessage;
        
        try {
            savedMessage = chatMessageRepository.save(chatMessage);
            System.out.println("Message saved to database with ID: " + savedMessage.getId());
        } catch (Exception e) {
            System.err.println("Failed to save message to DB: " + e.getMessage());
            savedMessage = chatMessage; // Fallback to unsaved message
        }

        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getHistory() {
        List<ChatMessage> messages = chatMessageRepository.findAllByOrderByTimestampDesc();
        return ResponseEntity.ok(messages);
    }

    private String generateBotResponse(String userMessage, String sentiment) {
        String message = userMessage.toLowerCase();
        
        // 1. UNIVERSAL KNOWLEDGE & TOOLS
        if (message.contains("code") || message.contains("java") || message.contains("python") || message.contains("script")) {
            return "I can certainly help with coding! 💻 Here is a basic template:\n```java\npublic class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println(\"AI Assistant is here!\");\n    }\n}\n```\nWhat specific language or problem are you working on?";
        } else if (message.contains("math") || message.contains("calculate") || message.contains("plus") || message.contains("minus")) {
            return "I can assist with mathematics! 🔢 Whether it's algebra, calculus, or simple arithmetic, just give me the equation and I'll walk you through the solution.";
        } else if (message.contains("write") || message.contains("poem") || message.contains("story") || message.contains("email")) {
            return "I'd love to help you write! ✍️ I can draft professional emails, creative stories, or even poetry. Give me a topic and a tone (e.g., formal, friendly, poetic) and I'll get started.";
        } else if (message.contains("translate") || message.contains("language") || message.contains("spanish") || message.contains("french")) {
            return "I am multilingual! 🌍 I can translate phrases and help you learn new languages. What would you like to translate today?";
        }

        // 2. GENERAL ASSISTANCE
        if (message.contains("who are you") || message.contains("what can you do")) {
            return "I am your Universal AI Assistant. 🤖 I can help you with:\n- **Writing & Content**\n- **Coding & Technical Support**\n- **Math & Science**\n- **Business & Pricing Queries**\n- **Sentiment Analysis**\nHow can I help you excel today?";
        }

        // 3. LEGACY SUPPORT KNOWLEDGE
        if (message.contains("help") || message.contains("support")) {
            return "I'm here to support you! 🛡️ Whether it's technical issues, order tracking, or billing, I've got you covered. What's the problem?";
        } else if (message.contains("price") || message.contains("cost") || message.contains("plan")) {
            return "Our Universal Plans:\n- **Free**: $0/mo (Standard AI)\n- **Pro**: $20/mo (Faster AI + Priority)\n- **Enterprise**: Custom solutions\nWhich one fits your needs?";
        }

        // 4. SENTIMENT-AWARE RESPONSES
        if ("positive".equals(sentiment)) {
            return "I love the positive energy! 🌟 It's a pleasure helping someone so enthusiastic. Anything else on your mind?";
        } else if ("negative".equals(sentiment)) {
            return "I'm sorry things are tough right now. 😔 I'm here to listen and help in any way I can. Let's work through it together.";
        }

        return "I'm your versatile AI Assistant! 🧠 I'm not quite sure how to handle that specific request yet, but you can ask me to **write a story**, **fix code**, or **solve math problems**. Try one!";
    }

    static class ChatRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}