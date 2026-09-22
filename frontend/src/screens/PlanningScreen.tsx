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
import { ScheduleScreen } from './ScheduleScreen';

export const PlanningScreen: React.FC = () => {
  const [showSchedule, setShowSchedule] = useState(false);
  const [selectedDay, setSelectedDay] = useState('Mon 2 Sep');
  const [checkedTasks, setCheckedTasks] = useState<{ [key: string]: boolean }>({});

  if (showSchedule) {
    return <ScheduleScreen onBack={() => setShowSchedule(false)} />;
  }

  const toggleTask = (taskId: string) => {
    setCheckedTasks((prev) => ({ ...prev, [taskId]: !prev[taskId] }));
  };

  const daysList = [
    { day: 'Mon', date: '2 Sep', full: 'Mon 2 Sep' },
    { day: 'Tue', date: '3 Sep', full: 'Tue 3 Sep' },
    { day: 'Wed', date: '4 Sep', full: 'Wed 4 Sep' },
    { day: 'Thu', date: '5 Sep', full: 'Thu 5 Sep' },
    { day: 'Fri', date: '6 Sep', full: 'Fri 6 Sep' },
    { day: 'Sat', date: '7 Sep', full: 'Sat 7 Sep' },
  ];

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
          <TouchableOpacity style={styles.datePickerPill} onPress={() => setShowSchedule(true)}>
            <Feather name="calendar" size={14} color="#64748B" style={{ marginRight: 6 }} />
            <Text style={styles.datePickerText}>Today, Mon 2 Sep 2024</Text>
            <Feather name="chevron-down" size={14} color="#64748B" style={{ marginLeft: 6 }} />
          </TouchableOpacity>

          <View style={styles.dateRightGroup}>
            <TouchableOpacity style={styles.todayBtnPill} onPress={() => setShowSchedule(true)}>
              <Text style={styles.todayBtnText}>Today</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.dateArrowBtn} onPress={() => setShowSchedule(true)}>
              <Feather name="chevron-right" size={14} color="#64748B" />
            </TouchableOpacity>
          </View>
        </View>

        {/* 4. Today's Workload Widget */}
        <View style={styles.workloadCard}>
          {/* 72% Circular Gauge */}
          <View style={styles.gaugeContainer}>
            <View style={styles.gaugeCircleOuter}>
              <Text style={styles.gaugePercentText}>72%</Text>
            </View>
          </View>

          {/* Workload Stats */}
          <View style={styles.workloadInfoCol}>
            <Text style={styles.workloadTitle}>Today's workload</Text>
            <Text style={styles.workloadSub}>4 tasks • 3 events • 6h 30m planned</Text>
            <Text style={styles.workloadHint}>You have a balanced day. Keep it up!</Text>
          </View>

          {/* CTA Action Button */}
          <View style={styles.workloadCtaCol}>
            <TouchableOpacity style={styles.planMyDayBtn}>
              <Text style={styles.planMyDayText}>Plan my day ✨</Text>
            </TouchableOpacity>
            <Text style={styles.planMyDaySub}>Let LIVO optimize your day</Text>
          </View>
        </View>

        {/* 5. Weekly Date Selector Strip */}
        <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.weeklyStripScroll}>
          {daysList.map((item) => {
            const isSelected = selectedDay === item.full;
            return (
              <TouchableOpacity
                key={item.full}
                onPress={() => setSelectedDay(item.full)}
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
              <Feather name="menu" size={16} color="#0F172A" style={{ marginRight: 6 }} />
              <Text style={styles.cardSectionTitle}>Today's Plan</Text>
            </View>
            <TouchableOpacity style={styles.addBtnWrap}>
              <Feather name="plus" size={14} color="#64748B" />
              <Text style={styles.addBtnText}>Add</Text>
            </TouchableOpacity>
          </View>

          {/* Timeline Schedule Items */}
          <View style={styles.timelineListContainer}>
            {/* 8:00 AM - Morning Routine */}
            <View style={styles.scheduleRow}>
              <Text style={styles.scheduleTime}>8:00 AM</Text>
              <View style={styles.timelineDotCol}>
                <View style={styles.timelineGrayDot} />
                <View style={styles.timelineVerticalLine} />
              </View>
              <View style={[styles.scheduleCard, styles.whiteScheduleCard]}>
                <View style={styles.scheduleCardMain}>
                  <Text style={styles.scheduleCardTitle}>Morning Routine</Text>
                  <Text style={styles.scheduleCardSub}>Get ready for the day</Text>
                </View>
              </View>
            </View>

            {/* 9:00 AM - Work on UI Designs (Task) */}
            <View style={styles.scheduleRow}>
              <Text style={styles.scheduleTime}>9:00 AM</Text>
              <View style={styles.timelineDotCol}>
                <View style={styles.timelineGreenDot} />
                <View style={styles.timelineVerticalLine} />
              </View>
              <View style={[styles.scheduleCard, styles.taskScheduleCard]}>
                <View style={styles.scheduleCardMain}>
                  <Text style={styles.scheduleCardTitle}>Work on UI Designs</Text>
                  <Text style={styles.scheduleCardSub}>Deep Work</Text>
                </View>
                <View style={styles.badgeMenuRow}>
                  <View style={styles.taskTypeBadge}>
                    <Text style={styles.taskTypeBadgeText}>Task</Text>
                  </View>
                  <Feather name="more-vertical" size={14} color="#64748B" style={{ marginLeft: 4 }} />
                </View>
              </View>
            </View>

            {/* 11:30 AM - Team Sync (Event) */}
            <View style={styles.scheduleRow}>
              <Text style={styles.scheduleTime}>11:30 AM</Text>
              <View style={styles.timelineDotCol}>
                <View style={styles.timelineBlueDot} />
                <View style={styles.timelineVerticalLine} />
              </View>
              <View style={[styles.scheduleCard, styles.eventScheduleCard]}>
                <View style={styles.scheduleCardMain}>
                  <Text style={styles.scheduleCardTitle}>Team Sync</Text>
                  <Text style={styles.scheduleCardSub}>Google Meet</Text>
                </View>
                <View style={styles.badgeMenuRow}>
                  <View style={styles.eventTypeBadge}>
                    <Text style={styles.eventTypeBadgeText}>Event</Text>
                  </View>
                  <Feather name="more-vertical" size={14} color="#64748B" style={{ marginLeft: 4 }} />
                </View>
              </View>
            </View>

            {/* 1:00 PM - Lunch Break */}
            <View style={styles.scheduleRow}>
              <Text style={styles.scheduleTime}>1:00 PM</Text>
              <View style={styles.timelineDotCol}>
                <View style={styles.timelineGrayDot} />
                <View style={styles.timelineVerticalLine} />
              </View>
              <View style={[styles.scheduleCard, styles.whiteScheduleCard]}>
                <View style={styles.scheduleCardMain}>
                  <Text style={styles.scheduleCardTitle}>Lunch Break</Text>
                  <Text style={styles.scheduleCardSub}>Take some rest</Text>
                </View>
              </View>
            </View>

            {/* 2:00 PM - Client meet (Event) */}
            <View style={styles.scheduleRow}>
              <Text style={styles.scheduleTime}>2:00 PM</Text>
              <View style={styles.timelineDotCol}>
                <View style={styles.timelineBlueDot} />
                <View style={styles.timelineVerticalLine} />
              </View>
              <View style={[styles.scheduleCard, styles.eventScheduleCard]}>
                <View style={styles.scheduleCardMain}>
                  <Text style={styles.scheduleCardTitle}>Client meet</Text>
                  <Text style={styles.scheduleCardSub}>Discuss updates</Text>
                </View>
                <View style={styles.badgeMenuRow}>
                  <View style={styles.eventTypeBadge}>
                    <Text style={styles.eventTypeBadgeText}>Event</Text>
                  </View>
                  <Feather name="more-vertical" size={14} color="#64748B" style={{ marginLeft: 4 }} />
                </View>
              </View>
            </View>

            {/* 4:00 PM - Portfolio review (Task) */}
            <View style={styles.scheduleRow}>
              <Text style={styles.scheduleTime}>4:00 PM</Text>
              <View style={styles.timelineDotCol}>
                <View style={styles.timelineGreenDot} />
                <View style={styles.timelineVerticalLine} />
              </View>
              <View style={[styles.scheduleCard, styles.taskScheduleCard]}>
                <View style={styles.scheduleCardMain}>
                  <Text style={styles.scheduleCardTitle}>Portfolio review</Text>
                  <Text style={styles.scheduleCardSub}>Go through designs</Text>
                </View>
                <View style={styles.badgeMenuRow}>
                  <View style={styles.taskTypeBadge}>
                    <Text style={styles.taskTypeBadgeText}>Task</Text>
                  </View>
                  <Feather name="more-vertical" size={14} color="#64748B" style={{ marginLeft: 4 }} />
                </View>
              </View>
            </View>

            {/* 5:00 PM - Gym (Habit) */}
            <View style={styles.scheduleRow}>
              <Text style={styles.scheduleTime}>5:00 PM</Text>
              <View style={styles.timelineDotCol}>
                <View style={styles.timelineOrangeDot} />
                <View style={styles.timelineVerticalLine} />
              </View>
              <View style={[styles.scheduleCard, styles.habitScheduleCard]}>
                <View style={styles.scheduleCardMain}>
                  <Text style={styles.scheduleCardTitle}>Gym</Text>
                  <Text style={styles.scheduleCardSub}>Stay consistent</Text>
                </View>
                <View style={styles.badgeMenuRow}>
                  <View style={styles.habitTypeBadge}>
                    <Text style={styles.habitTypeBadgeText}>Habit</Text>
                  </View>
                  <Feather name="more-vertical" size={14} color="#64748B" style={{ marginLeft: 4 }} />
                </View>
              </View>
            </View>

            {/* 7:00 PM - Free Time */}
            <View style={styles.scheduleRow}>
              <Text style={styles.scheduleTime}>7:00 PM</Text>
              <View style={styles.timelineDotCol}>
                <View style={styles.timelineOpenDot} />
              </View>
              <View style={[styles.scheduleCard, styles.whiteScheduleCard]}>
                <View style={styles.scheduleCardMain}>
                  <Text style={styles.scheduleCardTitle}>Free Time</Text>
                  <Text style={styles.scheduleCardSub}>Get ready for the next day.</Text>
                </View>
              </View>
            </View>
          </View>
        </View>

        {/* 7. LIVO Suggests (AI Intelligent Tip Banner) */}
        <View style={styles.livoSuggestsCard}>
          <View style={styles.suggestsHeaderRow}>
            <View style={styles.suggestsTitleWrap}>
              <Ionicons name="sparkles" size={14} color="#7C3AED" style={{ marginRight: 6 }} />
              <Text style={styles.suggestsTitleText}>LIVO Suggests</Text>
            </View>
            <Feather name="more-vertical" size={14} color="#7C3AED" />
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
            <TouchableOpacity style={styles.smallPlusBtn}>
              <Feather name="plus" size={14} color="#64748B" />
            </TouchableOpacity>
          </View>

          <View style={styles.unscheduledList}>
            {/* Task 1 */}
            <TouchableOpacity
              style={styles.taskCheckRow}
              onPress={() => toggleTask('t1')}
            >
              <View style={[styles.checkBoxCircle, checkedTasks['t1'] && styles.checkBoxChecked]}>
                {checkedTasks['t1'] && <Feather name="check" size={10} color="#FFFFFF" />}
              </View>
              <Text style={[styles.taskCheckLabel, checkedTasks['t1'] && styles.taskCheckLabelDone]}>
                Finish portfolio case study
              </Text>
            </TouchableOpacity>

            {/* Task 2 */}
            <TouchableOpacity
              style={styles.taskCheckRow}
              onPress={() => toggleTask('t2')}
            >
              <View style={[styles.checkBoxCircle, checkedTasks['t2'] && styles.checkBoxChecked]}>
                {checkedTasks['t2'] && <Feather name="check" size={10} color="#FFFFFF" />}
              </View>
              <Text style={[styles.taskCheckLabel, checkedTasks['t2'] && styles.taskCheckLabelDone]}>
                Read React chapter
              </Text>
            </TouchableOpacity>

            {/* Task 3 */}
            <TouchableOpacity
              style={styles.taskCheckRow}
              onPress={() => toggleTask('t3')}
            >
              <View style={[styles.checkBoxCircle, checkedTasks['t3'] && styles.checkBoxChecked]}>
                {checkedTasks['t3'] && <Feather name="check" size={10} color="#FFFFFF" />}
              </View>
              <Text style={[styles.taskCheckLabel, checkedTasks['t3'] && styles.taskCheckLabelDone]}>
                Plan weekend trip
              </Text>
            </TouchableOpacity>
          </View>

          <TouchableOpacity style={styles.viewAllTasksBtn}>
            <Text style={styles.viewAllTasksText}>View all tasks →</Text>
          </TouchableOpacity>
        </View>

        {/* 9. Upcoming Section */}
        <View style={styles.whiteCardSection}>
          <View style={styles.cardSectionHeaderRow}>
            <Text style={styles.cardSectionTitle}>Upcoming</Text>
            <TouchableOpacity style={styles.seeAllBtn}>
              <Text style={styles.seeAllText}>See all</Text>
              <Feather name="chevron-right" size={14} color="#64748B" />
            </TouchableOpacity>
          </View>

          <View style={styles.upcomingList}>
            {/* Item 1 */}
            <View style={styles.upcomingRow}>
              <View style={styles.upcomingLeft}>
                <View style={styles.calendarIconSquare}>
                  <Feather name="calendar" size={14} color="#64748B" />
                </View>
                <View>
                  <Text style={styles.upcomingItemTitle}>Client Presentation</Text>
                  <Text style={styles.upcomingItemTime}>Tomorrow, 10:00 AM</Text>
                </View>
              </View>
              <View style={styles.highPriorityPill}>
                <Text style={styles.highPriorityText}>High</Text>
              </View>
            </View>

            {/* Item 2 */}
            <View style={styles.upcomingRow}>
              <View style={styles.upcomingLeft}>
                <View style={styles.calendarIconSquare}>
                  <Feather name="calendar" size={14} color="#64748B" />
                </View>
                <View>
                  <Text style={styles.upcomingItemTitle}>Project Review</Text>
                  <Text style={styles.upcomingItemTime}>Wed, 4 Sep, 2:00 PM</Text>
                </View>
              </View>
              <View style={styles.mediumPriorityPill}>
                <Text style={styles.mediumPriorityText}>Medium</Text>
              </View>
            </View>

            {/* Item 3 */}
            <View style={styles.upcomingRow}>
              <View style={styles.upcomingLeft}>
                <View style={styles.calendarIconSquare}>
                  <Feather name="calendar" size={14} color="#64748B" />
                </View>
                <View>
                  <Text style={styles.upcomingItemTitle}>Design Handover</Text>
                  <Text style={styles.upcomingItemTime}>Fri, 6 Sep, 11:00 AM</Text>
                </View>
              </View>
              <View style={styles.lowPriorityPill}>
                <Text style={styles.lowPriorityText}>Low</Text>
              </View>
            </View>
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

          <TouchableOpacity style={styles.chatWithLivoPill}>
            <Text style={styles.chatWithLivoText}>Chat with LIVO ∨</Text>
          </TouchableOpacity>
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

  /* 1. Top Header Row */
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
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
});
