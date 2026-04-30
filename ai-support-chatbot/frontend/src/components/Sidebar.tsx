import React from 'react';
import { MessageSquare, Plus, Clock } from 'lucide-react';

interface SidebarProps {
  history: any[];
  onNewChat: () => void;
}

const Sidebar: React.FC<SidebarProps> = ({ history, onNewChat }) => {
  return (
    <div className="sidebar glass">
      <button className="new-chat-btn" onClick={onNewChat}>
        <Plus size={18} />
        New Chat
      </button>
      
      <div className="history-list">
        <div className="history-label">
          <Clock size={14} />
          <span>Recent History</span>
        </div>
        {history.length === 0 ? (
          <p className="empty-history">No chats yet</p>
        ) : (
          history.slice(0, 10).map((msg, index) => (
            <div key={index} className="history-item">
              <MessageSquare size={16} />
              <span className="truncate">{msg.userMessage}</span>
            </div>
          ))
        )}
      </div>

      <div className="sidebar-footer">
        <div className="user-profile">
          <div className="avatar">O</div>
          <span>AI Chat Assistant</span>
        </div>
      </div>
    </div>
  );
};

export default Sidebar;
