import React from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import { Colors } from '../theme/colors';
import { MOCK_TASKS, MOCK_GOALS, MOCK_INSIGHTS } from '../services/api';

export const HomeScreen: React.FC = () => {
  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {/* Header Banner */}
      <View style={styles.header}>
        <View>
          <Text style={styles.greeting}>Good Morning,</Text>
          <Text style={styles.userName}>Alex 👋</Text>
        </View>
        <View style={styles.badge}>
          <Text style={styles.badgeText}>LIVO OS v1.0</Text>
        </View>
      </View>

      {/* AI Insight Highlight Widget */}
      <View style={styles.aiWidget}>
        <View style={styles.aiHeaderRow}>
          <Text style={styles.aiWidgetTag}>⚡ LIVO AI Intelligence</Text>
          <Text style={styles.aiWidgetTime}>Just now</Text>
        </View>
        <Text style={styles.aiWidgetTitle}>{MOCK_INSIGHTS[0].title}</Text>
        <Text style={styles.aiWidgetDesc}>{MOCK_INSIGHTS[0].summary}</Text>
        <TouchableOpacity style={styles.aiActionButton}>
          <Text style={styles.aiActionText}>Apply Recommendation →</Text>
        </TouchableOpacity>
      </View>

      {/* Active Goals Section */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Active Goals</Text>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.horizontalScroll}>
          {MOCK_GOALS.map((goal) => (
            <View key={goal.id} style={styles.goalCard}>
              <Text style={styles.goalTitle}>{goal.title}</Text>
              <Text style={styles.goalTarget}>Target: {goal.targetDate}</Text>
              <View style={styles.progressTrack}>
                <View style={[styles.progressBar, { width: `${goal.progressPercentage}%` }]} />
              </View>
              <Text style={styles.progressPercent}>{goal.progressPercentage}% completed</Text>
            </View>
          ))}
        </ScrollView>
      </View>

      {/* Today's Tasks Section */}
      <View style={styles.section}>
        <View style={styles.sectionHeaderRow}>
          <Text style={styles.sectionTitle}>Today's Focus</Text>
          <Text style={styles.sectionCount}>{MOCK_TASKS.length} tasks</Text>
        </View>
        {MOCK_TASKS.map((task) => (
          <View key={task.id} style={styles.taskCard}>
            <View style={styles.taskLeft}>
              <View style={[styles.priorityDot, { backgroundColor: task.priority === 'HIGH' ? Colors.danger : Colors.warning }]} />
              <View>
                <Text style={styles.taskTitle}>{task.title}</Text>
                <Text style={styles.taskMeta}>{task.estimatedMinutes} mins • Status: {task.status}</Text>
              </View>
            </View>
          </View>
        ))}
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  content: {
    padding: 20,
    paddingTop: 50,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  greeting: {
    fontSize: 16,
    color: Colors.textSecondary,
  },
  userName: {
    fontSize: 24,
    fontWeight: 'bold',
    color: Colors.textPrimary,
  },
  badge: {
    backgroundColor: Colors.border,
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
  },
  badgeText: {
    color: Colors.primary,
    fontWeight: '600',
    fontSize: 12,
  },
  aiWidget: {
    backgroundColor: Colors.cardBackground,
    borderColor: Colors.aiPurple,
    borderWidth: 1.5,
    borderRadius: 16,
    padding: 16,
    marginBottom: 24,
  },
  aiHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  aiWidgetTag: {
    color: Colors.aiPurple,
    fontWeight: 'bold',
    fontSize: 13,
  },
  aiWidgetTime: {
    color: Colors.textSecondary,
    fontSize: 12,
  },
  aiWidgetTitle: {
    color: Colors.textPrimary,
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  aiWidgetDesc: {
    color: Colors.textSecondary,
    fontSize: 14,
    lineHeight: 20,
    marginBottom: 12,
  },
  aiActionButton: {
    backgroundColor: Colors.aiPurple,
    paddingVertical: 8,
    paddingHorizontal: 14,
    borderRadius: 8,
    alignSelf: 'flex-start',
  },
  aiActionText: {
    color: Colors.white,
    fontWeight: '600',
    fontSize: 13,
  },
  section: {
    marginBottom: 24,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: Colors.textPrimary,
    marginBottom: 12,
  },
  sectionHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  sectionCount: {
    color: Colors.textSecondary,
    fontSize: 13,
  },
  horizontalScroll: {
    marginHorizontal: -20,
    paddingHorizontal: 20,
  },
  goalCard: {
    backgroundColor: Colors.cardBackground,
    borderRadius: 14,
    padding: 16,
    width: 220,
    marginRight: 14,
    borderColor: Colors.border,
    borderWidth: 1,
  },
  goalTitle: {
    color: Colors.textPrimary,
    fontWeight: 'bold',
    fontSize: 15,
    marginBottom: 4,
  },
  goalTarget: {
    color: Colors.textSecondary,
    fontSize: 12,
    marginBottom: 12,
  },
  progressTrack: {
    height: 6,
    backgroundColor: Colors.border,
    borderRadius: 3,
    marginBottom: 6,
    overflow: 'hidden',
  },
  progressBar: {
    height: '100%',
    backgroundColor: Colors.accent,
  },
  progressPercent: {
    color: Colors.textSecondary,
    fontSize: 12,
  },
  taskCard: {
    backgroundColor: Colors.cardBackground,
    borderRadius: 12,
    padding: 14,
    marginBottom: 10,
    borderColor: Colors.border,
    borderWidth: 1,
  },
  taskLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  priorityDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    marginRight: 12,
  },
  taskTitle: {
    color: Colors.textPrimary,
    fontSize: 15,
    fontWeight: '600',
  },
  taskMeta: {
    color: Colors.textSecondary,
    fontSize: 12,
    marginTop: 2,
  },
});
