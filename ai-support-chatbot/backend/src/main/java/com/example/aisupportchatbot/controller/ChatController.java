package com.example.aisupportchatbot.controller;

import com.example.aisupportchatbot.model.ChatMessage;
import com.example.aisupportchatbot.repository.ChatMessageRepository;
import com.example.aisupportchatbot.service.SentimentAnalysisService;
import com.example.aisupportchatbot.service.AiResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "${FRONTEND_URL:*}"})
public class ChatController {

    @Autowired
    private SentimentAnalysisService sentimentAnalysisService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private AiResponseService aiResponseService;

    @PostMapping("/chat")
    public ResponseEntity<ChatMessage> chat(@RequestBody ChatRequest request) {
        String userMessage = request.getMessage();
        String sentiment = "neutral";
        
        try {
            sentiment = sentimentAnalysisService.analyzeSentiment(userMessage);
        } catch (Exception e) {
            System.err.println("Sentiment analysis failed: " + e.getMessage());
        }

        // Get context from last 3 messages
        List<ChatMessage> history = chatMessageRepository.findAllByOrderByTimestampDesc();
        List<ChatMessage> context = history.size() > 3 ? history.subList(0, 3) : history;

        String botResponse = aiResponseService.generateResponse(userMessage, sentiment, context);

        ChatMessage chatMessage = new ChatMessage(userMessage, botResponse, sentiment);
        ChatMessage savedMessage;
        
        try {
            savedMessage = chatMessageRepository.save(chatMessage);
        } catch (Exception e) {
            System.err.println("Failed to save message to DB: " + e.getMessage());
            savedMessage = chatMessage; 
        }

        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping("/chat/history")
    public ResponseEntity<List<ChatMessage>> getHistory() {
        List<ChatMessage> messages = chatMessageRepository.findAllByOrderByTimestampDesc();
        return ResponseEntity.ok(messages);
    }
    @DeleteMapping("/chat/clear")
    public ResponseEntity<Void> clearHistory() {
        chatMessageRepository.deleteAll();
        return ResponseEntity.ok().build();
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