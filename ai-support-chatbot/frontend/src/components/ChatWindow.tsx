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
      if (Array.isArray(history)) {
        setMessages(history);
      } else {
        console.error('History is not an array:', history);
        setMessages([]);
      }
    } catch (error) {
      console.error('Error loading chat history:', error);
      setMessages([]);
    }
  };

  const handleNewChat = async () => {
    try {
      await import('../services/api').then(m => m.clearChatHistory());
      setMessages([]);
      setInput('');
    } catch (error) {
      console.error('Error clearing chat history:', error);
      // Fallback: clear local only
      setMessages([]);
    }
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
    } catch (error: any) {
      console.error('FULL ERROR:', error);
      const errorMessage = error?.response?.data?.message || error?.message || String(error);
      const errorMsg: Message = {
        id: Date.now(),
        userMessage: userMsg,
        botResponse: `**Error:** ${errorMessage}`,
        sentiment: "negative",
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
              <h1 style={{ fontSize: '1.2rem', fontWeight: 600 }}>AI Chat Assistant</h1>
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
                <React.Fragment key={msg.id}>
                  {/* User Message (Right) */}
                  <div className="message-wrapper user-wrapper">
                    <motion.div 
                      initial={{ opacity: 0, x: 20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ duration: 0.3 }}
                      className="user-message"
                    >
                      <div className="message-bubble">
                        {msg.userMessage}
                      </div>
                    </motion.div>
                  </div>

                  {/* Bot Message (Left) */}
                  <div className="message-wrapper bot-wrapper">
                    <motion.div 
                      initial={{ opacity: 0, x: -20 }}
                      animate={{ opacity: 1, x: 0 }}
                      transition={{ duration: 0.4, delay: 0.1 }}
                      className="bot-message"
                    >
                      <div className="bot-content">
                        <div className="avatar" style={{ width: '32px', height: '32px', fontSize: '0.85rem', background: 'linear-gradient(135deg, #2563eb, #7c3aed)', borderRadius: '10px' }}>AI</div>
                        <div className="bot-response-text">
                          <div className="message-bubble">
                            {formatText(msg.botResponse)}
                          </div>
                        </div>
                      </div>
                    </motion.div>
                  </div>
                </React.Fragment>
              ))}
            </AnimatePresence>
            
            {isLoading && (
              <div className="message-wrapper">
                <div className="bot">
                  <div className="bot-content">
                    <div className="avatar" style={{ width: '32px', height: '32px', fontSize: '0.85rem', background: 'linear-gradient(135deg, #2563eb, #7c3aed)', borderRadius: '10px' }}>AI</div>
                    <div className="bot-response-text">
                      <div style={{ opacity: 0.5 }}>Thinking...</div>
                    </div>
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
                placeholder="Message AI Chat Assistant..."
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