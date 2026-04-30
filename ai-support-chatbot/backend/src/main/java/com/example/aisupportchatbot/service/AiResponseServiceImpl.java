package com.example.aisupportchatbot.service;
// Final UI Polishing complete

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
        System.out.println("Generating response for: " + userMessage + " (Version: Premium-Math-v2)");
        
        // Try Gemini AI first if API Key is present
        if (geminiApiKey != null && !geminiApiKey.isEmpty()) {
            try {
                return callGemini(SYSTEM_PROMPT + "\n\nUser: " + userMessage);
            } catch (Exception e) {
                System.err.println("Gemini API failed: " + e.getMessage());
            }
        }

        String msg = userMessage.toLowerCase().trim().replaceAll("\\s+", "");

        // 1. MATH FIRST (Direct Evaluation)
        if (msg.matches(".*\\d+[\\+\\-\\*\\/]\\d+.*")) {
            return handleMath(msg);
        }

        // 3. TRAVEL & GEOGRAPHY
        if (msg.contains("hyderabad")) return "Hyderabad is a major city in India known for its rich history, the iconic **Charminar**, and world-famous **Biryani**. It's also a leading tech hub! 🏰";
        if (msg.contains("tokyo") || msg.contains("japan")) return "Tokyo is a dazzling metropolis where traditional temples meet neon-lit skyscrapers. Highlights include **Shibuya Crossing** and incredible sushi. 🍣";
        if (msg.contains("paris") || msg.contains("france")) return "Paris, the 'City of Light', is renowned for its art, fashion, and history. The **Eiffel Tower** and **Louvre** are its most famous landmarks. 🥐";

        // 4. TECH & CODING
        if (msg.contains("python")) return "**Python** is a high-level language loved for its simplicity. Perfect for AI and data science. \n```python\nprint(\"Hello World\")\n```";
        if (msg.contains("java")) return "**Java** is a robust, enterprise-grade language. It powers everything from Android apps to massive server systems. ☕";

        // 5. GENERAL & PHILOSOPHY
        if (msg.contains("joke")) return getRandomJoke();
        if (msg.contains("who are you") || msg.contains("your name")) return "I am your **Universal AI Assistant**. I'm here to help you with anything from coding and math to general knowledge! 🚀";
        if (msg.contains("meaning of life")) return "The ultimate answer is **42**, but the journey to find it is what truly matters! 🌌";

        // 6. GREETINGS
        if (msg.matches("hi|hello|hey|hey there|hiii|gm|gn")) return "Hello! 👋 I'm your Universal AI Assistant. How can I help you today?";

        // 7. FALLBACK
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
