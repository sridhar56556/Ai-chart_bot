package com.example.aisupportchatbot.service;
// Final Agentic Extreme Directness Engine v2

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

    @Value("${OPENAI_API_KEY:}")
    private String openAiApiKey;

    private final Random random = new Random();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String SYSTEM_PROMPT = 
        "You are a knowledgeable and helpful system assistant. Your goal is to provide accurate, " +
        "informative, and engaging responses while mimicking the conversational style of ChatGPT. " +
        "Always respond promptly to user queries once you receive the input.";

    @Override
    public String generateResponse(String userMessage, String sentiment, List<ChatMessage> context) {
        // Try OpenAI first for full intelligence
        if (openAiApiKey != null && !openAiApiKey.isEmpty()) {
            try {
                return callOpenAi(SYSTEM_PROMPT, userMessage);
            } catch (Exception e) {
                System.err.println("OpenAI API failed: " + e.getMessage());
                // Fallback to local logic if API fails
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

    private String executeCommand(String command) {
        try {
            ProcessBuilder pb;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                pb = new ProcessBuilder("cmd.exe", "/c", command);
            } else {
                pb = new ProcessBuilder("bash", "-c", command);
            }
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            try (java.util.Scanner s = new java.util.Scanner(process.getInputStream(), "UTF-8").useDelimiter("\\A")) {
                String output = s.hasNext() ? s.next() : "";
                
                // Truncate output if it's too long to prevent token overflow
                if (output.length() > 2000) {
                    output = output.substring(0, 2000) + "\n...[output truncated]";
                }
                return output.isEmpty() ? "Command executed successfully with no output." : output;
            }
        } catch (Exception e) {
            return "Error executing command: " + e.getMessage();
        }
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

    private String callOpenAi(String systemPrompt, String userMessage) {
        String url = "https://api.openai.com/v1/chat/completions";
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        body.put("messages", new Object[]{systemMessage, userMsg});

        org.springframework.http.HttpEntity<Map<String, Object>> request = new org.springframework.http.HttpEntity<>(body, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            throw new RuntimeException("OpenAI API failed", e);
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
