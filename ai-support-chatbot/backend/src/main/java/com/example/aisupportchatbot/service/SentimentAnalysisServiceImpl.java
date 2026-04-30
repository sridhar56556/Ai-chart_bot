package com.example.aisupportchatbot.service;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class SentimentAnalysisServiceImpl implements SentimentAnalysisService {

    private final List<String> positiveWords = Arrays.asList(
        "good", "great", "excellent", "happy", "love", "awesome", "wonderful", "thanks", "thank", "nice", "perfect", "amazing", "help", "yes"
    );

    private final List<String> negativeWords = Arrays.asList(
        "bad", "worst", "hate", "unhappy", "terrible", "horrible", "error", "fail", "broken", "stop", "no", "not", "busy", "wait", "slow"
    );

    @Override
    public String analyzeSentiment(String text) {
        if (text == null || text.isEmpty()) {
            return "neutral";
        }

        String lowerText = text.toLowerCase();
        
        long posCount = positiveWords.stream().filter(lowerText::contains).count();
        long negCount = negativeWords.stream().filter(lowerText::contains).count();

        if (posCount > negCount) {
            return "positive";
        } else if (negCount > posCount) {
            return "negative";
        }

        return "neutral";
    }
}