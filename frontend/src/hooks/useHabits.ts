import { useState, useEffect, useCallback } from 'react';
import { Habit } from '../types';
import { habitService } from '../services/habitService';

export const useHabits = () => {
  const [habits, setHabits] = useState<Habit[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchHabits = useCallback(async () => {
    setIsLoading(true);
    try {
      const fetchedHabits = await habitService.getHabits();
      setHabits(fetchedHabits);
      setError(null);
    } catch (err) {
      setError('Failed to load habits');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchHabits();
  }, [fetchHabits]);

  const addHabit = async (habitData: Omit<Habit, 'id' | 'createdAt' | 'streakCount' | 'completedToday'> & { streakCount?: number; completedToday?: boolean }) => {
    try {
      const newHabit = await habitService.saveHabit({
        streakCount: 0,
        completedToday: false,
        ...habitData,
      });
      setHabits(prev => [newHabit, ...prev]);
      return newHabit;
    } catch (err) {
      setError('Failed to add habit');
      console.error(err);
      throw err;
    }
  };

  const updateHabit = async (habitData: Habit) => {
    try {
      const updatedHabit = await habitService.updateHabit(habitData);
      setHabits(prev => prev.map(h => h.id === updatedHabit.id ? updatedHabit : h));
      return updatedHabit;
    } catch (err) {
      setError('Failed to update habit');
      console.error(err);
      throw err;
    }
  };

  const deleteHabit = async (habitId: string) => {
    try {
      await habitService.deleteHabit(habitId);
      setHabits(prev => prev.filter(h => h.id !== habitId));
    } catch (err) {
      setError('Failed to delete habit');
      console.error(err);
      throw err;
    }
  };
  
  const toggleHabit = async (habitId: string) => {
    try {
      const habit = habits.find(h => h.id === habitId);
      if (!habit) return;
      const isCompleted = !!habit.completedToday;
      const updatedHabit = {
        ...habit,
        completedToday: !isCompleted,
        streakCount: !isCompleted ? (habit.streakCount || 0) + 1 : Math.max(0, (habit.streakCount || 1) - 1),
      };
      await updateHabit(updatedHabit);
    } catch (err) {
      console.error(err);
    }
  };

  return {
    habits,
    isLoading,
    error,
    addHabit,
    updateHabit,
    deleteHabit,
    toggleHabitComplete: toggleHabit,
    toggleHabit,
    refreshHabits: fetchHabits,
  };
};
