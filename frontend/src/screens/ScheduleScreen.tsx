import React, { useState, useEffect, useMemo, useCallback } from 'react';
import {
  View,
  Text,
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
import { Feather, Ionicons } from '@expo/vector-icons';
import { useFocusEffect, useNavigation } from '@react-navigation/native';
import { useTasks } from '../hooks/useTasks';
import { useEvents } from '../hooks/useEvents';
import { useHabits } from '../hooks/useHabits';

interface ScheduleScreenProps {
  onBack?: () => void;
}

interface TimelineItem {
  id: string;
  rawId: string;
  sourceType: 'task' | 'event' | 'habit';
  title: string;
  subtitle: string;
  timeFormatted: string;
  startMinutes: number;
  endMinutes: number;
  isNow: boolean;
  completed: boolean;
  originalData: any;
}

// Helpers for robust date and time parsing
const parseDateHelper = (dateInput?: string | Date | null): Date | null => {
  if (!dateInput) return null;
  if (dateInput instanceof Date) return isNaN(dateInput.getTime()) ? null : dateInput;

  const str = String(dateInput).trim();
  if (!str) return null;

  const lower = str.toLowerCase();
  const today = new Date();

  if (lower === 'today' || lower.startsWith('today')) {
    return today;
  }
  if (lower === 'tomorrow' || lower.startsWith('tomorrow')) {
    const tmrw = new Date(today);
    tmrw.setDate(tmrw.getDate() + 1);
    return tmrw;
  }
  if (lower === 'yesterday' || lower.startsWith('yesterday')) {
    const yest = new Date(today);
    yest.setDate(yest.getDate() - 1);
    return yest;
  }

  const d = new Date(str);
  if (!isNaN(d.getTime())) return d;

  const parts = str.match(/^(\d{4})-(\d{1,2})-(\d{1,2})/);
  if (parts) {
    return new Date(parseInt(parts[1], 10), parseInt(parts[2], 10) - 1, parseInt(parts[3], 10));
  }

  return null;
};

const isSameDay = (date1?: Date | string | null, date2?: Date | string | null): boolean => {
  if (!date1 || !date2) return false;
  const d1 = typeof date1 === 'string' ? parseDateHelper(date1) : new Date(date1);
  const d2 = typeof date2 === 'string' ? parseDateHelper(date2) : new Date(date2);
  if (!d1 || !d2 || isNaN(d1.getTime()) || isNaN(d2.getTime())) return false;

  return (
    d1.getFullYear() === d2.getFullYear() &&
    d1.getMonth() === d2.getMonth() &&
    d1.getDate() === d2.getDate()
  );
};

const parseScheduleTime = (
  timeInput?: string | Date | null
): { hours: number; minutes: number; formatted: string; totalMinutes: number } | null => {
  if (!timeInput) return null;
  try {
    if (timeInput instanceof Date) {
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

export const ScheduleScreen: React.FC<ScheduleScreenProps> = ({ onBack }) => {
  const navigation = useNavigation<any>();
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [currentTime, setCurrentTime] = useState<Date>(new Date());

  const { tasks, toggleTask, deleteTask, refreshTasks } = useTasks();
  const { events, deleteEvent, refreshEvents } = useEvents();
  const { habits, toggleHabit, deleteHabit, refreshHabits } = useHabits();

  // Selected item for action sheet modal
  const [selectedActionItem, setSelectedActionItem] = useState<TimelineItem | null>(null);
  const [showActionModal, setShowActionModal] = useState(false);

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  // Live clock updating every 30 seconds
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 30000);
    return () => clearInterval(timer);
  }, []);

  useFocusEffect(
    useCallback(() => {
      refreshTasks?.();
      refreshEvents?.();
      refreshHabits?.();
      setCurrentTime(new Date());
    }, [refreshTasks, refreshEvents, refreshHabits])
  );

  const isToday = useMemo(() => {
    const today = new Date();
    return (
      selectedDate.getFullYear() === today.getFullYear() &&
      selectedDate.getMonth() === today.getMonth() &&
      selectedDate.getDate() === today.getDate()
    );
  }, [selectedDate]);

  const formattedCurrentTime = useMemo(() => {
    const hours = currentTime.getHours();
    const minutes = currentTime.getMinutes();
    const ampm = hours >= 12 ? 'PM' : 'AM';
    const displayHours = hours % 12 || 12;
    return `${String(displayHours).padStart(2, '0')}:${String(minutes).padStart(2, '0')} ${ampm}`;
  }, [currentTime]);

  const handlePrevDate = () => {
    setSelectedDate((prev) => {
      const next = new Date(prev);
      next.setDate(next.getDate() - 1);
      return next;
    });
  };

  const handleNextDate = () => {
    setSelectedDate((prev) => {
      const next = new Date(prev);
      next.setDate(next.getDate() + 1);
      return next;
    });
  };

  const handleResetToday = () => {
    setSelectedDate(new Date());
  };

  const getFormattedDateString = (date: Date) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const target = new Date(date);
    target.setHours(0, 0, 0, 0);

    const diffTime = target.getTime() - today.getTime();
    const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24));

    const dayName = date.toLocaleDateString('en-US', { weekday: 'short' });
    const monthName = date.toLocaleDateString('en-US', { month: 'short' });
    const dayNum = date.getDate();

    let prefix = '';
    if (diffDays === 0) {
      prefix = 'Today, ';
    } else if (diffDays === 1) {
      prefix = 'Tomorrow, ';
    } else if (diffDays === -1) {
      prefix = 'Yesterday, ';
    }

    return `${prefix}${dayName} ${dayNum} ${monthName}`;
  };

  // 1. Normalized Scheduled Items for Selected Date
  const timelineItems = useMemo<TimelineItem[]>(() => {
    const rawList: TimelineItem[] = [];

    // Filter Tasks with time
    tasks.forEach((t) => {
      if (t.status === 'CANCELLED') return;
      if (!t.time) return;

      const belongsToDate =
        isSameDay(t.date, selectedDate) ||
        isSameDay(t.dueDate, selectedDate) ||
        (!t.date && !t.dueDate && isSameDay(t.time, selectedDate)) ||
        (!t.date && !t.dueDate && isToday && String(t.time).toLowerCase().includes('today'));

      if (!belongsToDate) return;

      const parsedTime = parseScheduleTime(t.time);
      if (!parsedTime) return;

      const durMins = parseDurationMinutes(t.duration || t.estimatedMinutes);
      const subtitle = t.description?.trim() || t.category?.trim() || 'Task';

      rawList.push({
        id: `task-${t.id}`,
        rawId: t.id,
        sourceType: 'task',
        title: t.title,
        subtitle,
        timeFormatted: parsedTime.formatted,
        startMinutes: parsedTime.totalMinutes,
        endMinutes: parsedTime.totalMinutes + durMins,
        isNow: false,
        completed: !!t.completed || t.status === 'COMPLETED',
        originalData: t,
      });
    });

    // Filter Events
    events.forEach((e) => {
      const belongsToDate = isSameDay(e.date, selectedDate) || isSameDay(e.startTime, selectedDate);
      if (!belongsToDate) return;

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
        rawId: e.id,
        sourceType: 'event',
        title: e.title,
        subtitle,
        timeFormatted: parsedTime.formatted,
        startMinutes: parsedTime.totalMinutes,
        endMinutes,
        isNow: false,
        completed: false,
        originalData: e,
      });
    });

    // Filter Habits with time
    habits.forEach((h) => {
      const habitTime = h.time || h.reminderTime;
      if (!habitTime) return;

      const isDaily = !h.frequency || h.frequency.toUpperCase() === 'DAILY';
      const habitDate = h.date;
      const belongsToDate = isDaily || (habitDate ? isSameDay(habitDate, selectedDate) : true);
      if (!belongsToDate) return;

      const parsedTime = parseScheduleTime(habitTime);
      if (!parsedTime) return;

      const subtitle = h.description?.trim() || h.category?.trim() || 'Daily habit';

      rawList.push({
        id: `habit-${h.id}`,
        rawId: h.id,
        sourceType: 'habit',
        title: h.title,
        subtitle,
        timeFormatted: parsedTime.formatted,
        startMinutes: parsedTime.totalMinutes,
        endMinutes: parsedTime.totalMinutes + 30,
        isNow: false,
        completed: isToday ? !!h.completedToday : false,
        originalData: h,
      });
    });

    // Sort chronologically
    rawList.sort((a, b) => a.startMinutes - b.startMinutes);

    // If today, determine active "Now" item
    if (isToday) {
      const currentTotalMinutes = currentTime.getHours() * 60 + currentTime.getMinutes();
      let activeId: string | null = null;
      let latestActiveStart = -1;

      for (const item of rawList) {
        if (
          currentTotalMinutes >= item.startMinutes &&
          currentTotalMinutes < item.endMinutes &&
          item.startMinutes >= latestActiveStart
        ) {
          activeId = item.id;
          latestActiveStart = item.startMinutes;
        }
      }

      if (activeId) {
        rawList.forEach((item) => {
          item.isNow = item.id === activeId;
        });
      }
    }

    return rawList;
  }, [tasks, events, habits, selectedDate, isToday, currentTime]);

  // 2. Unscheduled Tasks for Selected Date
  const unscheduledTasks = useMemo(() => {
    return tasks.filter((t) => {
      if (t.completed || t.status === 'COMPLETED' || t.status === 'CANCELLED') return false;
      // Must not have a scheduled time
      const hasTime = !!t.time && t.time.trim() !== '';
      if (hasTime) return false;

      // Belongs to selected date OR is a general unscheduled task
      if (!t.date && !t.dueDate) return true;
      return isSameDay(t.date, selectedDate) || isSameDay(t.dueDate, selectedDate);
    });
  }, [tasks, selectedDate]);

  // 3. Dynamic Real-time Statistics
  const stats = useMemo(() => {
    let plannedMinutes = 0;
    timelineItems.forEach((item) => {
      plannedMinutes += Math.max(15, item.endMinutes - item.startMinutes);
    });

    const plannedHours = Math.floor(plannedMinutes / 60);
    const plannedMins = plannedMinutes % 60;
    const plannedText = plannedHours > 0 ? `${plannedHours}h ${plannedMins}m` : `${plannedMins}m`;

    const taskCount = timelineItems.filter((i) => i.sourceType === 'task').length + unscheduledTasks.length;
    const eventCount = timelineItems.filter((i) => i.sourceType === 'event').length;

    let workload = 'Light';
    if (plannedMinutes >= 360 || taskCount + eventCount >= 8) {
      workload = 'Heavy';
    } else if (plannedMinutes >= 180 || taskCount + eventCount >= 4) {
      workload = 'Medium';
    }

    return {
      plannedText,
      taskCount,
      eventCount,
      workload,
    };
  }, [timelineItems, unscheduledTasks]);

  const handleOpenActionModal = (item: TimelineItem) => {
    setSelectedActionItem(item);
    setShowActionModal(true);
  };

  const handleActionEdit = () => {
    if (!selectedActionItem) return;
    const item = selectedActionItem;
    setShowActionModal(false);

    if (item.sourceType === 'task') {
      navigation.navigate('AddTask', { existingTask: item.originalData });
    } else if (item.sourceType === 'event') {
      navigation.navigate('AddEvent', { existingEvent: item.originalData });
    }
  };

  const handleActionToggleComplete = async () => {
    if (!selectedActionItem) return;
    setShowActionModal(false);

    if (selectedActionItem.sourceType === 'task') {
      await toggleTask(selectedActionItem.rawId);
    } else if (selectedActionItem.sourceType === 'habit') {
      await toggleHabit(selectedActionItem.rawId);
    }
  };

  const handleActionDelete = () => {
    if (!selectedActionItem) return;
    const item = selectedActionItem;
    setShowActionModal(false);

    Alert.alert(
      'Delete Item',
      `Are you sure you want to delete "${item.title}"?`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            if (item.sourceType === 'task') {
              await deleteTask(item.rawId);
            } else if (item.sourceType === 'event') {
              await deleteEvent(item.rawId);
            } else if (item.sourceType === 'habit') {
              await deleteHabit(item.rawId);
            }
          },
        },
      ]
    );
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#F8FAF5" />

      {/* Header Bar */}
      <View style={styles.headerBar}>
        <TouchableOpacity
          style={styles.backBtn}
          onPress={handleBack}
          hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
        >
          <Feather name="arrow-left" size={22} color="#0F172A" />
        </TouchableOpacity>

        <View style={styles.headerTitleWrap}>
          <Text style={styles.headerTitle}>Schedule</Text>
          <Text style={styles.headerSub}>See your day at a glance.</Text>
        </View>
      </View>

      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.contentContainer}
        showsVerticalScrollIndicator={false}
      >
        {/* Date Selector Row */}
        <View style={styles.dateBarRow}>
          <TouchableOpacity style={styles.dateNavBtn} onPress={handlePrevDate}>
            <Feather name="chevron-left" size={16} color="#475569" />
          </TouchableOpacity>

          <TouchableOpacity style={styles.datePickerPill} onPress={handleResetToday}>
            <Feather name="calendar" size={14} color="#64748B" style={{ marginRight: 6 }} />
            <Text style={styles.datePickerText}>{getFormattedDateString(selectedDate)}</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.dateNavBtn} onPress={handleNextDate}>
            <Feather name="chevron-right" size={16} color="#475569" />
          </TouchableOpacity>
        </View>

        {/* Workload / Metrics Summary Card */}
        <View style={styles.metricsCard}>
          {/* Planned Time */}
          <View style={styles.metricCol}>
            <View style={styles.metricIconWrap}>
              <Feather name="clock" size={16} color="#2D6A00" />
            </View>
            <View>
              <Text style={styles.metricValue}>{stats.plannedText}</Text>
              <Text style={styles.metricLabel}>planned</Text>
            </View>
          </View>

          <View style={styles.metricDivider} />

          {/* Tasks Count */}
          <View style={styles.metricCol}>
            <View style={styles.metricIconWrap}>
              <Feather name="check-square" size={16} color="#2D6A00" />
            </View>
            <View>
              <Text style={styles.metricValue}>{stats.taskCount}</Text>
              <Text style={styles.metricLabel}>tasks</Text>
            </View>
          </View>

          <View style={styles.metricDivider} />

          {/* Events Count */}
          <View style={styles.metricCol}>
            <View style={styles.metricIconWrap}>
              <Feather name="calendar" size={16} color="#2D6A00" />
            </View>
            <View>
              <Text style={styles.metricValue}>{stats.eventCount}</Text>
              <Text style={styles.metricLabel}>events</Text>
            </View>
          </View>

          <View style={styles.metricDivider} />

          {/* Workload */}
          <TouchableOpacity
            style={styles.metricCol}
            onPress={() => {
              navigation.navigate('OverloadedDay', {
                plannedWork: stats.plannedText,
                availableTime: '5h 45m',
                totalTasks: stats.taskCount,
                totalEvents: stats.eventCount,
              });
            }}
            activeOpacity={0.7}
          >
            <View style={styles.metricIconWrap}>
              <Ionicons name="stats-chart" size={16} color={stats.workload === 'Heavy' ? '#DC2626' : '#2D6A00'} />
            </View>
            <View>
              <Text style={[styles.metricValue, stats.workload === 'Heavy' && { color: '#DC2626' }]}>
                {stats.workload}
              </Text>
              <Text style={styles.metricLabel}>workload</Text>
            </View>
          </TouchableOpacity>
        </View>

        {/* Main Schedule Timeline Card */}
        <View style={styles.timelineCard}>
          {/* Current Time Bar (Shown only if viewing today) */}
          {isToday && (
            <View style={styles.currentTimeBar}>
              <View style={styles.currentTimeLeft}>
                <View style={styles.greenPulseDot} />
                <Text style={styles.currentTimeText}>{formattedCurrentTime}</Text>
              </View>
              <View style={styles.dashedLine} />
              <Text style={styles.currentTimeLabel}>Current time</Text>
            </View>
          )}

          {/* Schedule List */}
          {timelineItems.length === 0 ? (
            <TouchableOpacity
              style={styles.emptyTimelineWrap}
              onPress={() => navigation.navigate('FreeDay', { selectedDate: getFormattedDateString(selectedDate) })}
              activeOpacity={0.7}
            >
              <Feather name="calendar" size={28} color="#0D9488" style={{ marginBottom: 6 }} />
              <Text style={styles.emptyTimelineTitle}>No scheduled activities</Text>
              <Text style={styles.emptyTimelineSub}>It's a free day! Tap to view suggestions & plan time.</Text>
              <View style={{ flexDirection: 'row', alignItems: 'center', marginTop: 8, backgroundColor: '#EBF9DB', paddingHorizontal: 12, paddingVertical: 6, borderRadius: 16 }}>
                <Text style={{ color: '#2D6A00', fontSize: 12, fontWeight: '700' }}>Open Free Day View →</Text>
              </View>
            </TouchableOpacity>
          ) : (
            <View style={styles.timelineList}>
              {timelineItems.map((item, index) => {
                const isLast = index === timelineItems.length - 1;
                return (
                  <View key={item.id} style={styles.timelineItemRow}>
                    <View style={styles.timeCol}>
                      <Text style={item.isNow ? styles.timeTextActive : styles.timeText}>
                        {item.timeFormatted}
                      </Text>
                    </View>

                    <View style={styles.dotLineCol}>
                      {item.isNow ? (
                        <View style={styles.outerRingDot}>
                          <View style={styles.innerRingDot} />
                        </View>
                      ) : (
                        <View style={styles.grayDot} />
                      )}
                      {!isLast && <View style={styles.verticalLine} />}
                    </View>

                    <View style={styles.itemContentCol}>
                      <View style={styles.itemMainRow}>
                        <TouchableOpacity
                          style={styles.itemTitleWrap}
                          onPress={() => handleOpenActionModal(item)}
                          activeOpacity={0.7}
                        >
                          <Text
                            style={[
                              styles.itemTitle,
                              item.completed && styles.taskTitleChecked,
                            ]}
                          >
                            {item.title}
                          </Text>
                          {item.isNow && (
                            <View style={styles.nowBadge}>
                              <Text style={styles.nowBadgeText}>Now</Text>
                            </View>
                          )}
                        </TouchableOpacity>
                        <TouchableOpacity
                          style={styles.moreBtn}
                          onPress={() => handleOpenActionModal(item)}
                          hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
                        >
                          <Feather name="more-horizontal" size={16} color="#94A3B8" />
                        </TouchableOpacity>
                      </View>
                      <Text style={styles.itemSubText}>{item.subtitle}</Text>
                    </View>
                  </View>
                );
              })}
            </View>
          )}
        </View>

        {/* Unscheduled Section */}
        <View style={styles.unscheduledCard}>
          <View style={styles.unscheduledHeaderRow}>
            <View>
              <Text style={styles.unscheduledTitle}>Unscheduled</Text>
              <Text style={styles.unscheduledSub}>Tasks you can plan for later.</Text>
            </View>

            <TouchableOpacity
              style={styles.seeAllBtn}
              onPress={() => {
                if (onBack) onBack();
              }}
              activeOpacity={0.7}
            >
              <Text style={styles.seeAllText}>See all</Text>
              <Feather name="arrow-right" size={14} color="#66C400" style={{ marginLeft: 2 }} />
            </TouchableOpacity>
          </View>

          <View style={styles.unscheduledList}>
            {unscheduledTasks.length === 0 ? (
              <View style={styles.emptyUnscheduledWrap}>
                <Text style={styles.emptyUnscheduledText}>All tasks are scheduled! 🎉</Text>
              </View>
            ) : (
              unscheduledTasks.map((task) => (
                <View key={task.id} style={styles.unscheduledRow}>
                  <TouchableOpacity
                    style={styles.checkbox}
                    onPress={() => toggleTask(task.id)}
                    activeOpacity={0.7}
                  >
                    {task.completed && <Feather name="check" size={14} color="#66C400" />}
                  </TouchableOpacity>

                  <View style={styles.taskInfoCol}>
                    <Text
                      style={[
                        styles.taskTitle,
                        task.completed && styles.taskTitleChecked,
                      ]}
                    >
                      {task.title}
                    </Text>
                    <Text style={styles.taskSub}>
                      {task.description || task.category || 'Flexible task'}
                    </Text>
                  </View>

                  <TouchableOpacity
                    style={styles.addPillBtn}
                    onPress={() => navigation.navigate('AddTask', { existingTask: task })}
                    activeOpacity={0.7}
                  >
                    <Text style={styles.addPillText}>Add</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={styles.moreBtn}
                    onPress={() => {
                      handleOpenActionModal({
                        id: `task-${task.id}`,
                        rawId: task.id,
                        sourceType: 'task',
                        title: task.title,
                        subtitle: task.description || task.category || 'Task',
                        timeFormatted: '',
                        startMinutes: 0,
                        endMinutes: 0,
                        isNow: false,
                        completed: !!task.completed,
                        originalData: task,
                      });
                    }}
                    hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
                  >
                    <Feather name="more-horizontal" size={16} color="#94A3B8" />
                  </TouchableOpacity>
                </View>
              ))
            )}
          </View>
        </View>

        {/* LIVO Suggests Banner */}
        <View style={styles.suggestsCard}>
          <View style={styles.suggestsLeft}>
            <View style={styles.starIconBadge}>
              <Ionicons name="star" size={14} color="#7C3AED" />
            </View>
            <View style={styles.suggestsTextWrap}>
              <Text style={styles.suggestsTitle}>LIVO Suggests</Text>
              <Text style={styles.suggestsBody}>
                {stats.workload === 'Heavy'
                  ? 'Your schedule is packed today. Remember to take regular breaks!'
                  : 'You have open focus time available today. Want to work on your goals?'}
              </Text>
            </View>
          </View>

          <TouchableOpacity
            style={styles.addToPlanBtn}
            onPress={() => {
              if (onBack) onBack();
            }}
            activeOpacity={0.7}
          >
            <Text style={styles.addToPlanText}>Add to plan</Text>
          </TouchableOpacity>
        </View>

        <View style={{ height: 30 }} />
      </ScrollView>

      {/* Item Action Modal */}
      <Modal
        visible={showActionModal}
        transparent
        animationType="fade"
        onRequestClose={() => setShowActionModal(false)}
      >
        <TouchableWithoutFeedback onPress={() => setShowActionModal(false)}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>

        <View style={styles.actionModalContent}>
          <View style={styles.actionModalHeader}>
            <Text style={styles.actionModalTitle} numberOfLines={1}>
              {selectedActionItem?.title || 'Item Options'}
            </Text>
            <TouchableOpacity
              onPress={() => setShowActionModal(false)}
              hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
            >
              <Feather name="x" size={20} color="#64748B" />
            </TouchableOpacity>
          </View>

          {selectedActionItem?.sourceType === 'task' && (
            <TouchableOpacity
              style={styles.actionRowBtn}
              onPress={handleActionToggleComplete}
              activeOpacity={0.7}
            >
              <Feather
                name={selectedActionItem.completed ? 'rotate-ccw' : 'check-circle'}
                size={18}
                color="#66C400"
                style={{ marginRight: 12 }}
              />
              <Text style={styles.actionRowText}>
                {selectedActionItem.completed ? 'Mark as Incomplete' : 'Mark as Completed'}
              </Text>
            </TouchableOpacity>
          )}

          <TouchableOpacity
            style={styles.actionRowBtn}
            onPress={handleActionEdit}
            activeOpacity={0.7}
          >
            <Feather name="edit-2" size={18} color="#2563EB" style={{ marginRight: 12 }} />
            <Text style={styles.actionRowText}>Edit Details</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.actionRowBtn, { borderBottomWidth: 0 }]}
            onPress={handleActionDelete}
            activeOpacity={0.7}
          >
            <Feather name="trash-2" size={18} color="#EF4444" style={{ marginRight: 12 }} />
            <Text style={[styles.actionRowText, { color: '#EF4444' }]}>Delete Item</Text>
          </TouchableOpacity>
        </View>
      </Modal>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#F8FAF5',
  },

  /* Header Bar */
  headerBar: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) + 8 : 12,
    backgroundColor: '#F8FAF5',
  },
  backBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  headerTitleWrap: {
    flex: 1,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: '800',
    color: '#0F172A',
  },
  headerSub: {
    fontSize: 13,
    color: '#64748B',
    marginTop: 1,
  },

  container: {
    flex: 1,
  },
  contentContainer: {
    paddingHorizontal: 16,
    paddingTop: 8,
  },

  /* Date Bar */
  dateBarRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  dateNavBtn: {
    width: 34,
    height: 34,
    borderRadius: 12,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    alignItems: 'center',
    justifyContent: 'center',
  },
  datePickerPill: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 14,
    paddingVertical: 8,
    marginHorizontal: 8,
  },
  datePickerText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
  },

  /* Metrics Card */
  metricsCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    paddingVertical: 14,
    paddingHorizontal: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 16,
    justifyContent: 'space-between',
  },
  metricCol: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  metricIconWrap: {
    width: 30,
    height: 30,
    borderRadius: 8,
    backgroundColor: '#E2F7C5',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 6,
  },
  metricValue: {
    fontSize: 13,
    fontWeight: '800',
    color: '#0F172A',
  },
  metricLabel: {
    fontSize: 10,
    color: '#64748B',
    marginTop: -2,
  },
  metricDivider: {
    width: 1,
    height: 24,
    backgroundColor: '#F1F5F9',
  },

  /* Timeline Card */
  timelineCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    padding: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 16,
  },

  /* Current time bar */
  currentTimeBar: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 20,
  },
  currentTimeLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  greenPulseDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#66C400',
    marginRight: 6,
  },
  currentTimeText: {
    fontSize: 12.5,
    fontWeight: '800',
    color: '#2D6A00',
  },
  dashedLine: {
    flex: 1,
    height: 1,
    borderWidth: 1,
    borderColor: '#66C400',
    borderStyle: 'dashed',
    marginHorizontal: 8,
  },
  currentTimeLabel: {
    fontSize: 12,
    fontWeight: '700',
    color: '#2D6A00',
  },

  /* Timeline list */
  timelineList: {
    paddingLeft: 2,
  },
  timelineItemRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 16,
  },
  timeCol: {
    width: 65,
    paddingTop: 2,
  },
  timeTextActive: {
    fontSize: 13,
    fontWeight: '700',
    color: '#64748B',
  },
  timeText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#94A3B8',
  },
  dotLineCol: {
    alignItems: 'center',
    marginRight: 14,
    width: 20,
  },
  outerRingDot: {
    width: 18,
    height: 18,
    borderRadius: 9,
    borderWidth: 2,
    borderColor: '#66C400',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FFFFFF',
  },
  innerRingDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#66C400',
  },
  grayDot: {
    width: 14,
    height: 14,
    borderRadius: 7,
    backgroundColor: '#CBD5E1',
    marginTop: 2,
  },
  verticalLine: {
    width: 2,
    flex: 1,
    backgroundColor: '#E2E8F0',
    marginTop: 4,
    marginBottom: -16,
  },

  itemContentCol: {
    flex: 1,
  },
  itemMainRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  itemTitleWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  itemTitle: {
    fontSize: 14.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  nowBadge: {
    backgroundColor: '#E2F7C5',
    borderRadius: 10,
    paddingHorizontal: 8,
    paddingVertical: 2,
    marginLeft: 8,
  },
  nowBadgeText: {
    fontSize: 11,
    fontWeight: '700',
    color: '#2D6A00',
  },
  itemSubText: {
    fontSize: 12.5,
    color: '#94A3B8',
    marginTop: 2,
  },
  moreBtn: {
    padding: 4,
  },

  emptyTimelineWrap: {
    paddingVertical: 24,
    alignItems: 'center',
    justifyContent: 'center',
  },
  emptyTimelineTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: '#475569',
  },
  emptyTimelineSub: {
    fontSize: 12,
    color: '#94A3B8',
    marginTop: 2,
    textAlign: 'center',
  },

  /* Unscheduled Card */
  unscheduledCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    padding: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 16,
  },
  unscheduledHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 14,
  },
  unscheduledTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
  },
  unscheduledSub: {
    fontSize: 12,
    color: '#94A3B8',
    marginTop: 1,
  },
  seeAllBtn: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  seeAllText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#66C400',
  },

  unscheduledList: {},
  unscheduledRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    borderTopWidth: 1,
    borderTopColor: '#F8FAFC',
  },
  checkbox: {
    width: 20,
    height: 20,
    borderRadius: 6,
    borderWidth: 1.5,
    borderColor: '#CBD5E1',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  taskInfoCol: {
    flex: 1,
  },
  taskTitle: {
    fontSize: 13.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  taskTitleChecked: {
    textDecorationLine: 'line-through',
    color: '#94A3B8',
  },
  taskSub: {
    fontSize: 11.5,
    color: '#94A3B8',
    marginTop: 1,
  },
  addPillBtn: {
    backgroundColor: '#E2F7C5',
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 4,
    marginRight: 6,
  },
  addPillText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#2D6A00',
  },
  emptyUnscheduledWrap: {
    paddingVertical: 14,
    alignItems: 'center',
    justifyContent: 'center',
  },
  emptyUnscheduledText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#64748B',
  },

  /* LIVO Suggests Card */
  suggestsCard: {
    backgroundColor: '#F3E8FF',
    borderRadius: 18,
    padding: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderWidth: 1,
    borderColor: '#E9D5FF',
  },
  suggestsLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    paddingRight: 8,
  },
  starIconBadge: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: '#DDD6FE',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  suggestsTextWrap: {
    flex: 1,
  },
  suggestsTitle: {
    fontSize: 13.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  suggestsBody: {
    fontSize: 11,
    color: '#64748B',
    marginTop: 1,
    lineHeight: 15,
  },
  addToPlanBtn: {
    backgroundColor: '#EDE9FE',
    borderRadius: 14,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  addToPlanText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#7C3AED',
  },

  /* Action Modal */
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.4)',
  },
  actionModalContent: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 20,
    paddingBottom: 36,
  },
  actionModalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
    paddingBottom: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  actionModalTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
    flex: 1,
    marginRight: 10,
  },
  actionRowBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: '#F8FAFC',
  },
  actionRowText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#334155',
  },
});
