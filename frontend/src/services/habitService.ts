import { Habit } from '../types';
import { storage } from '../storage/asyncStorage';

const HABITS_KEY = '@livo_habits';

export const habitService = {
  async getHabits(): Promise<Habit[]> {
    const habits = await storage.getItem<Habit[]>(HABITS_KEY);
    return habits || [];
  },

  async saveHabit(habit: Omit<Habit, 'id' | 'createdAt' | 'streakCount' | 'completedToday'> & { streakCount?: number; completedToday?: boolean }): Promise<Habit> {
    const habits = await this.getHabits();
    
    const newHabit: Habit = {
      streakCount: 0,
      completedToday: false,
      ...habit,
      id: Date.now().toString() + Math.random().toString(36).substring(2, 9),
      createdAt: new Date().toISOString(),
    };
    
    habits.push(newHabit);
    await storage.setItem(HABITS_KEY, habits);
    
    return newHabit;
  },

  async updateHabit(updatedHabit: Habit): Promise<Habit> {
    const habits = await this.getHabits();
    const index = habits.findIndex(h => h.id === updatedHabit.id);
    
    if (index !== -1) {
      habits[index] = updatedHabit;
      await storage.setItem(HABITS_KEY, habits);
    }
    
    return updatedHabit;
  },

  async deleteHabit(habitId: string): Promise<void> {
    const habits = await this.getHabits();
    const filteredHabits = habits.filter(h => h.id !== habitId);
    await storage.setItem(HABITS_KEY, filteredHabits);
  }
};
