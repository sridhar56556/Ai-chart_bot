import React from 'react';
import ChatWindow from './components/ChatWindow';
import './index.css';

function App() {
  return (
    <div className="App" style={{ width: '100%', height: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
      <ChatWindow />
    </div>
  );
}

export default App;