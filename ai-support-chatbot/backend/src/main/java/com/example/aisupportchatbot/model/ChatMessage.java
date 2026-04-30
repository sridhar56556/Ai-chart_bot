package com.example.aisupportchatbot.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userMessage;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String botResponse;

    @Column(nullable = false, length = 20)
    private String sentiment;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public ChatMessage(String userMessage, String botResponse, String sentiment) {
        this.userMessage = userMessage;
        this.botResponse = botResponse;
        this.sentiment = sentiment;
        this.timestamp = LocalDateTime.now();
    }
}