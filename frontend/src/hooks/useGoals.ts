import { useState, useEffect, useCallback } from 'react';
import { Goal } from '../types';
import { goalService } from '../services/goalService';

export const useGoals = () => {
  const [goals, setGoals] = useState<Goal[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchGoals = useCallback(async () => {
    setIsLoading(true);
    try {
      const fetchedGoals = await goalService.getGoals();
      setGoals(fetchedGoals);
      setError(null);
    } catch (err) {
      setError('Failed to load goals');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchGoals();
  }, [fetchGoals]);

  const addGoal = async (goalData: Omit<Goal, 'id' | 'createdAt'>) => {
    try {
      const newGoal = await goalService.saveGoal(goalData);
      setGoals(prev => [newGoal, ...prev]);
      return newGoal;
    } catch (err) {
      setError('Failed to add goal');
      console.error(err);
      throw err;
    }
  };

  const updateGoal = async (goalData: Goal) => {
    try {
      const updatedGoal = await goalService.updateGoal(goalData);
      setGoals(prev => prev.map(g => g.id === updatedGoal.id ? updatedGoal : g));
      return updatedGoal;
    } catch (err) {
      setError('Failed to update goal');
      console.error(err);
      throw err;
    }
  };

  const deleteGoal = async (goalId: string) => {
    try {
      await goalService.deleteGoal(goalId);
      setGoals(prev => prev.filter(g => g.id !== goalId));
    } catch (err) {
      setError('Failed to delete goal');
      console.error(err);
      throw err;
    }
  };

  return {
    goals,
    isLoading,
    error,
    addGoal,
    updateGoal,
    deleteGoal,
    refreshGoals: fetchGoals,
  };
};
