import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';
import { Colors } from '../theme/colors';

export const InsightsScreen: React.FC = () => {
  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>Insights & Analytics</Text>
      <Text style={styles.subtitle}>Cross-Module Life Analytics</Text>

      {/* Analytics Metric Cards Grid */}
      <View style={styles.grid}>
        <View style={styles.metricCard}>
          <Text style={styles.metricLabel}>Task Completion</Text>
          <Text style={styles.metricValue}>85%</Text>
          <Text style={styles.metricSub}>+12% vs last week</Text>
        </View>

        <View style={styles.metricCard}>
          <Text style={styles.metricLabel}>Habit Streak</Text>
          <Text style={styles.metricValue}>14 Days</Text>
          <Text style={styles.metricSub}>Personal Best 🔥</Text>
        </View>

        <View style={styles.metricCard}>
          <Text style={styles.metricLabel}>Schedule Efficiency</Text>
          <Text style={styles.metricValue}>92%</Text>
          <Text style={styles.metricSub}>Optimal focus time</Text>
        </View>

        <View style={styles.metricCard}>
          <Text style={styles.metricLabel}>Monthly Expenses</Text>
          <Text style={styles.metricValue}>$1,240</Text>
          <Text style={styles.metricSub}>15% under budget</Text>
        </View>
      </View>

      {/* Pattern Detection Summary */}
      <View style={styles.patternSection}>
        <Text style={styles.sectionTitle}>Detected Patterns</Text>
        <View style={styles.patternCard}>
          <Text style={styles.patternTitle}>Peak Productivity Hours</Text>
          <Text style={styles.patternDesc}>
            Your highest task completion rate occurs between 9:00 AM and 11:30 AM. Schedule critical work in this window.
          </Text>
        </View>
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
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: Colors.textPrimary,
  },
  subtitle: {
    fontSize: 14,
    color: Colors.textSecondary,
    marginBottom: 20,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    marginBottom: 20,
  },
  metricCard: {
    backgroundColor: Colors.cardBackground,
    width: '48%',
    borderRadius: 14,
    padding: 16,
    marginBottom: 14,
    borderWidth: 1,
    borderColor: Colors.border,
  },
  metricLabel: {
    color: Colors.textSecondary,
    fontSize: 12,
    marginBottom: 6,
  },
  metricValue: {
    color: Colors.textPrimary,
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  metricSub: {
    color: Colors.accent,
    fontSize: 11,
    fontWeight: '600',
  },
  patternSection: {
    marginTop: 10,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: Colors.textPrimary,
    marginBottom: 12,
  },
  patternCard: {
    backgroundColor: Colors.cardBackground,
    borderRadius: 12,
    padding: 16,
    borderColor: Colors.border,
    borderWidth: 1,
  },
  patternTitle: {
    color: Colors.textPrimary,
    fontSize: 15,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  patternDesc: {
    color: Colors.textSecondary,
    fontSize: 13,
    lineHeight: 18,
  },
});
