import { Goal } from '../types';
import { storage } from '../storage/asyncStorage';
import { apiClient } from './api';

const GOALS_KEY = '@livo_goals';

const mapBackendGoal = (g: any): Goal => ({
  id: String(g.id),
  title: g.title,
  description: g.description || '',
  targetDate: g.targetDate || '',
  progressPercentage: typeof g.progressPercentage === 'number' ? Math.round(g.progressPercentage) : 0,
  status: g.status === 'COMPLETED' ? 'COMPLETED' : (g.status === 'CANCELLED' ? 'BEHIND' : 'ACTIVE'),
  createdAt: g.createdAt || new Date().toISOString(),
});

export const goalService = {
  async getGoals(): Promise<Goal[]> {
    try {
      const response = await apiClient.get<{ data: any[] }>('/goals');
      const backendGoals = response.data?.data;
      if (Array.isArray(backendGoals)) {
        const mapped = backendGoals.map(mapBackendGoal);
        await storage.setItem(GOALS_KEY, mapped);
        return mapped;
      }
    } catch (err) {
      console.warn('Backend /goals fetch failed, loading from local cache:', err);
    }
    const cached = await storage.getItem<Goal[]>(GOALS_KEY);
    return cached || [];
  },

  async saveGoal(goal: Omit<Goal, 'id' | 'createdAt'>): Promise<Goal> {
    const goals = await this.getGoals();
    const tempId = Date.now().toString() + Math.random().toString(36).substring(2, 9);
    
    const newGoal: Goal = {
      ...goal,
      id: tempId,
      createdAt: new Date().toISOString(),
    };
    
    goals.push(newGoal);
    await storage.setItem(GOALS_KEY, goals);

    try {
      const payload = {
        title: goal.title,
        description: goal.description || '',
        category: 'PERSONAL',
        targetDate: goal.targetDate || new Date().toISOString().split('T')[0],
        targetValue: 100.0,
        currentValue: goal.progressPercentage || 0.0,
      };

      const response = await apiClient.post<{ data: any }>('/goals', payload);
      const serverGoal = response.data?.data;
      if (serverGoal?.id) {
        newGoal.id = String(serverGoal.id);
        const updatedList = goals.map(g => (g.id === tempId ? newGoal : g));
        await storage.setItem(GOALS_KEY, updatedList);
      }
    } catch (err) {
      console.warn('Failed to sync new goal to backend, kept in offline cache:', err);
    }
    
    return newGoal;
  },

  async updateGoal(updatedGoal: Goal): Promise<Goal> {
    const goals = await this.getGoals();
    const index = goals.findIndex(g => g.id === updatedGoal.id);
    
    if (index !== -1) {
      goals[index] = updatedGoal;
      await storage.setItem(GOALS_KEY, goals);
    }

    try {
      await apiClient.put(`/goals/${updatedGoal.id}`, {
        title: updatedGoal.title,
        description: updatedGoal.description,
        targetDate: updatedGoal.targetDate,
        currentValue: updatedGoal.progressPercentage,
      });
    } catch (err) {
      console.warn('Failed to sync goal update to backend:', err);
    }
    
    return updatedGoal;
  },

  async deleteGoal(goalId: string): Promise<void> {
    const goals = await this.getGoals();
    const filteredGoals = goals.filter(g => g.id !== goalId);
    await storage.setItem(GOALS_KEY, filteredGoals);

    try {
      await apiClient.delete(`/goals/${goalId}`);
    } catch (err) {
      console.warn('Failed to sync goal deletion to backend:', err);
    }
  }
};
