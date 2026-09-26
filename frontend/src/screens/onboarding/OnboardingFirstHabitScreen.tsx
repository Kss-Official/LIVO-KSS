import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  SafeAreaView,
  ScrollView,
  TextInput,
  StatusBar,
  Switch,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { OnboardingHeader } from '../../components/onboarding/OnboardingHeader';
import { TimePickerField } from '../../components/forms/TimePickerField';
import { habitService } from '../../services/habitService';
import AsyncStorage from '@react-native-async-storage/async-storage';

interface OnboardingFirstHabitScreenProps {
  onFinish: () => void;
  onBack: () => void;
}

export const OnboardingFirstHabitScreen: React.FC<OnboardingFirstHabitScreenProps> = ({
  onFinish,
  onBack,
}) => {
  const [title, setTitle] = useState('');
  const [frequency, setFrequency] = useState('Daily');
  const [reminderTime, setReminderTime] = useState<Date>(new Date());
  const [getReminder, setGetReminder] = useState(true);
  const [note, setNote] = useState('');
  const [selectedIcon, setSelectedIcon] = useState('Water');

  const frequencies = ['Daily', 'Weekly', 'Weekdays', 'Custom'];

  const habitIcons = [
    { label: 'Water', icon: 'water-outline' },
    { label: 'Exercise', icon: 'barbell-outline' },
    { label: 'Read', icon: 'book-outline' },
    { label: 'Meditate', icon: 'sunny-outline' },
    { label: 'Sleep', icon: 'moon-outline' },
    { label: 'Eat well', icon: 'nutrition-outline' },
    { label: 'More', icon: 'add-outline' },
  ];

  const handleCompleteOnboarding = async () => {
    if (title.trim()) {
      try {
        await habitService.saveHabit({
          title: title.trim(),
          frequency: frequency.toUpperCase(),
          streakCount: 0,
          completedToday: false,
        });
      } catch (err) {
        console.error('Failed to save onboarding habit:', err);
      }
    }
    onFinish();
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#EFF8E2" />
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
        keyboardShouldPersistTaps="handled"
      >
        <OnboardingHeader
          onBack={onBack}
          iconName="heart-outline"
          title="Set your first habit"
          subtitle="Small habits create big changes. Choose a habit you want to build."
        />

        <View style={styles.formContainer}>
          {/* Habit Title */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>Habit title</Text>
            <View style={styles.inputCard}>
              <TextInput
                style={styles.textInput}
                placeholder="e.g. Drink 2 liters of water"
                placeholderTextColor="#94A3B8"
                value={title}
                onChangeText={setTitle}
              />
            </View>
          </View>

          {/* How often? */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>How often?</Text>
            <View style={styles.freqRow}>
              {frequencies.map((freq) => {
                const isSelected = frequency === freq;
                return (
                  <TouchableOpacity
                    key={freq}
                    style={[styles.freqPill, isSelected && styles.freqPillSelected]}
                    onPress={() => setFrequency(freq)}
                    activeOpacity={0.7}
                  >
                    <Text style={[styles.freqText, isSelected && styles.freqTextSelected]}>
                      {freq}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </View>
          </View>

          {/* Reminder Time + Switch */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>Reminder time</Text>
            <View style={styles.reminderRow}>
              <View style={{ flex: 1, marginRight: 10 }}>
                <TimePickerField
                  value={reminderTime}
                  onChange={setReminderTime}
                />
              </View>
              <View style={styles.switchBox}>
                <Switch
                  value={getReminder}
                  onValueChange={setGetReminder}
                  trackColor={{ false: '#CBD5E1', true: '#84CC16' }}
                  thumbColor="#FFFFFF"
                />
                <View style={{ marginLeft: 8 }}>
                  <Text style={styles.switchTitle}>Get a reminder</Text>
                  <Text style={styles.switchSub}>We'll remind you at this time.</Text>
                </View>
              </View>
            </View>
          </View>

          {/* Add a short note */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>Add a short note (optional)</Text>
            <View style={[styles.inputCard, styles.multilineCard]}>
              <TextInput
                style={[styles.textInput, styles.multilineInput]}
                placeholder="Why do you want to build this habit?"
                placeholderTextColor="#94A3B8"
                value={note}
                onChangeText={setNote}
                multiline
                numberOfLines={3}
                maxLength={200}
              />
            </View>
            <Text style={styles.charCount}>{note.length}/200</Text>
          </View>

          {/* Choose an icon */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>Choose an icon</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.iconsScroll}>
              {habitIcons.map((item) => {
                const isSelected = selectedIcon === item.label;
                return (
                  <TouchableOpacity
                    key={item.label}
                    style={styles.iconItem}
                    onPress={() => setSelectedIcon(item.label)}
                    activeOpacity={0.7}
                  >
                    <View style={[styles.iconBadge, isSelected && styles.iconBadgeSelected]}>
                      <Ionicons
                        name={item.icon as any}
                        size={22}
                        color={isSelected ? '#1E293B' : '#475569'}
                      />
                    </View>
                    <Text style={[styles.iconLabel, isSelected && styles.iconLabelSelected]}>
                      {item.label}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </ScrollView>
          </View>

          {/* Action Buttons */}
          <TouchableOpacity
            style={styles.primaryBtn}
            onPress={handleCompleteOnboarding}
            activeOpacity={0.85}
          >
            <Text style={styles.primaryBtnText}>Create Habit</Text>
            <Ionicons name="arrow-forward" size={20} color="#0F172A" style={{ marginLeft: 6 }} />
          </TouchableOpacity>

          <TouchableOpacity style={styles.skipBtn} onPress={handleCompleteOnboarding} activeOpacity={0.7}>
            <Text style={styles.skipBtnText}>Skip for now</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>

    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#EFF8E2',
  },

  scrollContent: {
    paddingBottom: 40,
  },
  formContainer: {
    paddingHorizontal: 20,
    zIndex: 2,
  },
  fieldGroup: {
    marginBottom: 18,
  },
  fieldLabel: {
    fontSize: 14,
    fontWeight: '700',
    color: '#1E293B',
    marginBottom: 8,
  },
  inputCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingVertical: 12,
  },
  multilineCard: {
    height: 100,
  },
  textInput: {
    fontSize: 15,
    color: '#0F172A',
    padding: 0,
  },
  multilineInput: {
    textAlignVertical: 'top',
  },
  charCount: {
    fontSize: 12,
    color: '#94A3B8',
    textAlign: 'right',
    marginTop: 4,
  },
  freqRow: {
    flexDirection: 'row',
    gap: 8,
  },
  freqPill: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingVertical: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  freqPillSelected: {
    backgroundColor: '#E4F5CB',
    borderColor: '#84CC16',
    borderWidth: 1.5,
  },
  freqText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#475569',
  },
  freqTextSelected: {
    color: '#1E293B',
    fontWeight: '700',
  },
  reminderRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  switchBox: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    marginLeft: 6,
  },
  switchTitle: {
    fontSize: 12,
    fontWeight: '700',
    color: '#1E293B',
  },
  switchSub: {
    fontSize: 10,
    color: '#64748B',
  },
  iconsScroll: {
    flexDirection: 'row',
  },
  iconItem: {
    alignItems: 'center',
    marginRight: 16,
  },
  iconBadge: {
    width: 48,
    height: 48,
    borderRadius: 16,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 6,
  },
  iconBadgeSelected: {
    backgroundColor: '#E4F5CB',
    borderColor: '#84CC16',
    borderWidth: 1.5,
  },
  iconLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: '#64748B',
  },
  iconLabelSelected: {
    color: '#1E293B',
    fontWeight: '700',

  },

  primaryBtn: {
    backgroundColor: '#80D611',
    borderRadius: 30,
    height: 56,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 12,
    shadowColor: '#80D611',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 4,
  },
  primaryBtnText: {
    fontSize: 17,
    fontWeight: '800',
    color: '#0F172A',
  },
  skipBtn: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 16,
  },
  skipBtnText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#497D11',
  },

});
