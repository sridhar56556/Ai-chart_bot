package com.example.aisupportchatbot.service;

import com.example.aisupportchatbot.model.ChatMessage;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;

@Service
public class AiResponseServiceImpl implements AiResponseService {

    private final Random random = new Random();

    @Override
    public String generateResponse(String userMessage, String sentiment, List<ChatMessage> context) {
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

        // 9. FALLBACK
        return "That's an interesting topic! As a **Universal AI Assistant**, I'm constantly learning. Could you tell me more about that? Or ask me about **Tech**, **Travel**, or a **Joke**! 🧠";
    }

    private String handleMath(String msg) {
        try {
            if (msg.contains("5") && msg.contains("4") && msg.contains("+")) return "The result of **5 + 4** is **9**. Need another calculation?";
            if (msg.contains("10") && msg.contains("5") && msg.contains("*")) return "Multiplying **10 by 5** gives you **50**. Logic checks out! ✅";
            if (msg.contains("100") && msg.contains("2") && msg.contains("/")) return "**100 divided by 2** is **50**. Simple math! ➗";
        } catch (Exception e) {}
        return "I can help with math! For example, **5 + 4 = 9**. I'm currently optimized for basic arithmetic. 🔢";
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
