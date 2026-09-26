import React, { useState } from 'react';
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
  DimensionValue,
  Modal,
  TouchableWithoutFeedback,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { TimeDetailsScreen } from './TimeDetailsScreen';
import { ChatWithLivoScreen } from './ChatWithLivoScreen';
import { useTasks } from '../hooks/useTasks';
import { useHabits } from '../hooks/useHabits';
import { useGoals } from '../hooks/useGoals';
import { useEvents } from '../hooks/useEvents';

import { useNavigation, useFocusEffect } from '@react-navigation/native';
import { useProfile } from '../hooks/useProfile';

interface InsightsScreenProps {
  onNavigateToTimeDetails?: () => void;
}

export const InsightsScreen: React.FC<InsightsScreenProps> = ({ onNavigateToTimeDetails }) => {
  const navigation = useNavigation<any>();
  const { profile, refreshProfile } = useProfile();
  const [selectedTimeframe, setSelectedTimeframe] = useState('This Week');
  const [showTimeframeModal, setShowTimeframeModal] = useState(false);

  useFocusEffect(
    React.useCallback(() => {
      refreshProfile();
    }, [refreshProfile])
  );

  // Detail Modal States
  const [activeDetailModal, setActiveDetailModal] = useState<'tasks' | 'goals' | 'habits' | 'learning' | 'insight' | null>(null);
  const [currentInsightIndex, setCurrentInsightIndex] = useState(0);

  // Dynamic data from hooks
  const { tasks } = useTasks();
  const { habits } = useHabits();
  const { goals } = useGoals();
  const { events } = useEvents();

  const getFilteredData = <T extends { date?: string; dueDate?: string; targetDate?: string; createdAt?: string }>(data: T[], timeframe: string) => {
    return data.filter(item => {
      if (timeframe === 'Overall') return true;
      const dateStr = item.date || item.dueDate || item.targetDate || item.createdAt;
      if (!dateStr) return false;
      
      const date = new Date(dateStr);
      const now = new Date();
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

      if (timeframe === 'This Month') {
        return date.getFullYear() === today.getFullYear() && date.getMonth() === today.getMonth();
      }

      if (timeframe === 'This Week') {
        const day = today.getDay();
        const diff = today.getDate() - day + (day === 0 ? -6 : 1);
        const start = new Date(today);
        start.setDate(diff);
        const end = new Date(start);
        end.setDate(start.getDate() + 6);
        end.setHours(23, 59, 59, 999);
        return date >= start && date <= end;
      }

      if (timeframe === 'Last Week') {
        const day = today.getDay();
        const diff = today.getDate() - day + (day === 0 ? -6 : 1);
        const startThis = new Date(today);
        startThis.setDate(diff);
        const start = new Date(startThis);
        start.setDate(startThis.getDate() - 7);
        const end = new Date(start);
        end.setDate(start.getDate() + 6);
        end.setHours(23, 59, 59, 999);
        return date >= start && date <= end;
      }

      return true;
    });
  };

  const filteredTasks = getFilteredData(tasks, selectedTimeframe);
  const filteredHabits = getFilteredData(habits, selectedTimeframe);
  const filteredGoals = getFilteredData(goals, selectedTimeframe);
  const filteredEvents = getFilteredData(events, selectedTimeframe);

  const realTotalTasks = filteredTasks.length;
  const realCompletedTasks = filteredTasks.filter(t => t.completed).length;
  const realTotalHabits = filteredHabits.length;
  const realDoneHabits = filteredHabits.filter(h => h.streakCount > 0 || h.completedToday).length;
  const realTotalGoals = filteredGoals.length;
  const realProgressingGoals = filteredGoals.filter(g => (g.progressPercentage || 0) > 0).length;

  const insightsList = [
    `You completed ${realCompletedTasks} tasks for ${selectedTimeframe}! Keep up the momentum.`,
    `You have ${realDoneHabits} active habits right now. Consistency is key!`,
    `You're making progress on ${realProgressingGoals} goals. Keep focusing on what matters.`,
  ];

  let totalMinutes = 0;
  const categoryCounts: Record<string, number> = { Work: 0, Personal: 0, Learning: 0, Health: 0, Travel: 0, Others: 0 };
  
  const processItem = (category?: string, duration?: string | number) => {
    let cat = category || 'Others';
    const matchCat = ['Work', 'Personal', 'Learning', 'Health', 'Travel', 'Others'].find(c => c.toLowerCase() === cat.toLowerCase());
    cat = matchCat || 'Others';
    let mins = 30; // default 30 mins
    if (typeof duration === 'number') mins = duration;
    else if (typeof duration === 'string') {
      const num = parseInt(duration);
      if (!isNaN(num)) mins = num;
    }
    categoryCounts[cat] += mins;
    totalMinutes += mins;
  };

  filteredTasks.forEach(t => processItem(t.category, t.estimatedMinutes || t.duration));
  filteredEvents.forEach(e => {
    let mins = 60; // default
    if (e.startTime && e.endTime) {
      const startMatch = e.startTime.match(/(\d+):(\d+)\s*(AM|PM)?/i);
      const endMatch = e.endTime.match(/(\d+):(\d+)\s*(AM|PM)?/i);
      if (startMatch && endMatch) {
         let sh = parseInt(startMatch[1]), sm = parseInt(startMatch[2]);
         if (startMatch[3]?.toUpperCase() === 'PM' && sh < 12) sh += 12;
         if (startMatch[3]?.toUpperCase() === 'AM' && sh === 12) sh = 0;
         let eh = parseInt(endMatch[1]), em = parseInt(endMatch[2]);
         if (endMatch[3]?.toUpperCase() === 'PM' && eh < 12) eh += 12;
         if (endMatch[3]?.toUpperCase() === 'AM' && eh === 12) eh = 0;
         const diff = (eh * 60 + em) - (sh * 60 + sm);
         if (diff > 0) mins = diff;
      }
    }
    processItem(e.category, mins);
  });

  const colors: Record<string, string> = {
    Work: '#66C400', Personal: '#3B82F6', Learning: '#8B5CF6', 
    Health: '#EF4444', Travel: '#F97316', Others: '#CBD5E1'
  };

  const legendData = Object.entries(categoryCounts).map(([label, score]) => {
    const percent = totalMinutes > 0 ? Math.round((score / totalMinutes) * 100) : (label === 'Others' ? 100 : 0);
    return { label, percent: percent + '%', color: colors[label] || '#CBD5E1', score };
  }).sort((a, b) => b.score - a.score);

  const totalTimeStr = totalMinutes > 0 ? `${Math.floor(totalMinutes / 60)}h ${totalMinutes % 60}m` : '0h 0m';
  const totalTimeHours = totalMinutes > 0 ? Math.floor(totalMinutes / 60) : 0;
  const totalTimeMins = totalMinutes > 0 ? totalMinutes % 60 : 0;

  const hourBuckets = { '6am': 0, '9am': 0, '12pm': 0, '3pm': 0, '6pm': 0, '9pm': 0 };
  const parseHour = (timeStr?: string) => {
    if (!timeStr) return null;
    const match = timeStr.match(/(\d+):(\d+)\s*(AM|PM)/i);
    if (match) {
        let h = parseInt(match[1]);
        if (match[3].toUpperCase() === 'PM' && h < 12) h += 12;
        if (match[3].toUpperCase() === 'AM' && h === 12) h = 0;
        return h;
    }
    const match24 = timeStr.match(/(\d+):(\d+)/);
    if (match24) return parseInt(match24[1]);
    return null;
  };
  const processTime = (timeStr?: string) => {
    const h = parseHour(timeStr);
    if (h === null) return;
    if (h >= 5 && h < 9) hourBuckets['6am']++;
    else if (h >= 9 && h < 12) hourBuckets['9am']++;
    else if (h >= 12 && h < 15) hourBuckets['12pm']++;
    else if (h >= 15 && h < 18) hourBuckets['3pm']++;
    else if (h >= 18 && h < 21) hourBuckets['6pm']++;
    else hourBuckets['9pm']++;
  };
  filteredTasks.forEach(t => processTime(t.time));
  filteredEvents.forEach(e => processTime(e.startTime));
  
  const maxBucket = Math.max(...Object.values(hourBuckets), 1);
  const productiveHours: { label: string; height: DimensionValue; color: string }[] = [
    { label: '6am', height: `${(hourBuckets['6am'] / maxBucket) * 100}%`, color: hourBuckets['6am'] === maxBucket && hourBuckets['6am'] > 0 ? '#66C400' : '#EBF9DB' },
    { label: '9am', height: `${(hourBuckets['9am'] / maxBucket) * 100}%`, color: hourBuckets['9am'] === maxBucket && hourBuckets['9am'] > 0 ? '#66C400' : '#EBF9DB' },
    { label: '12pm', height: `${(hourBuckets['12pm'] / maxBucket) * 100}%`, color: hourBuckets['12pm'] === maxBucket && hourBuckets['12pm'] > 0 ? '#66C400' : '#EBF9DB' },
    { label: '3pm', height: `${(hourBuckets['3pm'] / maxBucket) * 100}%`, color: hourBuckets['3pm'] === maxBucket && hourBuckets['3pm'] > 0 ? '#66C400' : '#EBF9DB' },
    { label: '6pm', height: `${(hourBuckets['6pm'] / maxBucket) * 100}%`, color: hourBuckets['6pm'] === maxBucket && hourBuckets['6pm'] > 0 ? '#66C400' : '#EBF9DB' },
    { label: '9pm', height: `${(hourBuckets['9pm'] / maxBucket) * 100}%`, color: hourBuckets['9pm'] === maxBucket && hourBuckets['9pm'] > 0 ? '#66C400' : '#EBF9DB' },
  ];
  
  let bestTime = 'morning';
  if (hourBuckets['12pm'] + hourBuckets['3pm'] > hourBuckets['6am'] + hourBuckets['9am']) bestTime = 'afternoon';
  if (hourBuckets['6pm'] + hourBuckets['9pm'] > Math.max(hourBuckets['6am'] + hourBuckets['9am'], hourBuckets['12pm'] + hourBuckets['3pm'])) bestTime = 'evening';
  if (maxBucket === 1 && Object.values(hourBuckets).every(v => v === 0)) bestTime = 'any time';

  const habitDays = Array.from({ length: 7 }).map((_, i) => {
    const d = 6 - i;
    const date = new Date();
    date.setDate(date.getDate() - d);
    const dayName = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'][date.getDay()];
    const completed = habits.some(h => {
      if (d === 0) return h.completedToday;
      if (h.completedToday) return d < h.streakCount;
      return d <= h.streakCount && d > 0;
    });
    return { day: dayName, completed };
  });

  const goalsList = filteredGoals.slice(0, 3).map(g => ({
    title: g.title,
    progress: Math.round(g.progressPercentage || 0)
  }));
  if (goalsList.length === 0) {
    goalsList.push({ title: 'No goals set for this timeframe', progress: 0 });
  }

  const expenseBars: { day: string; height: DimensionValue }[] = [
    { day: 'Mon', height: '40%' },
    { day: 'Tue', height: '30%' },
    { day: 'Wed', height: '25%' },
    { day: 'Thu', height: '20%' },
    { day: 'Fri', height: '85%' },
    { day: 'Sat', height: '30%' },
    { day: 'Sun', height: '20%' },
  ];

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
            <TouchableOpacity style={styles.thisWeekPill} onPress={() => setShowTimeframeModal(true)}>
              <Text style={styles.thisWeekText}>{selectedTimeframe}</Text>
              <Feather name="chevron-down" size={13} color="#64748B" style={{ marginLeft: 4 }} />
            </TouchableOpacity>

            <TouchableOpacity style={styles.avatarCircle} onPress={() => navigation.navigate('Profile' as never)}>
              <Text style={styles.avatarText}>{profile.name.charAt(0).toUpperCase()}</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Caveat Handwritten Slogan Banner (Top Right Area) */}
        <View style={styles.sloganWrap}>
          <Text style={styles.sloganText}>Progress looks good!</Text>
          <View style={styles.sloganUnderline} />
        </View>

        {/* 2. Title Section */}
        <View style={styles.titleSection}>
          <Text style={styles.mainTitle}>Insights</Text>
          <Text style={styles.mainSubtitle}>Understand your progress. Make a better tomorrow.</Text>
        </View>

        {/* 3. Consistency Banner */}
        <View style={styles.consistencyBanner}>
          <View style={styles.plusCircleBox}>
            <Feather name="plus" size={14} color="#66C400" />
          </View>
          <View style={styles.consistencyTextWrap}>
            <Text style={styles.consistencyTitle}>You showed up consistently this week!</Text>
            <Text style={styles.consistencySub}>
              You completed {realCompletedTasks} out of {realTotalTasks} planned tasks. Keep the momentum going.
            </Text>
          </View>
        </View>

        {/* 4. This Week at a Glance */}
        <View style={styles.atAGlanceSection}>
          <View style={styles.glanceHeaderRow}>
            <Text style={styles.glanceTitle}>This Week at a Glance</Text>
            <Text style={styles.glanceVsText}>
              vs. last week <Text style={{ color: '#66C400', fontWeight: '800' }}>↑ 12%</Text>
            </Text>
          </View>

          <View style={styles.glanceGrid}>
            {/* Stat 1: Tasks */}
            <TouchableOpacity style={styles.glanceCard} onPress={() => setActiveDetailModal('tasks')}>
              <View style={styles.glanceCardHeader}>
                <Feather name="check" size={12} color="#66C400" style={{ marginRight: 4 }} />
                <Text style={styles.glanceCardLabel}>Tasks</Text>
              </View>
              <Text style={styles.glanceCardValue}>{realCompletedTasks} / {realTotalTasks}</Text>
              <Text style={[styles.glanceCardSub, { color: '#66C400' }]}>↑ 2</Text>
            </TouchableOpacity>

            {/* Stat 2: Goals */}
            <TouchableOpacity style={styles.glanceCard} onPress={() => setActiveDetailModal('goals')}>
              <View style={styles.glanceCardHeader}>
                <Ionicons name="disc-outline" size={12} color="#8B5CF6" style={{ marginRight: 4 }} />
                <Text style={styles.glanceCardLabel}>Goals</Text>
              </View>
              <Text style={styles.glanceCardValue}>{realProgressingGoals} / {realTotalGoals}</Text>
              <Text style={[styles.glanceCardSub, { color: '#8B5CF6' }]}>↑ 1</Text>
            </TouchableOpacity>

            {/* Stat 3: Habits */}
            <TouchableOpacity style={styles.glanceCard} onPress={() => setActiveDetailModal('habits')}>
              <View style={styles.glanceCardHeader}>
                <Ionicons name="stats-chart-outline" size={12} color="#F97316" style={{ marginRight: 4 }} />
                <Text style={styles.glanceCardLabel}>Habits</Text>
              </View>
              <Text style={styles.glanceCardValue}>{realDoneHabits} / {realTotalHabits}</Text>
              <Text style={[styles.glanceCardSub, { color: '#F97316' }]}>↑ 1</Text>
            </TouchableOpacity>

            {/* Stat 4: Learning */}
            <TouchableOpacity style={styles.glanceCard} onPress={() => setActiveDetailModal('learning')}>
              <View style={styles.glanceCardHeader}>
                <Feather name="book-open" size={12} color="#3B82F6" style={{ marginRight: 4 }} />
                <Text style={styles.glanceCardLabel}>Learning</Text>
              </View>
              <Text style={styles.glanceCardValue}>4h 30m</Text>
              <Text style={[styles.glanceCardSub, { color: '#3B82F6' }]}>↑ 1h</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* 5. Your Time This Week (Donut Chart Widget) */}
        <View style={styles.whiteCardSection}>
          <View style={styles.cardHeaderRow}>
            <Text style={styles.cardTitle}>Your Time This Week</Text>
            <TouchableOpacity
              style={styles.seeDetailsBtn}
              onPress={() => {
                if (onNavigateToTimeDetails) onNavigateToTimeDetails();
                else navigation.navigate('TimeDetails');
              }}
              activeOpacity={0.7}
            >
              <Text style={styles.seeDetailsText}>See details</Text>
              <Feather name="arrow-right" size={12} color="#66C400" style={{ marginLeft: 4 }} />
            </TouchableOpacity>
          </View>

          <Text style={styles.totalTimeSub}>Total {totalTimeStr}</Text>

          <View style={styles.donutWidgetRow}>
            {/* Donut Graphic */}
            <View style={styles.donutGraphicWrap}>
              <View style={styles.donutOuterCircle}>
                <View style={styles.donutInnerCircle}>
                  <Text style={styles.donutCenterValue}>{totalTimeHours}h</Text>
                  <Text style={styles.donutCenterSub}>{totalTimeMins}m</Text>
                </View>
              </View>
            </View>

            {/* Legend List */}
            <View style={styles.legendContainer}>
              {legendData.map((item) => (
                <View key={item.label} style={styles.legendItemRow}>
                  <View style={styles.legendLeft}>
                    <View style={[styles.legendDot, { backgroundColor: item.color }]} />
                    <Text style={styles.legendLabel}>{item.label}</Text>
                  </View>
                  <Text style={styles.legendPercent}>{item.percent}</Text>
                </View>
              ))}
            </View>
          </View>
        </View>

        {/* 6. Most Productive Time Bar Chart */}
        <View style={styles.whiteCardSection}>
          <Text style={styles.cardTitle}>Most Productive Time</Text>
          <View style={styles.hintSubRow}>
            <Feather name="sun" size={13} color="#F59E0B" style={{ marginRight: 4 }} />
            <Text style={styles.hintSubText}>You do your best work in the {bestTime}.</Text>
          </View>

          <View style={styles.productiveChartContainer}>
            {productiveHours.map((bar) => (
              <View key={bar.label} style={styles.chartCol}>
                <View style={styles.chartTrack}>
                  <View
                    style={[
                      styles.chartFill,
                      { height: bar.height, backgroundColor: bar.color },
                    ]}
                  />
                </View>
                <Text style={styles.chartLabelText}>{bar.label}</Text>
              </View>
            ))}
          </View>
        </View>

        {/* 7. Habit Consistency */}
        <View style={styles.whiteCardSection}>
          <Text style={styles.cardTitle}>Habit Consistency</Text>
          <Text style={styles.subtextLabel}>{realDoneHabits} / {realTotalHabits} habits this week</Text>

          <View style={styles.habitDaysRow}>
            {habitDays.map((h) => (
              <View key={h.day} style={styles.habitDayCol}>
                <View
                  style={[
                    styles.habitCircle,
                    h.completed ? styles.habitCircleCompleted : styles.habitCircleEmpty,
                  ]}
                />
                <Text style={styles.habitDayLabel}>{h.day}</Text>
              </View>
            ))}
          </View>
        </View>

        {/* 8. Goal Progress */}
        <View style={styles.whiteCardSection}>
          <Text style={styles.cardTitle}>Goal Progress</Text>
          <Text style={styles.subtextLabel}>You're making good progress!</Text>

          <View style={styles.goalsList}>
            {goalsList.map((goal) => (
              <TouchableOpacity key={goal.title} style={styles.goalItemBlock} onPress={() => setActiveDetailModal('goals')}>
                <View style={styles.goalRowHeader}>
                  <View style={styles.goalTitleWrap}>
                    <View style={styles.goalDotIcon} />
                    <Text style={styles.goalTitleText}>{goal.title}</Text>
                  </View>
                  <Text style={styles.goalPercentText}>{goal.progress}%</Text>
                </View>

                <View style={styles.goalProgressTrack}>
                  <View style={[styles.goalProgressFill, { width: `${goal.progress}%` }]} />
                </View>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        {/* 9. Expenses Bar Chart */}
        <View style={styles.whiteCardSection}>
          <Text style={styles.cardTitle}>Expenses</Text>
          <View style={styles.expenseValueRow}>
            <Text style={styles.expenseAmountText}>₹ 5,320</Text>
            <View style={styles.expenseDownBadge}>
              <Feather name="arrow-down" size={10} color="#2D6A00" style={{ marginRight: 2 }} />
              <Text style={styles.expenseDownText}>10%</Text>
            </View>
          </View>

          <View style={styles.expenseChartContainer}>
            {expenseBars.map((b) => (
              <View key={b.day} style={styles.chartCol}>
                <View style={styles.chartTrack}>
                  <View style={[styles.chartFill, { height: b.height, backgroundColor: '#A4E83A' }]} />
                </View>
                <Text style={styles.chartLabelText}>{b.day}</Text>
              </View>
            ))}
          </View>
        </View>

        {/* 10. LIVO Insight Carousel Card */}
        <TouchableOpacity
          style={styles.livoInsightCard}
          onPress={() => setCurrentInsightIndex((prev) => (prev + 1) % insightsList.length)}
        >
          <View style={styles.insightHeaderRow}>
            <Ionicons name="sparkles" size={14} color="#7C3AED" style={{ marginRight: 6 }} />
            <Text style={styles.insightHeaderText}>LIVO Insight</Text>
          </View>

          <View style={styles.insightQuoteBox}>
            <Text style={styles.insightQuoteText}>
              “{insightsList[currentInsightIndex]}”
            </Text>
            <Feather name="chevron-right" size={16} color="#7C3AED" style={{ marginLeft: 6 }} />
          </View>

          {/* Carousel Dots */}
          <View style={styles.carouselDotsRow}>
            {insightsList.map((_, idx) => (
              <View
                key={idx}
                style={[
                  styles.carouselDot,
                  currentInsightIndex === idx && styles.carouselDotActive,
                ]}
              />
            ))}
          </View>
        </TouchableOpacity>

        {/* 11. Get deeper insights with LIVO (Bottom AI Banner) */}
        <View style={styles.deeperBanner}>
          <View style={styles.deeperIconCircle}>
            <MaterialCommunityIcons name="circle-half-full" size={16} color="#7C3AED" />
          </View>

          <View style={{ flex: 1, paddingRight: 8 }}>
            <Text style={styles.deeperTitle}>Get deeper insights with LIVO</Text>
            <Text style={styles.deeperSub}>
              Ask anything about your progress, habits, time or goals.
            </Text>
          </View>

          <TouchableOpacity style={styles.askLivoBtn} onPress={() => navigation.navigate('ChatWithLivo')}>
            <Text style={styles.askLivoText}>Ask LIVO →</Text>
          </TouchableOpacity>
        </View>

        <View style={{ height: 40 }} />
      </ScrollView>

      {/* Timeframe Filter Modal */}
      <Modal visible={showTimeframeModal} transparent animationType="fade" onRequestClose={() => setShowTimeframeModal(false)}>
        <TouchableWithoutFeedback onPress={() => setShowTimeframeModal(false)}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>
        <View style={styles.modalContent}>
          <Text style={styles.modalTitle}>Select Timeframe</Text>
          {['This Week', 'Last Week', 'This Month', 'Overall'].map((tf) => (
            <TouchableOpacity
              key={tf}
              style={[styles.modalOptionRow, selectedTimeframe === tf && styles.modalOptionSelected]}
              onPress={() => {
                setSelectedTimeframe(tf);
                setShowTimeframeModal(false);
              }}
            >
              <Text style={[styles.modalOptionText, selectedTimeframe === tf && styles.modalOptionTextSelected]}>{tf}</Text>
              {selectedTimeframe === tf && <Feather name="check" size={16} color="#66C400" />}
            </TouchableOpacity>
          ))}
        </View>
      </Modal>

      {/* Detail Breakdown Modal */}
      <Modal visible={activeDetailModal !== null} transparent animationType="fade" onRequestClose={() => setActiveDetailModal(null)}>
        <TouchableWithoutFeedback onPress={() => setActiveDetailModal(null)}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>
        <View style={styles.modalContent}>
          <View style={styles.modalHeaderRow}>
            <Text style={styles.modalTitle}>
              {activeDetailModal === 'tasks' && 'Task Performance'}
              {activeDetailModal === 'goals' && 'Goal Progress Breakdown'}
              {activeDetailModal === 'habits' && 'Habit Consistency Analysis'}
              {activeDetailModal === 'learning' && 'Learning Hours Breakdown'}
            </Text>
            <TouchableOpacity onPress={() => setActiveDetailModal(null)}>
              <Feather name="x" size={20} color="#64748B" />
            </TouchableOpacity>
          </View>

          {activeDetailModal === 'tasks' && (
            <View>
              <Text style={styles.modalSubText}>
                You've completed {realCompletedTasks} of {realTotalTasks} tasks for {selectedTimeframe.toLowerCase()}. High priority items had an 85% completion rate.
              </Text>
              <TouchableOpacity style={styles.modalActionBtn} onPress={() => setActiveDetailModal(null)}>
                <Text style={styles.modalActionBtnText}>Close</Text>
              </TouchableOpacity>
            </View>
          )}

          {activeDetailModal === 'goals' && (
            <View>
              <Text style={styles.modalSubText}>
                {realProgressingGoals} out of {realTotalGoals} active goals are actively advancing this week. Your top moving goal is "Improve fitness" at 75%.
              </Text>
              <TouchableOpacity style={styles.modalActionBtn} onPress={() => setActiveDetailModal(null)}>
                <Text style={styles.modalActionBtnText}>Close</Text>
              </TouchableOpacity>
            </View>
          )}

          {activeDetailModal === 'habits' && (
            <View>
              <Text style={styles.modalSubText}>
                {realDoneHabits} of {realTotalHabits} habits completed. Your current longest streak is 12 consecutive days!
              </Text>
              <TouchableOpacity style={styles.modalActionBtn} onPress={() => setActiveDetailModal(null)}>
                <Text style={styles.modalActionBtnText}>Close</Text>
              </TouchableOpacity>
            </View>
          )}

          {activeDetailModal === 'learning' && (
            <View>
              <Text style={styles.modalSubText}>
                4 hours and 30 minutes logged in learning activities this week. Most focused learning happened on Tuesday morning.
              </Text>
              <TouchableOpacity style={styles.modalActionBtn} onPress={() => setActiveDetailModal(null)}>
                <Text style={styles.modalActionBtnText}>Close</Text>
              </TouchableOpacity>
            </View>
          )}
        </View>
      </Modal>
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

  /* 1. Header */
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
  thisWeekPill: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 18,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginRight: 8,
  },
  thisWeekText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#0F172A',
  },
  avatarCircle: {
    width: 34,
    height: 34,
    borderRadius: 17,
    backgroundColor: '#E2F7C5',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#2D6A00',
  },

  /* Slogan Caveat Banner */
  sloganWrap: {
    alignSelf: 'flex-end',
    marginTop: -6,
    marginBottom: 10,
    marginRight: 6,
    alignItems: 'flex-start',
  },
  sloganText: {
    fontFamily: 'Caveat_700Bold',
    fontSize: 20,
    color: '#4D8000',
    lineHeight: 21,
  },
  sloganUnderline: {
    width: 120,
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

  /* 3. Consistency Banner */
  consistencyBanner: {
    backgroundColor: '#F1F9E8',
    borderRadius: 18,
    padding: 14,
    borderWidth: 1,
    borderColor: '#E2F2D0',
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 18,
  },
  plusCircleBox: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  consistencyTextWrap: {
    flex: 1,
  },
  consistencyTitle: {
    fontSize: 13.5,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 2,
  },
  consistencySub: {
    fontSize: 11.5,
    color: '#64748B',
    lineHeight: 16,
  },

  /* 4. This Week at a Glance */
  atAGlanceSection: {
    marginBottom: 18,
  },
  glanceHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  glanceTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
  },
  glanceVsText: {
    fontSize: 11.5,
    color: '#64748B',
  },

  glanceGrid: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  glanceCard: {
    backgroundColor: '#FFFFFF',
    width: '23.5%',
    borderRadius: 16,
    padding: 10,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  glanceCardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 6,
  },
  glanceCardLabel: {
    fontSize: 10.5,
    fontWeight: '700',
    color: '#64748B',
  },
  glanceCardValue: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 2,
  },
  glanceCardSub: {
    fontSize: 10,
    fontWeight: '700',
  },

  /* 5. Donut Chart Widget */
  whiteCardSection: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 18,
  },
  cardHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
  },
  seeDetailsBtn: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  seeDetailsText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#66C400',
    marginRight: 3,
  },
  totalTimeSub: {
    fontSize: 11.5,
    color: '#64748B',
    marginTop: 2,
    marginBottom: 14,
  },

  donutWidgetRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  donutGraphicWrap: {
    width: 120,
    alignItems: 'center',
    justifyContent: 'center',
  },
  donutOuterCircle: {
    width: 100,
    height: 100,
    borderRadius: 50,
    borderWidth: 14,
    borderColor: '#66C400',
    borderTopColor: '#3B82F6',
    borderRightColor: '#8B5CF6',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FFFFFF',
  },
  donutInnerCircle: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  donutCenterValue: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0F172A',
  },
  donutCenterSub: {
    fontSize: 10,
    color: '#64748B',
  },

  legendContainer: {
    flex: 1,
    paddingLeft: 12,
  },
  legendItemRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginVertical: 3,
  },
  legendLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  legendDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 6,
  },
  legendLabel: {
    fontSize: 12,
    color: '#475569',
  },
  legendPercent: {
    fontSize: 12,
    fontWeight: '700',
    color: '#0F172A',
  },

  /* 6. Productive Time Bar Chart */
  hintSubRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 4,
    marginBottom: 14,
  },
  hintSubText: {
    fontSize: 12,
    color: '#64748B',
  },
  productiveChartContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
    height: 110,
  },
  chartCol: {
    alignItems: 'center',
    flex: 1,
  },
  chartTrack: {
    width: 18,
    height: 85,
    backgroundColor: '#F1F5F9',
    borderRadius: 9,
    justifyContent: 'flex-end',
    overflow: 'hidden',
  },
  chartFill: {
    width: '100%',
    borderRadius: 9,
  },
  chartLabelText: {
    fontSize: 11,
    color: '#64748B',
    marginTop: 6,
    fontWeight: '600',
  },

  /* 7. Habit Consistency */
  subtextLabel: {
    fontSize: 12,
    color: '#64748B',
    marginTop: 2,
    marginBottom: 14,
  },
  habitDaysRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 4,
  },
  habitDayCol: {
    alignItems: 'center',
  },
  habitCircle: {
    width: 32,
    height: 32,
    borderRadius: 16,
    marginBottom: 6,
  },
  habitCircleCompleted: {
    backgroundColor: '#66C400',
  },
  habitCircleEmpty: {
    backgroundColor: '#E2E8F0',
  },
  habitDayLabel: {
    fontSize: 11,
    color: '#64748B',
    fontWeight: '600',
  },

  /* 8. Goal Progress */
  goalsList: {
    paddingTop: 4,
  },
  goalItemBlock: {
    marginBottom: 12,
  },
  goalRowHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  goalTitleWrap: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  goalDotIcon: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: '#66C400',
    marginRight: 6,
  },
  goalTitleText: {
    fontSize: 12.5,
    fontWeight: '700',
    color: '#0F172A',
  },
  goalPercentText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#64748B',
  },
  goalProgressTrack: {
    height: 6,
    backgroundColor: '#F1F5F9',
    borderRadius: 3,
    overflow: 'hidden',
  },
  goalProgressFill: {
    height: '100%',
    backgroundColor: '#66C400',
    borderRadius: 3,
  },

  /* 9. Expenses */
  expenseValueRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 4,
    marginBottom: 14,
  },
  expenseAmountText: {
    fontSize: 20,
    fontWeight: '900',
    color: '#0F172A',
    marginRight: 8,
  },
  expenseDownBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#EBF9DB',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 10,
  },
  expenseDownText: {
    fontSize: 10.5,
    fontWeight: '700',
    color: '#2D6A00',
  },
  expenseChartContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
    height: 100,
  },

  /* 10. LIVO Insight Carousel */
  livoInsightCard: {
    backgroundColor: '#F5EFFF',
    borderRadius: 18,
    padding: 16,
    borderWidth: 1,
    borderColor: '#EDE9FE',
    marginBottom: 18,
  },
  insightHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
  },
  insightHeaderText: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0F172A',
  },
  insightQuoteBox: {
    backgroundColor: '#EFEBFF',
    borderRadius: 14,
    padding: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 10,
  },
  insightQuoteText: {
    flex: 1,
    fontSize: 12.5,
    color: '#0F172A',
    lineHeight: 18,
  },
  carouselDotsRow: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
  },
  carouselDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: '#CBD5E1',
    marginHorizontal: 3,
  },
  carouselDotActive: {
    backgroundColor: '#7C3AED',
    width: 8,
    height: 8,
    borderRadius: 4,
  },

  /* 11. Deeper Insights Banner */
  deeperBanner: {
    backgroundColor: '#F5EFFF',
    borderRadius: 18,
    padding: 14,
    borderWidth: 1,
    borderColor: '#EDE9FE',
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  deeperIconCircle: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: '#E9D5FF',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  deeperTitle: {
    fontSize: 12.5,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 2,
  },
  deeperSub: {
    fontSize: 10.5,
    color: '#64748B',
    lineHeight: 14,
  },
  askLivoBtn: {
    backgroundColor: '#7C3AED',
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 14,
  },
  askLivoText: {
    fontSize: 11.5,
    fontWeight: '700',
    color: '#FFFFFF',
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
  modalHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 10,
  },
  modalSubText: {
    fontSize: 14,
    color: '#475569',
    lineHeight: 20,
    marginBottom: 20,
  },
  modalActionBtn: {
    backgroundColor: '#66C400',
    borderRadius: 14,
    paddingVertical: 12,
    alignItems: 'center',
  },
  modalActionBtnText: {
    fontSize: 14,
    fontWeight: '800',
    color: '#FFFFFF',
  },
  modalOptionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: 12,
    borderRadius: 12,
    marginBottom: 6,
    backgroundColor: '#F8FAFC',
  },
  modalOptionSelected: {
    backgroundColor: '#EBF9DB',
  },
  modalOptionText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#334155',
  },
  modalOptionTextSelected: {
    color: '#2D6A00',
    fontWeight: '700',
  },
});
