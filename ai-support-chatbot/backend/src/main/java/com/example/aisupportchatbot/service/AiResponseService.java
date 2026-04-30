package com.example.aisupportchatbot.service;

import com.example.aisupportchatbot.model.ChatMessage;
import java.util.List;

public interface AiResponseService {
    String generateResponse(String userMessage, String sentiment, List<ChatMessage> context);
}
