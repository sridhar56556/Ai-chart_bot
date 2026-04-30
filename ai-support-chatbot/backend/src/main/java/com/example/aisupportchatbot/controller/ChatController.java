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
        
        // 1. GENERAL KNOWLEDGE (Hyderabad, Cities, History)
        if (message.contains("hyderabad")) {
            return "Hyderabad is a vibrant major city in India, famous for its rich history, the iconic **Charminar**, and its world-renowned **Biryani**. It is also a global hub for the IT and pharmaceutical industries (HITEC City).";
        } else if (message.contains("city") || message.contains("country") || message.contains("capital")) {
            return "I have a vast database of global geography! Are you asking about a specific city like **Hyderabad**, a country, or perhaps some historical landmarks? Tell me more!";
        }

        // 2. TECHNICAL & CODING (AI, Java, Python, Scripts)
        if (message.contains("what is ai") || message.contains("artificial intelligence")) {
            return "Artificial Intelligence (AI) is the simulation of human intelligence in machines. It covers fields like **Machine Learning**, **Natural Language Processing**, and **Computer Vision**.";
        } else if (message.contains("code") || message.contains("java") || message.contains("python") || message.contains("programming")) {
            return "I can certainly help with coding! 💻 Here is a basic template:\n```java\npublic class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println(\"AI Assistant is here!\");\n    }\n}\n```\nWhat specific language or problem are you working on?";
        }

        // 3. MATHEMATICS (Solve, Calculate, Equations)
        if (message.contains("math") || message.contains("calculate") || message.contains("plus") || message.contains("minus") || message.contains("solve")) {
            return "I can assist with mathematics! 🔢 Whether it's algebra, calculus, or simple arithmetic, just give me the equation and I'll walk you through the solution.";
        }

        // 4. CREATIVE WRITING (Emails, Stories, Poems)
        if (message.contains("write") || message.contains("poem") || message.contains("story") || message.contains("email")) {
            return "I'd love to help you write! ✍️ I can draft professional emails, creative stories, or even poetry. Give me a topic and a tone (e.g., formal, friendly, poetic) and I'll get started.";
        }

        // 5. DAILY CONVERSATION (Hello, How are you)
        if (message.contains("hello") || message.contains("hi") || message.contains("hey")) {
            return "Hello! 👋 I'm your intelligent AI assistant. How is your day going? I'm ready to help you with knowledge, tech, or business queries.";
        } else if (message.contains("how are you")) {
            return "I'm functioning at peak efficiency! 🚀 Ready to assist you with anything from history to coding. How can I help you today?";
        }

        // 6. BUSINESS & SUPPORT (Pricing, Refunds, Plans)
        if (message.contains("price") || message.contains("cost") || message.contains("plan")) {
            return "We offer flexible plans for every need:\n- **Basic**: $19/mo\n- **Pro**: $49/mo (Advanced AI)\n- **Enterprise**: Custom solutions\nWhich one would you like to explore?";
        } else if (message.contains("refund") || message.contains("cancel")) {
            return "We offer a **30-day money-back guarantee**. If you're not satisfied, you can initiate a refund through your account settings.";
        }

        // 7. SENTIMENT-AWARE FALLBACKS
        if ("positive".equals(sentiment)) {
            return "I'm glad to see you're happy! 😊 Is there anything else you'd like to learn or discuss today?";
        } else if ("negative".equals(sentiment)) {
            return "I'm sorry if I'm not meeting your expectations. 😔 Please let me know how I can be more helpful, or tell me more about your specific question.";
        }

        // 8. DEFAULT INTELLIGENT CATCH-ALL
        return "That's an interesting topic! 🧠 I'm designed to be a universal assistant. Could you provide a bit more detail? I can handle **General Knowledge**, **Math**, **Coding**, or **Business** questions.";
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