import { Event } from '../types';
import { storage } from '../storage/asyncStorage';

const EVENTS_KEY = '@livo_events';

export const eventService = {
  async getEvents(): Promise<Event[]> {
    const events = await storage.getItem<Event[]>(EVENTS_KEY);
    return events || [];
  },

  async saveEvent(event: Omit<Event, 'id' | 'createdAt'>): Promise<Event> {
    const events = await this.getEvents();
    
    const newEvent: Event = {
      ...event,
      id: Date.now().toString() + Math.random().toString(36).substring(2, 9),
      createdAt: new Date().toISOString(),
    };
    
    events.push(newEvent);
    await storage.setItem(EVENTS_KEY, events);
    
    return newEvent;
  },

  async updateEvent(updatedEvent: Event): Promise<Event> {
    const events = await this.getEvents();
    const index = events.findIndex(e => e.id === updatedEvent.id);
    
    if (index !== -1) {
      events[index] = updatedEvent;
      await storage.setItem(EVENTS_KEY, events);
    }
    
    return updatedEvent;
  },

  async deleteEvent(eventId: string): Promise<void> {
    const events = await this.getEvents();
    const filteredEvents = events.filter(e => e.id !== eventId);
    await storage.setItem(EVENTS_KEY, filteredEvents);
  }
};
