import { Feather } from '@expo/vector-icons';
import { SelectionOption } from '../components/forms/SelectionModal';

export interface ItemConfig {
  name: string;
  icon: keyof typeof Feather.glyphMap;
  color: string;
  bg: string;
}

export const getCategoryConfig = (categoryName?: string): ItemConfig => {
  if (!categoryName) {
    return { name: 'Work', icon: 'briefcase', color: '#16A34A', bg: '#DCFCE7' };
  }
  const lower = categoryName.trim().toLowerCase();

  if (lower.includes('work')) {
    return { name: categoryName, icon: 'briefcase', color: '#16A34A', bg: '#E2F7C5' };
  }
  if (lower.includes('personal')) {
    return { name: categoryName, icon: 'user', color: '#7C3AED', bg: '#F3E8FF' };
  }
  if (lower.includes('health') || lower.includes('fitness') || lower.includes('med')) {
    return { name: categoryName, icon: 'heart', color: '#E11D48', bg: '#FFE4E6' };
  }
  if (lower.includes('learn') || lower.includes('study') || lower.includes('read')) {
    return { name: categoryName, icon: 'book-open', color: '#2563EB', bg: '#DBEAFE' };
  }
  if (lower.includes('finance') || lower.includes('money') || lower.includes('budget')) {
    return { name: categoryName, icon: 'dollar-sign', color: '#D97706', bg: '#FEF3C7' };
  }
  if (lower.includes('trip') || lower.includes('travel') || lower.includes('flight')) {
    return { name: categoryName, icon: 'map-pin', color: '#0D9488', bg: '#CCFBF1' };
  }
  if (lower.includes('food') || lower.includes('dining') || lower.includes('cafe')) {
    return { name: categoryName, icon: 'coffee', color: '#EA580C', bg: '#FFEDD5' };
  }
  if (lower.includes('shop')) {
    return { name: categoryName, icon: 'shopping-bag', color: '#9333EA', bg: '#FAF5FF' };
  }
  if (lower.includes('bill') || lower.includes('utilities')) {
    return { name: categoryName, icon: 'file-text', color: '#DC2626', bg: '#FEF2F2' };
  }
  if (lower.includes('transport') || lower.includes('commute')) {
    return { name: categoryName, icon: 'navigation', color: '#0284C7', bg: '#E0F2FE' };
  }
  if (lower.includes('entertain') || lower.includes('movie')) {
    return { name: categoryName, icon: 'tv', color: '#D97706', bg: '#FEF3C7' };
  }

  return { name: categoryName, icon: 'tag', color: '#64748B', bg: '#F1F5F9' };
};

export const getGoalConfig = (goalName?: string): ItemConfig => {
  if (!goalName || goalName.toLowerCase() === 'none') {
    return { name: goalName || 'None', icon: 'slash', color: '#94A3B8', bg: '#F1F5F9' };
  }
  const lower = goalName.trim().toLowerCase();

  if (lower.includes('portfolio') || lower.includes('career') || lower.includes('project')) {
    return { name: goalName, icon: 'award', color: '#7C3AED', bg: '#F3E8FF' };
  }
  if (lower.includes('react') || lower.includes('code') || lower.includes('program') || lower.includes('develop')) {
    return { name: goalName, icon: 'code', color: '#2563EB', bg: '#DBEAFE' };
  }
  if (lower.includes('health') || lower.includes('fitness') || lower.includes('gym') || lower.includes('run')) {
    return { name: goalName, icon: 'activity', color: '#E11D48', bg: '#FFE4E6' };
  }
  if (lower.includes('money') || lower.includes('save') || lower.includes('wealth') || lower.includes('invest')) {
    return { name: goalName, icon: 'trending-up', color: '#16A34A', bg: '#DCFCE7' };
  }
  if (lower.includes('travel') || lower.includes('trip') || lower.includes('visit') || lower.includes('vacation')) {
    return { name: goalName, icon: 'globe', color: '#0D9488', bg: '#CCFBF1' };
  }
  if (lower.includes('book') || lower.includes('read') || lower.includes('learn')) {
    return { name: goalName, icon: 'book-open', color: '#D97706', bg: '#FEF3C7' };
  }
  if (lower.includes('event')) {
    return { name: goalName, icon: 'calendar', color: '#2563EB', bg: '#EFF6FF' };
  }

  return { name: goalName, icon: 'target', color: '#7C3AED', bg: '#F3E8FF' };
};

export const DEFAULT_CATEGORY_OPTIONS: SelectionOption[] = [
  { label: 'Work', value: 'Work', icon: 'briefcase', iconColor: '#16A34A', iconBgColor: '#E2F7C5' },
  { label: 'Personal', value: 'Personal', icon: 'user', iconColor: '#7C3AED', iconBgColor: '#F3E8FF' },
  { label: 'Health', value: 'Health', icon: 'heart', iconColor: '#E11D48', iconBgColor: '#FFE4E6' },
  { label: 'Learning', value: 'Learning', icon: 'book-open', iconColor: '#2563EB', iconBgColor: '#DBEAFE' },
  { label: 'Finance', value: 'Finance', icon: 'dollar-sign', iconColor: '#D97706', iconBgColor: '#FEF3C7' },
  { label: 'Travel', value: 'Travel', icon: 'map-pin', iconColor: '#0D9488', iconBgColor: '#CCFBF1' },
];

export const DEFAULT_GOAL_OPTIONS: SelectionOption[] = [
  { label: 'Build a strong portfolio', value: 'Build a strong portfolio', icon: 'award', iconColor: '#7C3AED', iconBgColor: '#F3E8FF' },
  { label: 'Learn React Native', value: 'Learn React Native', icon: 'code', iconColor: '#2563EB', iconBgColor: '#DBEAFE' },
  { label: 'Improve fitness & health', value: 'Improve fitness & health', icon: 'activity', iconColor: '#E11D48', iconBgColor: '#FFE4E6' },
  { label: 'Save $10,000 this year', value: 'Save $10,000 this year', icon: 'trending-up', iconColor: '#16A34A', iconBgColor: '#DCFCE7' },
  { label: 'None', value: 'None', icon: 'slash', iconColor: '#94A3B8', iconBgColor: '#F1F5F9' },
];
