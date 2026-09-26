import { Goal } from '../types';
import { storage } from '../storage/asyncStorage';

const GOALS_KEY = '@livo_goals';

export const goalService = {
  async getGoals(): Promise<Goal[]> {
    const goals = await storage.getItem<Goal[]>(GOALS_KEY);
    return goals || [];
  },

  async saveGoal(goal: Omit<Goal, 'id' | 'createdAt'>): Promise<Goal> {
    const goals = await this.getGoals();
    
    const newGoal: Goal = {
      ...goal,
      id: Date.now().toString() + Math.random().toString(36).substring(2, 9),
      createdAt: new Date().toISOString(),
    };
    
    goals.push(newGoal);
    await storage.setItem(GOALS_KEY, goals);
    
    return newGoal;
  },

  async updateGoal(updatedGoal: Goal): Promise<Goal> {
    const goals = await this.getGoals();
    const index = goals.findIndex(g => g.id === updatedGoal.id);
    
    if (index !== -1) {
      goals[index] = updatedGoal;
      await storage.setItem(GOALS_KEY, goals);
    }
    
    return updatedGoal;
  },

  async deleteGoal(goalId: string): Promise<void> {
    const goals = await this.getGoals();
    const filteredGoals = goals.filter(g => g.id !== goalId);
    await storage.setItem(GOALS_KEY, filteredGoals);
  }
};
