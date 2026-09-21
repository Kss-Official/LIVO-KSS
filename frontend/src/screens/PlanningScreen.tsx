import React from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity } from 'react-native';
import { Colors } from '../theme/colors';

export const PlanningScreen: React.FC = () => {
  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>Daily Schedule & Planning</Text>
      <Text style={styles.subtitle}>Friday, September 19, 2026</Text>

      {/* Conflict / Overload State Banner */}
      <View style={styles.conflictCard}>
        <Text style={styles.conflictHeader}>⚠️ Overlap Detected in Schedule</Text>
        <Text style={styles.conflictBody}>
          "Finish LIVO AI Engine setup" (9:00 AM - 10:30 AM) overlaps with "Team Architecture Review" (10:00 AM - 11:00 AM).
        </Text>
        <TouchableOpacity style={styles.resolveButton}>
          <Text style={styles.resolveText}>Ask LIVO AI to Auto-Reschedule</Text>
        </TouchableOpacity>
      </View>

      {/* Timeline Schedule */}
      <View style={styles.timelineSection}>
        <Text style={styles.sectionTitle}>Timeline View</Text>
        
        <View style={styles.timelineItem}>
          <Text style={styles.timeLabel}>08:00 AM</Text>
          <View style={styles.eventCard}>
            <Text style={styles.eventTitle}>Morning Routine & Habit Check-in</Text>
            <Text style={styles.eventSub}>Habit • 30 mins</Text>
          </View>
        </View>

        <View style={styles.timelineItem}>
          <Text style={styles.timeLabel}>09:00 AM</Text>
          <View style={[styles.eventCard, styles.conflictBorder]}>
            <Text style={styles.eventTitle}>Finish LIVO AI Engine setup</Text>
            <Text style={styles.eventSub}>High Priority Task • 90 mins</Text>
          </View>
        </View>

        <View style={styles.timelineItem}>
          <Text style={styles.timeLabel}>10:00 AM</Text>
          <View style={[styles.eventCard, styles.conflictBorder]}>
            <Text style={styles.eventTitle}>Team Architecture Review</Text>
            <Text style={styles.eventSub}>Calendar Event • 60 mins</Text>
          </View>
        </View>

        <View style={styles.timelineItem}>
          <Text style={styles.timeLabel}>01:00 PM</Text>
          <View style={styles.eventCard}>
            <Text style={styles.eventTitle}>Deep Work: React Native Refactoring</Text>
            <Text style={styles.eventSub}>Learning & Task • 120 mins</Text>
          </View>
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
  conflictCard: {
    backgroundColor: 'rgba(239, 68, 68, 0.1)',
    borderColor: Colors.danger,
    borderWidth: 1,
    borderRadius: 14,
    padding: 16,
    marginBottom: 24,
  },
  conflictHeader: {
    color: Colors.danger,
    fontWeight: 'bold',
    fontSize: 15,
    marginBottom: 6,
  },
  conflictBody: {
    color: Colors.textPrimary,
    fontSize: 13,
    lineHeight: 18,
    marginBottom: 12,
  },
  resolveButton: {
    backgroundColor: Colors.danger,
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 8,
    alignSelf: 'flex-start',
  },
  resolveText: {
    color: Colors.white,
    fontWeight: 'bold',
    fontSize: 12,
  },
  timelineSection: {
    marginTop: 10,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: Colors.textPrimary,
    marginBottom: 16,
  },
  timelineItem: {
    flexDirection: 'row',
    marginBottom: 16,
  },
  timeLabel: {
    width: 75,
    color: Colors.textSecondary,
    fontSize: 13,
    fontWeight: '600',
    paddingTop: 8,
  },
  eventCard: {
    flex: 1,
    backgroundColor: Colors.cardBackground,
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: Colors.border,
  },
  conflictBorder: {
    borderColor: Colors.warning,
    borderLeftWidth: 4,
  },
  eventTitle: {
    color: Colors.textPrimary,
    fontSize: 14,
    fontWeight: '600',
  },
  eventSub: {
    color: Colors.textSecondary,
    fontSize: 12,
    marginTop: 2,
  },
});
