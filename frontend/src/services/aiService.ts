import { AiChatMessage } from '../types';
import { storage } from '../storage/asyncStorage';
import { apiClient } from './api';

const AI_CHAT_KEY = '@livo_ai_chat';

export interface AiChatReply {
  content: string;
  conversationId?: string;
  suggestedReplies?: string[];
  cards?: Record<string, any>;
}

export interface UniversalAddParsedResult {
  detectedDomain: 'TASK' | 'HABIT' | 'EVENT' | 'GOAL' | 'FINANCE' | 'NOTE' | 'TRIP';
  title: string;
  parsedEntityDraft: Record<string, any>;
  confidence: number;
}

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

  async sendChatMessage(userText: string, conversationId?: string): Promise<AiChatReply> {
    // 1. Save user message locally
    await this.saveMessage({
      text: userText,
      sender: 'USER',
    });

    try {
      // 2. Call backend Gemini AI endpoint
      const response = await apiClient.post<{ data: any }>('/ai/chat', {
        message: userText,
        conversationId: conversationId || undefined,
        operatingMode: 'GENERAL',
      });

      const replyData = response.data?.data;
      const aiContent = replyData?.content || "I've processed your request.";

      // 3. Save assistant message locally
      await this.saveMessage({
        text: aiContent,
        sender: 'LIVO_AI',
      });

      return {
        content: aiContent,
        conversationId: replyData?.conversationId,
        suggestedReplies: replyData?.suggestedReplies || [],
        cards: replyData?.cards,
      };
    } catch (err) {
      console.warn('Backend AI call failed, generating local fallback:', err);
      const fallbackReply = "I'm currently running in offline mode. Your message has been saved and will sync with the server once connected.";
      
      await this.saveMessage({
        text: fallbackReply,
        sender: 'LIVO_AI',
      });

      return {
        content: fallbackReply,
      };
    }
  },

  async parseQuickCapture(input: string, useAi: boolean = false): Promise<UniversalAddParsedResult | null> {
    try {
      const response = await apiClient.post<{ data: UniversalAddParsedResult }>('/universal-add/parse', {
        input,
        useAi,
      });
      return response.data?.data || null;
    } catch (err) {
      console.warn('Universal-add parse call failed:', err);
      return null;
    }
  },

  async confirmQuickCapture(confirmPayload: any): Promise<any> {
    try {
      const response = await apiClient.post('/universal-add/confirm', confirmPayload);
      return response.data;
    } catch (err) {
      console.warn('Universal-add confirm call failed:', err);
      throw err;
    }
  },

  async clearHistory(): Promise<void> {
    await storage.removeItem(AI_CHAT_KEY);
  }
};
