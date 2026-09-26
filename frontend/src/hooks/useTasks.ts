import { useState, useEffect, useCallback } from 'react';
import { Task } from '../types';
import { taskService } from '../services/taskService';

export const useTasks = () => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTasks = useCallback(async () => {
    setIsLoading(true);
    try {
      const fetchedTasks = await taskService.getTasks();
      // Sort tasks: pending first, then by creation date (newest first)
      const sorted = fetchedTasks.sort((a, b) => {
        if (a.completed === b.completed) {
          return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
        }
        return a.completed ? 1 : -1;
      });
      setTasks(sorted);
      setError(null);
    } catch (err) {
      setError('Failed to load tasks');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTasks();
  }, [fetchTasks]);

  const addTask = async (taskData: Omit<Task, 'id' | 'createdAt' | 'status' | 'completed'>) => {
    try {
      const newTask = await taskService.saveTask(taskData);
      setTasks(prev => [newTask, ...prev].sort((a, b) => {
        if (a.completed === b.completed) {
          return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
        }
        return a.completed ? 1 : -1;
      }));
      return newTask;
    } catch (err) {
      setError('Failed to add task');
      console.error(err);
      throw err;
    }
  };

  const updateTask = async (taskData: Task) => {
    try {
      const updatedTask = await taskService.updateTask(taskData);
      setTasks(prev => prev.map(t => t.id === updatedTask.id ? updatedTask : t));
      return updatedTask;
    } catch (err) {
      setError('Failed to update task');
      console.error(err);
      throw err;
    }
  };

  const deleteTask = async (taskId: string) => {
    try {
      await taskService.deleteTask(taskId);
      setTasks(prev => prev.filter(t => t.id !== taskId));
    } catch (err) {
      setError('Failed to delete task');
      console.error(err);
      throw err;
    }
  };

  const toggleTask = async (taskId: string) => {
    try {
      // Optimistic update
      setTasks(prev => {
        const newTasks = prev.map(t => {
          if (t.id === taskId) {
            return { ...t, completed: !t.completed, status: (!t.completed ? 'COMPLETED' : 'PENDING') as import('../types').TaskStatus };
          }
          return t;
        });
        
        return newTasks.sort((a, b) => {
          if (a.completed === b.completed) {
            return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
          }
          return a.completed ? 1 : -1;
        });
      });
      
      await taskService.toggleTaskCompletion(taskId);
    } catch (err) {
      setError('Failed to toggle task');
      console.error(err);
      // Revert optimistic update by refetching
      fetchTasks();
      throw err;
    }
  };

  return {
    tasks,
    isLoading,
    error,
    addTask,
    updateTask,
    deleteTask,
    toggleTask,
    refreshTasks: fetchTasks,
  };
};
