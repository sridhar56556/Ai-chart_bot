import React, { useState, useEffect, useRef } from 'react';
import { sendMessage, getChatHistory } from '../services/api';
import { Send, Bot, Sparkles, User } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import Sidebar from './Sidebar';

interface Message {
  id: number;
  userMessage: string;
  botResponse: string;
  sentiment: string;
  timestamp: string;
}

const ChatWindow: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    loadMessages();
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadMessages = async () => {
    try {
      const history = await getChatHistory();
      setMessages(history);
    } catch (error) {
      console.error('Error loading chat history:', error);
    }
  };

  const handleNewChat = () => {
    setMessages([]);
    setInput('');
  };

  const formatText = (text: string) => {
    // Basic formatting for **bold** and newlines
    return text.split('\n').map((line, i) => (
      <span key={i}>
        {line.split(/(\*\*.*?\*\*)/).map((part, j) => {
          if (part.startsWith('**') && part.endsWith('**')) {
            return <strong key={j}>{part.slice(2, -2)}</strong>;
          }
          return part;
        })}
        <br />
      </span>
    ));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;

    const userMsg = input;
    setInput('');
    setIsLoading(true);

    try {
      const response = await sendMessage(userMsg);
      setTimeout(() => {
        setMessages(prev => [...prev, response]);
        setIsLoading(false);
      }, 400);
    } catch (error) {
      console.error('Error sending message:', error);
      const errorMsg: Message = {
        id: Date.now(),
        userMessage: userMsg,
        botResponse: "**Error:** The AI Assistant is temporarily unavailable. Please try again in a moment.",
        sentiment: "neutral",
        timestamp: new Date().toISOString()
      };
      setMessages(prev => [...prev, errorMsg]);
      setIsLoading(false);
    }
  };

  return (
    <>
      <Sidebar history={messages} onNewChat={handleNewChat} />
      
      <main className="main-content">
        <div className="chat-container">
          <header className="chat-header">
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <h1 style={{ fontSize: '1.2rem', fontWeight: 600 }}>Universal AI Assistant</h1>
            </div>
            <Bot size={20} color="var(--text-muted)" />
          </header>

          <div className="chat-messages">
            {messages.length === 0 && !isLoading && (
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', opacity: 0.2 }}>
                <Bot size={48} style={{ marginBottom: '16px' }} />
                <h2 style={{ fontSize: '1.2rem' }}>How can I help you today?</h2>
              </div>
            )}
            
            <AnimatePresence>
              {messages.map((msg) => (
                <div key={msg.id} className="message-wrapper">
                  {/* User Message */}
                  <motion.div 
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="user"
                  >
                    <div className="message-bubble">
                      {msg.userMessage}
                    </div>
                  </motion.div>

                  {/* Bot Response */}
                  <motion.div 
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="bot"
                  >
                    <div className="bot-content">
                      <div className="avatar" style={{ width: '28px', height: '28px', fontSize: '0.8rem', background: '#2563eb', borderRadius: '50%' }}>AI</div>
                      <div className="bot-response-text">
                        <div className="message-bubble">
                          {formatText(msg.botResponse)}
                        </div>
                        <span className="sentiment-badge">
                          {msg.sentiment.toLowerCase()}
                        </span>
                      </div>
                    </div>
                  </motion.div>
                </div>
              ))}
            </AnimatePresence>
            
            {isLoading && (
              <div className="message-wrapper">
                <div className="bot-content">
                  <div className="avatar" style={{ width: '28px', height: '28px', fontSize: '0.8rem', background: '#2563eb', borderRadius: '50%' }}>AI</div>
                  <div className="bot-response-text">
                    <div style={{ opacity: 0.5 }}>Thinking...</div>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          <form className="chat-input-container" onSubmit={handleSubmit}>
            <div className="input-wrapper">
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder="Message Universal AI Assistant..."
                disabled={isLoading}
              />
              <button type="submit" disabled={!input.trim() || isLoading}>
                <Send size={18} />
              </button>
            </div>
          </form>
        </div>
      </main>
    </>
  );
};

export default ChatWindow;