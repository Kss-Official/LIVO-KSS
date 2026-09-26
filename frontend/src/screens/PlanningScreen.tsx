import React, { useState, useMemo, useCallback } from 'react';
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
  Modal,
  TouchableWithoutFeedback,
  Alert,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { useFocusEffect, useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useEvents } from '../hooks/useEvents';
import { useTasks } from '../hooks/useTasks';
import { useHabits } from '../hooks/useHabits';
import { useGoals } from '../hooks/useGoals';
import { SelectionModal } from '../components/forms';
import { AnimatedHamburger } from '../components/ui/AnimatedHamburger';
export const parseDateTime = (rawDate?: string, rawTime?: string): Date | null => {
  if (!rawDate) return null;
  let d: Date | null = null;
  const lowerDate = String(rawDate).trim().toLowerCase();

  if (lowerDate.startsWith('today')) {
    d = new Date();
  } else if (lowerDate.startsWith('tomorrow')) {
    d = new Date();
    d.setDate(d.getDate() + 1);
  } else {
    const parsed = new Date(rawDate);
    if (!isNaN(parsed.getTime())) {
      d = parsed;
    } else {
      const parts = String(rawDate).match(/^(\d{4})[-/](\d{1,2})[-/](\d{1,2})/);
      if (parts) {
        d = new Date(parseInt(parts[1], 10), parseInt(parts[2], 10) - 1, parseInt(parts[3], 10));
      }
    }
  }

  if (!d || isNaN(d.getTime())) return null;

  if (rawTime) {
    const timeAsDate = new Date(rawTime);
    if (!isNaN(timeAsDate.getTime()) && rawTime.includes('T') && rawTime.includes('Z')) {
      d.setHours(timeAsDate.getHours(), timeAsDate.getMinutes(), 0, 0);
    } else {
      const timeMatch = String(rawTime).match(/(\d{1,2}):(\d{2})(?::\d{2})?\s*(AM|PM)?/i);
      if (timeMatch) {
        let hours = parseInt(timeMatch[1], 10);
        const minutes = parseInt(timeMatch[2], 10);
        const ampm = timeMatch[3]?.toUpperCase();

        if (ampm === 'PM' && hours < 12) hours += 12;
        if (ampm === 'AM' && hours === 12) hours = 0;

        d.setHours(hours, minutes, 0, 0);
      }
    }
  }

  return d;
};

import { useProfile } from '../hooks/useProfile';

export const PlanningScreen: React.FC = () => {
  const navigation = useNavigation<NativeStackNavigationProp<any>>();
  const { profile, refreshProfile } = useProfile();
  const { events, deleteEvent, refreshEvents } = useEvents();
  const { tasks: realTasks, toggleTask, refreshTasks, deleteTask } = useTasks();
  const { habits, deleteHabit, refreshHabits } = useHabits();
  const { goals, deleteGoal, refreshGoals } = useGoals();

  const getTypeTheme = (type: string) => {
    switch (type) {
      case 'Task': return { bg: '#F0FDF4', color: '#16A34A' };
      case 'Event': return { bg: '#EFF6FF', color: '#2563EB' };
      case 'Habit': return { bg: '#FFF7ED', color: '#EA580C' };
      case 'Goal': return { bg: '#FAF5FF', color: '#9333EA' };
      case 'Expense': return { bg: '#FEF2F2', color: '#DC2626' };
      case 'Trip': return { bg: '#F0FDFA', color: '#0D9488' };
      case 'Learning': return { bg: '#FEFCE8', color: '#CA8A04' };
      case 'Health': return { bg: '#FDF2F8', color: '#DB2777' };
      default: return { bg: '#EFF6FF', color: '#2563EB' };
    }
  };

  useFocusEffect(
    useCallback(() => {
      refreshEvents();
      refreshTasks();
      refreshHabits();
      refreshGoals();
      profile.name; // just access
      refreshProfile?.();
    }, [refreshEvents, refreshTasks, refreshHabits, refreshGoals, refreshProfile])
  );

  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [selectedTaskForDetail, setSelectedTaskForDetail] = useState<any>(null);
  const [showTaskDetailModal, setShowTaskDetailModal] = useState(false);
  const [selectedEventForAction, setSelectedEventForAction] = useState<any>(null);
  const [showEventActionModal, setShowEventActionModal] = useState(false);

  const formatDetailDate = (item: any): string => {
    if (!item) return '';
    const parsed = parseDateTime(item.date || item.dueDate, item.time || item.startTime);
    const dateObj = parsed || (item.rawDate instanceof Date ? item.rawDate : selectedDate);
    if (!dateObj || isNaN(dateObj.getTime())) {
      return selectedDate.toLocaleDateString('en-US', {
        weekday: 'short',
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    }
    return dateObj.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  const formatDetailTimeAndDuration = (item: any): string => {
    if (!item) return '';
    const timeStr = item.displayTime || item.time || item.startTime || '';
    const durStr = item.duration || (item.durationMinutes ? `${item.durationMinutes}m` : '');
    if (timeStr && durStr) {
      return `${timeStr} (${durStr})`;
    }
    return timeStr || durStr || 'All Day';
  };

  const getPriorityBadgeStyle = (priority?: string) => {
    const p = (priority || 'Low').toUpperCase();
    if (p === 'HIGH' || p === 'URGENT') {
      return { bg: '#FFEBEB', color: '#DC2626', label: 'High Priority' };
    }
    if (p === 'MEDIUM') {
      return { bg: '#F3E8FF', color: '#7C3AED', label: 'Medium Priority' };
    }
    return { bg: '#F1F5F9', color: '#64748B', label: 'Low Priority' };
  };

  const [filterType, setFilterType] = useState('All');
  const [showFilterMenu, setShowFilterMenu] = useState(false);

  const filterOptions = [
    { label: 'View All', value: 'All', icon: 'list' as any },
    { label: 'Tasks', value: 'Tasks', icon: 'check-square' as any },
    { label: 'Events', value: 'Events', icon: 'calendar' as any },
    { label: 'Habits', value: 'Habits', icon: 'refresh-cw' as any },
    { label: 'Completed', value: 'Completed', icon: 'check-circle' as any },
    { label: 'Manage Plan', value: 'Manage', icon: 'settings' as any },
  ];

  const formatUpcomingDate = (date: Date): string => {
    const now = new Date();
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const itemStart = new Date(date.getFullYear(), date.getMonth(), date.getDate());

    const diffMs = itemStart.getTime() - todayStart.getTime();
    const diffDays = Math.round(diffMs / (1000 * 60 * 60 * 24));

    const timeStr = date.toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true,
    });

    if (diffDays === 0) {
      return `Today, ${timeStr}`;
    } else if (diffDays === 1) {
      return `Tomorrow, ${timeStr}`;
    } else {
      const weekday = date.toLocaleDateString('en-US', { weekday: 'short' });
      const day = date.getDate();
      const month = date.toLocaleDateString('en-US', { month: 'short' });
      return `${weekday}, ${day} ${month}, ${timeStr}`;
    }
  };

  const generateDaysList = () => {
    const list = [];
    const today = new Date();
    for (let i = 0; i < 7; i++) {
      const d = new Date(today);
      d.setDate(today.getDate() + i);
      list.push({
        dateObj: d,
        day: d.toLocaleDateString('en-US', { weekday: 'short' }),
        date: d.toLocaleDateString('en-US', { day: 'numeric', month: 'short' }),
        full: d.toDateString(),
      });
    }
    return list;
  };
  const daysList = useMemo(() => generateDaysList(), []);

  const upcomingItems = useMemo(() => {
    const now = new Date();

    const list: Array<{
      id: string;
      title: string;
      displayTime: string;
      rawDate: Date;
      type: 'Task' | 'Event' | 'Habit' | 'Goal' | 'Trip' | 'Expense' | 'Learning' | 'Health';
      priority?: string;
      category?: string;
    }> = [];

    // Filter Tasks (exclude completed/cancelled)
    realTasks.forEach(t => {
      if (t.completed || t.status === 'COMPLETED' || t.status === 'CANCELLED') return;
      const itemDate = parseDateTime(t.date || t.dueDate, t.time);
      if (!itemDate) return;

      if (itemDate.getTime() >= now.getTime() - 60000) {
        list.push({
          id: `task-${t.id}`,
          title: t.title,
          displayTime: formatUpcomingDate(itemDate),
          rawDate: itemDate,
          type: 'Task',
          priority: t.priority,
          category: t.category,
        });
      }
    });

    // Filter Events (including Expenses, Trips, Learning, Health)
    events.forEach(e => {
      const itemDate = parseDateTime(e.date, e.startTime);
      if (!itemDate) return;

      if (itemDate.getTime() >= now.getTime() - 60000) {
        list.push({
          id: `event-${e.id}`,
          title: e.title,
          displayTime: formatUpcomingDate(itemDate),
          rawDate: itemDate,
          type: (e.category && ['Trip', 'Expense', 'Learning', 'Health'].includes(e.category)) ? (e.category as any) : 'Event',
          category: e.category,
        });
      }
    });

    // Filter Habits
    habits.forEach((h: any) => {
      const itemDate = h.date ? parseDateTime(h.date, h.time) : null;
      if (!itemDate) return;

      if (itemDate.getTime() >= now.getTime() - 60000) {
        list.push({
          id: `habit-${h.id}`,
          title: h.title,
          displayTime: formatUpcomingDate(itemDate),
          rawDate: itemDate,
          type: 'Habit',
          category: 'Habit',
        });
      }
    });

    // Filter Goals
    goals.forEach((g: any) => {
      const itemDate = parseDateTime(g.targetDate || g.date);
      if (!itemDate) return;

      if (itemDate.getTime() >= now.getTime() - 60000) {
        list.push({
          id: `goal-${g.id}`,
          title: g.title,
          displayTime: formatUpcomingDate(itemDate),
          rawDate: itemDate,
          type: 'Goal',
          category: 'Goal',
        });
      }
    });

    return list.sort((a, b) => a.rawDate.getTime() - b.rawDate.getTime());
  }, [realTasks, events, habits, goals]);

  const renderPriorityBadge = (priority?: string) => {
    const p = (priority || 'Low').toUpperCase();
    if (p === 'HIGH' || p === 'URGENT') {
      return (
        <View style={styles.highPriorityPill}>
          <Text style={styles.highPriorityText}>{priority || 'High'}</Text>
        </View>
      );
    }
    if (p === 'MEDIUM') {
      return (
        <View style={styles.mediumPriorityPill}>
          <Text style={styles.mediumPriorityText}>Medium</Text>
        </View>
      );
    }
    return (
      <View style={styles.lowPriorityPill}>
        <Text style={styles.lowPriorityText}>{priority || 'Low'}</Text>
      </View>
    );
  };



  // Combine Events, Tasks (with time), Habits for the timeline
  const planItems = useMemo(() => {
    const isSameDate = (d1: Date | null, d2: Date) => {
      if (!d1) return false;
      return d1.getFullYear() === d2.getFullYear() &&
        d1.getMonth() === d2.getMonth() &&
        d1.getDate() === d2.getDate();
    };

    const getDisplayTime = (d: Date | null, fallback: string) => {
      if (!d) return fallback;
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    const dailyEvents = events.reduce((acc, e) => {
      const parsed = parseDateTime(e.date, e.startTime);
      if (isSameDate(parsed, selectedDate)) {
        acc.push({ ...e, itemType: 'Event', type: e.category || 'Event', displayTime: getDisplayTime(parsed, e.startTime || '2:00 PM') });
      }
      return acc;
    }, [] as any[]);

    const dailyTasks = realTasks.reduce((acc, t) => {
      const parsed = parseDateTime(t.date || t.dueDate, t.time);
      if (isSameDate(parsed, selectedDate)) {
        acc.push({ ...t, itemType: 'Task', type: 'Task', displayTime: getDisplayTime(parsed, t.time || '12:00 PM') });
      }
      return acc;
    }, [] as any[]);

    const dailyHabits = habits.reduce((acc, h: any) => {
      const parsed = h.date ? parseDateTime(h.date) : null;
      if (!h.date || isSameDate(parsed, selectedDate)) {
        acc.push({ ...h, itemType: 'Habit', type: 'Habit', displayTime: getDisplayTime(parsed, h.time || '8:00 AM') });
      }
      return acc;
    }, [] as any[]);

    const dailyGoals = goals.reduce((acc, g: any) => {
      const parsed = parseDateTime(g.targetDate || g.date);
      if (isSameDate(parsed, selectedDate)) {
        acc.push({ ...g, itemType: 'Goal', type: 'Goal', displayTime: getDisplayTime(parsed, '10:00 AM') });
      }
      return acc;
    }, [] as any[]);

    return [...dailyEvents, ...dailyTasks, ...dailyHabits, ...dailyGoals].sort((a, b) => {
      const timeA = parseDateTime(selectedDate.toISOString(), a.displayTime)?.getTime() || 0;
      const timeB = parseDateTime(selectedDate.toISOString(), b.displayTime)?.getTime() || 0;
      return timeA - timeB;
    });
  }, [events, realTasks, habits, goals, selectedDate]);

  const filteredPlanItems = useMemo(() => {
    let merged = [...planItems];

    if (filterType === 'Tasks') {
      merged = merged.filter(i => i.type === 'Task');
    } else if (filterType === 'Events') {
      merged = merged.filter(i => i.type === 'Event');
    } else if (filterType === 'Habits') {
      merged = merged.filter(i => i.type === 'Habit');
    } else if (filterType === 'Completed') {
      merged = merged.filter(i => (i as any).completed || (i as any).status === 'COMPLETED');
    }

    return merged;
  }, [planItems, filterType]);

  const workloadStats = useMemo(() => {
    const tasksToday = planItems.filter(item => item.type === 'Task');
    const eventsToday = planItems.filter(item => item.type === 'Event');

    let plannedMinutes = 0;

    tasksToday.forEach((t: any) => {
      if (t.estimatedMinutes) plannedMinutes += t.estimatedMinutes;
      else plannedMinutes += 30; // default to 30 mins
    });

    eventsToday.forEach((e: any) => {
      if (e.durationMinutes) plannedMinutes += e.durationMinutes;
      else plannedMinutes += 60; // default 1 hour
    });

    const plannedHours = Math.floor(plannedMinutes / 60);
    const plannedMinsRemain = plannedMinutes % 60;
    const timeString = `${plannedHours}h ${plannedMinsRemain}m planned`;

    const totalItems = planItems.length;
    const completedItems = planItems.filter((i: any) => i.completed || i.status === 'COMPLETED').length;
    const completionPercent = totalItems === 0 ? 0 : Math.round((completedItems / totalItems) * 100);

    return {
      numTasks: tasksToday.length,
      numEvents: eventsToday.length,
      timeString,
      completionPercent
    };
  }, [planItems]);

  const unscheduledTasks = realTasks.length > 0
    ? realTasks.filter(t => !t.completed).slice(0, 4)
    : [
      { id: 't1', title: 'Finish portfolio case study', completed: false },
      { id: 't2', title: 'Read React chapter', completed: false },
      { id: 't3', title: 'Plan weekend trip', completed: false },
    ];

  const handleDeleteEvent = async () => {
    if (selectedEventForAction) {
      const itemType = selectedEventForAction.itemType || selectedEventForAction.type;
      const id = selectedEventForAction.id;

      if (itemType === 'Task') {
        await deleteTask(id);
        await refreshTasks();
      } else if (itemType === 'Habit') {
        await deleteHabit(id);
        await refreshHabits();
      } else if (itemType === 'Goal') {
        await deleteGoal(id);
        await refreshGoals();
      } else {
        await deleteEvent(id);
        await refreshEvents();
      }

      setShowEventActionModal(false);
      setSelectedEventForAction(null);
    }
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.contentContainer}
        showsVerticalScrollIndicator={false}
      >
        {/* 1. Top Header Row */}
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


            <TouchableOpacity
              style={styles.avatarCircle}
              onPress={() => navigation.navigate('Profile' as never)}
              activeOpacity={0.8}
            >
              <Text style={styles.avatarText}>{profile.name.charAt(0).toUpperCase()}</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Caveat Handwritten Slogan Banner (Top Right Area) */}
        <View style={styles.sloganWrap}>
          <Text style={styles.sloganText}>Good plans create freedom</Text>
          <View style={styles.sloganUnderline} />
        </View>

        {/* 2. Title & Subtitle */}
        <View style={styles.titleSection}>
          <Text style={styles.mainTitle}>Plan</Text>
          <Text style={styles.mainSubtitle}>Organize today. A better tomorrow.</Text>
        </View>

        {/* 3. Date Navigation Bar */}
        <View style={styles.dateBarRow}>
          <TouchableOpacity style={styles.datePickerPill} onPress={() => navigation.navigate('Schedule')}>
            <Feather name="calendar" size={14} color="#64748B" style={{ marginRight: 6 }} />
            <Text style={styles.datePickerText}>
              {selectedDate.toDateString() === new Date().toDateString() ? 'Today, ' : ''}
              {selectedDate.toLocaleDateString('en-US', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' })}
            </Text>
            <Feather name="chevron-down" size={14} color="#64748B" style={{ marginLeft: 6 }} />
          </TouchableOpacity>

          <View style={styles.dateRightGroup}>
            <TouchableOpacity style={styles.todayBtnPill} onPress={() => setSelectedDate(new Date())}>
              <Text style={styles.todayBtnText}>Today</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.dateArrowBtn} onPress={() => navigation.navigate('Schedule')}>
              <Feather name="chevron-right" size={14} color="#64748B" />
            </TouchableOpacity>
          </View>
        </View>

        {/* 4. Today's Workload Widget */}
        <TouchableOpacity
          style={styles.workloadCard}
          onPress={() => {
            navigation.navigate('OverloadedDay', {
              plannedWork: workloadStats.timeString,
              availableTime: '5h 45m',
              totalTasks: workloadStats.numTasks,
              totalEvents: workloadStats.numEvents,
            });
          }}
          activeOpacity={0.8}
        >
          {/* Circular Gauge */}
          <View style={styles.gaugeContainer}>
            <View style={styles.gaugeCircleOuter}>
              <Text style={styles.gaugePercentText}>{workloadStats.completionPercent}%</Text>
            </View>
          </View>

          {/* Workload Stats */}
          <View style={styles.workloadInfoCol}>
            <Text style={styles.workloadTitle}>Workload</Text>
            <Text style={styles.workloadSub}>{workloadStats.numTasks} tasks • {workloadStats.numEvents} events • {workloadStats.timeString}</Text>
            <Text style={styles.workloadHint}>Tap to optimize & view overloaded day</Text>
          </View>

          {/* CTA Action Button */}
          <View style={styles.workloadCtaCol}>
            <TouchableOpacity style={styles.planMyDayBtn} onPress={() => navigation.navigate('ChatWithLivo')}>
              <Text style={styles.planMyDayText}>Plan my day ✨</Text>
            </TouchableOpacity>
            <Text style={styles.planMyDaySub}>Let LIVO optimize your day</Text>
          </View>
        </TouchableOpacity>

        {/* 5. Weekly Date Selector Strip */}
        <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.weeklyStripScroll}>
          {daysList.map((item) => {
            const isSelected = selectedDate.toDateString() === item.dateObj.toDateString();
            return (
              <TouchableOpacity
                key={item.full}
                onPress={() => setSelectedDate(item.dateObj)}
                style={[
                  styles.dayCard,
                  isSelected ? styles.dayCardSelected : styles.dayCardUnselected,
                ]}
              >
                <Text style={[styles.dayCardLabel, isSelected && styles.dayCardLabelSelected]}>
                  {item.day}
                </Text>
                <Text style={[styles.dayCardDate, isSelected && styles.dayCardDateSelected]}>
                  {item.date}
                </Text>
              </TouchableOpacity>
            );
          })}
        </ScrollView>

        {/* 6. Today's Plan (Main Schedule List) */}
        <View style={styles.whiteCardSection}>
          <View style={styles.cardSectionHeaderRow}>
            <View style={styles.planTitleGroup}>
              <View style={{ marginRight: 8, marginTop: 4 }}>
                <AnimatedHamburger
                  isOpen={showFilterMenu}
                  onPress={() => setShowFilterMenu(!showFilterMenu)}
                />
              </View>
              <Text style={styles.cardSectionTitle}>
                {selectedDate.toDateString() === new Date().toDateString() ? "Today's Plan" : `${selectedDate.toLocaleDateString('en-US', { weekday: 'long' })}'s Plan`}
              </Text>
            </View>
            <TouchableOpacity style={styles.addBtnWrap} onPress={() => navigation.navigate('AddOptains')}>
              <Feather name="plus" size={14} color="#64748B" />
              <Text style={styles.addBtnText}>Add</Text>
            </TouchableOpacity>
          </View>

          {/* Timeline Schedule Items */}
          <View style={styles.timelineListContainer}>
            {filteredPlanItems.length > 0 ? (
              filteredPlanItems.map((item, index) => (
                <View key={item.id} style={styles.scheduleRow}>
                  <Text style={styles.scheduleTime}>{item.displayTime}</Text>
                  <View style={styles.timelineDotCol}>
                    <View style={[styles.timelineBlueDot, { backgroundColor: getTypeTheme(item.type).color }]} />
                    {index < filteredPlanItems.length - 1 && <View style={styles.timelineVerticalLine} />}
                  </View>
                  <View
                    style={[
                      styles.scheduleCard,
                      {
                        backgroundColor: getTypeTheme(item.type).bg,
                        borderColor: getTypeTheme(item.type).color + '33',
                      },
                    ]}
                  >
                    <TouchableOpacity
                      style={styles.scheduleCardMainTouch}
                      activeOpacity={0.7}
                      onPress={() => {
                        setSelectedTaskForDetail(item);
                        setShowTaskDetailModal(true);
                      }}
                    >
                      <Text style={styles.scheduleCardTitle}>{item.title}</Text>
                      <Text style={styles.scheduleCardSub}>
                        {item.description || item.category || 'Scheduled'}
                      </Text>
                    </TouchableOpacity>

                    <View style={styles.badgeMenuRow}>
                      <TouchableOpacity
                        activeOpacity={0.7}
                        onPress={() => {
                          setSelectedTaskForDetail(item);
                          setShowTaskDetailModal(true);
                        }}
                      >
                        <View
                          style={[
                            styles.eventTypeBadge,
                            { backgroundColor: getTypeTheme(item.type).color + '22' },
                          ]}
                        >
                          <Text
                            style={[
                              styles.eventTypeBadgeText,
                              { color: getTypeTheme(item.type).color },
                            ]}
                          >
                            {item.type}
                          </Text>
                        </View>
                      </TouchableOpacity>

                      <TouchableOpacity
                        hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
                        style={styles.threeDotBtn}
                        activeOpacity={0.6}
                        onPress={(e) => {
                          e?.stopPropagation?.();
                          setSelectedEventForAction(item);
                          setShowEventActionModal(true);
                        }}
                      >
                        <Feather name="more-vertical" size={15} color="#64748B" />
                      </TouchableOpacity>
                    </View>
                  </View>
                </View>
              ))
            ) : (
              <TouchableOpacity
                style={{ alignItems: 'center', padding: 20, backgroundColor: '#F8FAFC', borderRadius: 12 }}
                onPress={() => navigation.navigate('FreeDay', { selectedDate: selectedDate.toLocaleDateString('en-US', { weekday: 'short', day: 'numeric', month: 'short' }) })}
                activeOpacity={0.7}
              >
                <Feather name="calendar" size={24} color="#0D9488" style={{ marginBottom: 6 }} />
                <Text style={{ color: '#0F172A', fontSize: 14, fontWeight: '700', marginBottom: 2 }}>Free Day</Text>
                <Text style={{ color: '#64748B', fontSize: 12, marginBottom: 8, textAlign: 'center' }}>No items scheduled for this day. Tap to explore suggestions!</Text>
                <View style={{ flexDirection: 'row', alignItems: 'center', backgroundColor: '#EBF9DB', paddingHorizontal: 12, paddingVertical: 6, borderRadius: 16 }}>
                  <Text style={{ color: '#2D6A00', fontSize: 12, fontWeight: '600' }}>Explore Free Day Ideas →</Text>
                </View>
              </TouchableOpacity>
            )}
          </View>
        </View>

        {/* 7. LIVO Suggests (AI Intelligent Tip Banner) */}
        <View style={styles.livoSuggestsCard}>
          <View style={styles.suggestsHeaderRow}>
            <View style={styles.suggestsTitleWrap}>
              <Ionicons name="sparkles" size={14} color="#7C3AED" style={{ marginRight: 6 }} />
              <Text style={styles.suggestsTitleText}>LIVO Suggests</Text>
            </View>
            <TouchableOpacity onPress={() => navigation.navigate('ChatWithLivo')}>
              <Feather name="arrow-right" size={14} color="#7C3AED" />
            </TouchableOpacity>
          </View>

          <View style={styles.suggestsContentBox}>
            <Text style={styles.suggestsGapTitle}>☀️ You have a 1h 30m gap at 4:00 PM.</Text>
            <Text style={styles.suggestsGapSub}>Want to work on your portfolio case study?</Text>
          </View>
        </View>

        {/* 8. Unscheduled Tasks Box */}
        <View style={styles.whiteCardSection}>
          <View style={styles.cardSectionHeaderRow}>
            <View style={styles.planTitleGroup}>
              <Feather name="edit-3" size={15} color="#0F172A" style={{ marginRight: 6 }} />
              <Text style={styles.cardSectionTitle}>Unscheduled Tasks</Text>
            </View>
            <TouchableOpacity style={styles.smallPlusBtn} onPress={() => navigation.navigate('Tasks')}>
              <Feather name="plus" size={14} color="#64748B" />
            </TouchableOpacity>
          </View>

          <View style={styles.unscheduledList}>
            {unscheduledTasks.map((t: any) => (
              <TouchableOpacity
                key={t.id}
                style={styles.taskCheckRow}
                onPress={() => toggleTask(t.id)}
              >
                <View style={[styles.checkBoxCircle, t.completed && styles.checkBoxChecked]}>
                  {t.completed && <Feather name="check" size={10} color="#FFFFFF" />}
                </View>
                <Text style={[styles.taskCheckLabel, t.completed && styles.taskCheckLabelDone]}>
                  {t.title}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          <TouchableOpacity style={styles.viewAllTasksBtn} onPress={() => navigation.navigate('Tasks')}>
            <Text style={styles.viewAllTasksText}>View all tasks →</Text>
          </TouchableOpacity>
        </View>

        {/* 9. Upcoming Section */}
        <View style={styles.whiteCardSection}>
          <View style={styles.cardSectionHeaderRow}>
            <Text style={styles.cardSectionTitle}>Upcoming</Text>
            <TouchableOpacity style={styles.seeAllBtn} onPress={() => navigation.navigate('Schedule')}>
              <Text style={styles.seeAllText}>See all</Text>
              <Feather name="chevron-right" size={14} color="#64748B" />
            </TouchableOpacity>
          </View>

          <View style={styles.upcomingList}>
            {upcomingItems.length > 0 ? (
              upcomingItems.map((item, index) => (
                <View
                  key={item.id}
                  style={[
                    styles.upcomingRow,
                    index === upcomingItems.length - 1 && { borderBottomWidth: 0 },
                  ]}
                >
                  <View style={styles.upcomingLeft}>
                    <View style={styles.calendarIconSquare}>
                      <Feather name="calendar" size={14} color="#64748B" />
                    </View>
                    <View>
                      <Text style={styles.upcomingItemTitle}>{item.title}</Text>
                      <Text style={styles.upcomingItemTime}>{item.displayTime}</Text>
                    </View>
                  </View>
                  {item.type === 'Task' ? (
                    renderPriorityBadge(item.priority)
                  ) : (
                    <View style={styles.eventTypeBadge}>
                      <Text style={styles.eventTypeBadgeText}>{item.category || 'Event'}</Text>
                    </View>
                  )}
                </View>
              ))
            ) : (
              <View style={{ alignItems: 'center', paddingVertical: 16 }}>
                <Feather name="calendar" size={24} color="#94A3B8" style={{ marginBottom: 6 }} />
                <Text style={{ color: '#94A3B8', fontSize: 13, fontWeight: '500' }}>No upcoming activities</Text>
              </View>
            )}
          </View>
        </View>

        {/* 10. Need help planning your day? (Bottom AI Banner) */}
        <View style={styles.needHelpBanner}>
          <View style={styles.needHelpLeft}>
            <View style={styles.sparkleCircleSmall}>
              <Ionicons name="sparkles" size={13} color="#7C3AED" />
            </View>
            <View style={{ flex: 1, paddingRight: 8 }}>
              <Text style={styles.needHelpTitle}>Need help planning your day?</Text>
              <Text style={styles.needHelpSub}>
                Tell LIVO your priorities and I'll create the best plan for you.
              </Text>
            </View>
          </View>

          <TouchableOpacity style={styles.chatWithLivoPill} onPress={() => navigation.navigate('ChatWithLivo')}>
            <Text style={styles.chatWithLivoText}>Chat with LIVO ∨</Text>
          </TouchableOpacity>
        </View>

        <View style={{ height: 40 }} />
      </ScrollView>

      {/* 1. Task Detail Bottom Sheet */}
      <Modal
        visible={showTaskDetailModal}
        transparent
        animationType="slide"
        onRequestClose={() => setShowTaskDetailModal(false)}
      >
        <TouchableWithoutFeedback onPress={() => setShowTaskDetailModal(false)}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>
        <View style={styles.modalContent}>
          <View style={styles.modalGrabber} />

          <Text style={styles.detailModalTitle}>
            {selectedTaskForDetail?.title || 'Task Details'}
          </Text>

          <View style={styles.detailRowsContainer}>
            {/* Date Row */}
            <View style={styles.detailRow}>
              <View style={styles.detailIconWrap}>
                <Feather name="calendar" size={16} color="#64748B" />
              </View>
              <Text style={styles.detailRowText}>{formatDetailDate(selectedTaskForDetail)}</Text>
            </View>

            {/* Time & Duration Row */}
            <View style={styles.detailRow}>
              <View style={styles.detailIconWrap}>
                <Feather name="clock" size={16} color="#64748B" />
              </View>
              <Text style={styles.detailRowText}>
                {formatDetailTimeAndDuration(selectedTaskForDetail)}
              </Text>
            </View>

            {/* Location / Meeting Row */}
            <View style={styles.detailRow}>
              <View style={styles.detailIconWrap}>
                <Feather name="map-pin" size={16} color="#64748B" />
              </View>
              <View style={{ flexDirection: 'row', alignItems: 'center' }}>
                <Text style={styles.detailRowText}>
                  {selectedTaskForDetail?.location ||
                    (selectedTaskForDetail?.meetingType === 'online'
                      ? 'Google Meet'
                      : selectedTaskForDetail?.meetingType === 'phone'
                      ? 'Phone Call'
                      : 'Office / Location')}
                </Text>
                {selectedTaskForDetail?.meetingType === 'online' && (
                  <Feather name="video" size={14} color="#64748B" style={{ marginLeft: 8 }} />
                )}
              </View>
            </View>

            {/* Priority Row */}
            <View style={styles.detailRow}>
              <View style={styles.detailIconWrap}>
                <Feather name="tag" size={16} color="#64748B" />
              </View>
              {(() => {
                const priorityInfo = getPriorityBadgeStyle(selectedTaskForDetail?.priority);
                return (
                  <View style={[styles.detailPriorityPill, { backgroundColor: priorityInfo.bg }]}>
                    <Text style={[styles.detailPriorityText, { color: priorityInfo.color }]}>
                      {priorityInfo.label}
                    </Text>
                  </View>
                );
              })()}
            </View>

            {/* Description Row */}
            {Boolean(selectedTaskForDetail?.description) && (
              <View style={[styles.detailRow, { alignItems: 'flex-start' }]}>
                <View style={[styles.detailIconWrap, { marginTop: 2 }]}>
                  <Feather name="file-text" size={16} color="#64748B" />
                </View>
                <Text style={styles.detailDescriptionText}>
                  {selectedTaskForDetail?.description}
                </Text>
              </View>
            )}
          </View>

          <TouchableOpacity
            style={styles.modalCancelBtn}
            onPress={() => setShowTaskDetailModal(false)}
          >
            <Text style={styles.modalCancelText}>Cancel</Text>
          </TouchableOpacity>
        </View>
      </Modal>

      {/* 2. Three-Dot Action Bottom Sheet (Edit/Delete) */}
      <Modal
        visible={showEventActionModal}
        transparent
        animationType="slide"
        onRequestClose={() => setShowEventActionModal(false)}
      >
        <TouchableWithoutFeedback onPress={() => setShowEventActionModal(false)}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>
        <View style={styles.modalContent}>
          <View style={styles.modalGrabber} />

          <Text style={styles.modalTitle}>
            {selectedEventForAction?.title || 'Task'}
          </Text>

          <TouchableOpacity
            style={styles.actionModalRow}
            onPress={() => {
              setShowEventActionModal(false);
              if (selectedEventForAction?.type === 'Task' || selectedEventForAction?.itemType === 'Task') {
                const cleanId = selectedEventForAction.id ? selectedEventForAction.id.replace(/^(task|event|habit|goal)-/, '') : '';
                const foundTask = realTasks.find((t) => t.id === selectedEventForAction.id || t.id === cleanId) || selectedEventForAction;
                navigation.navigate('AddTask', { existingTask: foundTask, taskId: foundTask?.id });
              } else if (selectedEventForAction?.type === 'Habit' || selectedEventForAction?.itemType === 'Habit') {
                const cleanId = selectedEventForAction.id ? selectedEventForAction.id.replace(/^(task|event|habit|goal)-/, '') : '';
                const foundHabit = habits.find((h: any) => h.id === selectedEventForAction.id || h.id === cleanId) || selectedEventForAction;
                navigation.navigate('AddHabit', { existingHabit: foundHabit, habitId: foundHabit?.id });
              } else if (selectedEventForAction?.type === 'Goal' || selectedEventForAction?.itemType === 'Goal') {
                const cleanId = selectedEventForAction.id ? selectedEventForAction.id.replace(/^(task|event|habit|goal)-/, '') : '';
                const foundGoal = goals.find((g: any) => g.id === selectedEventForAction.id || g.id === cleanId) || selectedEventForAction;
                navigation.navigate('AddGoal', { existingGoal: foundGoal, goalId: foundGoal?.id });
              } else {
                const cleanId = selectedEventForAction.id ? selectedEventForAction.id.replace(/^(task|event|habit|goal)-/, '') : '';
                const foundEvent = events.find((e) => e.id === selectedEventForAction.id || e.id === cleanId) || selectedEventForAction;
                navigation.navigate('AddEvent', { existingEvent: foundEvent, eventId: foundEvent?.id });
              }
            }}
          >
            <Feather name="edit-2" size={18} color="#0F172A" style={{ marginRight: 12 }} />
            <Text style={[styles.actionModalText, { color: '#0F172A' }]}>
              Edit {selectedEventForAction?.type === 'Task' || selectedEventForAction?.itemType === 'Task' ? 'Task' : selectedEventForAction?.type || 'Event'}
            </Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.actionModalRow} onPress={handleDeleteEvent}>
            <Feather name="trash-2" size={18} color="#EF4444" style={{ marginRight: 12 }} />
            <Text style={[styles.actionModalText, { color: '#EF4444' }]}>
              Delete {selectedEventForAction?.type === 'Task' || selectedEventForAction?.itemType === 'Task' ? 'Task' : selectedEventForAction?.type || 'Event'}
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.modalCancelBtn}
            onPress={() => setShowEventActionModal(false)}
          >
            <Text style={styles.modalCancelText}>Cancel</Text>
          </TouchableOpacity>
        </View>
      </Modal>

      <SelectionModal
        visible={showFilterMenu}
        onClose={() => setShowFilterMenu(false)}
        title="Filter Plan"
        options={filterOptions}
        selectedValue={filterType}
        onSelect={(val) => {
          if (val === 'Manage') {
            setShowFilterMenu(false);
            navigation.navigate('Schedule');
          } else {
            setFilterType(val);
          }
        }}
      />
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
    paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) + 14 : 20,
    paddingBottom: 40,
  },

  /* 1. Top Header Row */
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
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

  /* Slogan Caveat Banner */
  sloganWrap: {
    alignSelf: 'flex-end',
    marginTop: -8,
    marginBottom: 10,
    marginRight: 6,
    alignItems: 'flex-start',
  },
  sloganText: {
    fontFamily: 'Caveat_700Bold',
    fontSize: 21,
    color: '#4D8000',
    lineHeight: 22,
  },
  sloganUnderline: {
    width: 140,
    height: 2.5,
    backgroundColor: '#66C400',
    borderRadius: 1.5,
    marginTop: 1,
  },

  /* 2. Title Section */
  titleSection: {
    marginBottom: 14,
  },
  mainTitle: {
    fontSize: 28,
    fontWeight: '900',
    color: '#0F172A',
    marginBottom: 2,
  },
  mainSubtitle: {
    fontSize: 13.5,
    color: '#64748B',
  },

  /* 3. Date Bar */
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
  dateRightGroup: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  todayBtnPill: {
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 14,
    paddingVertical: 7,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginRight: 6,
  },
  todayBtnText: {
    fontSize: 12.5,
    fontWeight: '600',
    color: '#0F172A',
  },
  dateArrowBtn: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    alignItems: 'center',
    justifyContent: 'center',
  },

  /* 4. Today's Workload Widget */
  workloadCard: {
    backgroundColor: '#F1F9E8',
    borderRadius: 18,
    padding: 14,
    borderWidth: 1,
    borderColor: '#E2F2D0',
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 18,
  },
  gaugeContainer: {
    marginRight: 10,
  },
  gaugeCircleOuter: {
    width: 48,
    height: 48,
    borderRadius: 24,
    borderWidth: 3.5,
    borderColor: '#66C400',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FFFFFF',
  },
  gaugePercentText: {
    fontSize: 12.5,
    fontWeight: '800',
    color: '#2D6A00',
  },
  workloadInfoCol: {
    flex: 1,
    paddingRight: 6,
  },
  workloadTitle: {
    fontSize: 13.5,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 2,
  },
  workloadSub: {
    fontSize: 11.5,
    fontWeight: '600',
    color: '#334155',
    marginBottom: 2,
  },
  workloadHint: {
    fontSize: 10.5,
    color: '#64748B',
  },
  workloadCtaCol: {
    alignItems: 'center',
  },
  planMyDayBtn: {
    backgroundColor: '#66C400',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 14,
    marginBottom: 3,
  },
  planMyDayText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  planMyDaySub: {
    fontSize: 9,
    color: '#64748B',
  },

  /* 5. Weekly Date Selector Strip */
  weeklyStripScroll: {
    marginHorizontal: -4,
    marginBottom: 18,
  },
  dayCard: {
    width: 62,
    height: 64,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    marginHorizontal: 4,
  },
  dayCardSelected: {
    backgroundColor: '#E2F7C5',
    borderWidth: 1.5,
    borderColor: '#66C400',
  },
  dayCardUnselected: {
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  dayCardLabel: {
    fontSize: 11.5,
    color: '#64748B',
    marginBottom: 2,
  },
  dayCardLabelSelected: {
    color: '#2D6A00',
    fontWeight: '700',
  },
  dayCardDate: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
  },
  dayCardDateSelected: {
    color: '#2D6A00',
  },

  /* 6. Today's Plan Main Schedule */
  whiteCardSection: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 18,
  },
  cardSectionHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 14,
  },
  planTitleGroup: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  cardSectionTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
  },
  addBtnWrap: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  addBtnText: {
    fontSize: 12.5,
    fontWeight: '600',
    color: '#64748B',
    marginLeft: 2,
  },

  timelineListContainer: {
    paddingLeft: 2,
  },
  scheduleRow: {
    flexDirection: 'row',
    marginBottom: 10,
  },
  scheduleTime: {
    width: 60,
    fontSize: 11.5,
    fontWeight: '600',
    color: '#64748B',
    paddingTop: 12,
  },
  timelineDotCol: {
    alignItems: 'center',
    width: 18,
    marginRight: 8,
  },
  timelineGrayDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#94A3B8',
    marginTop: 14,
  },
  timelineGreenDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#66C400',
    marginTop: 14,
  },
  timelineBlueDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#3B82F6',
    marginTop: 14,
  },
  timelineOrangeDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#F97316',
    marginTop: 14,
  },
  timelineOpenDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    borderWidth: 1.5,
    borderColor: '#94A3B8',
    backgroundColor: '#FFFFFF',
    marginTop: 14,
  },
  timelineVerticalLine: {
    width: 1.5,
    flex: 1,
    backgroundColor: '#E2E8F0',
    marginTop: 4,
  },

  scheduleCard: {
    flex: 1,
    borderRadius: 14,
    paddingHorizontal: 14,
    paddingVertical: 10,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderWidth: 1,
  },
  whiteScheduleCard: {
    backgroundColor: '#F8FAFC',
    borderColor: '#E2E8F0',
  },
  taskScheduleCard: {
    backgroundColor: '#F1FAEB',
    borderColor: '#E2F2D0',
  },
  eventScheduleCard: {
    backgroundColor: '#EFF6FF',
    borderColor: '#DBEAFE',
  },
  habitScheduleCard: {
    backgroundColor: '#FFF7ED',
    borderColor: '#FFEDD5',
  },

  scheduleCardMain: {
    flex: 1,
  },
  scheduleCardTitle: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
  },
  scheduleCardSub: {
    fontSize: 11.5,
    color: '#64748B',
    marginTop: 1,
  },

  badgeMenuRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  taskTypeBadge: {
    backgroundColor: '#D9F99D',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 10,
  },
  taskTypeBadgeText: {
    fontSize: 10.5,
    fontWeight: '700',
    color: '#2D6A00',
  },
  eventTypeBadge: {
    backgroundColor: '#BFDBFE',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 10,
  },
  eventTypeBadgeText: {
    fontSize: 10.5,
    fontWeight: '700',
    color: '#1D4ED8',
  },
  habitTypeBadge: {
    backgroundColor: '#FFEDD5',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 10,
  },
  habitTypeBadgeText: {
    fontSize: 10.5,
    fontWeight: '700',
    color: '#C2410C',
  },

  /* 7. LIVO Suggests */
  livoSuggestsCard: {
    backgroundColor: '#F5EFFF',
    borderRadius: 18,
    padding: 14,
    borderWidth: 1,
    borderColor: '#EDE9FE',
    marginBottom: 18,
  },
  suggestsHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  suggestsTitleWrap: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  suggestsTitleText: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0F172A',
  },
  suggestsContentBox: {
    backgroundColor: '#EFEBFF',
    borderRadius: 12,
    padding: 12,
  },
  suggestsGapTitle: {
    fontSize: 12.5,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 2,
  },
  suggestsGapSub: {
    fontSize: 11.5,
    color: '#64748B',
  },

  /* 8. Unscheduled Tasks Box */
  smallPlusBtn: {
    width: 22,
    height: 22,
    borderRadius: 11,
    backgroundColor: '#F1F5F9',
    alignItems: 'center',
    justifyContent: 'center',
  },
  unscheduledList: {
    marginBottom: 10,
  },
  taskCheckRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 8,
  },
  checkBoxCircle: {
    width: 16,
    height: 16,
    borderRadius: 8,
    borderWidth: 1.5,
    borderColor: '#CBD5E1',
    marginRight: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkBoxChecked: {
    backgroundColor: '#66C400',
    borderColor: '#66C400',
  },
  taskCheckLabel: {
    fontSize: 13,
    color: '#0F172A',
  },
  taskCheckLabelDone: {
    textDecorationLine: 'line-through',
    color: '#94A3B8',
  },
  viewAllTasksBtn: {
    alignSelf: 'flex-end',
    backgroundColor: '#EBF9DB',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 14,
  },
  viewAllTasksText: {
    fontSize: 11.5,
    fontWeight: '700',
    color: '#2D6A00',
  },

  /* 9. Upcoming Section */
  seeAllBtn: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  seeAllText: {
    fontSize: 12,
    color: '#64748B',
    marginRight: 2,
  },
  upcomingList: {
    paddingTop: 2,
  },
  upcomingRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#F8FAFC',
  },
  upcomingLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  calendarIconSquare: {
    width: 30,
    height: 30,
    borderRadius: 8,
    backgroundColor: '#F1F5F9',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  upcomingItemTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
  },
  upcomingItemTime: {
    fontSize: 11,
    color: '#64748B',
    marginTop: 1,
  },
  highPriorityPill: {
    backgroundColor: '#FFEBEB',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 10,
  },
  highPriorityText: {
    fontSize: 10.5,
    fontWeight: '700',
    color: '#DC2626',
  },
  mediumPriorityPill: {
    backgroundColor: '#F3E8FF',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 10,
  },
  mediumPriorityText: {
    fontSize: 10.5,
    fontWeight: '700',
    color: '#7C3AED',
  },
  lowPriorityPill: {
    backgroundColor: '#F1F5F9',
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 10,
  },
  lowPriorityText: {
    fontSize: 10.5,
    fontWeight: '700',
    color: '#64748B',
  },

  /* 10. Need help planning your day? */
  needHelpBanner: {
    backgroundColor: '#F5EFFF',
    borderRadius: 18,
    padding: 14,
    borderWidth: 1,
    borderColor: '#EDE9FE',
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  needHelpLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  sparkleCircleSmall: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: '#E9D5FF',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  needHelpTitle: {
    fontSize: 12.5,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 2,
  },
  needHelpSub: {
    fontSize: 10.5,
    color: '#64748B',
    lineHeight: 14,
  },
  chatWithLivoPill: {
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#DDD6FE',
  },
  chatWithLivoText: {
    fontSize: 11,
    fontWeight: '700',
    color: '#7C3AED',
  },

  /* Modals */
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.4)',
  },
  modalContent: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 24,
    paddingBottom: 40,
  },
  modalGrabber: {
    width: 38,
    height: 4,
    borderRadius: 2,
    backgroundColor: '#CBD5E1',
    alignSelf: 'center',
    marginBottom: 16,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 16,
  },
  detailModalTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 16,
  },
  detailRowsContainer: {
    marginBottom: 8,
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 9,
  },
  detailIconWrap: {
    width: 28,
    marginRight: 10,
    alignItems: 'flex-start',
    justifyContent: 'center',
  },
  detailRowText: {
    fontSize: 14,
    fontWeight: '500',
    color: '#334155',
  },
  detailDescriptionText: {
    fontSize: 13.5,
    fontWeight: '400',
    color: '#64748B',
    lineHeight: 20,
    flex: 1,
  },
  detailPriorityPill: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
  },
  detailPriorityText: {
    fontSize: 12,
    fontWeight: '700',
  },
  scheduleCardMainTouch: {
    flex: 1,
    paddingRight: 8,
  },
  threeDotBtn: {
    padding: 6,
    marginLeft: 4,
  },
  actionModalRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: '#F8FAFC',
  },
  actionModalText: {
    fontSize: 15,
    fontWeight: '600',
  },
  modalCancelBtn: {
    backgroundColor: '#F1F5F9',
    borderRadius: 14,
    paddingVertical: 12,
    alignItems: 'center',
    marginTop: 20,
  },
  modalCancelText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#475569',
  },
  scanAddCard: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#FFFFFF',
    padding: 16,
    borderRadius: 16,
    marginBottom: 24,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.05,
    shadowRadius: 10,
    elevation: 3,
  },
  scanAddLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  scanAddIconWrap: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: '#F0FDF4',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  scanAddTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 2,
  },
  scanAddSub: {
    fontSize: 13,
    color: '#64748B',
  },
});
