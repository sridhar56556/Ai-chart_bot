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
        
        // 1. Core Support Knowledge Base
        if (message.contains("help") || message.contains("support")) {
            return "I'm your AI Support Assistant. I can help you with **pricing**, **order status**, **refunds**, or **technical support**. What's on your mind?";
        } else if (message.contains("price") || message.contains("cost") || message.contains("plan")) {
            return "We offer three plans:\n- **Basic**: $19/mo (1 user)\n- **Pro**: $49/mo (5 users)\n- **Enterprise**: Custom pricing\nWould you like to start a trial?";
        } else if (message.contains("order") || message.contains("status") || message.contains("track")) {
            return "To track your order, please provide your **Order ID**. You can also find this in your dashboard under 'Recent Activity'.";
        } else if (message.contains("refund") || message.contains("cancel") || message.contains("money back")) {
            return "We have a **30-day money-back guarantee**. If you're not satisfied, you can initiate a refund through your account settings or by contacting billing@example.com.";
        } else if (message.contains("tech") || message.contains("error") || message.contains("bug") || message.contains("broken")) {
            return "I'm sorry you're experiencing technical issues. Please describe the error, or you can check our **Status Page** for ongoing maintenance updates.";
        } else if (message.contains("hello") || message.contains("hi") || message.contains("hey")) {
            return "Hello! 👋 I'm here to assist you. How is your day going?";
        } else if (message.contains("thank") || message.contains("thanks")) {
            return "You're very welcome! I'm happy to help. Is there anything else I can do for you?";
        }

        // 2. Advanced Sentiment-Aware Fallbacks
        if ("positive".equals(sentiment)) {
            return "I'm thrilled to hear that! 😊 We strive to provide the best service. Do you have any other questions?";
        } else if ("negative".equals(sentiment)) {
            return "I can see you're frustrated, and I truly apologize. 😔 Let me escalate this to a human agent for you, or I can try to help if you provide more details.";
        }

        return "I'm not quite sure I understand. Could you please rephrase that? You can ask about our **plans**, **refunds**, or **technical support**.";
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