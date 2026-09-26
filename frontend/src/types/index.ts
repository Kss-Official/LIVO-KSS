export type Priority = 'Low' | 'Medium' | 'High' | 'Urgent' | 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
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
  date?: string;
  time?: string;
  priority: Priority;
  status: TaskStatus;
  category?: string;
  goal?: string;
  reminder?: string;
  repeat?: string;
  subtasks?: string[];
  dueDate?: string;
  duration?: string;
  location?: string;
  meetingType?: 'in_person' | 'online' | 'phone' | string;
  attachments?: any[];
  estimatedMinutes?: number;
  goalId?: string;
  createdAt: string;
  completed: boolean;
}

export interface Event {
  id: string;
  title: string;
  description?: string;
  date?: string;
  startTime?: string;
  endTime?: string;
  location?: string;
  category?: string;
  createdAt: string;
}

export interface Goal {
  id: string;
  title: string;
  description?: string;
  targetDate: string;
  progressPercentage: number;
  status: 'ACTIVE' | 'COMPLETED' | 'BEHIND';
  createdAt: string;
}

export interface Habit {
  id: string;
  title: string;
  description?: string;
  frequency: 'DAILY' | 'WEEKLY' | string;
  color?: string;
  category?: string;
  streakCount: number;
  completedToday: boolean;
  createdAt: string;
  time?: string;
  reminderTime?: string;
  date?: string;
}

export interface Expense {
  id: string;
  title: string;
  amount: number;
  category: string;
  date: string;
  createdAt: string;
}

export interface Trip {
  id: string;
  title: string;
  destination: string;
  startDate: string;
  endDate: string;
  createdAt: string;
}

export interface Learning {
  id: string;
  title: string;
  topic: string;
  duration?: string;
  createdAt: string;
}

export interface Health {
  id: string;
  title: string;
  activityType: string;
  duration?: string;
  createdAt: string;
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
    type: 'MOVE_TASK' | 'SCHEDULE_RETRY' | 'CREATE_EVENT' | 'CREATE_TASK' | string;
    description: string;
    payload: Record<string, any>;
  };
}
