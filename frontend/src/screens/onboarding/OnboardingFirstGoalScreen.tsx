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
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { OnboardingHeader } from '../../components/onboarding/OnboardingHeader';
import { SelectionModal, SelectionOption } from '../../components/forms/SelectionModal';
import { goalService } from '../../services/goalService';

interface OnboardingFirstGoalScreenProps {
  onNext: () => void;
  onBack: () => void;
}

export const OnboardingFirstGoalScreen: React.FC<OnboardingFirstGoalScreenProps> = ({
  onNext,
  onBack,
}) => {
  const [title, setTitle] = useState('');
  const [timeline, setTimeline] = useState('This Month');
  const [note, setNote] = useState('');
  const [showTimelineModal, setShowTimelineModal] = useState(false);

  const timelineOptions: SelectionOption[] = [
    { label: 'This Week', value: 'This Week' },
    { label: 'This Month', value: 'This Month' },
    { label: '3 Months', value: '3 Months' },
    { label: '6 Months', value: '6 Months' },
    { label: '1 Year', value: '1 Year' },
  ];

  const inspirations = [
    'Get fit and healthy',
    'Learn a new skill',
    'Grow my career',
    'Be more productive',
    'Improve my finances',
    'Travel the world',
  ];

  const handleSaveAndNext = async () => {
    if (title.trim()) {
      try {
        await goalService.saveGoal({
          title: title.trim(),
          description: note.trim(),
          targetDate: new Date().toISOString().split('T')[0],
          status: 'ACTIVE',
          progressPercentage: 0,
        });
      } catch (err) {
        console.error('Failed to save onboarding goal:', err);
      }
    }
    onNext();
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
          iconName="disc-outline"
          title="What's your goal?"
          subtitle="Tell us what you want to achieve. We'll help you break it down into smaller steps."
        />

        <View style={styles.formContainer}>
          {/* Goal Title */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>Goal title</Text>
            <View style={styles.inputCard}>
              <TextInput
                style={styles.textInput}
                placeholder="e.g. Become a Full-Stack Developer"
                placeholderTextColor="#94A3B8"
                value={title}
                onChangeText={setTitle}
              />
            </View>
          </View>

          {/* Timeline Picker */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>When would you like to achieve this?</Text>
            <TouchableOpacity
              style={styles.selectCard}
              onPress={() => setShowTimelineModal(true)}
              activeOpacity={0.7}
            >
              <View style={styles.selectLeft}>
                <Ionicons name="calendar-outline" size={20} color="#84CC16" style={{ marginRight: 10 }} />
                <Text style={styles.selectValue}>{timeline}</Text>
              </View>
              <Ionicons name="chevron-down" size={20} color="#64748B" />
            </TouchableOpacity>
          </View>

          {/* Add a short note */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>Add a short note (optional)</Text>
            <View style={[styles.inputCard, styles.multilineCard]}>
              <TextInput
                style={[styles.textInput, styles.multilineInput]}
                placeholder="Why is this goal important to you?"
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

          {/* Need inspiration? */}
          <View style={styles.fieldGroup}>
            <View style={styles.inspirationHeader}>
              <Text style={styles.fieldLabel}>Need inspiration?</Text>
              <TouchableOpacity>
                <Text style={styles.viewAllText}>View all →</Text>
              </TouchableOpacity>
            </View>
            <View style={styles.inspirationsGrid}>
              {inspirations.map((item) => (
                <TouchableOpacity
                  key={item}
                  style={styles.inspirationPill}
                  onPress={() => setTitle(item)}
                  activeOpacity={0.7}
                >
                  <Text style={styles.inspirationText}>{item}</Text>
                </TouchableOpacity>
              ))}
            </View>
          </View>

          {/* Action Buttons */}
          <TouchableOpacity
            style={styles.primaryBtn}
            onPress={handleSaveAndNext}
            activeOpacity={0.85}
          >
            <Text style={styles.primaryBtnText}>Create Goal</Text>
            <Ionicons name="arrow-forward" size={20} color="#0F172A" style={{ marginLeft: 6 }} />
          </TouchableOpacity>

          <TouchableOpacity style={styles.skipBtn} onPress={onNext} activeOpacity={0.7}>
            <Text style={styles.skipBtnText}>Skip for now</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>

      {/* Timeline Selection Modal */}
      <SelectionModal
        visible={showTimelineModal}
        title="Select Timeline"
        options={timelineOptions}
        selectedValue={timeline}
        onSelect={setTimeline}
        onClose={() => setShowTimelineModal(false)}
      />

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
  selectCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingVertical: 14,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  selectLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  selectValue: {
    fontSize: 15,
    color: '#0F172A',
    fontWeight: '500',
  },
  inspirationHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  viewAllText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#497D11',
  },
  inspirationsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  inspirationPill: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingVertical: 9,
  },
  inspirationText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#334155',
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
