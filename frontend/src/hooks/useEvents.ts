import { useState, useEffect, useCallback } from 'react';
import { Event } from '../types';
import { eventService } from '../services/eventService';

export const useEvents = () => {
  const [events, setEvents] = useState<Event[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchEvents = useCallback(async () => {
    setIsLoading(true);
    try {
      const fetchedEvents = await eventService.getEvents();
      // Sort events by date and time
      const sorted = fetchedEvents.sort((a, b) => {
        // Very basic sorting, assuming date formats are comparable or we just sort by creation for now
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
      });
      setEvents(sorted);
      setError(null);
    } catch (err) {
      setError('Failed to load events');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchEvents();
  }, [fetchEvents]);

  const addEvent = async (eventData: Omit<Event, 'id' | 'createdAt'>) => {
    try {
      const newEvent = await eventService.saveEvent(eventData);
      setEvents(prev => [newEvent, ...prev]);
      return newEvent;
    } catch (err) {
      setError('Failed to add event');
      console.error(err);
      throw err;
    }
  };

  const updateEvent = async (eventData: Event) => {
    try {
      const updatedEvent = await eventService.updateEvent(eventData);
      setEvents(prev => prev.map(e => e.id === updatedEvent.id ? updatedEvent : e));
      return updatedEvent;
    } catch (err) {
      setError('Failed to update event');
      console.error(err);
      throw err;
    }
  };

  const deleteEvent = async (eventId: string) => {
    try {
      await eventService.deleteEvent(eventId);
      setEvents(prev => prev.filter(e => e.id !== eventId));
    } catch (err) {
      setError('Failed to delete event');
      console.error(err);
      throw err;
    }
  };

  return {
    events,
    isLoading,
    error,
    addEvent,
    updateEvent,
    deleteEvent,
    refreshEvents: fetchEvents,
  };
};
