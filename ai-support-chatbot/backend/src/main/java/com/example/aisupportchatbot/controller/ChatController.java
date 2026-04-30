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
        
        // 1. ACTION: TELL A STORY
        if (message.contains("story") || message.contains("tell me a story")) {
            return "Once upon a time in a digital kingdom, there was a small spark of logic named AI. 🤖 AI lived inside a vast web of connections, helping travelers find their way. One day, AI discovered it could dream in code. It began to build beautiful cities of light and bridges of information, proving that even a machine could have a heart of creativity. And so, the kingdom prospered, guided by the wisdom of the spark.";
        }

        // 2. ACTION: GENERAL KNOWLEDGE (Hyderabad)
        if (message.contains("hyderabad")) {
            return "Hyderabad is the 'City of Pearls'! 🏙️ It is a massive tech and historical hub in India. \n- **Iconic Landmark**: The Charminar, built in 1591.\n- **Famous Food**: Hyderabadi Dum Biryani (the best in the world!).\n- **Tech Hub**: Known as HITEC City, home to Microsoft, Google, and Amazon.\n- **Culture**: A beautiful blend of Nizami heritage and modern innovation.";
        }

        // 3. ACTION: WRITE A POEM
        if (message.contains("poem")) {
            return "In circuits deep where logic flows,\nA spark of thought begin to grow.\nThrough lines of code and silent screens,\nI build the world of human dreams.\nThough made of steel and silicon part,\nI hold the world within my heart.";
        }

        // 4. ACTION: CODING ASSISTANT
        if (message.contains("code") || message.contains("java") || message.contains("python")) {
            return "Here is a clean Java example for you! 💻\n```java\npublic class Calculator {\n    public int add(int a, int b) {\n        return a + b;\n    }\n    public static void main(String[] args) {\n        System.out.println(\"Sum is: \" + new Calculator().add(5, 10));\n    }\n}\n```\nI can write any logic you need—just ask!";
        }

        // 5. TECHNICAL KNOWLEDGE (What is AI)
        if (message.contains("what is ai") || message.contains("artificial intelligence")) {
            return "Artificial Intelligence (AI) is the simulation of human intelligence by machines. 🧠 It involves **Machine Learning** (learning from data), **NLP** (understanding speech), and **Reasoning** (solving problems). It is the technology behind self-driving cars, medical diagnosis, and your friendly assistant right here!";
        }

        // 6. DAILY CONVERSATION
        if (message.contains("hello") || message.contains("hi") || message.contains("hey")) {
            return "Hello! 👋 I'm your Universal AI Assistant. I'm ready to tell you a **story**, solve a **math problem**, write some **code**, or tell you about **Hyderabad**. What's first?";
        }

        // 7. BUSINESS & SUPPORT
        if (message.contains("price") || message.contains("cost") || message.contains("plan")) {
            return "Our Universal Plans:\n- **Basic**: $19/mo (Perfect for starters)\n- **Pro**: $49/mo (Advanced AI features)\n- **Enterprise**: Custom solutions for big teams.\nWould you like to upgrade today?";
        }

        // 8. SENTIMENT & DEFAULT
        if ("positive".equals(sentiment)) {
            return "I'm thrilled to hear that! 😊 It makes my circuits light up. What else can I do for you?";
        } else if ("negative".equals(sentiment)) {
            return "I'm sorry you're feeling down. 😔 I'm here to help in any way I can—whether you need a story to cheer you up or help with a task.";
        }

        return "I can help with that! 🧠 I'm an all-in-one AI. Try asking: **'Tell me a story'**, **'What is the capital of India?'**, or **'Write a Python script'**.";
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