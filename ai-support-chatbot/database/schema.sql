-- Database schema for AI Support Chatbot

CREATE DATABASE IF NOT EXISTS chatbot_db;
USE chatbot_db;

-- Table to store chat messages
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_message TEXT NOT NULL,
    bot_response TEXT NOT NULL,
    sentiment VARCHAR(20) NOT NULL,
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index on timestamp for faster retrieval of recent messages
CREATE INDEX IF NOT EXISTS idx_timestamp ON chat_messages(timestamp);