import React, { useState, useEffect, useRef } from 'react';
import { sendMessage, getChatHistory } from '../services/api';
import { Send, Bot, Sparkles } from 'lucide-react';
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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;

    const userMsg = input;
    setInput('');
    setIsLoading(true);

    try {
      const response = await sendMessage(userMsg);
      // Simulate natural AI thinking time
      setTimeout(() => {
        setMessages(prev => [...prev, response]);
        setIsLoading(false);
      }, 600);
    } catch (error) {
      console.error('Error sending message:', error);
      const errorMsg: Message = {
        id: Date.now(),
        userMessage: userMsg,
        botResponse: "⚠️ The AI Assistant is still initializing or the server is busy. Please wait 1-2 minutes and try again.",
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
              <div style={{ padding: '8px', borderRadius: '8px', background: 'var(--primary)' }}>
                <Sparkles size={20} color="white" />
              </div>
              <h1 style={{ fontSize: '1.2rem' }}>Universal AI Assistant</h1>
            </div>
            <Bot size={20} color="var(--primary)" />
          </header>

          <div className="chat-messages">
            {messages.length === 0 && !isLoading && (
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', opacity: 0.3 }}>
                <Bot size={64} style={{ marginBottom: '20px' }} />
                <h2 style={{ fontSize: '1.5rem', marginBottom: '8px' }}>How can I help you today?</h2>
                <p>Ask about Math, Coding, Stories, or Hyderabad.</p>
              </div>
            )}
            
            <AnimatePresence>
              {messages.map((msg) => (
                <React.Fragment key={msg.id}>
                  {/* User Message */}
                  <motion.div 
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="message-wrapper user"
                  >
                    <div className="message-bubble">
                      {msg.userMessage}
                    </div>
                  </motion.div>

                  {/* Bot Response */}
                  <motion.div 
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="message-wrapper bot"
                  >
                    <div style={{ display: 'flex', gap: '16px' }}>
                      <div className="avatar" style={{ width: '28px', height: '28px', fontSize: '0.8rem' }}>AI</div>
                      <div className="message-bubble" style={{ padding: '0', background: 'transparent', border: 'none' }}>
                        {msg.botResponse}
                        <div style={{ marginTop: '8px' }}>
                          <span className={`sentiment-badge sentiment-${msg.sentiment.toLowerCase()}`}>
                            {msg.sentiment}
                          </span>
                        </div>
                      </div>
                    </div>
                  </motion.div>
                </React.Fragment>
              ))}
            </AnimatePresence>
            
            {isLoading && (
              <div className="message-wrapper bot">
                <div style={{ display: 'flex', gap: '16px' }}>
                  <div className="avatar" style={{ width: '28px', height: '28px', fontSize: '0.8rem' }}>AI</div>
                  <div className="loading-dots">
                    <div className="dot"></div>
                    <div className="dot"></div>
                    <div className="dot"></div>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          <div className="quick-actions" style={{ padding: '0 24px', marginBottom: '10px', display: 'flex', gap: '8px', overflowX: 'auto', justifyContent: 'center' }}>
            {['Tell a Story', 'Solve Math', 'Write Code', 'About Hyderabad'].map(action => (
              <button 
                key={action}
                onClick={() => { setInput(action); }}
                className="glass"
                style={{ padding: '8px 16px', borderRadius: '12px', fontSize: '0.8rem', cursor: 'pointer', whiteSpace: 'nowrap', border: '1px solid var(--border)', color: 'var(--text-muted)', background: 'transparent' }}
              >
                {action}
              </button>
            ))}
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