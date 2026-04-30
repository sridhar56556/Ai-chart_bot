import React, { useState, useEffect, useRef } from 'react';
import { sendMessage, getChatHistory } from '../services/api';
import { Send, Bot, User, Sparkles, MessageSquare } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

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
        botResponse: "⚠️ The AI Assistant is still initializing or the server is busy. Please wait 1-2 minutes and try again. If the issue persists, check your Render logs.",
        sentiment: "neutral",
        timestamp: new Date().toISOString()
      };
      setMessages(prev => [...prev, errorMsg]);
      setIsLoading(false);
    }
  };

  return (
    <div className="chat-container glass">
      <header className="chat-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div className="glass" style={{ padding: '10px', borderRadius: '12px', background: 'var(--primary)' }}>
            <Sparkles size={24} color="white" />
          </div>
          <div>
            <h1>Universal AI Assistant</h1>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>24/7 Inteligent Agent • Powered by Advanced ML</p>
          </div>
        </div>
        <Bot size={20} color="var(--primary)" />
      </header>

      <div className="chat-messages">
        {messages.length === 0 && !isLoading && (
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', opacity: 0.5 }}>
            <Sparkles size={48} style={{ marginBottom: '16px' }} />
            <p>Ready to help with Code, Writing, Math, and more.</p>
          </div>
        )}
        
        <AnimatePresence>
          {messages.map((msg) => (
            <React.Fragment key={msg.id}>
              {/* User Message */}
              <motion.div 
                initial={{ opacity: 0, x: 20, y: 10 }}
                animate={{ opacity: 1, x: 0, y: 0 }}
                className="message-wrapper user"
              >
                <div className="message-bubble">
                  {msg.userMessage}
                </div>
                <div style={{ alignSelf: 'flex-end' }}>
                  <span className={`sentiment-badge sentiment-${msg.sentiment.toLowerCase()}`}>
                    {msg.sentiment}
                  </span>
                </div>
              </motion.div>

              {/* Bot Response */}
              <motion.div 
                initial={{ opacity: 0, x: -20, y: 10 }}
                animate={{ opacity: 1, x: 0, y: 0 }}
                transition={{ delay: 0.2 }}
                className="message-wrapper bot"
              >
                <div style={{ display: 'flex', gap: '10px' }}>
                  <div className="glass" style={{ width: '32px', height: '32px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--surface)', border: '1px solid var(--border)' }}>
                    <Bot size={16} />
                  </div>
                  <div className="message-bubble" style={{ whiteSpace: 'pre-wrap' }}>
                    {msg.botResponse}
                  </div>
                </div>
              </motion.div>
            </React.Fragment>
          ))}
        </AnimatePresence>
        
        {isLoading && (
          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="message-wrapper bot"
          >
            <div style={{ display: 'flex', gap: '10px' }}>
              <div className="glass" style={{ width: '32px', height: '32px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--surface)', border: '1px solid var(--border)' }}>
                <Bot size={16} />
              </div>
              <div className="loading-dots">
                <div className="dot"></div>
                <div className="dot"></div>
                <div className="dot"></div>
              </div>
            </div>
          </motion.div>
        )}
        <div ref={messagesEndRef} />
      </div>

      <div className="quick-actions" style={{ padding: '0 20px', marginBottom: '10px', display: 'flex', gap: '8px', overflowX: 'auto' }}>
        {['Coding Helper', 'Creative Writing', 'Solve Math', 'Translate', 'Pricing', 'Tech Support'].map(action => (
          <button 
            key={action}
            onClick={() => { setInput(action); }}
            className="glass"
            style={{ padding: '6px 12px', borderRadius: '20px', fontSize: '0.8rem', cursor: 'pointer', whiteSpace: 'nowrap', border: '1px solid var(--border)', color: 'var(--text)' }}
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
            placeholder="Type your message here..."
            disabled={isLoading}
          />
          <button type="submit" disabled={!input.trim() || isLoading}>
            <Send size={18} />
            <span>Send</span>
          </button>
        </div>
      </form>
    </div>
  );
};

export default ChatWindow;