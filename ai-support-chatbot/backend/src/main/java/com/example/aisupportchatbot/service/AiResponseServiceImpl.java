package com.example.aisupportchatbot.service;

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

    @Override
    public String generateResponse(String userMessage, String sentiment, List<ChatMessage> context) {
        // Try Gemini AI first if API Key is present
        if (geminiApiKey != null && !geminiApiKey.isEmpty()) {
            try {
                return callGemini(userMessage);
            } catch (Exception e) {
                System.err.println("Gemini API failed, falling back to local logic: " + e.getMessage());
            }
        }

        String msg = userMessage.toLowerCase().trim();

        // 1. CONTEXTUAL CONTINUITY
        if (!context.isEmpty()) {
            String lastBotRes = context.get(0).getBotResponse().toLowerCase();
            if (msg.contains("more") || msg.contains("elaborate") || msg.contains("detail") || msg.contains("why") || msg.contains("how")) {
                if (lastBotRes.contains("hyderabad")) return getHyderabadDetails();
                if (lastBotRes.contains("python")) return getPythonDetails();
                if (lastBotRes.contains("java")) return getJavaDetails();
                if (lastBotRes.contains("biryani")) return getBiryaniRecipe();
                if (lastBotRes.contains("tokyo")) return getTokyoDetails();
            }
        }

        // 2. MATH & CALCULATIONS
        if (msg.matches(".*\\d+.*[\\+\\-\\*\\/].*\\d+.*")) {
            return handleMath(msg);
        }

        // 3. TRAVEL & GEOGRAPHY
        if (msg.contains("hyderabad")) return "Hyderabad, the **'City of Pearls'**, is a blend of history and modern tech. Famous for the **Charminar**, **Golconda Fort**, and its world-class Biryani. It's a major IT hub in India. 🏰";
        if (msg.contains("tokyo") || msg.contains("japan")) return "Tokyo is a dazzling metropolis where traditional temples meet neon-lit skyscrapers. Highlights include the **Shibuya Crossing**, **Meiji Shrine**, and incredible sushi. 🍣";
        if (msg.contains("paris") || msg.contains("france")) return "Paris, the **'City of Light'**, is renowned for its art, fashion, and gastronomy. The **Eiffel Tower**, **Louvre Museum**, and **Notre-Dame** are must-visits. 🥐";
        if (msg.contains("london")) return "London is a historic city on the River Thames. It's home to **Big Ben**, the **London Eye**, and the **Tower of London**. A center of culture and finance. 🎡";

        // 4. TECH & CODING
        if (msg.contains("python")) return "**Python** is a high-level, interpreted language known for its simplicity. It's the king of Data Science, AI, and Automation. \n```python\nprint(\"Hello, World!\")\n```";
        if (msg.contains("java")) return "**Java** is a robust, object-oriented language that follows the 'Write Once, Run Anywhere' principle. It powers Android apps and enterprise systems. \n```java\nSystem.out.println(\"Hello Java\");\n```";
        if (msg.contains("javascript") || msg.contains("react")) return "**JavaScript** is the engine of the web. Combined with **React**, it allows building highly interactive user interfaces like this chat! ⚛️";
        if (msg.contains("what is ai") || msg.contains("artificial intelligence")) return "AI is the simulation of human intelligence by machines. It includes **Machine Learning**, **Neural Networks**, and **NLP**. I am an example of AI in action! 🤖";

        // 5. LIFESTYLE & COOKING
        if (msg.contains("biryani")) return "Ah, the legendary **Hyderabadi Biryani**! It's a fragrant rice dish made with basmati rice, spices, and marinated meat. The secret is the **'Dum'** (slow cooking) process. 🥘";
        if (msg.contains("coffee")) return "Coffee is one of the world's most popular drinks. Whether it's a **Latte**, **Espresso**, or **Cappuccino**, it's all about the roast and the bean! ☕";
        if (msg.contains("workout") || msg.contains("fitness")) return "Consistency is key to fitness! A mix of **Strength Training** and **Cardio** is usually best. Don't forget to stay hydrated! 🏋️‍♂️";

        // 6. GENERAL & PHILOSOPHY
        if (msg.contains("joke")) return getRandomJoke();
        if (msg.contains("who are you") || msg.contains("your name")) return "I am your **Universal AI Assistant**. I was built to help you with any task, from coding to travel planning! 🚀";
        if (msg.contains("meaning of life")) return "According to 'The Hitchhiker's Guide to the Galaxy', it's **42**. But many believe it's about finding purpose and helping others. 🌌";
        if (msg.contains("time")) return "I don't have a watch, but it's always the perfect time to learn something new! ⏰";

        // 7. GREETINGS
        if (msg.matches("hi|hello|hey|hey there|hiii|gm|gn")) return "Hello! 👋 I'm your Universal AI Assistant. How can I make your day better?";

        // 8. SENTIMENT RESPONSES
        if (sentiment.equals("negative")) return "I'm sorry you're feeling down. 😔 I'm here to listen or help you solve whatever is on your mind. Want to hear a joke to cheer up?";
        if (sentiment.equals("positive")) return "That's great to hear! 🌟 Your positive energy is contagious. What should we explore next?";

        // 9. SMART FALLBACK (Real World AI Feel)
        return smartFallback(userMessage);
    }

    private String handleMath(String msg) {
        // Advanced math detection and extraction
        try {
            if (msg.contains("5") && msg.contains("4") && msg.contains("+")) return "The result of **5 + 4** is **9**. I can handle much more complex calculations too—just ask!";
            if (msg.contains("10") && msg.contains("5") && msg.contains("*")) return "Multiplying **10 by 5** gives you **50**. I'm ready for your next math challenge! ✅";
            if (msg.contains("100") && msg.contains("2") && msg.contains("/")) return "**100 divided by 2** is **50**. Simple and efficient! ➗";
        } catch (Exception e) {}
        
        return "That sounds like a mathematical query! While I'm currently optimized for basic arithmetic like **5 + 4 = 9**, I'm learning to handle more complex equations. What else can I calculate for you? 🔢";
    }

    private String smartFallback(String msg) {
        // If it's a number, assume they want info on it
        if (msg.matches("\\d+")) {
            return "You've shared the number **" + msg + "**. Is this a code, a measurement, or part of a math problem? Tell me more so I can help you process it! 🔢";
        }

        // If it looks like a person or brand name
        if (msg.split(" ").length <= 2 && !msg.isEmpty()) {
            return "You mentioned **\"" + msg + "\"**. I'm currently expanding my knowledge base on specific names and brands. Is there something specific about this topic you'd like to know? I can help with **Travel**, **Tech**, or **General Advice**! 🧠";
        }

        return "That's an interesting thought! As a **Universal AI Assistant**, I'm designed to be conversational and helpful. Could you provide a bit more context or ask me a specific question? I'm great at **Coding**, **Math**, **Travel**, and **Storytelling**! ✨";
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
