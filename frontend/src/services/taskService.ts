import { Task } from '../types';
import { storage } from '../storage/asyncStorage';

const TASKS_KEY = '@livo_tasks';

export const taskService = {
  async getTasks(): Promise<Task[]> {
    const tasks = await storage.getItem<Task[]>(TASKS_KEY);
    return tasks || [];
  },

  async saveTask(task: Omit<Task, 'id' | 'createdAt' | 'status' | 'completed'>): Promise<Task> {
    const tasks = await this.getTasks();
    
    const newTask: Task = {
      ...task,
      id: Date.now().toString() + Math.random().toString(36).substring(2, 9),
      createdAt: new Date().toISOString(),
      status: 'PENDING',
      completed: false,
    };
    
    tasks.push(newTask);
    await storage.setItem(TASKS_KEY, tasks);
    
    return newTask;
  },

  async updateTask(updatedTask: Task): Promise<Task> {
    const tasks = await this.getTasks();
    const index = tasks.findIndex(t => t.id === updatedTask.id);
    
    if (index !== -1) {
      tasks[index] = updatedTask;
      await storage.setItem(TASKS_KEY, tasks);
    }
    
    return updatedTask;
  },

  async deleteTask(taskId: string): Promise<void> {
    const tasks = await this.getTasks();
    const filteredTasks = tasks.filter(t => t.id !== taskId);
    await storage.setItem(TASKS_KEY, filteredTasks);
  },
  
  async toggleTaskCompletion(taskId: string): Promise<Task | null> {
    const tasks = await this.getTasks();
    const index = tasks.findIndex(t => t.id === taskId);
    
    if (index !== -1) {
      const task = tasks[index];
      task.completed = !task.completed;
      task.status = task.completed ? 'COMPLETED' : 'PENDING';
      tasks[index] = task;
      await storage.setItem(TASKS_KEY, tasks);
      return task;
    }
    
    return null;
  }
};
