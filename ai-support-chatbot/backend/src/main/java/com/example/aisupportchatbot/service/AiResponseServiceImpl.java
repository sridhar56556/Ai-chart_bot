package com.example.aisupportchatbot.service;

import com.example.aisupportchatbot.model.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

@Service
public class AiResponseServiceImpl implements AiResponseService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    private final Random random = new Random();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String SYSTEM_PROMPT = """
        You are a highly capable AI assistant designed to provide accurate, clear, and helpful responses across a wide range of topics.

        Rules:
        - Always give correct, well-structured answers
        - Be concise but complete
        - If the question is unclear, ask for clarification
        - Do not hallucinate facts; say "I don’t know" if unsure
        - Provide step-by-step explanations when solving problems
        - Adapt tone based on user input (technical, casual, formal)
        - For coding questions: give working, clean, production-level code
        - For math: show steps clearly
        - For general questions: explain in simple terms first, then add detail

        Behavior:
        - Never mention being an AI unless explicitly asked
        - Do not include unnecessary disclaimers
        - Focus on solving the user's problem directly
        """;

    @Override
    public String generateResponse(String userMessage, String sentiment, List<ChatMessage> context) {
        try {
            return callOpenAI(userMessage, context);
        } catch (Exception e) {
            System.err.println("Error calling OpenAI/OpenRouter API: " + e.getMessage());
            return fallbackGenerateResponse(userMessage);
        }
    }

    private String callOpenAI(String userMessage, List<ChatMessage> context) {
        // Critical Check: Ensure the key isn't the literal string "${OPENROUTER_API_KEY}"
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("{")) {
            throw new RuntimeException("ERROR: OPENROUTER_API_KEY is not set or invalid in environment variables. Check Render dashboard.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        // OpenRouter optional headers
        headers.set("HTTP-Referer", "http://localhost:8080");
        headers.set("X-Title", "AI Support Chatbot");

        Map<String, Object> requestBody = new HashMap<>();
        // Using standard OpenAI model as requested now that we have a new key
        requestBody.put("model", "openai/gpt-3.5-turbo"); 

        List<Map<String, String>> messages = new ArrayList<>();
        
        // Add System Prompt
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_PROMPT);
        messages.add(systemMsg);

        // Add Context
        if (context != null) {
            for (ChatMessage msg : context) {
                if (msg.getUserMessage() != null && !msg.getUserMessage().isEmpty()) {
                    Map<String, String> userMsg = new HashMap<>();
                    userMsg.put("role", "user");
                    userMsg.put("content", msg.getUserMessage());
                    messages.add(userMsg);
                }
                if (msg.getBotResponse() != null && !msg.getBotResponse().isEmpty()) {
                    Map<String, String> botMsg = new HashMap<>();
                    botMsg.put("role", "assistant");
                    botMsg.put("content", msg.getBotResponse());
                    messages.add(botMsg);
                }
            }
        }

        // Add Current User Message
        Map<String, String> currentMsg = new HashMap<>();
        currentMsg.put("role", "user");
        currentMsg.put("content", userMessage);
        messages.add(currentMsg);

        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null) {
                        return (String) message.get("content");
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("OpenRouter API Call Failed: " + e.getMessage());
        }
        
        throw new RuntimeException("Invalid response from API");
    }

    private String fallbackGenerateResponse(String userMessage) {
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

    private String getRandomJoke() {
        String[] jokes = {
            "Why don’t programmers like nature? Because it has too many bugs! 😄",
            "Why do Java developers wear glasses? Because they don't C#! 👓",
            "What do you call a fake noodle? An Impasta! 🍝"
        };
        return jokes[random.nextInt(jokes.length)];
    }
}

