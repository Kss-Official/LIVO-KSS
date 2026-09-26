import axios, { AxiosInstance } from 'axios';
import { Platform } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';

// In Android Emulator, localhost maps to 10.0.2.2. In iOS / Web, localhost maps to localhost.
export const API_BASE_URL = Platform.OS === 'android'
  ? 'http://10.0.2.2:8081/api/v1'
  : 'http://localhost:8081/api/v1';

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
  timeout: 10000,
});

let authToken: string | null = null;
const AUTH_TOKEN_KEY = '@livo_auth_token';

// Load stored token on startup
export const initAuthToken = async (): Promise<string | null> => {
  try {
    const token = await AsyncStorage.getItem(AUTH_TOKEN_KEY);
    if (token) {
      authToken = token;
      apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    }
    return token;
  } catch {
    return null;
  }
};

export const setAuthToken = async (token: string | null): Promise<void> => {
  authToken = token;
  if (token) {
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    try {
      await AsyncStorage.setItem(AUTH_TOKEN_KEY, token);
    } catch {}
  } else {
    delete apiClient.defaults.headers.common['Authorization'];
    try {
      await AsyncStorage.removeItem(AUTH_TOKEN_KEY);
    } catch {}
  }
};

export const getAuthToken = (): string | null => authToken;

// Interceptor to ensure Authorization header is always up-to-date
apiClient.interceptors.request.use(async (config) => {
  if (!authToken) {
    try {
      const storedToken = await AsyncStorage.getItem(AUTH_TOKEN_KEY);
      if (storedToken) {
        authToken = storedToken;
        config.headers.Authorization = `Bearer ${storedToken}`;
      }
    } catch {}
  } else {
    config.headers.Authorization = `Bearer ${authToken}`;
  }
  return config;
}, (error) => Promise.reject(error));

// Initialize token on import
initAuthToken();

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
