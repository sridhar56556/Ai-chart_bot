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
        "You are a highly capable AI assistant like ChatGPT. " +
        "Answer ANY question directly and helpfully. Do not restrict yourself to support or pricing. " +
        "For simple math (e.g. 3+2), give just the answer (5). For jokes, tell a natural joke. " +
        "Be conversational, intelligent, and human-like. Never say you are a limited assistant.";

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
        if (msg.matches(".*\\d+[\\+\\-\\*\\/]\\d+.*")) {
            return handleMath(msg);
        }

        // 2. GREETINGS
        if (msg.matches("hi|hello|hey|heythere|hiii|gm|gn")) return "Hello! How can I help you today?";

        // 3. FACTS
        if (msg.contains("hyderabad")) return getHyderabadDetails();
        if (msg.contains("joke")) return getRandomJoke();
        if (msg.contains("whoareyou")) return "I'm your AI assistant, ready to help with any question.";

        // 4. FALLBACK
        return smartFallback(userMessage);
    }

    private String handleMath(String msg) {
        String clean = msg.replaceAll("[^0-9\\+\\-\\*\\/\\.]", "");
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
        return "Please provide a valid math expression.";
    }

    private String formatMathResult(double res) {
        if (res == (long) res) return String.format("%d", (long) res);
        return String.format("%.2f", res);
    }

    private String smartFallback(String msg) {
        return "Tell me more about that.";
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
            throw new RuntimeException("Gemini API call failed", e);
        }
    }

    private String getRandomJoke() {
        String[] jokes = {
            "Why did the web developer walk out of a restaurant? Because of the table layout! 💻",
            "Why do Java developers wear glasses? Because they don't C#! 👓",
            "What do you call a fake noodle? An Impasta! 🍝"
        };
        return jokes[random.nextInt(jokes.length)];
    }

    private String getHyderabadDetails() {
        return "Hyderabad is famous for Charminar, Golconda Fort, and world-class Biryani. It's a major IT hub.";
    }

    private String getPythonDetails() {
        return "Python is a versatile language used for AI, Data Science, and Web Development.";
    }

    private String getJavaDetails() {
        return "Java is a robust, object-oriented language used for enterprise applications and Android.";
    }

    private String getBiryaniRecipe() {
        return "Biryani is made by layering partially cooked rice over marinated meat and slow-cooking (Dum).";
    }

    private String getTokyoDetails() {
        return "Tokyo is a metropolis known for mixing traditional culture with high-tech skyscrapers.";
    }
}
