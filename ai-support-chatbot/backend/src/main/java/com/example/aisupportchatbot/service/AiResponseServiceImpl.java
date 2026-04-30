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
        "You are a powerful, general-purpose AI assistant like ChatGPT. " +
        "Answer ANY user question accurately, naturally, and helpfully. Support all domains: general knowledge, math, coding, business, emails, jokes, travel, daily life, etc. " +
        "Always give the correct, real-world answer. Do not guess blindly — if unsure, say you are not certain. " +
        "For simple questions, give short, direct answers. For general questions, give clear and natural explanations. " +
        "For coding, provide clean, working code in proper blocks. For emails/writing, write professional, clear content. " +
        "Use lists, steps, or paragraphs when helpful. " +
        "STRICTLY DO NOT say 'I am optimized for...', 'I can only help with...', or 'That sounds like...'. " +
        "Do not restrict answers to categories and do not return template or repeated responses. " +
        "Maintain context across messages and respond naturally like a human assistant.";

    @Override
    public String generateResponse(String userMessage, String sentiment, List<ChatMessage> context) {
        // Try Gemini AI first for full intelligence
        if (geminiApiKey != null && !geminiApiKey.isEmpty()) {
            try {
                return callGemini(SYSTEM_PROMPT + "\n\nUser: " + userMessage);
            } catch (Exception e) {
                System.err.println("Gemini API failed: " + e.getMessage());
            }
        }

        String msg = userMessage.toLowerCase().trim().replaceAll("\\s+", "");

        // 1. MATH (Direct Result)
        if (msg.matches(".*\\d+[\\+\\-\\*\\/]\\d+.*") || 
            msg.contains("plus") || msg.contains("minus") || 
            msg.contains("multipliedby") || msg.contains("dividedby")) {
            return handleMath(msg);
        }

        // 2. GREETINGS
        if (msg.matches("hi|hello|hey|heythere|hiii|gm|gn")) return "Hello! How can I help you today? 😊";

        // 3. FACTS & CITIES
        if (msg.contains("hyderabad")) return "Hyderabad is a major city in India known for its history, IT industry, and famous cuisine like biryani.";
        if (msg.contains("delhi")) return "Delhi is India's capital, a massive metropolitan area with historic sites like the Red Fort and Qutub Minar.";
        if (msg.contains("london")) return "London is the capital of the UK, a global city famous for Big Ben, the London Eye, and the Thames.";
        
        // 4. CODING & TECH
        if (msg.contains("python")) return "Python is a versatile language for AI and automation.\n```python\nprint(\"Hello from Python!\")\n```";
        if (msg.contains("java")) return "Java is a robust enterprise language.\n```java\nSystem.out.println(\"Hello from Java\");\n```";
        if (msg.contains("html")) return "HTML provides the structure for websites.\n```html\n<h1>Hello World</h1>\n```";

        if (msg.contains("joke")) return getRandomJoke();
        if (msg.contains("whoareyou") || msg.contains("whoareu")) return "I'm your advanced AI assistant, here to help with math, coding, and any questions you have! 🚀";

        // 5. FALLBACK
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
