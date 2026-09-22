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
  DimensionValue,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { TimeDetailsScreen } from './TimeDetailsScreen';

interface InsightsScreenProps {
  onNavigateToTimeDetails?: () => void;
}

export const InsightsScreen: React.FC<InsightsScreenProps> = ({ onNavigateToTimeDetails }) => {
  const [showTimeDetails, setShowTimeDetails] = useState(false);

  if (showTimeDetails) {
    return <TimeDetailsScreen onBack={() => setShowTimeDetails(false)} />;
  }
  const legendData = [
    { label: 'Work', percent: '40%', color: '#66C400' },
    { label: 'Personal', percent: '20%', color: '#3B82F6' },
    { label: 'Learning', percent: '15%', color: '#8B5CF6' },
    { label: 'Health', percent: '10%', color: '#EF4444' },
    { label: 'Travel', percent: '5%', color: '#F97316' },
    { label: 'Others', percent: '10%', color: '#CBD5E1' },
  ];

  const productiveHours: { label: string; height: DimensionValue; color: string }[] = [
    { label: '6am', height: '40%', color: '#EBF9DB' },
    { label: '9am', height: '90%', color: '#66C400' },
    { label: '12pm', height: '80%', color: '#66C400' },
    { label: '3pm', height: '35%', color: '#EBF9DB' },
    { label: '6pm', height: '55%', color: '#EBF9DB' },
    { label: '9pm', height: '30%', color: '#EBF9DB' },
  ];

  const habitDays = [
    { day: 'Mon', completed: true },
    { day: 'Tue', completed: true },
    { day: 'Wed', completed: true },
    { day: 'Thu', completed: false },
    { day: 'Fri', completed: true },
    { day: 'Sat', completed: true },
    { day: 'Sun', completed: false },
  ];

  const goalsList = [
    { title: 'Build a strong port', progress: 60 },
    { title: 'Learn React', progress: 40 },
    { title: 'Improve fitness', progress: 75 },
  ];

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
            <View style={styles.logoRow}>
              <Text style={styles.logoText}>LIVO</Text>
              <View style={styles.logoDot} />
            </View>
            <Text style={styles.logoSubtitle}>A BETTER YOU</Text>
          </View>

          <View style={styles.headerRightActions}>
            <TouchableOpacity style={styles.thisWeekPill}>
              <Text style={styles.thisWeekText}>This Week</Text>
              <Feather name="chevron-down" size={13} color="#64748B" style={{ marginLeft: 4 }} />
            </TouchableOpacity>

            <TouchableOpacity style={styles.avatarCircle}>
              <Text style={styles.avatarText}>R</Text>
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
              You completed 8 out of 10 planned tasks. Keep the momentum going.
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
            <View style={styles.glanceCard}>
              <View style={styles.glanceCardHeader}>
                <Feather name="check" size={12} color="#66C400" style={{ marginRight: 4 }} />
                <Text style={styles.glanceCardLabel}>Tasks</Text>
              </View>
              <Text style={styles.glanceCardValue}>8 / 10</Text>
              <Text style={[styles.glanceCardSub, { color: '#66C400' }]}>↑ 2</Text>
            </View>

            {/* Stat 2: Goals */}
            <View style={styles.glanceCard}>
              <View style={styles.glanceCardHeader}>
                <Ionicons name="disc-outline" size={12} color="#8B5CF6" style={{ marginRight: 4 }} />
                <Text style={styles.glanceCardLabel}>Goals</Text>
              </View>
              <Text style={styles.glanceCardValue}>2 / 3</Text>
              <Text style={[styles.glanceCardSub, { color: '#8B5CF6' }]}>↑ 1</Text>
            </View>

            {/* Stat 3: Habits */}
            <View style={styles.glanceCard}>
              <View style={styles.glanceCardHeader}>
                <Ionicons name="stats-chart-outline" size={12} color="#F97316" style={{ marginRight: 4 }} />
                <Text style={styles.glanceCardLabel}>Habits</Text>
              </View>
              <Text style={styles.glanceCardValue}>5 / 7</Text>
              <Text style={[styles.glanceCardSub, { color: '#F97316' }]}>↑ 1</Text>
            </View>

            {/* Stat 4: Learning */}
            <View style={styles.glanceCard}>
              <View style={styles.glanceCardHeader}>
                <Feather name="book-open" size={12} color="#3B82F6" style={{ marginRight: 4 }} />
                <Text style={styles.glanceCardLabel}>Learning</Text>
              </View>
              <Text style={styles.glanceCardValue}>4h 30m</Text>
              <Text style={[styles.glanceCardSub, { color: '#3B82F6' }]}>↑ 1h</Text>
            </View>
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
                setShowTimeDetails(true);
              }}
              activeOpacity={0.7}
            >
              <Text style={styles.seeDetailsText}>See details</Text>
              <Feather name="arrow-right" size={12} color="#66C400" style={{ marginLeft: 4 }} />
            </TouchableOpacity>
          </View>

          <Text style={styles.totalTimeSub}>Total 28h 30m</Text>

          <View style={styles.donutWidgetRow}>
            {/* Donut Graphic */}
            <View style={styles.donutGraphicWrap}>
              <View style={styles.donutOuterCircle}>
                <View style={styles.donutInnerCircle}>
                  <Text style={styles.donutCenterValue}>28h</Text>
                  <Text style={styles.donutCenterSub}>30m</Text>
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
            <Text style={styles.hintSubText}>You do your best work in the morning.</Text>
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
          <Text style={styles.subtextLabel}>5 / 7 habits this week</Text>

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
              <View key={goal.title} style={styles.goalItemBlock}>
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
              </View>
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
        <View style={styles.livoInsightCard}>
          <View style={styles.insightHeaderRow}>
            <Ionicons name="sparkles" size={14} color="#7C3AED" style={{ marginRight: 6 }} />
            <Text style={styles.insightHeaderText}>LIVO Insight</Text>
          </View>

          <View style={styles.insightQuoteBox}>
            <Text style={styles.insightQuoteText}>
              “You're more productive on days when you work out. Consider keeping your morning workouts!”
            </Text>
            <Feather name="chevron-right" size={16} color="#7C3AED" style={{ marginLeft: 6 }} />
          </View>

          {/* Carousel Dots */}
          <View style={styles.carouselDotsRow}>
            <View style={[styles.carouselDot, styles.carouselDotActive]} />
            <View style={styles.carouselDot} />
            <View style={styles.carouselDot} />
          </View>
        </View>

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

          <TouchableOpacity style={styles.askLivoBtn}>
            <Text style={styles.askLivoText}>Ask LIVO →</Text>
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

  /* 1. Header */
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
});
