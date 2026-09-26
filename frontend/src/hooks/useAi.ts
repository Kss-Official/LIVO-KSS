import { useState, useEffect, useCallback } from 'react';
import { AiChatMessage } from '../types';
import { aiService } from '../services/aiService';

export const useAi = () => {
  const [messages, setMessages] = useState<AiChatMessage[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchMessages = useCallback(async () => {
    try {
      const fetched = await aiService.getMessages();
      setMessages(fetched);
      setError(null);
    } catch (err) {
      setError('Failed to load chat history');
      console.error(err);
    }
  }, []);

  useEffect(() => {
    fetchMessages();
  }, [fetchMessages]);

  const sendMessage = async (text: string) => {
    if (!text.trim()) return;
    
    // Optimistic UI for user message
    const tempUserMsg: AiChatMessage = {
      id: Date.now().toString(),
      text,
      sender: 'USER',
      timestamp: new Date().toISOString(),
    };
    
    setMessages(prev => [...prev, tempUserMsg]);
    setIsLoading(true);

    try {
      await aiService.saveMessage({ text, sender: 'USER' });

      // Mock AI response delay
      setTimeout(async () => {
        const mockResponse = "I'm your LIVO AI assistant! This is a mock response, as backend is not connected yet. How can I help you organize your day?";
        const aiMsg = await aiService.saveMessage({
          text: mockResponse,
          sender: 'LIVO_AI',
        });
        setMessages(prev => [...prev, aiMsg]);
        setIsLoading(false);
      }, 1500);

    } catch (err) {
      setError('Failed to send message');
      console.error(err);
      setIsLoading(false);
    }
  };

  const clearChat = async () => {
    try {
      await aiService.clearHistory();
      setMessages([]);
    } catch (err) {
      console.error(err);
    }
  };

  return {
    messages,
    isLoading,
    error,
    sendMessage,
    clearChat,
  };
};
