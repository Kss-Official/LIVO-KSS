import { Task } from '../types';
import { storage } from '../storage/asyncStorage';
import { apiClient } from './api';

const TASKS_KEY = '@livo_tasks';

const mapBackendTask = (t: any): Task => ({
  id: String(t.id),
  title: t.title,
  description: t.description || '',
  date: t.dueDate || '',
  time: t.dueTime || '',
  dueDate: t.dueDate || '',
  priority: (t.priority ? t.priority.toUpperCase() : 'MEDIUM') as any,
  status: t.status || 'PENDING',
  completed: t.status === 'COMPLETED',
  category: t.category || 'WORK',
  goalId: t.goalId ? String(t.goalId) : undefined,
  estimatedMinutes: t.durationMins || 30,
  subtasks: Array.isArray(t.subtasks)
    ? t.subtasks.map((s: any) => (typeof s === 'string' ? s : s.title || ''))
    : [],
  repeat: t.repeatRule || 'Does not repeat',
  reminder: t.reminderMinutesBefore ? `${t.reminderMinutesBefore} Min` : undefined,
  createdAt: t.createdAt || new Date().toISOString(),
});

export const taskService = {
  async getTasks(): Promise<Task[]> {
    try {
      const response = await apiClient.get<{ data: any[] }>('/tasks');
      const backendTasks = response.data?.data;
      if (Array.isArray(backendTasks)) {
        const mapped = backendTasks.map(mapBackendTask);
        await storage.setItem(TASKS_KEY, mapped);
        return mapped;
      }
    } catch (err) {
      // Offline fallback: load from local storage
      console.warn('Backend /tasks fetch failed, loading from local cache:', err);
    }
    const cached = await storage.getItem<Task[]>(TASKS_KEY);
    return cached || [];
  },

  async saveTask(task: Omit<Task, 'id' | 'createdAt' | 'status' | 'completed'>): Promise<Task> {
    const tasks = await this.getTasks();
    const tempId = Date.now().toString() + Math.random().toString(36).substring(2, 9);
    
    const newTask: Task = {
      ...task,
      id: tempId,
      createdAt: new Date().toISOString(),
      status: 'PENDING',
      completed: false,
    };
    
    // Save to local cache first (zero-lag optimistic UI)
    tasks.push(newTask);
    await storage.setItem(TASKS_KEY, tasks);

    // Sync with backend
    try {
      const payload: any = {
        title: task.title,
        description: task.description || '',
        dueDate: task.dueDate || task.date || new Date().toISOString().split('T')[0],
        dueTime: task.time && task.time.length === 5 ? `${task.time}:00` : task.time,
        priority: (task.priority ? task.priority.toUpperCase() : 'MEDIUM'),
        category: task.category || 'WORK',
        durationMins: task.estimatedMinutes || 30,
      };

      if (task.goalId) {
        payload.goalId = task.goalId;
      }

      const response = await apiClient.post<{ data: any }>('/tasks', payload);
      const serverTask = response.data?.data;
      if (serverTask?.id) {
        newTask.id = String(serverTask.id);
        const updatedList = tasks.map(t => (t.id === tempId ? newTask : t));
        await storage.setItem(TASKS_KEY, updatedList);
      }
    } catch (err) {
      console.warn('Failed to sync new task to backend, retained in offline cache:', err);
    }
    
    return newTask;
  },

  async updateTask(updatedTask: Task): Promise<Task> {
    const tasks = await this.getTasks();
    const index = tasks.findIndex(t => t.id === updatedTask.id);
    
    if (index !== -1) {
      tasks[index] = updatedTask;
      await storage.setItem(TASKS_KEY, tasks);
    }

    try {
      await apiClient.put(`/tasks/${updatedTask.id}`, {
        title: updatedTask.title,
        description: updatedTask.description,
        dueDate: updatedTask.dueDate || updatedTask.date,
        dueTime: updatedTask.time && updatedTask.time.length === 5 ? `${updatedTask.time}:00` : updatedTask.time,
        priority: updatedTask.priority?.toUpperCase(),
        category: updatedTask.category,
        status: updatedTask.status || (updatedTask.completed ? 'COMPLETED' : 'PENDING'),
      });
    } catch (err) {
      console.warn('Failed to sync task update to backend:', err);
    }
    
    return updatedTask;
  },

  async deleteTask(taskId: string): Promise<void> {
    const tasks = await this.getTasks();
    const filteredTasks = tasks.filter(t => t.id !== taskId);
    await storage.setItem(TASKS_KEY, filteredTasks);

    try {
      await apiClient.delete(`/tasks/${taskId}`);
    } catch (err) {
      console.warn('Failed to sync task deletion to backend:', err);
    }
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

      try {
        await apiClient.put(`/tasks/${taskId}`, {
          title: task.title,
          status: task.status,
        });
      } catch (err) {
        console.warn('Failed to sync toggle completion to backend:', err);
      }

      return task;
    }
    
    return null;
  }
};
