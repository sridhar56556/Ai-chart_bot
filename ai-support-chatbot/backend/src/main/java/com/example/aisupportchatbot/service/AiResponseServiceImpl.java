package com.example.aisupportchatbot.service;
// Final Extreme Directness Engine v1

import com.example.aisupportchatbot.model.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

@Service
public class AiResponseServiceImpl implements AiResponseService {

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    private final Random random = new Random();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String SYSTEM_PROMPT = 
        "You are a smart, helpful AI assistant like ChatGPT. " +
        "Answer any question naturally and correctly. Support math, knowledge, jokes, and coding. " +
        "For simple questions, give short direct answers. Be human-like, accurate, and helpful. " +
        "No robotic phrases or limited bot behavior.";

    @Override
    public String generateResponse(String userMessage, String sentiment, List<ChatMessage> context) {
        // Try Gemini AI first if API Key is present
        if (geminiApiKey != null && !geminiApiKey.isEmpty()) {
            try {
                return callGemini(SYSTEM_PROMPT + "\n\nUser: " + userMessage);
            } catch (Exception e) {
                System.err.println("Gemini API failed: " + e.getMessage());
            }
        }

        String msg = userMessage.toLowerCase().trim().replaceAll("\\s+", "");

        // 1. MATH FIRST (Direct Answer)
        if (msg.matches(".*\\d+[\\+\\-\\*\\/]\\d+.*") || 
            msg.contains("plus") || msg.contains("minus") || 
            msg.contains("multipliedby") || msg.contains("dividedby")) {
            return handleMath(msg);
        }

        // 2. GREETINGS
        if (msg.matches("hi|hello|hey|heythere|hiii|gm|gn")) return "Hello! I'm your AI assistant. How can I help you today? 😊";

        // 3. TIME
        if (msg.contains("time")) {
            return "The current time is " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + ".";
        }

        // 4. FACTS & CITIES (ChatGPT Style)
        if (msg.contains("hyderabad")) {
            return "Hyderabad is a major city in India known for its history, IT industry, and world-famous Biryani. It's often called the 'City of Pearls'.";
        }
        if (msg.contains("delhi")) return "Delhi is the capital of India, a massive metropolitan area with a rich history spanning centuries.";
        if (msg.contains("london")) return "London is the capital of the UK, famous for its iconic landmarks like Big Ben and the Tower of London.";
        
        // 5. PROGRAMMING
        if (msg.contains("python")) return "Python is a powerful language used for AI, web dev, and automation. Example: `print('Hello World')`";
        if (msg.contains("java")) return "Java is a robust, object-oriented language widely used for enterprise and Android applications.";

        if (msg.contains("joke")) return getRandomJoke();
        if (msg.contains("whoareyou") || msg.contains("whoareu")) return "I'm your smart AI assistant, ready to help you with anything from math to general knowledge! 🚀";

        // 6. FALLBACK
        return smartFallback(userMessage);
    }

    private String handleMath(String msg) {
        // Convert words to symbols
        String normalized = msg.toLowerCase()
            .replace("plus", "+")
            .replace("minus", "-")
            .replace("multipliedby", "*")
            .replace("times", "*")
            .replace("into", "*")
            .replace("dividedby", "/")
            .replaceAll("\\s+", "");
            
        String clean = normalized.replaceAll("[^0-9\\+\\-\\*\\/\\.]", "");
        
        try {
            if (clean.contains("+")) {
                String[] parts = clean.split("\\+");
                return formatMathResult(Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]));
            }
            if (clean.contains("-")) {
                String[] parts = clean.split("-");
                return formatMathResult(Double.parseDouble(parts[0]) - Double.parseDouble(parts[1]));
            }
            if (clean.contains("*")) {
                String[] parts = clean.split("\\*");
                return formatMathResult(Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]));
            }
            if (clean.contains("/")) {
                String[] parts = clean.split("/");
                return formatMathResult(Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]));
            }
        } catch (Exception e) {}
        
        return "I can help with that! If you're asking a math question, please use numbers like '5 + 2'.";
    }

    private String formatMathResult(double res) {
        if (res == (long) res) return String.format("%d", (long) res);
        return String.format("%.2f", res);
    }

    private String smartFallback(String msg) {
        return "I'm here to help. Could you provide a bit more detail so I can give you the most accurate answer?";
    }

    private String callGemini(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;
        
        Map<String, Object> body = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);
        content.put("parts", new Object[]{part});
        body.put("contents", new Object[]{content});

        try {
            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            return (String) ((List<Map<String, Object>>) ((Map<String, Object>) candidates.get(0).get("content")).get("parts")).get(0).get("text");
        } catch (Exception e) {
            throw new RuntimeException("Gemini API failed", e);
        }
    }

    private String getRandomJoke() {
        String[] jokes = {
            "Why don’t programmers like nature? Because it has too many bugs! 😄",
            "Why do Java developers wear glasses? Because they don't C#! 👓",
            "What do you call a fake noodle? An Impasta! 🍝"
        };
        return jokes[random.nextInt(jokes.length)];
    }

    private String getHyderabadDetails() {
        return "Hyderabad is famous for its rich history, the Charminar, and world-class Biryani.";
    }
}
