import React, { useState } from 'react';
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
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { Colors } from '../theme/colors';
import { ChatWithLivoScreen } from './ChatWithLivoScreen';
import { ScheduleScreen } from './ScheduleScreen';
import { ProgressScreen } from './ProgressScreen';
import { AddTaskScreen } from './AddTaskScreen';
import { AddEventScreen } from './AddEventScreen';
import { AddGoalScreen } from './AddGoalScreen';
import { AddHabitScreen } from './AddHabitScreen';
import { AddExpenseScreen } from './AddExpenseScreen';
import { AddTripScreen } from './AddTripScreen';
import { AddLearningScreen } from './AddLearningScreen';
import { AddHealthScreen } from './AddHealthScreen';

interface HomeScreenProps {
  onNavigateTab?: (tabName: string) => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = () => {
  const navigation = useNavigation<any>();
  const [showChat, setShowChat] = useState(false);
  const [showSchedule, setShowSchedule] = useState(false);
  const [showProgress, setShowProgress] = useState(false);
  const [activeScreen, setActiveScreen] = useState<'task' | 'event' | 'goal' | 'habit' | 'expense' | 'trip' | 'learning' | 'health' | null>(null);

  if (activeScreen === 'task') {
    return <AddTaskScreen onBack={() => setActiveScreen(null)} />;
  }

  if (activeScreen === 'event') {
    return <AddEventScreen onBack={() => setActiveScreen(null)} />;
  }

  if (activeScreen === 'goal') {
    return <AddGoalScreen onBack={() => setActiveScreen(null)} />;
  }

  if (activeScreen === 'habit') {
    return <AddHabitScreen onBack={() => setActiveScreen(null)} />;
  }

  if (activeScreen === 'expense') {
    return <AddExpenseScreen onBack={() => setActiveScreen(null)} />;
  }

  if (activeScreen === 'trip') {
    return <AddTripScreen onBack={() => setActiveScreen(null)} />;
  }

  if (activeScreen === 'learning') {
    return <AddLearningScreen onBack={() => setActiveScreen(null)} />;
  }

  if (activeScreen === 'health') {
    return <AddHealthScreen onBack={() => setActiveScreen(null)} />;
  }

  if (showChat) {
    return <ChatWithLivoScreen onBack={() => setShowChat(false)} />;
  }

  if (showSchedule) {
    return <ScheduleScreen onBack={() => setShowSchedule(false)} />;
  }

  if (showProgress) {
    return <ProgressScreen onBack={() => setShowProgress(false)} />;
  }
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
            <View style={styles.logoRow}>
              <Text style={styles.logoText}>LIVO</Text>
              <View style={styles.logoDot} />
            </View>
            <Text style={styles.logoSubtitle}>A BETTER YOU</Text>
          </View>

          <View style={styles.headerRightActions}>
            <TouchableOpacity style={styles.iconBtn}>
              <Feather name="search" size={20} color="#0F172A" />
            </TouchableOpacity>

            <TouchableOpacity style={styles.iconBtn}>
              <Feather name="bell" size={20} color="#0F172A" />
              <View style={styles.notificationBadge} />
            </TouchableOpacity>

            <TouchableOpacity style={styles.avatarCircle}>
              <Text style={styles.avatarText}>R</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* 2. Greeting & Date Bar */}
        <View style={styles.greetingSection}>
          <Text style={styles.greetingSub}>Good morning,</Text>
          <Text style={styles.userName}>Ritish dev 👋</Text>
          <Text style={styles.greetingSubText}>Let’s make it a productive day.</Text>
        </View>

        <View style={styles.dateBarRow}>
          <TouchableOpacity style={styles.datePickerPill} onPress={() => setShowSchedule(true)}>
            <Feather name="calendar" size={14} color="#64748B" style={{ marginRight: 6 }} />
            <Text style={styles.datePickerText}>Today, Mon 2 Sep 2024</Text>
            <Feather name="chevron-down" size={14} color="#64748B" style={{ marginLeft: 6 }} />
          </TouchableOpacity>

          <TouchableOpacity style={styles.viewCalendarBtn} onPress={() => setShowSchedule(true)}>
            <Text style={styles.viewCalendarText}>View Calendar</Text>
            <Feather name="chevron-right" size={14} color="#64748B" />
          </TouchableOpacity>
        </View>

        {/* 3. LIVO's Priority Card */}
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
              <Text style={styles.carouselCounterText}>1 of 3</Text>
              <TouchableOpacity style={styles.carouselArrowBtn}>
                <Feather name="chevron-left" size={12} color="#64748B" />
              </TouchableOpacity>
              <TouchableOpacity style={styles.carouselArrowBtn}>
                <Feather name="chevron-right" size={12} color="#64748B" />
              </TouchableOpacity>
            </View>
          </View>

          {/* Card Body - Dual Column */}
          <View style={styles.priorityBodyRow}>
            {/* Left Column (Active Task) */}
            <View style={styles.priorityLeftCol}>
              <Text style={styles.taskTitle}>Finish UI Design</Text>

              <View style={styles.taskMetaRow}>
                <View style={styles.metaItem}>
                  <Feather name="calendar" size={12} color="#64748B" />
                  <Text style={styles.metaItemText}>Due today</Text>
                </View>
                <View style={styles.metaItem}>
                  <Feather name="clock" size={12} color="#64748B" />
                  <Text style={styles.metaItemText}>9:00 AM</Text>
                </View>
              </View>

              <View style={styles.highPriorityPill}>
                <Feather name="flag" size={10} color="#DC2626" style={{ marginRight: 4 }} />
                <Text style={styles.highPriorityText}>High</Text>
              </View>

              <Text style={styles.priorityReasonText}>
                You have 1h 30m before your meeting. Finishing this now keeps your afternoon free.
              </Text>

              <TouchableOpacity style={styles.whyLivoLink}>
                <Text style={styles.whyLivoText}>Why LIVO picked this?</Text>
              </TouchableOpacity>

              <View style={styles.priorityActionBtnsRow}>
                <TouchableOpacity style={styles.startTaskBtn}>
                  <Text style={styles.startTaskBtnText}>Start Task →</Text>
                </TouchableOpacity>

                <TouchableOpacity style={styles.markDoneBtn}>
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
              <Text style={styles.nextUpTitle}>Client Presentation</Text>

              <View style={styles.nextUpMetaItem}>
                <Feather name="calendar" size={11} color="#64748B" />
                <Text style={styles.nextUpMetaText}>Tomorrow</Text>
              </View>
              <View style={styles.nextUpMetaItem}>
                <Feather name="clock" size={11} color="#64748B" />
                <Text style={styles.nextUpMetaText}>10:00 AM</Text>
              </View>

              <View style={styles.mediumPriorityPill}>
                <Text style={styles.mediumPriorityText}>Medium</Text>
              </View>
            </View>
          </View>
        </View>

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
              onPress={() => setActiveScreen('task')}
            >
              <View style={[styles.quickAddIconWrap, { backgroundColor: 'rgba(102, 196, 0, 0.15)' }]}>
                <Feather name="check" size={16} color="#2D6A00" />
              </View>
              <Text style={[styles.quickAddCardText, { color: '#2D6A00' }]}>Task</Text>
            </TouchableOpacity>

            {/* Event */}
            <TouchableOpacity
              style={[styles.quickAddCard, { backgroundColor: '#EBF3FF' }]}
              onPress={() => setActiveScreen('event')}
            >
              <View style={[styles.quickAddIconWrap, { backgroundColor: 'rgba(29, 78, 216, 0.12)' }]}>
                <Feather name="calendar" size={16} color="#1D4ED8" />
              </View>
              <Text style={[styles.quickAddCardText, { color: '#1D4ED8' }]}>Event</Text>
            </TouchableOpacity>

            {/* Goal */}
            <TouchableOpacity
              style={[styles.quickAddCard, { backgroundColor: '#F3EBFB' }]}
              onPress={() => setActiveScreen('goal')}
            >
              <View style={[styles.quickAddIconWrap, { backgroundColor: 'rgba(109, 40, 217, 0.12)' }]}>
                <Ionicons name="disc-outline" size={16} color="#6D28D9" />
              </View>
              <Text style={[styles.quickAddCardText, { color: '#6D28D9' }]}>Goal</Text>
            </TouchableOpacity>

            {/* Habit */}
            <TouchableOpacity
              style={[styles.quickAddCard, { backgroundColor: '#FFF3E5' }]}
              onPress={() => setActiveScreen('habit')}
            >
              <View style={[styles.quickAddIconWrap, { backgroundColor: 'rgba(194, 65, 12, 0.12)' }]}>
                <Ionicons name="stats-chart-outline" size={16} color="#C2410C" />
              </View>
              <Text style={[styles.quickAddCardText, { color: '#C2410C' }]}>Habit</Text>
            </TouchableOpacity>

            {/* Expense */}
            <TouchableOpacity
              style={[styles.quickAddCard, { backgroundColor: '#FFEBF0' }]}
              onPress={() => setActiveScreen('expense')}
            >
              <View style={[styles.quickAddIconWrap, { backgroundColor: 'rgba(190, 18, 60, 0.12)' }]}>
                <Ionicons name="card-outline" size={16} color="#BE123C" />
              </View>
              <Text style={[styles.quickAddCardText, { color: '#BE123C' }]}>Expense</Text>
            </TouchableOpacity>

            {/* Trip */}
            <TouchableOpacity
              style={[styles.quickAddCard, { backgroundColor: '#E0F2FE' }]}
              onPress={() => setActiveScreen('trip')}
            >
              <View style={[styles.quickAddIconWrap, { backgroundColor: 'rgba(13, 148, 136, 0.12)' }]}>
                <Ionicons name="earth-outline" size={16} color="#0D9488" />
              </View>
              <Text style={[styles.quickAddCardText, { color: '#0D9488' }]}>Trip</Text>
            </TouchableOpacity>

            {/* Learning */}
            <TouchableOpacity
              style={[styles.quickAddCard, { backgroundColor: '#F3E8FF' }]}
              onPress={() => setActiveScreen('learning')}
            >
              <View style={[styles.quickAddIconWrap, { backgroundColor: 'rgba(124, 58, 237, 0.12)' }]}>
                <Ionicons name="school-outline" size={16} color="#7C3AED" />
              </View>
              <Text style={[styles.quickAddCardText, { color: '#7C3AED' }]}>Learning</Text>
            </TouchableOpacity>

            {/* Health */}
            <TouchableOpacity
              style={[styles.quickAddCard, { backgroundColor: '#EBF9DB' }]}
              onPress={() => setActiveScreen('health')}
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
            <TouchableOpacity style={styles.seeAllBtn} onPress={() => setShowSchedule(true)}>
              <Text style={styles.seeAllText}>See all</Text>
              <Feather name="chevron-right" size={14} color="#64748B" />
            </TouchableOpacity>
          </View>

          {/* Timeline List */}
          <View style={styles.timelineContainer}>
            {/* Item 1 - Work on UI Design */}
            <View style={styles.timelineItemRow}>
              <View style={styles.timelineLeftTime}>
                <Text style={styles.timelineTimeText}>9:00 AM</Text>
              </View>
              <View style={styles.timelineLineCol}>
                <View style={styles.timelineActiveDot} />
                <View style={styles.timelineVerticalLine} />
              </View>
              <View style={styles.timelineContentBox}>
                <View style={styles.timelineContentHeader}>
                  <Text style={styles.timelineTaskTitle}>Work on UI Design</Text>
                  <View style={styles.nowBadge}>
                    <Text style={styles.nowBadgeText}>Now</Text>
                  </View>
                </View>
                <Text style={styles.timelineTaskSub}>Deep work</Text>
              </View>
            </View>

            {/* Item 2 - Team Sync */}
            <View style={styles.timelineItemRow}>
              <View style={styles.timelineLeftTime}>
                <Text style={styles.timelineTimeText}>11:30 AM</Text>
              </View>
              <View style={styles.timelineLineCol}>
                <View style={styles.timelineInactiveDot} />
                <View style={styles.timelineVerticalLine} />
              </View>
              <View style={styles.timelineContentBox}>
                <Text style={styles.timelineTaskTitle}>Team Sync</Text>
                <Text style={styles.timelineTaskSub}>Google Meet</Text>
              </View>
            </View>

            {/* Item 3 - Client Meeting */}
            <View style={styles.timelineItemRow}>
              <View style={styles.timelineLeftTime}>
                <Text style={styles.timelineTimeText}>2:00 PM</Text>
              </View>
              <View style={styles.timelineLineCol}>
                <View style={styles.timelineInactiveDot} />
                <View style={styles.timelineVerticalLine} />
              </View>
              <View style={styles.timelineContentBox}>
                <Text style={styles.timelineTaskTitle}>Client Meeting</Text>
                <Text style={styles.timelineTaskSub}>Discuss project updates</Text>
              </View>
            </View>

            {/* Item 4 - Gym */}
            <View style={styles.timelineItemRow}>
              <View style={styles.timelineLeftTime}>
                <Text style={styles.timelineTimeText}>5:00 PM</Text>
              </View>
              <View style={styles.timelineLineCol}>
                <View style={styles.timelineInactiveDot} />
              </View>
              <View style={styles.timelineContentBox}>
                <Text style={styles.timelineTaskTitle}>Gym</Text>
                <Text style={styles.timelineTaskSub}>Stay consistent</Text>
              </View>
            </View>

            {/* Bottom Current Time Indicator Line */}
            <View style={styles.currentTimeRow}>
              <View style={styles.currentTimeLeft}>
                <View style={styles.currentTimeDot} />
                <Text style={styles.currentTimeText}>07:45 AM</Text>
              </View>
              <View style={styles.currentTimeDottedLine} />
              <Text style={styles.currentTimeLabel}>Current time</Text>
            </View>
          </View>
        </View>

        {/* 6. Today's Progress Section */}
        <TouchableOpacity
          style={styles.whiteCardSection}
          onPress={() => setShowProgress(true)}
          activeOpacity={0.9}
        >
          <View style={styles.cardSectionHeaderRow}>
            <Text style={styles.cardSectionTitle}>Today's Progress</Text>
            <TouchableOpacity onPress={() => setShowProgress(true)}>
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
              <Text style={styles.progressRowRatio}>3/5</Text>
            </View>
            <View style={styles.progressTrack}>
              <View style={[styles.progressBarFill, { width: '60%', backgroundColor: '#66C400' }]} />
            </View>

            {/* Habits */}
            <View style={styles.progressRow}>
              <View style={styles.progressRowLeft}>
                <Ionicons name="stats-chart-outline" size={14} color="#F97316" style={{ marginRight: 6 }} />
                <Text style={styles.progressRowLabel}>Habits</Text>
              </View>
              <Text style={styles.progressRowRatio}>2/4</Text>
            </View>
            <View style={styles.progressTrack}>
              <View style={[styles.progressBarFill, { width: '50%', backgroundColor: '#F97316' }]} />
            </View>

            {/* Goals */}
            <View style={styles.progressRow}>
              <View style={styles.progressRowLeft}>
                <Ionicons name="disc-outline" size={14} color="#8B5CF6" style={{ marginRight: 6 }} />
                <Text style={styles.progressRowLabel}>Goals</Text>
              </View>
              <Text style={styles.progressRowRatio}>1/3</Text>
            </View>
            <View style={styles.progressTrack}>
              <View style={[styles.progressBarFill, { width: '33%', backgroundColor: '#8B5CF6' }]} />
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

            <TouchableOpacity style={styles.chatLivoArrowBtn} onPress={() => setShowChat(true)}>
              <Feather name="arrow-right" size={14} color="#7C3AED" />
            </TouchableOpacity>
          </View>

          <Text style={styles.chatLivoPromptText}>
            You have 1hr of free time in this evening do you need me to add anything at that time line?
          </Text>

          <View style={styles.chatLivoChipsRow}>
            <TouchableOpacity style={styles.chatChipBtn}>
              <Text style={styles.chatChipText}>Plan my evening</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.chatChipBtn}>
              <Text style={styles.chatChipText}>Break down this task</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.chatChipBtn}>
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
    paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) + 14 : 20,
    paddingBottom: 40,
  },

  /* 1. Header */
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  logoRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  logoText: {
    fontSize: 24,
    fontWeight: '900',
    color: '#0F172A',
    letterSpacing: 0.5,
  },
  logoDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#66C400',
    marginLeft: 2,
    marginTop: 6,
  },
  logoSubtitle: {
    fontSize: 9,
    fontWeight: '700',
    color: '#94A3B8',
    letterSpacing: 1.2,
    marginTop: -2,
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
    height: 190,
    backgroundColor: '#E2EECC',
    marginHorizontal: 8,
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
    marginBottom: 8,
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
    backgroundColor: '#E9D5FF',
    alignItems: 'center',
    justifyContent: 'center',
  },
  chatLivoPromptText: {
    fontSize: 13,
    color: '#64748B',
    lineHeight: 18,
    marginBottom: 14,
  },
  chatLivoChipsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  chatChipBtn: {
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 16,
    marginRight: 8,
    marginBottom: 6,
    borderWidth: 1,
    borderColor: '#DDD6FE',
  },
  chatChipText: {
    fontSize: 11.5,
    fontWeight: '600',
    color: '#7C3AED',
  },
});

export const homeScreen = HomeScreen;
export default HomeScreen;
