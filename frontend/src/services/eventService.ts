import { Event } from '../types';
import { storage } from '../storage/asyncStorage';
import { apiClient } from './api';

const EVENTS_KEY = '@livo_events';

const formatTime = (timeStr?: string): string => {
  if (!timeStr) return '09:00:00';
  if (timeStr.length === 5) return `${timeStr}:00`;
  return timeStr;
};

const mapBackendBlock = (b: any): Event => ({
  id: String(b.id),
  title: b.title,
  description: b.description || '',
  date: b.blockDate || '',
  startTime: b.startTime ? String(b.startTime).substring(0, 5) : '09:00',
  endTime: b.endTime ? String(b.endTime).substring(0, 5) : '10:00',
  category: b.category || 'PERSONAL',
  createdAt: b.createdAt || new Date().toISOString(),
});

export const eventService = {
  async getEvents(): Promise<Event[]> {
    try {
      const response = await apiClient.get<{ data: any[] }>('/schedule-blocks');
      const backendBlocks = response.data?.data;
      if (Array.isArray(backendBlocks)) {
        const mapped = backendBlocks.map(mapBackendBlock);
        await storage.setItem(EVENTS_KEY, mapped);
        return mapped;
      }
    } catch (err) {
      console.warn('Backend /schedule-blocks fetch failed, loading from local cache:', err);
    }
    const cached = await storage.getItem<Event[]>(EVENTS_KEY);
    return cached || [];
  },

  async saveEvent(event: Omit<Event, 'id' | 'createdAt'>): Promise<Event> {
    const events = await this.getEvents();
    const tempId = Date.now().toString() + Math.random().toString(36).substring(2, 9);
    
    const newEvent: Event = {
      ...event,
      id: tempId,
      createdAt: new Date().toISOString(),
    };
    
    events.push(newEvent);
    await storage.setItem(EVENTS_KEY, events);

    try {
      const payload = {
        title: event.title,
        description: event.description || '',
        blockDate: event.date || new Date().toISOString().split('T')[0],
        startTime: formatTime(event.startTime),
        endTime: formatTime(event.endTime),
        category: event.category || 'PERSONAL',
        isLocked: false,
        allowConflict: false,
      };

      const response = await apiClient.post<{ data: any }>('/schedule-blocks', payload);
      const serverBlock = response.data?.data;
      if (serverBlock?.id) {
        newEvent.id = String(serverBlock.id);
        const updatedList = events.map(e => (e.id === tempId ? newEvent : e));
        await storage.setItem(EVENTS_KEY, updatedList);
      }
    } catch (err) {
      console.warn('Failed to sync new event to backend, kept in offline cache:', err);
    }
    
    return newEvent;
  },

  async updateEvent(updatedEvent: Event): Promise<Event> {
    const events = await this.getEvents();
    const index = events.findIndex(e => e.id === updatedEvent.id);
    
    if (index !== -1) {
      events[index] = updatedEvent;
      await storage.setItem(EVENTS_KEY, events);
    }

    try {
      await apiClient.put(`/schedule-blocks/${updatedEvent.id}`, {
        title: updatedEvent.title,
        blockDate: updatedEvent.date,
        startTime: formatTime(updatedEvent.startTime),
        endTime: formatTime(updatedEvent.endTime),
        category: updatedEvent.category,
      });
    } catch (err) {
      console.warn('Failed to sync event update to backend:', err);
    }
    
    return updatedEvent;
  },

  async deleteEvent(eventId: string): Promise<void> {
    const events = await this.getEvents();
    const filteredEvents = events.filter(e => e.id !== eventId);
    await storage.setItem(EVENTS_KEY, filteredEvents);

    try {
      await apiClient.delete(`/schedule-blocks/${eventId}`);
    } catch (err) {
      console.warn('Failed to sync event deletion to backend:', err);
    }
  }
};
