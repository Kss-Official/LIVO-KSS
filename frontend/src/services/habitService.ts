import { Habit } from '../types';
import { storage } from '../storage/asyncStorage';
import { apiClient } from './api';

const HABITS_KEY = '@livo_habits';

const mapBackendHabit = (h: any): Habit => ({
  id: String(h.id),
  title: h.title,
  description: h.description || '',
  frequency: h.frequencyType || 'DAILY',
  color: h.colorHex || '#10B981',
  category: 'HEALTH',
  streakCount: h.currentStreak || 0,
  completedToday: Boolean(h.completedToday),
  time: h.preferredClockTime || '',
  reminderTime: h.reminderTime || '',
  createdAt: h.createdAt || new Date().toISOString(),
});

export const habitService = {
  async getHabits(): Promise<Habit[]> {
    try {
      const response = await apiClient.get<{ data: any[] }>('/habits');
      const backendHabits = response.data?.data;
      if (Array.isArray(backendHabits)) {
        const mapped = backendHabits.map(mapBackendHabit);
        await storage.setItem(HABITS_KEY, mapped);
        return mapped;
      }
    } catch (err) {
      console.warn('Backend /habits fetch failed, loading from local cache:', err);
    }
    const cached = await storage.getItem<Habit[]>(HABITS_KEY);
    return cached || [];
  },

  async saveHabit(habit: Omit<Habit, 'id' | 'createdAt' | 'streakCount' | 'completedToday'> & { streakCount?: number; completedToday?: boolean }): Promise<Habit> {
    const habits = await this.getHabits();
    const tempId = Date.now().toString() + Math.random().toString(36).substring(2, 9);
    
    const newHabit: Habit = {
      streakCount: habit.streakCount || 0,
      completedToday: habit.completedToday || false,
      ...habit,
      id: tempId,
      createdAt: new Date().toISOString(),
    };
    
    // Optimistic local cache
    habits.push(newHabit);
    await storage.setItem(HABITS_KEY, habits);

    try {
      const payload = {
        title: habit.title,
        description: habit.description || '',
        colorHex: habit.color && habit.color.startsWith('#') ? habit.color : '#10B981',
        frequencyType: (habit.frequency || 'DAILY').toUpperCase(),
        targetCount: 1,
        targetUnit: 'times',
      };

      const response = await apiClient.post<{ data: any }>('/habits', payload);
      const serverHabit = response.data?.data;
      if (serverHabit?.id) {
        newHabit.id = String(serverHabit.id);
        const updatedList = habits.map(h => (h.id === tempId ? newHabit : h));
        await storage.setItem(HABITS_KEY, updatedList);
      }
    } catch (err) {
      console.warn('Failed to sync new habit to backend, kept in offline cache:', err);
    }
    
    return newHabit;
  },

  async updateHabit(updatedHabit: Habit): Promise<Habit> {
    const habits = await this.getHabits();
    const index = habits.findIndex(h => h.id === updatedHabit.id);
    
    if (index !== -1) {
      habits[index] = updatedHabit;
      await storage.setItem(HABITS_KEY, habits);
    }

    try {
      await apiClient.put(`/habits/${updatedHabit.id}`, {
        title: updatedHabit.title,
        description: updatedHabit.description,
        colorHex: updatedHabit.color,
        frequencyType: updatedHabit.frequency?.toUpperCase(),
      });
    } catch (err) {
      console.warn('Failed to sync habit update to backend:', err);
    }
    
    return updatedHabit;
  },

  async toggleHabit(habitId: string): Promise<Habit | null> {
    const habits = await this.getHabits();
    const index = habits.findIndex(h => h.id === habitId);
    
    if (index !== -1) {
      const h = habits[index];
      h.completedToday = !h.completedToday;
      h.streakCount = h.completedToday ? (h.streakCount || 0) + 1 : Math.max(0, (h.streakCount || 1) - 1);
      habits[index] = h;
      await storage.setItem(HABITS_KEY, habits);

      try {
        if (h.completedToday) {
          const today = new Date().toISOString().split('T')[0];
          await apiClient.post(`/habits/${habitId}/log`, {
            logDate: today,
            isCompleted: true,
          });
        }
      } catch (err) {
        console.warn('Failed to sync habit log to backend:', err);
      }

      return h;
    }
    return null;
  },

  async deleteHabit(habitId: string): Promise<void> {
    const habits = await this.getHabits();
    const filteredHabits = habits.filter(h => h.id !== habitId);
    await storage.setItem(HABITS_KEY, filteredHabits);

    try {
      await apiClient.delete(`/habits/${habitId}`);
    } catch (err) {
      console.warn('Failed to sync habit deletion to backend:', err);
    }
  }
};
