import axios from 'axios';

export const API_BASE_URL = 'http://10.0.2.2:8080/api/v1'; // Standard Android emulator localhost mapped URL

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

let authToken: string | null = null;

export const setAuthToken = (token: string | null) => {
  authToken = token;
  if (token) {
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  } else {
    delete apiClient.defaults.headers.common['Authorization'];
  }
};

// Mock data fallbacks for initial offline / demo state
export const MOCK_TASKS = [
  { id: '1', title: 'Finish LIVO AI Engine setup', priority: 'HIGH', status: 'IN_PROGRESS', dueDate: '2026-09-19', estimatedMinutes: 60, createdAt: '2026-09-18' },
  { id: '2', title: 'Review weekly expense breakdown', priority: 'MEDIUM', status: 'PENDING', dueDate: '2026-09-20', estimatedMinutes: 30, createdAt: '2026-09-18' },
  { id: '3', title: 'Morning 20-min workout', priority: 'HIGH', status: 'COMPLETED', dueDate: '2026-09-18', estimatedMinutes: 20, createdAt: '2026-09-18' },
];

export const MOCK_GOALS = [
  { id: 'g1', title: 'Launch LIVO Life OS v1.0', targetDate: '2026-10-15', progressPercentage: 75, status: 'ACTIVE' },
  { id: 'g2', title: 'Maintain 90% Habit Streak', targetDate: '2026-12-31', progressPercentage: 88, status: 'ACTIVE' },
];

export const MOCK_INSIGHTS = [
  {
    id: 'ins1',
    title: 'Workload Concentration Alert',
    summary: 'You have 4 high-priority tasks scheduled for Friday morning.',
    recommendation: 'Move 2 tasks to Thursday afternoon to prevent bottleneck.',
    priority: 'HIGH',
    createdAt: '2026-09-18T10:00:00Z',
  },
];
