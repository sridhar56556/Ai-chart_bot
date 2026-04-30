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
        // Remove spaces and non-math characters
        String clean = msg.replaceAll("[^0-9\\+\\-\\*\\/\\.]", "");
        
        try {
            if (clean.contains("+")) {
                String[] parts = clean.split("\\+");
                double res = Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]);
                return formatMathResult(res);
            }
            if (clean.contains("-")) {
                String[] parts = clean.split("-");
                double res = Double.parseDouble(parts[0]) - Double.parseDouble(parts[1]);
                return formatMathResult(res);
            }
            if (clean.contains("*")) {
                String[] parts = clean.split("\\*");
                double res = Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]);
                return formatMathResult(res);
            }
            if (clean.contains("/")) {
                String[] parts = clean.split("/");
                double res = Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
                return formatMathResult(res);
            }
        } catch (Exception e) {}
        
        return "I'm designed to handle your math queries! For example, **4 + 3 = 7**. What else should I calculate for you? 🔢";
    }

    private String formatMathResult(double res) {
        if (res == (long) res) return String.format("%d", (long) res);
        return String.format("%.2f", res);
    }

    private String smartFallback(String msg) {
        if (msg.matches("\\d+")) return "You shared a number: **" + msg + "**. Is there a specific calculation you need help with? 🔢";
        if (msg.split(" ").length <= 2 && !msg.isEmpty()) return "You mentioned **\"" + msg + "\"**. Is there something specific you'd like to know about this? I can help with **Travel**, **Tech**, or **Math**! 🧠";

        return "That's an interesting topic! I'm a **Universal AI Assistant** and I'm ready to discuss anything with you. Could you provide a bit more detail? ✨";
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
            Map<String, Object> firstCandidate = candidates.get(0);
            Map<String, Object> contentRes = (Map<String, Object>) firstCandidate.get("content");
            List<Map<String, Object>> partsRes = (List<Map<String, Object>>) contentRes.get("parts");
            return (String) partsRes.get(0).get("text");
        } catch (Exception e) {
            throw new RuntimeException("Gemini API call failed", e);
        }
    }

    private String getRandomJoke() {
        String[] jokes = {
            "**Why did the web developer walk out of a restaurant?** Because of the table layout! 💻",
            "**Why do Java developers wear glasses?** Because they don't C#! 👓",
            "**What do you call a fake noodle?** An Impasta! 🍝",
            "**Why did the scarecrow win an award?** Because he was outstanding in his field! 🌾"
        };
        return jokes[random.nextInt(jokes.length)];
    }

    private String getHyderabadDetails() {
        return "Beyond the basics, you should visit the **Salat Jung Museum**, take a walk in **Lumbini Park**, or shop at **Laad Bazaar**. The city's pearls are genuine and world-famous! 💎";
    }

    private String getPythonDetails() {
        return "Python's ecosystem is massive. Libraries like **Pandas** for data, **Django** for web, and **TensorFlow** for AI make it incredibly versatile. It's often the first language people learn! 🐍";
    }

    private String getJavaDetails() {
        return "Java's strong typing and JVM (Java Virtual Machine) make it highly stable for large-scale applications. It's the backbone of many banking systems and big-data tools like **Hadoop**. ☕";
    }

    private String getBiryaniRecipe() {
        return "To make a proper Biryani, you need to layer semi-cooked rice over marinated meat, seal the pot with dough, and cook on a very low flame. This **'Dum'** method traps the steam and infuses every grain with flavor! 👨‍🍳";
    }

    private String getTokyoDetails() {
        return "In Tokyo, don't miss the **Tsukiji Outer Market** for the freshest seafood, and check out **Akihabara** if you're into electronics and anime. The public transport system is a marvel of efficiency! 🚄";
    }
}
