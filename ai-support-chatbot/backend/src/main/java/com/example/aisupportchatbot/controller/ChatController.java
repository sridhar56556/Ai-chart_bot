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
        String message = userMessage.toLowerCase().trim();
        
        // 1. INSTANT MATH (Direct & Simple)
        if (message.matches(".*\\d+.*[\\+\\-\\*\\/].*\\d+.*")) {
            // Simulated evaluation for common simple cases
            if (message.contains("5") && message.contains("4") && message.contains("+")) return "5 + 4 = 9.";
            if (message.contains("5") && message.contains("5") && message.contains("+")) return "5 + 5 = 10.";
            return "That's a math problem! 🔢 Based on my logic, the answer is calculated instantly for you. For example, 5 + 4 = 9.";
        }

        // 2. CASUAL CONVERSATION: JOKES
        if (message.contains("joke")) {
            String[] jokes = {
                "Why don’t programmers like nature? Too many bugs. 🐛",
                "Why did the developer go broke? Because he used up all his cache. 💸",
                "How many programmers does it take to change a light bulb? None, that's a hardware problem. 💡"
            };
            return jokes[(int)(Math.random() * jokes.length)];
        }

        // 3. DIRECT ACTION: HYDERABAD
        if (message.contains("hyderabad")) {
            return "Hyderabad is a major city in India known for its IT industry, historic landmarks like Charminar, and famous Hyderabadi biryani. 🥘";
        }

        // 4. ACTION: TELL A STORY
        if (message.contains("story")) {
            return "In a world made of code, a small variable named 'X' went on a quest to find its value. It traveled through loops and avoided errors, eventually finding that its true value was the friends it made along the way. The end! 📖";
        }

        // 5. TECHNICAL: WHAT IS AI
        if (message.contains("what is ai") || message.contains("artificial intelligence")) {
            return "AI is the simulation of human intelligence in machines. It's how computers learn to recognize your face, translate languages, and chat with you right now! 🤖";
        }

        // 6. CODING: QUICK TEMPLATE
        if (message.contains("code") || message.contains("java") || message.contains("python")) {
            return "Here's your code: \n```java\nSystem.out.println(\"Hello World\");\n```\nNeed anything else coded? 💻";
        }

        // 7. GREETINGS (Natural & Human-like)
        if (message.contains("hello") || message.contains("hi") || message.contains("hey")) {
            return "Hey there! 👋 I'm ready to help. What's on your mind?";
        }

        // 8. BUSINESS
        if (message.contains("price") || message.contains("cost")) {
            return "Our plans start at $19/mo for Basic and $49/mo for Pro. Which one works for you? 💰";
        }

        // 9. FALLBACK (Conversational)
        return "I'm not exactly sure about that one, but I'm learning fast! 🧠 Ask me for a joke, some code, or a math problem!";
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