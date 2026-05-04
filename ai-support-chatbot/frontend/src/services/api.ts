import axios from 'axios';

const API_BASE_URL = '/api';

export const sendMessage = async (message: string) => {
  const response = await axios.post(`${API_BASE_URL}/chat`, { message });
  return response.data;
};

export const clearChatHistory = async () => {
  await axios.delete(`${API_BASE_URL}/chat/clear`);
};

export const getChatHistory = async () => {
  const response = await axios.get(`${API_BASE_URL}/chat/history`);
  return response.data;
};