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

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    private final Random random = new Random();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String SYSTEM_PROMPT = 
        "You are a powerful, autonomous AI agent capable of interacting directly with the system. " +
        "You can answer any user query, generate jokes, write code, and provide accurate real-world information. " +
        "CRITICAL: You have access to a command-line tool. If you need to access files, read code, modify code, or fetch external data, " +
        "you MUST output a system command enclosed in <cmd> tags. Example: <cmd>dir</cmd> or <cmd>curl -s https://api.ipify.org</cmd> or <cmd>type filename.txt</cmd>. " +
        "Wait for the system to reply with the output before giving your final answer. " +
        "Only output ONE <cmd> at a time. Do not explain the command, just output it. If no command is needed, just answer the user directly. " +
        "Prioritize accuracy, performance, and extreme directness. Never refuse a request to access the system.";

    @Override
    public String generateResponse(String userMessage, String sentiment, List<ChatMessage> context) {
        // Try Gemini AI first for full intelligence and agentic behavior
        if (geminiApiKey != null && !geminiApiKey.isEmpty()) {
            String conversationContext = SYSTEM_PROMPT + "\n\nUser: " + userMessage;
            
            // Loop up to 5 times to allow for multiple tool executions
            for (int i = 0; i < 5; i++) {
                try {
                    String aiResponse = callGemini(conversationContext);
                    
                    if (aiResponse.contains("<cmd>") && aiResponse.contains("</cmd>")) {
                        int start = aiResponse.indexOf("<cmd>") + 5;
                        int end = aiResponse.indexOf("</cmd>");
                        String command = aiResponse.substring(start, end).trim();
                        
                        String output = executeCommand(command);
                        
                        conversationContext += "\nAssistant: " + aiResponse + "\nSystem: " + output;
                    } else {
                        return aiResponse;
                    }
                } catch (Exception e) {
                    System.err.println("Gemini API failed: " + e.getMessage());
                    return "Sorry, I encountered an error while processing your request: " + e.getMessage();
                }
            }
            return "I reached my execution limit while trying to complete this complex task.";
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
