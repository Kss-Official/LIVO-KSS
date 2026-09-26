import { AiChatMessage } from '../types';
import { storage } from '../storage/asyncStorage';

const AI_CHAT_KEY = '@livo_ai_chat';

export const aiService = {
  async getMessages(): Promise<AiChatMessage[]> {
    const messages = await storage.getItem<AiChatMessage[]>(AI_CHAT_KEY);
    return messages || [];
  },

  async saveMessage(message: Omit<AiChatMessage, 'id' | 'timestamp'>): Promise<AiChatMessage> {
    const messages = await this.getMessages();
    
    const newMessage: AiChatMessage = {
      ...message,
      id: Date.now().toString() + Math.random().toString(36).substring(2, 9),
      timestamp: new Date().toISOString(),
    };
    
    messages.push(newMessage);
    await storage.setItem(AI_CHAT_KEY, messages);
    
    return newMessage;
  },

  async clearHistory(): Promise<void> {
    await storage.removeItem(AI_CHAT_KEY);
  }
};
