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

interface ScheduleScreenProps {
  onBack?: () => void;
}

export const ScheduleScreen: React.FC<ScheduleScreenProps> = ({ onBack }) => {
  const [unscheduledTasks, setUnscheduledTasks] = useState([
    {
      id: '1',
      title: 'Prepare client presentation',
      sub: "For next week's meeting",
      checked: false,
    },
    {
      id: '2',
      title: 'Read React chapter',
      sub: 'Build your skills',
      checked: false,
    },
    {
      id: '3',
      title: 'Plan weekend trip',
      sub: 'Short getaway',
      checked: false,
    },
  ]);

  const toggleCheck = (id: string) => {
    setUnscheduledTasks((prev) =>
      prev.map((item) =>
        item.id === id ? { ...item, checked: !item.checked } : item
      )
    );
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      {/* Header Bar */}
      <View style={styles.headerBar}>
        <TouchableOpacity
          style={styles.backBtn}
          onPress={onBack}
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
          <TouchableOpacity style={styles.dateNavBtn}>
            <Feather name="chevron-left" size={16} color="#475569" />
          </TouchableOpacity>

          <TouchableOpacity style={styles.datePickerPill}>
            <Feather name="calendar" size={14} color="#64748B" style={{ marginRight: 6 }} />
            <Text style={styles.datePickerText}>Today, Mon 2 Sep</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.dateNavBtn}>
            <Feather name="chevron-right" size={16} color="#475569" />
          </TouchableOpacity>

          <TouchableOpacity style={styles.todayBadge}>
            <Text style={styles.todayBadgeText}>Today</Text>
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
              <Text style={styles.metricValue}>6h 30m</Text>
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
              <Text style={styles.metricValue}>4</Text>
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
              <Text style={styles.metricValue}>3</Text>
              <Text style={styles.metricLabel}>events</Text>
            </View>
          </View>

          <View style={styles.metricDivider} />

          {/* Workload */}
          <View style={styles.metricCol}>
            <View style={styles.metricIconWrap}>
              <Ionicons name="stats-chart" size={16} color="#2D6A00" />
            </View>
            <View>
              <Text style={styles.metricValue}>Medium</Text>
              <Text style={styles.metricLabel}>workload</Text>
            </View>
          </View>
        </View>

        {/* Main Schedule Timeline Card */}
        <View style={styles.timelineCard}>
          {/* Current Time Bar */}
          <View style={styles.currentTimeBar}>
            <View style={styles.currentTimeLeft}>
              <View style={styles.greenPulseDot} />
              <Text style={styles.currentTimeText}>07:45 AM</Text>
            </View>
            <View style={styles.dashedLine} />
            <Text style={styles.currentTimeLabel}>Current time</Text>
          </View>

          {/* Schedule List */}
          <View style={styles.timelineList}>
            {/* Item 1: Work on UI Design (Now) */}
            <View style={styles.timelineItemRow}>
              <View style={styles.timeCol}>
                <Text style={styles.timeTextActive}>9:00 AM</Text>
              </View>

              <View style={styles.dotLineCol}>
                <View style={styles.outerRingDot}>
                  <View style={styles.innerRingDot} />
                </View>
                <View style={styles.verticalLine} />
              </View>

              <View style={styles.itemContentCol}>
                <View style={styles.itemMainRow}>
                  <View style={styles.itemTitleWrap}>
                    <Text style={styles.itemTitle}>Work on UI Design</Text>
                    <View style={styles.nowBadge}>
                      <Text style={styles.nowBadgeText}>Now</Text>
                    </View>
                  </View>
                  <TouchableOpacity style={styles.moreBtn}>
                    <Feather name="more-horizontal" size={16} color="#94A3B8" />
                  </TouchableOpacity>
                </View>
                <Text style={styles.itemSubText}>Deep work</Text>
              </View>
            </View>

            {/* Item 2: Team Sync */}
            <View style={styles.timelineItemRow}>
              <View style={styles.timeCol}>
                <Text style={styles.timeText}>11:30 AM</Text>
              </View>

              <View style={styles.dotLineCol}>
                <View style={styles.grayDot} />
                <View style={styles.verticalLine} />
              </View>

              <View style={styles.itemContentCol}>
                <View style={styles.itemMainRow}>
                  <Text style={styles.itemTitle}>Team Sync</Text>
                  <TouchableOpacity style={styles.moreBtn}>
                    <Feather name="more-horizontal" size={16} color="#94A3B8" />
                  </TouchableOpacity>
                </View>
                <Text style={styles.itemSubText}>Google Meet</Text>
              </View>
            </View>

            {/* Item 3: Client Meeting */}
            <View style={styles.timelineItemRow}>
              <View style={styles.timeCol}>
                <Text style={styles.timeText}>2:00 PM</Text>
              </View>

              <View style={styles.dotLineCol}>
                <View style={styles.grayDot} />
                <View style={styles.verticalLine} />
              </View>

              <View style={styles.itemContentCol}>
                <View style={styles.itemMainRow}>
                  <Text style={styles.itemTitle}>Client Meeting</Text>
                  <TouchableOpacity style={styles.moreBtn}>
                    <Feather name="more-horizontal" size={16} color="#94A3B8" />
                  </TouchableOpacity>
                </View>
                <Text style={styles.itemSubText}>Discuss project updates</Text>
              </View>
            </View>

            {/* Item 4: Gym */}
            <View style={styles.timelineItemRow}>
              <View style={styles.timeCol}>
                <Text style={styles.timeText}>5:00 PM</Text>
              </View>

              <View style={styles.dotLineCol}>
                <View style={styles.grayDot} />
              </View>

              <View style={styles.itemContentCol}>
                <View style={styles.itemMainRow}>
                  <Text style={styles.itemTitle}>Gym</Text>
                  <TouchableOpacity style={styles.moreBtn}>
                    <Feather name="more-horizontal" size={16} color="#94A3B8" />
                  </TouchableOpacity>
                </View>
                <Text style={styles.itemSubText}>Stay consistent</Text>
              </View>
            </View>
          </View>
        </View>

        {/* Unscheduled Section */}
        <View style={styles.unscheduledCard}>
          <View style={styles.unscheduledHeaderRow}>
            <View>
              <Text style={styles.unscheduledTitle}>Unscheduled</Text>
              <Text style={styles.unscheduledSub}>Tasks you can plan for later.</Text>
            </View>

            <TouchableOpacity style={styles.seeAllBtn}>
              <Text style={styles.seeAllText}>See all</Text>
              <Feather name="arrow-right" size={14} color="#66C400" style={{ marginLeft: 2 }} />
            </TouchableOpacity>
          </View>

          <View style={styles.unscheduledList}>
            {unscheduledTasks.map((task) => (
              <View key={task.id} style={styles.unscheduledRow}>
                <TouchableOpacity
                  style={styles.checkbox}
                  onPress={() => toggleCheck(task.id)}
                >
                  {task.checked && <Feather name="check" size={14} color="#66C400" />}
                </TouchableOpacity>

                <View style={styles.taskInfoCol}>
                  <Text
                    style={[
                      styles.taskTitle,
                      task.checked && styles.taskTitleChecked,
                    ]}
                  >
                    {task.title}
                  </Text>
                  <Text style={styles.taskSub}>{task.sub}</Text>
                </View>

                <TouchableOpacity style={styles.addPillBtn}>
                  <Text style={styles.addPillText}>Add</Text>
                </TouchableOpacity>

                <TouchableOpacity style={styles.moreBtn}>
                  <Feather name="more-horizontal" size={16} color="#94A3B8" />
                </TouchableOpacity>
              </View>
            ))}
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
                You have a 1h 30m gap at 4:00 PM.{'\n'}Want to work on your portfolio?
              </Text>
            </View>
          </View>

          <TouchableOpacity style={styles.addToPlanBtn}>
            <Text style={styles.addToPlanText}>Add to plan</Text>
          </TouchableOpacity>
        </View>

        <View style={{ height: 30 }} />
      </ScrollView>
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
  todayBadge: {
    backgroundColor: '#E2F7C5',
    borderRadius: 14,
    paddingHorizontal: 14,
    paddingVertical: 8,
    marginLeft: 4,
  },
  todayBadgeText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#2D6A00',
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
});
