import { useState, useEffect, useCallback } from 'react';
import { AiChatMessage } from '../types';
import { aiService } from '../services/aiService';

export const useAi = () => {
  const [messages, setMessages] = useState<AiChatMessage[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [conversationId, setConversationId] = useState<string | undefined>();

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
      // Real backend Gemini 1.5/2.0 Flash AI call
      const reply = await aiService.sendChatMessage(text, conversationId);
      if (reply.conversationId) {
        setConversationId(reply.conversationId);
      }

      const aiMsg: AiChatMessage = {
        id: (Date.now() + 1).toString(),
        text: reply.content,
        sender: 'LIVO_AI',
        timestamp: new Date().toISOString(),
      };

      setMessages(prev => [...prev, aiMsg]);
      setError(null);
    } catch (err) {
      setError('Failed to send message');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const clearChat = async () => {
    try {
      await aiService.clearHistory();
      setMessages([]);
      setConversationId(undefined);
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
