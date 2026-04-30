package com.example.aisupportchatbot.service;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class SentimentAnalysisServiceImpl implements SentimentAnalysisService {

    private StanfordCoreNLP pipeline;

    @PostConstruct
    public void init() {
        new Thread(() -> {
            try {
                System.out.println("Starting Stanford CoreNLP initialization (this may take a minute)...");
                Properties props = new Properties();
                props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
                this.pipeline = new StanfordCoreNLP(props);
                System.out.println("Stanford CoreNLP initialized successfully!");
            } catch (Exception e) {
                System.err.println("CRITICAL: Failed to initialize Stanford CoreNLP: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public String analyzeSentiment(String text) {
        if (text == null || text.isEmpty() || pipeline == null) {
            return "neutral";
        }

        Annotation annotation = pipeline.process(text);
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            if (sentiment != null) {
                if (sentiment.contains("Positive")) {
                    return "positive";
                } else if (sentiment.contains("Negative")) {
                    return "negative";
                }
            }
        }

        return "neutral";
    }
}