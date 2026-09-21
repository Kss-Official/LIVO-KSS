export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface User {
  id: string;
  email: string;
  fullName: string;
  avatarUrl?: string;
}

export interface Task {
  id: string;
  title: string;
  description?: string;
  priority: Priority;
  status: TaskStatus;
  dueDate?: string;
  estimatedMinutes?: number;
  goalId?: string;
  createdAt: string;
}

export interface Goal {
  id: string;
  title: string;
  description?: string;
  targetDate: string;
  progressPercentage: number;
  status: 'ACTIVE' | 'COMPLETED' | 'BEHIND';
}

export interface Habit {
  id: string;
  title: string;
  frequency: 'DAILY' | 'WEEKLY';
  streakCount: number;
  completedToday: boolean;
}

export interface Expense {
  id: string;
  title: string;
  amount: number;
  category: string;
  date: string;
}

export interface AiInsight {
  id: string;
  title: string;
  summary: string;
  recommendation: string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
  createdAt: string;
}

export interface AiChatMessage {
  id: string;
  sender: 'USER' | 'LIVO_AI';
  text: string;
  timestamp: string;
  suggestedAction?: {
    type: 'MOVE_TASK' | 'SCHEDULE_RETRY' | 'CREATE_EVENT';
    description: string;
    payload: Record<string, any>;
  };
}
