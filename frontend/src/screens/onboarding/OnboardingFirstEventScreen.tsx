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
import { DatePickerField } from '../../components/forms/DatePickerField';
import { TimePickerField } from '../../components/forms/TimePickerField';
import { SelectionModal, SelectionOption } from '../../components/forms/SelectionModal';
import { eventService } from '../../services/eventService';

interface OnboardingFirstEventScreenProps {
  onNext: () => void;
  onBack: () => void;
}

export const OnboardingFirstEventScreen: React.FC<OnboardingFirstEventScreenProps> = ({
  onNext,
  onBack,
}) => {
  const [title, setTitle] = useState('');
  const [eventDate, setEventDate] = useState<Date>(new Date());
  const [eventTime, setEventTime] = useState<Date>(new Date());
  const [duration, setDuration] = useState('30 minutes');
  const [repeatOption, setRepeatOption] = useState('Does not repeat');
  const [location, setLocation] = useState('');
  const [note, setNote] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('Work');

  const [showDurationModal, setShowDurationModal] = useState(false);
  const [showRepeatModal, setShowRepeatModal] = useState(false);

  const durationOptions: SelectionOption[] = [
    { label: '15 minutes', value: '15 minutes' },
    { label: '30 minutes', value: '30 minutes' },
    { label: '45 minutes', value: '45 minutes' },
    { label: '1 hour', value: '1 hour' },
    { label: '2 hours', value: '2 hours' },
  ];

  const repeatOptions: SelectionOption[] = [
    { label: 'Does not repeat', value: 'Does not repeat' },
    { label: 'Daily', value: 'Daily' },
    { label: 'Weekly', value: 'Weekly' },
    { label: 'Monthly', value: 'Monthly' },
  ];

  const categories = [
    { label: 'Work', icon: 'briefcase-outline' },
    { label: 'Personal', icon: 'home-outline' },
    { label: 'Study', icon: 'school-outline' },
    { label: 'Health', icon: 'heart-outline' },
    { label: 'Social', icon: 'people-outline' },
    { label: 'Travel', icon: 'airplane-outline' },
    { label: 'More', icon: 'add-outline' },
  ];

  const handleSaveAndNext = async () => {
    if (title.trim()) {
      try {
        await eventService.saveEvent({
          title: title.trim(),
          description: note.trim(),
          location: location.trim(),
          date: eventDate.toISOString().split('T')[0],
          category: selectedCategory,
        });
      } catch (err) {
        console.error('Failed to save onboarding event:', err);
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
          iconName="calendar-outline"
          title="Plan your first event"
          subtitle="Add an event to your schedule. Keep your day organized and stress-free."
        />

        <View style={styles.formContainer}>
          {/* Event Title */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>Event title</Text>
            <View style={styles.inputCard}>
              <TextInput
                style={styles.textInput}
                placeholder="e.g. Design team meeting"
                placeholderTextColor="#94A3B8"
                value={title}
                onChangeText={setTitle}
              />
            </View>
          </View>

          {/* Date & Time */}
          <View style={styles.twoColRow}>
            <View style={styles.col}>
              <DatePickerField
                label="Date"
                value={eventDate}
                onChange={setEventDate}
              />
            </View>
            <View style={styles.col}>
              <TimePickerField
                label="Time"
                value={eventTime}
                onChange={setEventTime}
              />
            </View>
          </View>

          {/* Duration & Repeat */}
          <View style={styles.twoColRow}>
            <View style={styles.col}>
              <Text style={styles.fieldLabel}>Duration</Text>
              <TouchableOpacity
                style={styles.selectCard}
                onPress={() => setShowDurationModal(true)}
                activeOpacity={0.7}
              >
                <Ionicons name="time-outline" size={18} color="#84CC16" style={{ marginRight: 6 }} />
                <Text style={styles.selectValue} numberOfLines={1}>{duration}</Text>
                <Ionicons name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>
            <View style={styles.col}>
              <Text style={styles.fieldLabel}>Repeat (optional)</Text>
              <TouchableOpacity
                style={styles.selectCard}
                onPress={() => setShowRepeatModal(true)}
                activeOpacity={0.7}
              >
                <Ionicons name="repeat-outline" size={18} color="#84CC16" style={{ marginRight: 6 }} />
                <Text style={styles.selectValue} numberOfLines={1}>{repeatOption}</Text>
                <Ionicons name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>
          </View>

          {/* Location */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>Location (optional)</Text>
            <View style={[styles.inputCard, styles.iconInputCard]}>
              <Ionicons name="location-outline" size={20} color="#84CC16" style={{ marginRight: 10 }} />
              <TextInput
                style={[styles.textInput, { flex: 1 }]}
                placeholder="e.g. Google Meet, Office, Home"
                placeholderTextColor="#94A3B8"
                value={location}
                onChangeText={setLocation}
              />
            </View>
          </View>

          {/* Add a short note */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>Add a short note (optional)</Text>
            <View style={[styles.inputCard, styles.multilineCard]}>
              <TextInput
                style={[styles.textInput, styles.multilineInput]}
                placeholder="Add any additional details..."
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

          {/* Choose a category */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>Choose a category</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.categoriesScroll}>
              {categories.map((cat) => {
                const isSelected = selectedCategory === cat.label;
                return (
                  <TouchableOpacity
                    key={cat.label}
                    style={styles.catItem}
                    onPress={() => setSelectedCategory(cat.label)}
                    activeOpacity={0.7}
                  >
                    <View style={[styles.catIconBadge, isSelected && styles.catIconBadgeSelected]}>
                      <Ionicons
                        name={cat.icon as any}
                        size={22}
                        color={isSelected ? '#1E293B' : '#475569'}
                      />
                    </View>
                    <Text style={[styles.catText, isSelected && styles.catTextSelected]}>
                      {cat.label}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </ScrollView>
          </View>

          {/* Action Buttons */}
          <TouchableOpacity
            style={styles.primaryBtn}
            onPress={handleSaveAndNext}
            activeOpacity={0.85}
          >
            <Text style={styles.primaryBtnText}>Add Event</Text>
            <Ionicons name="arrow-forward" size={20} color="#0F172A" style={{ marginLeft: 6 }} />
          </TouchableOpacity>

          <TouchableOpacity style={styles.skipBtn} onPress={onNext} activeOpacity={0.7}>
            <Text style={styles.skipBtnText}>Skip for now</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>

      <SelectionModal
        visible={showDurationModal}
        title="Select Duration"
        options={durationOptions}
        selectedValue={duration}
        onSelect={setDuration}
        onClose={() => setShowDurationModal(false)}
      />

      <SelectionModal
        visible={showRepeatModal}
        title="Select Repeat"
        options={repeatOptions}
        selectedValue={repeatOption}
        onSelect={setRepeatOption}
        onClose={() => setShowRepeatModal(false)}
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
  iconInputCard: {
    flexDirection: 'row',
    alignItems: 'center',
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
  twoColRow: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 18,
  },
  col: {
    flex: 1,
  },
  selectCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 12,
    paddingVertical: 12,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  selectValue: {
    fontSize: 13,
    color: '#0F172A',
    fontWeight: '500',
    flex: 1,
  },
  categoriesScroll: {
    flexDirection: 'row',
  },
  catItem: {
    alignItems: 'center',
    marginRight: 16,
  },
  catIconBadge: {
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
  catIconBadgeSelected: {
    backgroundColor: '#E4F5CB',
    borderColor: '#84CC16',
    borderWidth: 1.5,
  },
  catText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#64748B',
  },
  catTextSelected: {
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
