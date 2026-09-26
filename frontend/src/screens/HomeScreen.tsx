import React, { useState, useEffect, useMemo, useCallback } from 'react';
import {
  View,
  Text,
  Image,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  Platform,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { Colors } from '../theme/colors';
import { useTasks } from '../hooks/useTasks';
import { useEvents } from '../hooks/useEvents';
import { useHabits } from '../hooks/useHabits';
import { useGoals } from '../hooks/useGoals';

import { useFocusEffect } from '@react-navigation/native';
import { useProfile } from '../hooks/useProfile';

interface HomeScreenProps {
  onNavigateTab?: (tabName: string) => void;
}

// Helper to determine if a date represents today
const isDateToday = (dateInput?: string | Date | null): boolean => {
  if (!dateInput) return false;
  try {
    const now = new Date();
    const nowY = now.getFullYear();
    const nowM = now.getMonth();
    const nowD = now.getDate();

    if (dateInput instanceof Date) {
      if (isNaN(dateInput.getTime())) return false;
      return (
        dateInput.getFullYear() === nowY &&
        dateInput.getMonth() === nowM &&
        dateInput.getDate() === nowD
      );
    }

    const str = String(dateInput).trim();
    if (!str) return false;
    const lower = str.toLowerCase();
    if (lower.startsWith('today')) return true;
    if (lower.startsWith('tomorrow') || lower.startsWith('yesterday')) return false;

    const match = str.match(/^(\d{4})[-/](\d{1,2})[-/](\d{1,2})/);
    if (match) {
      const y = parseInt(match[1], 10);
      const m = parseInt(match[2], 10) - 1;
      const d = parseInt(match[3], 10);
      return y === nowY && m === nowM && d === nowD;
    }

    const parsed = new Date(str);
    if (isNaN(parsed.getTime())) return false;
    return (
      parsed.getFullYear() === nowY &&
      parsed.getMonth() === nowM &&
      parsed.getDate() === nowD
    );
  } catch {
    return false;
  }
};

interface ParsedScheduleTime {
  hours: number;
  minutes: number;
  formatted: string;
  totalMinutes: number;
}

const parseScheduleTime = (timeInput?: string | Date | null): ParsedScheduleTime | null => {
  if (!timeInput) return null;
  try {
    if (timeInput instanceof Date) {
      if (isNaN(timeInput.getTime())) return null;
      const hours = timeInput.getHours();
      const minutes = timeInput.getMinutes();
      const ampm = hours >= 12 ? 'PM' : 'AM';
      const displayHours = hours % 12 || 12;
      const formatted = `${displayHours}:${String(minutes).padStart(2, '0')} ${ampm}`;
      return { hours, minutes, formatted, totalMinutes: hours * 60 + minutes };
    }

    const str = String(timeInput).trim();
    if (!str) return null;

    if (str.includes('T') || str.includes('Z')) {
      const d = new Date(str);
      if (!isNaN(d.getTime())) {
        const hours = d.getHours();
        const minutes = d.getMinutes();
        const ampm = hours >= 12 ? 'PM' : 'AM';
        const displayHours = hours % 12 || 12;
        const formatted = `${displayHours}:${String(minutes).padStart(2, '0')} ${ampm}`;
        return { hours, minutes, formatted, totalMinutes: hours * 60 + minutes };
      }
    }

    const match = str.match(/(\d{1,2}):(\d{2})(?::\d{2})?\s*(AM|PM)?/i);
    if (match) {
      let hours = parseInt(match[1], 10);
      const minutes = parseInt(match[2], 10);
      const ampm = match[3]?.toUpperCase();

      if (ampm) {
        if (ampm === 'PM' && hours < 12) hours += 12;
        if (ampm === 'AM' && hours === 12) hours = 0;
      }
      const displayAmpm = hours >= 12 ? 'PM' : 'AM';
      const displayHours = hours % 12 || 12;
      const formatted = `${displayHours}:${String(minutes).padStart(2, '0')} ${displayAmpm}`;
      return { hours, minutes, formatted, totalMinutes: hours * 60 + minutes };
    }

    return null;
  } catch {
    return null;
  }
};

const parseDurationMinutes = (dur?: string | number | null): number => {
  if (!dur) return 45;
  if (typeof dur === 'number') return dur > 0 ? dur : 45;
  const lower = String(dur).toLowerCase().trim();
  const minMatch = lower.match(/(\d+)\s*(?:min|m)/);
  if (minMatch) return parseInt(minMatch[1], 10);
  const hrMatch = lower.match(/(\d+(?:\.\d+)?)\s*(?:hour|hr|h)/);
  if (hrMatch) return Math.round(parseFloat(hrMatch[1]) * 60);
  return 45;
};

interface ScheduleDisplayItem {
  id: string;
  sourceType: 'task' | 'event' | 'habit';
  title: string;
  subtitle: string;
  timeFormatted: string;
  startMinutes: number;
  endMinutes: number;
  isNow: boolean;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({ onNavigateTab }) => {
  const navigation = useNavigation<any>();
  const { profile, refreshProfile } = useProfile();
  const { tasks, toggleTask, refreshTasks } = useTasks();
  const { events, refreshEvents } = useEvents();
  const { habits, refreshHabits } = useHabits();
  const { goals, refreshGoals } = useGoals();

  const [currentTime, setCurrentTime] = useState<Date>(() => new Date());

  // Periodically update current time every 30 seconds
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 30000);
    return () => clearInterval(timer);
  }, []);

  useFocusEffect(
    useCallback(() => {
      refreshProfile?.();
      refreshTasks?.();
      refreshEvents?.();
      refreshHabits?.();
      refreshGoals?.();
      setCurrentTime(new Date());
    }, [refreshProfile, refreshTasks, refreshEvents, refreshHabits, refreshGoals])
  );

  const formattedCurrentTime = useMemo(() => {
    const hours = currentTime.getHours();
    const minutes = currentTime.getMinutes();
    const ampm = hours >= 12 ? 'PM' : 'AM';
    const displayHours = hours % 12 || 12;
    return `${String(displayHours).padStart(2, '0')}:${String(minutes).padStart(2, '0')} ${ampm}`;
  }, [currentTime]);

  // Convert real Tasks, Events, and Habits into unified Today's Schedule items
  const todayScheduleItems = useMemo<ScheduleDisplayItem[]>(() => {
    const rawList: Array<{
      id: string;
      sourceType: 'task' | 'event' | 'habit';
      title: string;
      subtitle: string;
      timeFormatted: string;
      startMinutes: number;
      endMinutes: number;
    }> = [];

    // 1. Tasks scheduled for today with a time (not completed or cancelled)
    tasks.forEach((t) => {
      if (t.completed || t.status === 'COMPLETED' || t.status === 'CANCELLED') return;
      if (!t.time) return;

      const belongsToToday =
        isDateToday(t.date) ||
        isDateToday(t.dueDate) ||
        (!t.date && !t.dueDate && isDateToday(t.time)) ||
        (!t.date && !t.dueDate && !t.time ? false : !t.date && !t.dueDate && String(t.time).toLowerCase().includes('today'));

      if (!belongsToToday) return;

      const parsedTime = parseScheduleTime(t.time);
      if (!parsedTime) return;

      const durMins = parseDurationMinutes(t.duration || t.estimatedMinutes);
      const subtitle = t.description?.trim() || t.category?.trim() || 'Task';

      rawList.push({
        id: `task-${t.id}`,
        sourceType: 'task',
        title: t.title,
        subtitle,
        timeFormatted: parsedTime.formatted,
        startMinutes: parsedTime.totalMinutes,
        endMinutes: parsedTime.totalMinutes + durMins,
      });
    });

    // 2. Events scheduled for today with a time
    events.forEach((e) => {
      const belongsToToday = isDateToday(e.date) || isDateToday(e.startTime);
      if (!belongsToToday) return;

      const parsedTime = parseScheduleTime(e.startTime || e.date);
      if (!parsedTime) return;

      let endMinutes = parsedTime.totalMinutes + 60;
      if (e.endTime) {
        const parsedEndTime = parseScheduleTime(e.endTime);
        if (parsedEndTime && parsedEndTime.totalMinutes > parsedTime.totalMinutes) {
          endMinutes = parsedEndTime.totalMinutes;
        }
      }

      const subtitle = e.location?.trim() || e.description?.trim() || e.category?.trim() || 'Event';

      rawList.push({
        id: `event-${e.id}`,
        sourceType: 'event',
        title: e.title,
        subtitle,
        timeFormatted: parsedTime.formatted,
        startMinutes: parsedTime.totalMinutes,
        endMinutes,
      });
    });

    // 3. Habits scheduled for today with a time (not completed today)
    habits.forEach((h) => {
      if (h.completedToday) return;

      const habitTime = h.time || h.reminderTime;
      if (!habitTime) return;

      const parsedTime = parseScheduleTime(habitTime);
      if (!parsedTime) return;

      const isDaily = !h.frequency || h.frequency.toUpperCase() === 'DAILY';
      const habitDate = h.date;
      const belongsToToday = isDaily || (habitDate ? isDateToday(habitDate) : true);
      if (!belongsToToday) return;

      const subtitle = h.description?.trim() || h.category?.trim() || 'Daily habit';

      rawList.push({
        id: `habit-${h.id}`,
        sourceType: 'habit',
        title: h.title,
        subtitle,
        timeFormatted: parsedTime.formatted,
        startMinutes: parsedTime.totalMinutes,
        endMinutes: parsedTime.totalMinutes + 30,
      });
    });

    // Chronologically sort all items by scheduled time
    rawList.sort((a, b) => a.startMinutes - b.startMinutes);

    // Current time in minutes from midnight
    const currentTotalMinutes = currentTime.getHours() * 60 + currentTime.getMinutes();

    // Determine currently active/ongoing item:
    // Item where currentTotalMinutes >= startMinutes && currentTotalMinutes < endMinutes
    let activeId: string | null = null;
    let latestActiveStart = -1;
    for (const item of rawList) {
      if (currentTotalMinutes >= item.startMinutes && currentTotalMinutes < item.endMinutes) {
        if (item.startMinutes > latestActiveStart) {
          latestActiveStart = item.startMinutes;
          activeId = item.id;
        }
      }
    }

    return rawList.map((item) => ({
      ...item,
      isNow: item.id === activeId,
    }));
  }, [tasks, events, habits, currentTime]);

  const [priorityIndex, setPriorityIndex] = useState(0);

  // Derive priority cards from actual tasks (not completed)
  const pendingTasks = tasks.filter(t => !t.completed).sort((a, b) => {
    // simple sort: High before Medium before Low
    const pWeight = { High: 3, Medium: 2, Low: 1 };
    return (pWeight[b.priority as keyof typeof pWeight] || 0) - (pWeight[a.priority as keyof typeof pWeight] || 0);
  });

  const formatISO = (isoStr?: string, mode: 'date' | 'time' = 'date') => {
    if (!isoStr) return '';
    try {
      const d = new Date(isoStr);
      if (isNaN(d.getTime())) return isoStr; // not a valid date, just return as-is
      if (mode === 'time') {
        return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      }
      return d.toLocaleDateString('en-US', { day: 'numeric', month: 'short', year: 'numeric' });
    } catch {
      return isoStr;
    }
  };

  const priorityCards = pendingTasks.map((t, idx) => {
    const nextTask = pendingTasks[idx + 1] || pendingTasks[0]; // circular or next
    const isHigh = t.priority === 'High' || t.priority === 'Urgent';
    const isMedium = t.priority === 'Medium';
    return {
      id: t.id,
      title: t.title,
      due: formatISO(t.date || t.dueDate, 'date') || 'Today',
      time: formatISO(t.time, 'time') || 'All Day',
      priority: t.priority,
      priorityColor: isHigh ? '#DC2626' : (isMedium ? '#D97706' : '#2563EB'),
      priorityBg: isHigh ? '#FFEBEB' : (isMedium ? '#FEF3C7' : '#DBEAFE'),
      reason: t.description || 'LIVO prioritizes this task based on your schedule and deadline.',
      nextUpTitle: nextTask ? nextTask.title : 'No upcoming tasks',
      nextUpDue: nextTask ? (formatISO(nextTask.date || nextTask.dueDate, 'date') || 'Today') : '',
      nextUpTime: nextTask ? (formatISO(nextTask.time, 'time') || 'All Day') : '',
      nextUpPriority: nextTask ? nextTask.priority : 'Low',
      nextUpColor: nextTask && (nextTask.priority === 'High' || nextTask.priority === 'Urgent') ? '#DC2626' : (nextTask && nextTask.priority === 'Medium' ? '#D97706' : '#2563EB'),
      nextUpBg: nextTask && (nextTask.priority === 'High' || nextTask.priority === 'Urgent') ? '#FFEBEB' : (nextTask && nextTask.priority === 'Medium' ? '#FEF3C7' : '#DBEAFE'),
    };
  });

  // Fallback if no tasks
  if (priorityCards.length === 0) {
    priorityCards.push({
      id: 'mock',
      title: 'No pending tasks!',
      due: 'Relax or plan ahead',
      time: '-',
      priority: 'Low',
      priorityColor: '#2563EB',
      priorityBg: '#DBEAFE',
      reason: 'You have cleared your queue. Enjoy your free time or tap "Create something".',
      nextUpTitle: '-',
      nextUpDue: '',
      nextUpTime: '',
      nextUpPriority: 'Low',
      nextUpColor: '#2563EB',
      nextUpBg: '#DBEAFE',
    });
  }

  const handleNextPriorityCard = () => {
    setPriorityIndex((prev) => (prev + 1) % priorityCards.length);
  };

  const handlePrevPriorityCard = () => {
    setPriorityIndex((prev) => (prev - 1 + priorityCards.length) % priorityCards.length);
  };


  const totalTasks = tasks.length;
  const completedTasks = tasks.filter(t => t.completed).length;
  const taskRatio = totalTasks === 0 ? 0 : completedTasks / totalTasks;

  const totalHabits = habits.length;
  const completedHabits = habits.filter(h => h.streakCount > 0).length;
  const habitRatio = totalHabits === 0 ? 0 : completedHabits / totalHabits;

  const totalGoals = goals.length;
  const activeGoals = goals.filter(g => g.progressPercentage && g.progressPercentage > 0).length;
  const goalRatio = totalGoals === 0 ? 0 : activeGoals / totalGoals;

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.contentContainer}
        showsVerticalScrollIndicator={false}
      >
        {/* 1. Header Row */}
        <View style={styles.headerRow}>
          <View>
            <Image
              source={require('../../assets/livo_logo.png')}
              style={styles.logoImage}
              resizeMode="contain"
            />
            <Text style={styles.logoSubtitle}>A BETTER YOU</Text>
          </View>

          <View style={styles.headerRightActions}>
            <TouchableOpacity style={styles.iconBtn} onPress={() => navigation.navigate('Search')} activeOpacity={0.7}>
              <Feather name="search" size={20} color="#0F172A" />
            </TouchableOpacity>

            <TouchableOpacity style={styles.iconBtn} onPress={() => navigation.navigate('Notifications')} activeOpacity={0.7}>
              <Feather name="bell" size={20} color="#0F172A" />
              <View style={styles.notificationBadge} />
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.avatarCircle}
              onPress={() => navigation.navigate('Profile')}
              activeOpacity={0.8}
            >
              <Text style={styles.avatarText}>{profile.name.charAt(0).toUpperCase()}</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* 2. Greeting & Date Bar */}
        <View style={styles.greetingSection}>
          <Text style={styles.greetingSub}>Good morning,</Text>
          <Text style={styles.userName}>{profile.name} 👋</Text>
          <Text style={styles.greetingSubText}>Let’s make it a productive day.</Text>
        </View>

        <View style={styles.dateBarRow}>
          <TouchableOpacity style={styles.datePickerPill} onPress={() => navigation.navigate('Calendar')}>
            <Feather name="calendar" size={14} color="#64748B" style={{ marginRight: 6 }} />
            <Text style={styles.datePickerText}>Today, Mon 2 Sep 2024</Text>
            <Feather name="chevron-down" size={14} color="#64748B" style={{ marginLeft: 6 }} />
          </TouchableOpacity>

          <TouchableOpacity style={styles.viewCalendarBtn} onPress={() => navigation.navigate('Calendar')}>
            <Text style={styles.viewCalendarText}>View Calendar</Text>
            <Feather name="chevron-right" size={14} color="#64748B" />
          </TouchableOpacity>
        </View>

        {/* 3. LIVO's Priority Card */}
        {(() => {
          const card = priorityCards[priorityIndex];
          return (
            <View style={styles.priorityCard}>
              {/* Top Header */}
              <View style={styles.priorityHeaderRow}>
                <View style={styles.priorityTitleWrap}>
                  <View style={styles.priorityIconBadge}>
                    <Ionicons name="sparkles" size={14} color="#2D6A00" />
                  </View>
                  <Text style={styles.priorityHeaderText}>LIVO’s Priority</Text>
                  <Feather name="info" size={13} color="#64748B" style={{ marginLeft: 4 }} />
                </View>

                <View style={styles.carouselCounterWrap}>
                  <Text style={styles.carouselCounterText}>{`${priorityIndex + 1} of ${priorityCards.length}`}</Text>
                  <TouchableOpacity style={styles.carouselArrowBtn} onPress={handlePrevPriorityCard} activeOpacity={0.7}>
                    <Feather name="chevron-left" size={12} color="#64748B" />
                  </TouchableOpacity>
                  <TouchableOpacity style={styles.carouselArrowBtn} onPress={handleNextPriorityCard} activeOpacity={0.7}>
                    <Feather name="chevron-right" size={12} color="#64748B" />
                  </TouchableOpacity>
                </View>
              </View>

              {/* Card Body - Dual Column */}
              <View style={styles.priorityBodyRow}>
                {/* Left Column (Active Task) */}
                <View style={styles.priorityLeftCol}>
                  <Text style={styles.taskTitle}>{card.title}</Text>

                  <View style={styles.taskMetaRow}>
                    <View style={styles.metaItem}>
                      <Feather name="calendar" size={12} color="#64748B" />
                      <Text style={styles.metaItemText}>{card.due}</Text>
                    </View>
                    <View style={styles.metaItem}>
                      <Feather name="clock" size={12} color="#64748B" />
                      <Text style={styles.metaItemText}>{card.time}</Text>
                    </View>
                  </View>

                  <View style={[styles.highPriorityPill, { backgroundColor: card.priorityBg }]}>
                    <Feather name="flag" size={10} color={card.priorityColor} style={{ marginRight: 4 }} />
                    <Text style={[styles.highPriorityText, { color: card.priorityColor }]}>{card.priority}</Text>
                  </View>

                  <Text style={styles.priorityReasonText}>{card.reason}</Text>

                  <TouchableOpacity style={styles.whyLivoLink} onPress={() => alert('AI Logic: This task is urgent and impacts your active goals.')}>
                    <Text style={styles.whyLivoText}>Why LIVO picked this?</Text>
                  </TouchableOpacity>

                  <View style={styles.priorityActionBtnsRow}>
                    <TouchableOpacity style={styles.startTaskBtn} onPress={() => navigation.navigate('AddTask')}>
                      <Text style={styles.startTaskBtnText}>Start Task →</Text>
                    </TouchableOpacity>

                    <TouchableOpacity style={styles.markDoneBtn} onPress={() => { if (card.id !== 'mock') toggleTask(card.id); }}>
                      <Feather name="check" size={14} color="#0F172A" style={{ marginRight: 4 }} />
                      <Text style={styles.markDoneBtnText}>Mark Done</Text>
                    </TouchableOpacity>
                  </View>
                </View>

                {/* Vertical Divider */}
                <View style={styles.priorityColDivider} />

                {/* Right Column (Next Up Preview) */}
                <View style={styles.priorityRightCol}>
                  <Text style={styles.nextUpLabel}>NEXT UP</Text>
                  <Text style={styles.nextUpTitle}>{card.nextUpTitle}</Text>

                  <View style={styles.nextUpMetaItem}>
                    <Feather name="calendar" size={11} color="#64748B" />
                    <Text style={styles.nextUpMetaText}>{card.nextUpDue}</Text>
                  </View>
                  <View style={styles.nextUpMetaItem}>
                    <Feather name="clock" size={11} color="#64748B" />
                    <Text style={styles.nextUpMetaText}>{card.nextUpTime}</Text>
                  </View>

                  <View style={[styles.mediumPriorityPill, { backgroundColor: card.nextUpBg }]}>
                    <Text style={[styles.mediumPriorityText, { color: card.nextUpColor }]}>{card.nextUpPriority}</Text>
                  </View>
                </View>
              </View>
            </View>
          );
        })()}

        {/* 4. Quick Add Bar ("Create something") */}
        <View style={styles.quickAddSection}>
          <View style={styles.sectionHeaderRow}>
            <View style={styles.createSomethingHeader}>
              <View style={styles.createPlusCircle}>
                <Feather name="plus" size={12} color="#FFFFFF" />
              </View>
              <Text style={styles.createSomethingTitle}>Create something</Text>
            </View>

            <Text style={styles.quickAddSubText}>Quick add to your life</Text>
          </View>

          <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.quickAddScroll}>
            {/* Task */}
            <TouchableOpacity
              style={[styles.quickAddCard, { backgroundColor: '#EBF9DB' }]}
              onPress={() => navigation.navigate('AddTask')}
            >
              <View style={[styles.quickAddIconWrap, { backgroundColor: 'rgba(102, 196, 0, 0.15)' }]}>
                <Feather name="check" size={16} color="#2D6A00" />
              </View>
              <Text style={[styles.quickAddCardText, { color: '#2D6A00' }]}>Task</Text>
            </TouchableOpacity>

            {/* Event */}
            <TouchableOpacity
              style={[styles.quickAddCard, { backgroundColor: '#EBF3FF' }]}
              onPress={() => navigation.navigate('AddEvent')}
            >
              <View style={[styles.quickAddIconWrap, { backgroundColor: 'rgba(29, 78, 216, 0.12)' }]}>
                <Feather name="calendar" size={16} color="#1D4ED8" />
              </View>
              <Text style={[styles.quickAddCardText, { color: '#1D4ED8' }]}>Event</Text>
            </TouchableOpacity>

            {/* Goal */}
            <TouchableOpacity
              style={[styles.quickAddCard, { backgroundColor: '#F3EBFB' }]}
              onPress={() => navigation.navigate('AddGoal')}
            >
              <View style={[styles.quickAddIconWrap, { backgroundColor: 'rgba(109, 40, 217, 0.12)' }]}>
                <Ionicons name="disc-outline" size={16} color="#6D28D9" />
              </View>
              <Text style={[styles.quickAddCardText, { color: '#6D28D9' }]}>Goal</Text>
            </TouchableOpacity>

            {/* Habit */}
            <TouchableOpacity
              style={[styles.quickAddCard, { backgroundColor: '#FFF3E5' }]}
              onPress={() => navigation.navigate('AddHabit')}
            >
              <View style={[styles.quickAddIconWrap, { backgroundColor: 'rgba(194, 65, 12, 0.12)' }]}>
                <Ionicons name="stats-chart-outline" size={16} color="#C2410C" />
              </View>
              <Text style={[styles.quickAddCardText, { color: '#C2410C' }]}>Habit</Text>
            </TouchableOpacity>

            {/* Expense */}
            <TouchableOpacity
              style={[styles.quickAddCard, { backgroundColor: '#FFEBF0' }]}
              onPress={() => navigation.navigate('AddExpense')}
            >
              <View style={[styles.quickAddIconWrap, { backgroundColor: 'rgba(190, 18, 60, 0.12)' }]}>
                <Ionicons name="card-outline" size={16} color="#BE123C" />
              </View>
              <Text style={[styles.quickAddCardText, { color: '#BE123C' }]}>Expense</Text>
            </TouchableOpacity>

            {/* Trip */}
            <TouchableOpacity
              style={[styles.quickAddCard, { backgroundColor: '#E0F2FE' }]}
              onPress={() => navigation.navigate('AddTrip')}
            >
              <View style={[styles.quickAddIconWrap, { backgroundColor: 'rgba(13, 148, 136, 0.12)' }]}>
                <Ionicons name="earth-outline" size={16} color="#0D9488" />
              </View>
              <Text style={[styles.quickAddCardText, { color: '#0D9488' }]}>Trip</Text>
            </TouchableOpacity>

            {/* Learning */}
            <TouchableOpacity
              style={[styles.quickAddCard, { backgroundColor: '#F3E8FF' }]}
              onPress={() => navigation.navigate('AddLearning')}
            >
              <View style={[styles.quickAddIconWrap, { backgroundColor: 'rgba(124, 58, 237, 0.12)' }]}>
                <Ionicons name="school-outline" size={16} color="#7C3AED" />
              </View>
              <Text style={[styles.quickAddCardText, { color: '#7C3AED' }]}>Learning</Text>
            </TouchableOpacity>

            {/* Health */}
            <TouchableOpacity
              style={[styles.quickAddCard, { backgroundColor: '#EBF9DB' }]}
              onPress={() => navigation.navigate('AddHealth')}
            >
              <View style={[styles.quickAddIconWrap, { backgroundColor: 'rgba(45, 106, 0, 0.15)' }]}>
                <Ionicons name="fitness-outline" size={16} color="#2D6A00" />
              </View>
              <Text style={[styles.quickAddCardText, { color: '#2D6A00' }]}>Health</Text>
            </TouchableOpacity>
          </ScrollView>
        </View>

        {/* 5. Today's Schedule Section */}
        <View style={styles.whiteCardSection}>
          <View style={styles.cardSectionHeaderRow}>
            <Text style={styles.cardSectionTitle}>Today's Schedule</Text>
            <TouchableOpacity style={styles.seeAllBtn} onPress={() => navigation.navigate('Schedule')}>
              <Text style={styles.seeAllText}>See all</Text>
              <Feather name="chevron-right" size={14} color="#64748B" />
            </TouchableOpacity>
          </View>

          {/* Timeline List */}
          <View style={styles.timelineContainer}>
            {todayScheduleItems.length === 0 ? (
              <TouchableOpacity
                style={styles.emptyScheduleBox}
                onPress={() => navigation.navigate('FreeDay', { selectedDate: 'Today' })}
                activeOpacity={0.7}
              >
                <Feather name="calendar" size={20} color="#0D9488" style={{ marginBottom: 6 }} />
                <Text style={styles.emptyScheduleTitle}>No plans scheduled for today.</Text>
                <Text style={styles.emptyScheduleSubtitle}>It's a Free Day! Tap to explore suggestions & plan ahead.</Text>
                <View style={{ flexDirection: 'row', alignItems: 'center', marginTop: 8, backgroundColor: '#EBF9DB', paddingHorizontal: 12, paddingVertical: 5, borderRadius: 14 }}>
                  <Text style={{ color: '#2D6A00', fontSize: 11, fontWeight: '700' }}>Open Free Day View →</Text>
                </View>
              </TouchableOpacity>
            ) : (
              todayScheduleItems.map((item, index) => {
                const isLast = index === todayScheduleItems.length - 1;
                return (
                  <View key={item.id} style={styles.timelineItemRow}>
                    <View style={styles.timelineLeftTime}>
                      <Text style={styles.timelineTimeText}>{item.timeFormatted}</Text>
                    </View>
                    <View style={styles.timelineLineCol}>
                      <View style={item.isNow ? styles.timelineActiveDot : styles.timelineInactiveDot} />
                      {!isLast && <View style={styles.timelineVerticalLine} />}
                    </View>
                    <View style={styles.timelineContentBox}>
                      <View style={styles.timelineContentHeader}>
                        <Text style={styles.timelineTaskTitle}>{item.title}</Text>
                        {item.isNow && (
                          <View style={styles.nowBadge}>
                            <Text style={styles.nowBadgeText}>Now</Text>
                          </View>
                        )}
                      </View>
                      {item.subtitle ? <Text style={styles.timelineTaskSub}>{item.subtitle}</Text> : null}
                    </View>
                  </View>
                );
              })
            )}

            {/* Bottom Current Time Indicator Line */}
            <View style={styles.currentTimeRow}>
              <View style={styles.currentTimeLeft}>
                <View style={styles.currentTimeDot} />
                <Text style={styles.currentTimeText}>{formattedCurrentTime}</Text>
              </View>
              <View style={styles.currentTimeDottedLine} />
              <Text style={styles.currentTimeLabel}>Current time</Text>
            </View>
          </View>
        </View>

        {/* 6. Today's Progress Section */}
        <TouchableOpacity
          style={styles.whiteCardSection}
          onPress={() => navigation.navigate('Progress')}
          activeOpacity={0.9}
        >
          <View style={styles.cardSectionHeaderRow}>
            <Text style={styles.cardSectionTitle}>Today's Progress</Text>
            <TouchableOpacity onPress={() => navigation.navigate('Progress')}>
              <Feather name="chevron-right" size={18} color="#64748B" />
            </TouchableOpacity>
          </View>

          {/* Progress Rows */}
          <View style={styles.progressRowsList}>
            {/* Tasks */}
            <View style={styles.progressRow}>
              <View style={styles.progressRowLeft}>
                <Feather name="check" size={14} color="#66C400" style={{ marginRight: 6 }} />
                <Text style={styles.progressRowLabel}>Tasks</Text>
              </View>
              <Text style={styles.progressRowRatio}>{completedTasks}/{totalTasks}</Text>
            </View>
            <View style={styles.progressTrack}>
              <View style={[styles.progressBarFill, { width: `${taskRatio * 100}%`, backgroundColor: '#66C400' }]} />
            </View>

            {/* Habits */}
            <View style={styles.progressRow}>
              <View style={styles.progressRowLeft}>
                <Ionicons name="stats-chart-outline" size={14} color="#F97316" style={{ marginRight: 6 }} />
                <Text style={styles.progressRowLabel}>Habits</Text>
              </View>
              <Text style={styles.progressRowRatio}>{completedHabits}/{totalHabits}</Text>
            </View>
            <View style={styles.progressTrack}>
              <View style={[styles.progressBarFill, { width: `${habitRatio * 100}%`, backgroundColor: '#F97316' }]} />
            </View>

            {/* Goals */}
            <View style={styles.progressRow}>
              <View style={styles.progressRowLeft}>
                <Ionicons name="disc-outline" size={14} color="#8B5CF6" style={{ marginRight: 6 }} />
                <Text style={styles.progressRowLabel}>Goals</Text>
              </View>
              <Text style={styles.progressRowRatio}>{activeGoals}/{totalGoals}</Text>
            </View>
            <View style={styles.progressTrack}>
              <View style={[styles.progressBarFill, { width: `${goalRatio * 100}%`, backgroundColor: '#8B5CF6' }]} />
            </View>
          </View>

          {/* Quote Box with Caveat font */}
          <View style={styles.quoteBoxContainer}>
            <Text style={styles.quoteText}>
              <Text style={styles.quoteMark}>“ </Text>
              Small steps every day lead to big results.
            </Text>
          </View>
        </TouchableOpacity>

        {/* 7. Chat with LIVO (AI Assistant Banner) */}
        <View style={styles.chatLivoBanner}>
          <View style={styles.chatLivoHeaderRow}>
            <View style={styles.chatLivoTitleWrap}>
              <View style={styles.sparkleIconBox}>
                <Ionicons name="sparkles" size={14} color="#7C3AED" />
              </View>
              <Text style={styles.chatLivoTitle}>Chat with LIVO</Text>
            </View>

            <TouchableOpacity style={styles.chatLivoArrowBtn} onPress={() => navigation.navigate('ChatWithLivo')}>
              <Feather name="arrow-right" size={14} color="#7C3AED" />
            </TouchableOpacity>
          </View>

          <Text style={styles.chatLivoPromptText}>
            You have 1hr of free time in this evening do you need me to add anything at that time line?
          </Text>

          <View style={styles.chatLivoChipsRow}>
            <TouchableOpacity style={styles.chatChipBtn} onPress={() => navigation.navigate('ChatWithLivo')}>
              <Text style={styles.chatChipText}>Plan my evening</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.chatChipBtn} onPress={() => navigation.navigate('ChatWithLivo')}>
              <Text style={styles.chatChipText}>Break down this task</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.chatChipBtn} onPress={() => navigation.navigate('ChatWithLivo')}>
              <Text style={styles.chatChipText}>Give me focus tips</Text>
            </TouchableOpacity>
          </View>
        </View>

        <View style={{ height: 40 }} />
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
  },
  contentContainer: {
    paddingHorizontal: 16,
    paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) + 5 : 20,
    paddingBottom: 40,
  },

  /* 1. Header */
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  logoImage: {
    width: 77,
    height: 32,
  },
  logoSubtitle: {
    fontSize: 7.5,
    fontWeight: '700',
    color: '#94A3B8',
    letterSpacing: 1.3,
    marginTop: 1,
    marginLeft: 5,
  },
  headerRightActions: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  iconBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#F1F5F9',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 8,
    position: 'relative',
  },
  notificationBadge: {
    position: 'absolute',
    top: 8,
    right: 8,
    width: 7,
    height: 7,
    borderRadius: 3.5,
    backgroundColor: '#EF4444',
  },
  avatarCircle: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#E2F7C5',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 10,
  },
  avatarText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#2D6A00',
  },

  /* 2. Greeting & Date Bar */
  greetingSection: {
    marginBottom: 14,
  },
  greetingSub: {
    fontSize: 14,
    color: '#64748B',
  },
  userName: {
    fontSize: 22,
    fontWeight: '800',
    color: '#0F172A',
    marginVertical: 2,
  },
  greetingSubText: {
    fontSize: 14,
    color: '#64748B',
  },
  dateBarRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  datePickerPill: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  datePickerText: {
    fontSize: 12.5,
    fontWeight: '600',
    color: '#0F172A',
  },
  viewCalendarBtn: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  viewCalendarText: {
    fontSize: 12.5,
    fontWeight: '600',
    color: '#64748B',
    marginRight: 2,
  },

  /* 3. LIVO Priority Card */
  priorityCard: {
    backgroundColor: '#F1F9E8',
    borderRadius: 18,
    padding: 14,
    borderWidth: 1,
    borderColor: '#E2F2D0',
    marginBottom: 20,
  },
  priorityHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  priorityTitleWrap: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  priorityIconBadge: {
    width: 22,
    height: 22,
    borderRadius: 11,
    backgroundColor: '#66C400',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 6,
  },
  priorityHeaderText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#0F172A',
  },
  carouselCounterWrap: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  carouselCounterText: {
    fontSize: 12,
    color: '#64748B',
    marginRight: 6,
  },
  carouselArrowBtn: {
    width: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 4,
  },

  priorityBodyRow: {
    flexDirection: 'row',
  },
  priorityLeftCol: {
    flex: 1.6,
    paddingRight: 10,
  },
  priorityColDivider: {
    width: 1,
    height: 120,
    backgroundColor: '#E2EECC',
    marginHorizontal: 7,
  },
  priorityRightCol: {
    flex: 1,
    paddingLeft: 4,
  },

  taskTitle: {
    fontSize: 17,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 6,
  },
  taskMetaRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  metaItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginRight: 12,
  },
  metaItemText: {
    fontSize: 12,
    color: '#64748B',
    marginLeft: 4,
  },
  highPriorityPill: {
    flexDirection: 'row',
    alignItems: 'center',
    alignSelf: 'flex-start',
    backgroundColor: '#FFEBEB',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 10,
    marginBottom: 10,
  },
  highPriorityText: {
    fontSize: 11,
    fontWeight: '700',
    color: '#DC2626',
  },
  priorityReasonText: {
    fontSize: 12.5,
    color: '#475569',
    lineHeight: 17,
    marginBottom: 10,
  },
  whyLivoLink: {
    marginBottom: 12,
  },
  whyLivoText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#2D6A00',
    textDecorationLine: 'underline',
  },
  priorityActionBtnsRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  startTaskBtn: {
    backgroundColor: '#66C400',
    paddingHorizontal: 14,
    paddingVertical: 9,
    borderRadius: 14,
    marginRight: 8,
  },
  startTaskBtnText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  markDoneBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#D4E9BE',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 14,
  },
  markDoneBtnText: {
    fontSize: 12.5,
    fontWeight: '700',
    color: '#0F172A',
  },

  /* Next Up Right Col */
  nextUpLabel: {
    fontSize: 10,
    fontWeight: '800',
    color: '#64748B',
    letterSpacing: 0.8,
    marginBottom: 4,
  },
  nextUpTitle: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 6,
  },
  nextUpMetaItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 4,
  },
  nextUpMetaText: {
    fontSize: 11.5,
    color: '#64748B',
    marginLeft: 4,
  },
  mediumPriorityPill: {
    alignSelf: 'flex-start',
    backgroundColor: '#F3E8FF',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 10,
    marginTop: 6,
  },
  mediumPriorityText: {
    fontSize: 11,
    fontWeight: '700',
    color: '#7C3AED',
  },

  /* 4. Quick Add Section */
  quickAddSection: {
    marginBottom: 20,
  },
  sectionHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  createSomethingHeader: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  createPlusCircle: {
    width: 18,
    height: 18,
    borderRadius: 9,
    backgroundColor: '#66C400',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 6,
  },
  createSomethingTitle: {
    fontSize: 15,
    fontWeight: '800',
    color: '#0F172A',
  },
  quickAddSubText: {
    fontSize: 12,
    color: '#64748B',
  },
  quickAddScroll: {
    marginHorizontal: -4,
  },
  quickAddCard: {
    width: 72,
    height: 72,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    marginHorizontal: 4,
  },
  quickAddIconWrap: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 4,
  },
  quickAddCardText: {
    fontSize: 11.5,
    fontWeight: '700',
  },

  /* 5. Today's Schedule (White Card) */
  whiteCardSection: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 20,
  },
  cardSectionHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  cardSectionTitle: {
    fontSize: 17,
    fontWeight: '800',
    color: '#0F172A',
  },
  seeAllBtn: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  seeAllText: {
    fontSize: 12.5,
    color: '#64748B',
    marginRight: 2,
  },

  timelineContainer: {
    paddingLeft: 4,
  },
  emptyScheduleBox: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 18,
    paddingHorizontal: 16,
  },
  emptyScheduleTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: '#334155',
    marginBottom: 4,
    textAlign: 'center',
  },
  emptyScheduleSubtitle: {
    fontSize: 12,
    color: '#94A3B8',
    textAlign: 'center',
  },
  timelineItemRow: {
    flexDirection: 'row',
    marginBottom: 16,
  },
  timelineLeftTime: {
    width: 65,
  },
  timelineTimeText: {
    fontSize: 12.5,
    fontWeight: '600',
    color: '#64748B',
  },
  timelineLineCol: {
    alignItems: 'center',
    width: 20,
    marginRight: 10,
  },
  timelineActiveDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: '#66C400',
    marginTop: 3,
  },
  timelineInactiveDot: {
    width: 9,
    height: 9,
    borderRadius: 4.5,
    backgroundColor: '#CBD5E1',
    marginTop: 3,
  },
  timelineVerticalLine: {
    width: 1.5,
    flex: 1,
    backgroundColor: '#E2E8F0',
    marginTop: 4,
  },
  timelineContentBox: {
    flex: 1,
  },
  timelineContentHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  timelineTaskTitle: {
    fontSize: 14.5,
    fontWeight: '700',
    color: '#0F172A',
  },
  nowBadge: {
    backgroundColor: '#EBF9DB',
    paddingHorizontal: 10,
    paddingVertical: 3,
    borderRadius: 12,
  },
  nowBadgeText: {
    fontSize: 11,
    fontWeight: '700',
    color: '#2D6A00',
  },
  timelineTaskSub: {
    fontSize: 12.5,
    color: '#64748B',
    marginTop: 2,
  },

  currentTimeRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 4,
    paddingTop: 8,
    borderTopWidth: 1,
    borderTopColor: '#F1F5F9',
  },
  currentTimeLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    marginRight: 8,
  },
  currentTimeDot: {
    width: 7,
    height: 7,
    borderRadius: 3.5,
    backgroundColor: '#66C400',
    marginRight: 4,
  },
  currentTimeText: {
    fontSize: 11.5,
    fontWeight: '700',
    color: '#66C400',
  },
  currentTimeDottedLine: {
    flex: 1,
    height: 1,
    borderWidth: 0.8,
    borderColor: '#CBD5E1',
    borderStyle: 'dashed',
    marginHorizontal: 8,
  },
  currentTimeLabel: {
    fontSize: 11.5,
    color: '#64748B',
  },

  /* 6. Today's Progress */
  progressRowsList: {
    marginBottom: 16,
  },
  progressRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
    marginTop: 6,
  },
  progressRowLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  progressRowLabel: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
  },
  progressRowRatio: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
  },
  progressTrack: {
    height: 6,
    borderRadius: 3,
    backgroundColor: '#F1F5F9',
    marginBottom: 12,
    overflow: 'hidden',
  },
  progressBarFill: {
    height: '100%',
    borderRadius: 3,
  },

  quoteBoxContainer: {
    backgroundColor: '#F1F9E8',
    borderRadius: 14,
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderWidth: 1,
    borderColor: '#E2F2D0',
  },
  quoteText: {
    fontFamily: 'Caveat_700Bold',
    fontSize: 20,
    color: '#2D6A00',
    lineHeight: 24,
  },
  quoteMark: {
    fontFamily: 'Caveat_700Bold',
    fontSize: 24,
    color: '#66C400',
  },

  /* 7. Chat with LIVO */
  chatLivoBanner: {
    backgroundColor: '#F5EFFF',
    borderRadius: 18,
    padding: 16,
    borderWidth: 1,
    borderColor: '#EDE9FE',
  },
  chatLivoHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  chatLivoTitleWrap: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  sparkleIconBox: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: '#E9D5FF',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  chatLivoTitle: {
    fontSize: 15,
    fontWeight: '800',
    color: '#0F172A',
  },
  chatLivoArrowBtn: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: 'rgba(233, 213, 255, 1)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  chatLivoPromptText: {
    fontSize: 13,
    color: '#64748B',
    lineHeight: 20,
    marginBottom: 14,
  },
  chatLivoChipsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  chatChipBtn: {
    backgroundColor: '#ECE7FE',
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 16,
    marginRight: 8,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: '#DDD6FE',
  },
  chatChipText: {
    fontSize: 10.5,
    fontWeight: '600',
    color: '#7C3AED',
  },
});

export const homeScreen = HomeScreen;
export default HomeScreen;
