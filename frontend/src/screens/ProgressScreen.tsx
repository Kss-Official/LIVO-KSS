import React, { useState, useMemo, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  Platform,
} from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';
import { useFocusEffect, useNavigation } from '@react-navigation/native';
import { useTasks } from '../hooks/useTasks';
import { useEvents } from '../hooks/useEvents';
import { useHabits } from '../hooks/useHabits';
import { useGoals } from '../hooks/useGoals';
import { SelectionModal } from '../components/forms/SelectionModal';
import { AddTaskScreen } from './AddTaskScreen';
import { AddEventScreen } from './AddEventScreen';
import { AddHabitScreen } from './AddHabitScreen';
import { AddGoalScreen } from './AddGoalScreen';

interface ProgressScreenProps {
  onBack?: () => void;
}

// Helpers for robust date matching
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

export const ProgressScreen: React.FC<ProgressScreenProps> = ({ onBack }) => {
  const navigation = useNavigation<any>();
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [timeframe, setTimeframe] = useState<'Today' | 'This Week' | 'This Month' | 'All Time'>('Today');
  const [showTimeframeModal, setShowTimeframeModal] = useState(false);

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  // Sub-screens for direct editing/viewing
  const [showAddTask, setShowAddTask] = useState(false);
  const [showAddEvent, setShowAddEvent] = useState(false);
  const [showAddHabit, setShowAddHabit] = useState(false);
  const [showAddGoal, setShowAddGoal] = useState(false);

  const { tasks, refreshTasks } = useTasks();
  const { events, refreshEvents } = useEvents();
  const { habits, refreshHabits } = useHabits();
  const { goals, refreshGoals } = useGoals();

  useFocusEffect(
    useCallback(() => {
      refreshTasks?.();
      refreshEvents?.();
      refreshHabits?.();
      refreshGoals?.();
    }, [refreshTasks, refreshEvents, refreshHabits, refreshGoals])
  );

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

  const isToday = useMemo(() => {
    const today = new Date();
    return (
      selectedDate.getFullYear() === today.getFullYear() &&
      selectedDate.getMonth() === today.getMonth() &&
      selectedDate.getDate() === today.getDate()
    );
  }, [selectedDate]);

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

  // 1. Filter Tasks for the selected period
  const filteredTasks = useMemo(() => {
    if (timeframe === 'All Time') return tasks;
    return tasks.filter((t) => {
      if (t.status === 'CANCELLED') return false;
      if (timeframe === 'Today') {
        return (
          isSameDay(t.date, selectedDate) ||
          isSameDay(t.dueDate, selectedDate) ||
          (!t.date && !t.dueDate && isSameDay(t.time, selectedDate)) ||
          (!t.date && !t.dueDate && isToday)
        );
      }
      return true;
    });
  }, [tasks, selectedDate, timeframe, isToday]);

  const totalTasks = filteredTasks.length;
  const completedTasks = filteredTasks.filter((t) => t.completed || t.status === 'COMPLETED').length;
  const taskRatio = totalTasks === 0 ? 0 : completedTasks / totalTasks;

  // 2. Filter Events for the selected period
  const filteredEvents = useMemo(() => {
    if (timeframe === 'All Time') return events;
    return events.filter((e) => {
      if (timeframe === 'Today') {
        return isSameDay(e.date, selectedDate) || isSameDay(e.startTime, selectedDate);
      }
      return true;
    });
  }, [events, selectedDate, timeframe]);

  const totalEvents = filteredEvents.length;
  const completedEvents = filteredEvents.filter((e) => {
    if (e.date) {
      const d = parseDateHelper(e.date);
      if (d && d.getTime() < new Date().getTime()) return true;
    }
    return false;
  }).length;
  const eventRatio = totalEvents === 0 ? 0 : completedEvents / totalEvents;

  // 3. Filter Habits for the selected period
  const filteredHabits = useMemo(() => {
    if (timeframe === 'All Time') return habits;
    return habits.filter((h) => {
      const isDaily = !h.frequency || h.frequency.toUpperCase() === 'DAILY';
      if (timeframe === 'Today') {
        return isDaily || (h.date ? isSameDay(h.date, selectedDate) : true);
      }
      return true;
    });
  }, [habits, selectedDate, timeframe]);

  const totalHabits = filteredHabits.length;
  const completedHabits = filteredHabits.filter((h) => {
    if (isToday) return !!h.completedToday;
    return (h.streakCount || 0) > 0;
  }).length;
  const habitRatio = totalHabits === 0 ? 0 : completedHabits / totalHabits;

  // 4. Goals Progress
  const totalGoals = goals.length;
  const activeGoals = goals.filter((g) => g.progressPercentage && g.progressPercentage > 0).length;
  const goalRatio = totalGoals === 0 ? 0 : activeGoals / totalGoals;

  // 5. Overall Progress
  const overallPercent = useMemo(() => {
    const totalItems = totalTasks + totalHabits + totalEvents;
    const completedItems = completedTasks + completedHabits + completedEvents;

    if (totalItems > 0) {
      return Math.round((completedItems / totalItems) * 100);
    }
    if (totalGoals > 0) {
      return Math.round(goalRatio * 100);
    }
    return 0;
  }, [totalTasks, completedTasks, totalHabits, completedHabits, totalEvents, completedEvents, totalGoals, goalRatio]);

  const isCompletelyEmpty = totalTasks === 0 && totalHabits === 0 && totalEvents === 0 && totalGoals === 0;

  // Sub-screens early return
  if (showAddTask) {
    return <AddTaskScreen onBack={() => { setShowAddTask(false); refreshTasks(); }} />;
  }
  if (showAddEvent) {
    return <AddEventScreen onBack={() => { setShowAddEvent(false); refreshEvents(); }} />;
  }
  if (showAddHabit) {
    return <AddHabitScreen onBack={() => { setShowAddHabit(false); refreshHabits(); }} />;
  }
  if (showAddGoal) {
    return <AddGoalScreen onBack={() => { setShowAddGoal(false); refreshGoals(); }} />;
  }

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
          <Text style={styles.headerTitle}>Today's Progress</Text>
          <Text style={styles.headerSub}>
            Track your small steps, see the bigger picture.
          </Text>
        </View>
      </View>

      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.contentContainer}
        showsVerticalScrollIndicator={false}
      >
        {/* Date & Filter Row */}
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

          <TouchableOpacity
            style={styles.filterDropdownPill}
            onPress={() => setShowTimeframeModal(true)}
            activeOpacity={0.8}
          >
            <Text style={styles.filterDropdownText}>{timeframe}</Text>
            <Feather name="chevron-down" size={14} color="#2D6A00" style={{ marginLeft: 4 }} />
          </TouchableOpacity>
        </View>

        {/* Motivational Banner */}
        <View style={styles.bannerCard}>
          <View style={styles.bannerLeft}>
            <View style={styles.bannerIconCircle}>
              <Ionicons name="sparkles" size={16} color="#7C3AED" />
            </View>
            <View style={styles.bannerTextWrap}>
              <Text style={styles.bannerTitle}>
                {overallPercent >= 75
                  ? 'Fantastic work!'
                  : overallPercent >= 40
                  ? "You're making progress!"
                  : 'Start your daily streak!'}
              </Text>
              <Text style={styles.bannerSub}>
                {overallPercent >= 75
                  ? "You've crushed most of your goals today. Keep it up!"
                  : "Keep going, small steps every day lead to big results."}
              </Text>
            </View>
          </View>

          {/* Handwritten callout */}
          <View style={styles.handwrittenBadge}>
            <Text style={styles.handwrittenText}>Progress</Text>
            <Text style={styles.handwrittenText2}>looks good!</Text>
            <View style={styles.handwrittenUnderline} />
          </View>
        </View>

        {/* Overall Progress Card */}
        <View style={styles.overallCard}>
          <Text style={styles.cardTitle}>Overall Progress</Text>

          {isCompletelyEmpty ? (
            <View style={styles.emptyCardWrap}>
              <Feather name="inbox" size={32} color="#CBD5E1" style={{ marginBottom: 6 }} />
              <Text style={styles.emptyTitle}>No activity planned for today.</Text>
              <Text style={styles.emptySub}>Add tasks, events or habits to track your progress.</Text>
            </View>
          ) : (
            <View style={styles.overallBodyRow}>
              {/* Left Circular Ring Gauge */}
              <View style={styles.gaugeWrap}>
                <View
                  style={[
                    styles.gaugeOuterRing,
                    {
                      borderColor: overallPercent > 0 ? '#66C400' : '#E2E8F0',
                      borderTopColor: overallPercent >= 75 ? '#66C400' : '#E2E8F0',
                    },
                  ]}
                >
                  <View style={styles.gaugeInnerCircle}>
                    <Text style={styles.gaugePercent}>{overallPercent}%</Text>
                    <Text style={styles.gaugeLabel}>Done today</Text>
                  </View>
                </View>
              </View>

              {/* Right Side Breakdown */}
              <View style={styles.breakdownCol}>
                {/* Tasks row */}
                <View style={styles.breakdownRow}>
                  <View style={styles.breakdownLabelRow}>
                    <View style={[styles.miniIconBadge, { backgroundColor: '#E2F7C5' }]}>
                      <Feather name="check" size={10} color="#2D6A00" />
                    </View>
                    <Text style={styles.breakdownLabel}>Tasks</Text>
                  </View>
                  <Text style={styles.breakdownRatio}>
                    {completedTasks}/{totalTasks}
                  </Text>
                </View>
                <View style={styles.miniTrack}>
                  <View
                    style={[
                      styles.miniFill,
                      { width: `${taskRatio * 100}%`, backgroundColor: '#66C400' },
                    ]}
                  />
                </View>

                {/* Habits row */}
                <View style={styles.breakdownRow}>
                  <View style={styles.breakdownLabelRow}>
                    <View style={[styles.miniIconBadge, { backgroundColor: '#FFEDD5' }]}>
                      <Ionicons name="stats-chart" size={10} color="#C2410C" />
                    </View>
                    <Text style={styles.breakdownLabel}>Habits</Text>
                  </View>
                  <Text style={styles.breakdownRatio}>
                    {completedHabits}/{totalHabits}
                  </Text>
                </View>
                <View style={styles.miniTrack}>
                  <View
                    style={[
                      styles.miniFill,
                      { width: `${habitRatio * 100}%`, backgroundColor: '#F97316' },
                    ]}
                  />
                </View>

                {/* Goals row */}
                <View style={styles.breakdownRow}>
                  <View style={styles.breakdownLabelRow}>
                    <View style={[styles.miniIconBadge, { backgroundColor: '#F3E8FF' }]}>
                      <Ionicons name="disc" size={10} color="#7C3AED" />
                    </View>
                    <Text style={styles.breakdownLabel}>Goals</Text>
                  </View>
                  <Text style={styles.breakdownRatio}>
                    {activeGoals}/{totalGoals}
                  </Text>
                </View>
                <View style={styles.miniTrack}>
                  <View
                    style={[
                      styles.miniFill,
                      { width: `${goalRatio * 100}%`, backgroundColor: '#8B5CF6' },
                    ]}
                  />
                </View>
              </View>
            </View>
          )}
        </View>

        {/* Details Section */}
        <View style={styles.detailsHeaderRow}>
          <Text style={styles.detailsTitle}>Details</Text>
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

        {/* Card 1: Tasks */}
        <TouchableOpacity
          style={styles.detailCard}
          onPress={() => setShowAddTask(true)}
          activeOpacity={0.8}
        >
          <View style={styles.detailHeaderRow}>
            <View style={[styles.detailIconCircle, { backgroundColor: '#E2F7C5' }]}>
              <Feather name="check" size={18} color="#66C400" />
            </View>
            <View style={styles.detailTitleWrap}>
              <Text style={styles.detailItemTitle}>Tasks</Text>
              <Text style={styles.detailItemSub}>
                {completedTasks} of {totalTasks} tasks completed
              </Text>
            </View>
            <Feather name="chevron-right" size={18} color="#94A3B8" />
          </View>

          <View style={styles.detailProgressRow}>
            <View style={styles.detailTrack}>
              <View
                style={[
                  styles.detailFill,
                  { width: `${taskRatio * 100}%`, backgroundColor: '#66C400' },
                ]}
              />
            </View>
            <Text style={styles.detailRatioText}>
              {completedTasks}/{totalTasks}
            </Text>
          </View>
        </TouchableOpacity>

        {/* Card 2: Habits */}
        <TouchableOpacity
          style={styles.detailCard}
          onPress={() => setShowAddHabit(true)}
          activeOpacity={0.8}
        >
          <View style={styles.detailHeaderRow}>
            <View style={[styles.detailIconCircle, { backgroundColor: '#FFEDD5' }]}>
              <Ionicons name="stats-chart" size={18} color="#F97316" />
            </View>
            <View style={styles.detailTitleWrap}>
              <Text style={styles.detailItemTitle}>Habits</Text>
              <Text style={styles.detailItemSub}>
                {completedHabits} of {totalHabits} habits done
              </Text>
            </View>
            <Feather name="chevron-right" size={18} color="#94A3B8" />
          </View>

          <View style={styles.detailProgressRow}>
            <View style={styles.detailTrack}>
              <View
                style={[
                  styles.detailFill,
                  { width: `${habitRatio * 100}%`, backgroundColor: '#F97316' },
                ]}
              />
            </View>
            <Text style={styles.detailRatioText}>
              {completedHabits}/{totalHabits}
            </Text>
          </View>
        </TouchableOpacity>

        {/* Card 3: Goals */}
        <TouchableOpacity
          style={styles.detailCard}
          onPress={() => setShowAddGoal(true)}
          activeOpacity={0.8}
        >
          <View style={styles.detailHeaderRow}>
            <View style={[styles.detailIconCircle, { backgroundColor: '#F3E8FF' }]}>
              <Ionicons name="disc-outline" size={18} color="#8B5CF6" />
            </View>
            <View style={styles.detailTitleWrap}>
              <Text style={styles.detailItemTitle}>Goals</Text>
              <Text style={styles.detailItemSub}>
                {activeGoals} of {totalGoals} goals progressing
              </Text>
            </View>
            <Feather name="chevron-right" size={18} color="#94A3B8" />
          </View>

          <View style={styles.detailProgressRow}>
            <View style={styles.detailTrack}>
              <View
                style={[
                  styles.detailFill,
                  { width: `${goalRatio * 100}%`, backgroundColor: '#8B5CF6' },
                ]}
              />
            </View>
            <Text style={styles.detailRatioText}>
              {activeGoals}/{totalGoals}
            </Text>
          </View>
        </TouchableOpacity>

        {/* Card 4: Events (if any) */}
        {totalEvents > 0 && (
          <TouchableOpacity
            style={styles.detailCard}
            onPress={() => setShowAddEvent(true)}
            activeOpacity={0.8}
          >
            <View style={styles.detailHeaderRow}>
              <View style={[styles.detailIconCircle, { backgroundColor: '#EFF6FF' }]}>
                <Feather name="calendar" size={18} color="#2563EB" />
              </View>
              <View style={styles.detailTitleWrap}>
                <Text style={styles.detailItemTitle}>Events</Text>
                <Text style={styles.detailItemSub}>
                  {completedEvents} of {totalEvents} events concluded
                </Text>
              </View>
              <Feather name="chevron-right" size={18} color="#94A3B8" />
            </View>

            <View style={styles.detailProgressRow}>
              <View style={styles.detailTrack}>
                <View
                  style={[
                    styles.detailFill,
                    { width: `${eventRatio * 100}%`, backgroundColor: '#2563EB' },
                  ]}
                />
              </View>
              <Text style={styles.detailRatioText}>
                {completedEvents}/{totalEvents}
              </Text>
            </View>
          </TouchableOpacity>
        )}

        {/* Quote Card */}
        <View style={styles.quoteCard}>
          <Text style={styles.quoteMark}>“</Text>
          <View style={styles.quoteContent}>
            <Text style={styles.quoteText}>
              Small steps every day lead to big results.
            </Text>
            <View style={styles.quoteUnderline} />
          </View>
        </View>

        <View style={{ height: 30 }} />
      </ScrollView>

      {/* Timeframe Selection Modal */}
      <SelectionModal
        visible={showTimeframeModal}
        onClose={() => setShowTimeframeModal(false)}
        title="Select Timeframe"
        options={[
          { label: 'Today', value: 'Today' },
          { label: 'This Week', value: 'This Week' },
          { label: 'This Month', value: 'This Month' },
          { label: 'All Time', value: 'All Time' },
        ]}
        selectedValue={timeframe}
        onSelect={(val) => {
          setTimeframe(val as any);
          setShowTimeframeModal(false);
        }}
      />
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
  filterDropdownPill: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#E2F7C5',
    borderRadius: 14,
    paddingHorizontal: 12,
    paddingVertical: 8,
    marginLeft: 4,
  },
  filterDropdownText: {
    fontSize: 12.5,
    fontWeight: '700',
    color: '#2D6A00',
  },

  /* Motivational Banner */
  bannerCard: {
    backgroundColor: '#F3F0FF',
    borderRadius: 18,
    padding: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderWidth: 1,
    borderColor: '#E9D5FF',
    marginBottom: 16,
  },
  bannerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  bannerIconCircle: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#E9D5FF',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  bannerTextWrap: {
    flex: 1,
  },
  bannerTitle: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0F172A',
  },
  bannerSub: {
    fontSize: 11.5,
    color: '#64748B',
    marginTop: 2,
    lineHeight: 16,
  },
  handwrittenBadge: {
    alignItems: 'center',
    transform: [{ rotate: '-3deg' }],
  },
  handwrittenText: {
    fontSize: 13,
    fontFamily: Platform.OS === 'web' ? 'Caveat, cursive' : 'Caveat_700Bold',
    fontWeight: '700',
    color: '#2D6A00',
  },
  handwrittenText2: {
    fontSize: 13,
    fontFamily: Platform.OS === 'web' ? 'Caveat, cursive' : 'Caveat_700Bold',
    fontWeight: '700',
    color: '#2D6A00',
    marginTop: -4,
  },
  handwrittenUnderline: {
    width: '80%',
    height: 2,
    backgroundColor: '#2D6A00',
    borderRadius: 1,
    marginTop: 0,
  },

  /* Overall Progress Card */
  overallCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    padding: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 20,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 14,
  },
  overallBodyRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  gaugeWrap: {
    width: 105,
    height: 105,
    marginRight: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  gaugeOuterRing: {
    width: 100,
    height: 100,
    borderRadius: 50,
    borderWidth: 9,
    alignItems: 'center',
    justifyContent: 'center',
    transform: [{ rotate: '-45deg' }],
  },
  gaugeInnerCircle: {
    transform: [{ rotate: '45deg' }],
    alignItems: 'center',
  },
  gaugePercent: {
    fontSize: 20,
    fontWeight: '900',
    color: '#0F172A',
  },
  gaugeLabel: {
    fontSize: 10,
    color: '#94A3B8',
    marginTop: -2,
  },

  breakdownCol: {
    flex: 1,
  },
  breakdownRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  breakdownLabelRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  miniIconBadge: {
    width: 16,
    height: 16,
    borderRadius: 4,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 6,
  },
  breakdownLabel: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
  },
  breakdownRatio: {
    fontSize: 12,
    fontWeight: '700',
    color: '#64748B',
  },
  miniTrack: {
    height: 6,
    backgroundColor: '#F1F5F9',
    borderRadius: 3,
    marginBottom: 10,
    overflow: 'hidden',
  },
  miniFill: {
    height: '100%',
    borderRadius: 3,
  },

  emptyCardWrap: {
    paddingVertical: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  emptyTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: '#475569',
  },
  emptySub: {
    fontSize: 12,
    color: '#94A3B8',
    marginTop: 2,
    textAlign: 'center',
  },

  /* Details Section */
  detailsHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  detailsTitle: {
    fontSize: 17,
    fontWeight: '800',
    color: '#0F172A',
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

  /* Detail Card */
  detailCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 12,
  },
  detailHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  detailIconCircle: {
    width: 38,
    height: 38,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  detailTitleWrap: {
    flex: 1,
  },
  detailItemTitle: {
    fontSize: 14.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  detailItemSub: {
    fontSize: 12,
    color: '#94A3B8',
    marginTop: 1,
  },

  detailProgressRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  detailTrack: {
    flex: 1,
    height: 8,
    backgroundColor: '#F1F5F9',
    borderRadius: 4,
    marginRight: 10,
    overflow: 'hidden',
  },
  detailFill: {
    height: '100%',
    borderRadius: 4,
  },
  detailRatioText: {
    fontSize: 12.5,
    fontWeight: '800',
    color: '#64748B',
  },

  /* Quote Box */
  quoteCard: {
    backgroundColor: '#F1F9E8',
    borderRadius: 16,
    padding: 14,
    borderWidth: 1,
    borderColor: '#E2F2D0',
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 8,
  },
  quoteMark: {
    fontSize: 28,
    color: '#66C400',
    fontWeight: '900',
    marginRight: 8,
    lineHeight: 30,
  },
  quoteContent: {
    flex: 1,
  },
  quoteText: {
    fontSize: 15,
    fontFamily: Platform.OS === 'web' ? 'Caveat, cursive' : 'Caveat_700Bold',
    fontWeight: '700',
    color: '#2D6A00',
  },
  quoteUnderline: {
    width: '60%',
    height: 2,
    backgroundColor: '#2D6A00',
    borderRadius: 1,
    marginTop: 1,
  },
});
