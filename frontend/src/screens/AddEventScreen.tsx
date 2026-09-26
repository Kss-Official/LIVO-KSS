import React, { useState } from 'react';
import { DatePickerField, TimePickerField, PrioritySelector, SelectionModal, SubtaskManager, AttachmentPicker, SelectionOption, CategoryPickerField, GoalPickerField } from '../components/forms';
import { CreationSuccessModal } from '../components/ui/CreationSuccessModal';
import { useEvents } from '../hooks/useEvents';

import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  SafeAreaView,
  StatusBar,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
  Alert,
} from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';

interface AddEventScreenProps {
  onBack?: () => void;
  onSubmit?: (data: any) => void;
  existingEvent?: any;
}

export const AddEventScreen: React.FC<AddEventScreenProps> = ({ onBack, onSubmit, existingEvent }) => {
  const navigation = useNavigation<any>();

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  const { events, addEvent, updateEvent } = useEvents();
  const [title, setTitle] = useState(existingEvent?.title || '');
  const [description, setDescription] = useState(existingEvent?.description || '');
  const [selectedDate, setSelectedDate] = useState<Date>(existingEvent?.date ? new Date(existingEvent.date) : new Date());
  const [selectedTime, setSelectedTime] = useState<Date>(existingEvent?.startTime ? new Date(existingEvent.startTime) : new Date());
  const [duration, setDuration] = useState(existingEvent?.duration || '30 Min');
  const [repeat, setRepeat] = useState(existingEvent?.repeat || 'Does not repeat');
  const [location, setLocation] = useState(existingEvent?.location || '');
  const [priority, setPriority] = useState<'high' | 'medium' | 'low'>(existingEvent?.priority || 'high');
  const [meetingType, setMeetingType] = useState<'in_person' | 'online' | 'phone'>(existingEvent?.meetingType || 'online');
  const [selectedCategory, setSelectedCategory] = useState(existingEvent?.category || 'Work');
  const [selectedGoal, setSelectedGoal] = useState(existingEvent?.goal || 'Build a strong portfolio');
  const [reminder, setReminder] = useState(existingEvent?.reminder || '30 Min');
  const [attachments, setAttachments] = useState<any[]>(existingEvent?.attachments || []);

  // Modals
  const [showDurationModal, setShowDurationModal] = useState(false);
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const [showGoalModal, setShowGoalModal] = useState(false);
  const [showReminderModal, setShowReminderModal] = useState(false);
  const [showRepeatModal, setShowRepeatModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [createdItem, setCreatedItem] = useState<any>(null);

  const durationOptions: SelectionOption[] = [
    { label: '15 Min', value: '15 Min' },
    { label: '30 Min', value: '30 Min' },
    { label: '45 Min', value: '45 Min' },
    { label: '1 Hour', value: '1 Hour' },
    { label: '1.5 Hours', value: '1.5 Hours' },
    { label: '2 Hours', value: '2 Hours' },
    { label: 'Custom', value: 'Custom' },
  ];

  const meetingTypeOptions: Array<{
    label: string;
    value: 'in_person' | 'online' | 'phone';
    icon: string;
  }> = [
      { label: 'In person', value: 'in_person', icon: 'user' },
      { label: 'Online', value: 'online', icon: 'video' },
      { label: 'Phone', value: 'phone', icon: 'phone' },
    ];

  const handleSave = async () => {
    if (!title.trim()) {
      Alert.alert('Required Field', 'Please enter an event title.');
      return;
    }
    const data = {
      title: title.trim(),
      description: description.trim(),
      date: selectedDate,
      time: selectedTime,
      duration,
      repeat,
      location: location.trim(),
      priority,
      meetingType,
      category: selectedCategory,
      goal: selectedGoal,
      reminder,
      attachments,
    };

    const isSameDate = (d1?: Date | string | null, d2?: Date) => {
      if (!d1 || !d2) return false;
      const date1 = new Date(d1);
      if (isNaN(date1.getTime())) return false;
      return (
        date1.getFullYear() === d2.getFullYear() &&
        date1.getMonth() === d2.getMonth() &&
        date1.getDate() === d2.getDate()
      );
    };

    const conflictingEvent = events.find((e: any) => {
      if (existingEvent && e.id === existingEvent.id) return false;
      if (!isSameDate(e.date || e.startTime, selectedDate)) return false;
      if (!e.startTime) return false;
      const eTime = new Date(e.startTime);
      const diffMins = Math.abs(
        (eTime.getHours() * 60 + eTime.getMinutes()) -
        (selectedTime.getHours() * 60 + selectedTime.getMinutes())
      );
      return diffMins < 45; // Within 45 minutes overlap
    });

    if (conflictingEvent && !existingEvent) {
      const formatTimeStr = (d: Date) => {
        let h = d.getHours();
        const m = d.getMinutes();
        const ampm = h >= 12 ? 'PM' : 'AM';
        h = h % 12 || 12;
        return `${h}:${String(m).padStart(2, '0')} ${ampm}`;
      };

      const nextSuggestedDate = new Date(selectedTime.getTime() + 60 * 60 * 1000);
      const suggestedTimeStr = `${formatTimeStr(nextSuggestedDate)} – ${formatTimeStr(new Date(nextSuggestedDate.getTime() + 60 * 60 * 1000))}`;

      navigation.navigate('ScheduleConflict', {
        existingEvent: {
          id: conflictingEvent.id,
          title: conflictingEvent.title,
          time: conflictingEvent.startTime ? formatTimeStr(new Date(conflictingEvent.startTime)) : '11:00 AM',
          location: conflictingEvent.location || 'Meeting Room / Online',
          category: conflictingEvent.category || 'Work',
          description: conflictingEvent.description,
          date: selectedDate.toISOString(),
        },
        newEvent: {
          title: title.trim(),
          time: `${formatTimeStr(selectedTime)} – ${formatTimeStr(new Date(selectedTime.getTime() + 60 * 60 * 1000))}`,
          location: location.trim(),
          category: selectedCategory,
          description: description.trim(),
          date: selectedDate.toISOString(),
        },
        suggestedTime: suggestedTimeStr,
        suggestionReason: `${formatTimeStr(nextSuggestedDate)} is clear and fits your focus hours`,
      });
      return;
    }

    if (existingEvent) {
      await updateEvent({
        ...existingEvent,
        title: title.trim(),
        description: description.trim(),
        date: selectedDate.toISOString(),
        startTime: selectedTime.toISOString(),
        location: location.trim(),
        category: selectedCategory,
      });
      if (onSubmit) {
        onSubmit(data);
      }
      handleBack();
    } else {
      await addEvent({
        title: title.trim(),
        description: description.trim(),
        date: selectedDate.toISOString(),
        startTime: selectedTime.toISOString(),
        location: location.trim(),
        category: selectedCategory,
      });
      if (onSubmit) {
        onSubmit(data);
      }
      setCreatedItem({ 
        title: title.trim(), 
        date: selectedDate, 
        time: selectedTime, 
        priority: priority, 
        category: selectedCategory 
      });
      setShowSuccessModal(true);
    }
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        <ScrollView
          style={styles.flex}
          contentContainerStyle={styles.scrollContent}
          showsVerticalScrollIndicator={false}
          keyboardShouldPersistTaps="handled"
        >
          {/* Header Bar */}
          <View style={styles.headerRow}>
            <TouchableOpacity style={styles.backButton} onPress={handleBack} activeOpacity={0.7}>
              <Feather name="arrow-left" size={22} color="#0F172A" />
            </TouchableOpacity>

            <View style={styles.headerTitleWrap}>
              <Text style={styles.headerTitle}>{existingEvent ? 'Edit Event' : 'Add Event'}</Text>
              <Text style={styles.headerSubtitle}>Plan it. Make space for what matters.</Text>
            </View>

            <TouchableOpacity style={styles.avatarCircle} onPress={() => navigation.navigate('Profile')}>
              <Text style={styles.avatarText}>R</Text>
            </TouchableOpacity>
          </View>

          {/* AI Banner */}
          <View style={styles.aiBanner}>
            <View style={styles.aiBannerLeft}>
              <View style={styles.aiIconWrap}>
                <Ionicons name="sparkles" size={18} color="#15803D" />
              </View>
              <View style={styles.aiTextWrap}>
                <Text style={styles.aiTitle}>Need help planning this event?</Text>
                <Text style={styles.aiSubtitle}>
                  Tell LIVO what you're planning and I'll help you set it up.
                </Text>
              </View>
            </View>
            <TouchableOpacity style={styles.useAiBtn} activeOpacity={0.8} onPress={() => navigation.navigate('ChatWithLivo')}>
              <Ionicons name="sparkles-outline" size={14} color="#15803D" style={{ marginRight: 4 }} />
              <Text style={styles.useAiBtnText}>Use AI</Text>
            </TouchableOpacity>
          </View>

          {/* Form Controls */}
          <View style={styles.formSection}>
            {/* Event Title */}
            <Text style={styles.label}>Event Title <Text style={styles.requiredStar}>*</Text></Text>
            <TextInput
              style={styles.textInput}
              placeholder="e.g. Client meeting"
              placeholderTextColor="#94A3B8"
              value={title}
              onChangeText={setTitle}
            />

            {/* Description */}
            <Text style={styles.label}>Description <Text style={styles.optionalText}>(optional)</Text></Text>
            <View style={styles.textAreaWrap}>
              <TextInput
                style={styles.textArea}
                placeholder="Add more details (optional)..."
                placeholderTextColor="#94A3B8"
                multiline
                numberOfLines={4}
                maxLength={300}
                value={description}
                onChangeText={setDescription}
                textAlignVertical="top"
              />
              <Text style={styles.charCount}>{description.length}/300</Text>
            </View>

            {/* Date & Time Row 1 */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <DatePickerField value={selectedDate} onChange={setSelectedDate} />
              </View>

              <View style={styles.col}>
                <TimePickerField value={selectedTime} onChange={setSelectedTime} />
              </View>
            </View>

            {/* Duration & Repeat Row 2 */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <Text style={styles.label}>Duration</Text>
                <TouchableOpacity
                  style={styles.dropdownSelect}
                  onPress={() => setShowDurationModal(true)}
                  activeOpacity={0.7}
                >
                  <Feather name="clock" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{duration}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>

              <View style={styles.col}>
                <Text style={styles.label}>Repeat</Text>
                <TouchableOpacity
                  style={styles.dropdownSelect}
                  onPress={() => setShowRepeatModal(true)}
                  activeOpacity={0.7}
                >
                  <Feather name="repeat" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText} numberOfLines={1}>{repeat}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>
            </View>

            {/* Location */}
            <Text style={styles.label}>Location <Text style={styles.optionalText}>(optional)</Text></Text>
            <View style={styles.inputIconWrap}>
              <Feather name="map-pin" size={16} color="#64748B" style={styles.fieldIcon} />
              <TextInput
                style={styles.textInputWithIcon}
                placeholder="e.g. Google Meet, Office, Home"
                placeholderTextColor="#94A3B8"
                value={location}
                onChangeText={setLocation}
              />
            </View>

            {/* Meeting Type / Format */}
            <Text style={styles.label}>Meeting Type / Format</Text>
            <View style={styles.meetingTypeRow}>
              {meetingTypeOptions.map((opt) => {
                const isSelected = meetingType === opt.value;
                return (
                  <TouchableOpacity
                    key={opt.value}
                    style={[styles.meetingTypePill, isSelected && styles.meetingTypePillSelected]}
                    onPress={() => setMeetingType(opt.value)}
                    activeOpacity={0.7}
                  >
                    <Feather
                      name={opt.icon as any}
                      size={14}
                      color={isSelected ? '#16A34A' : '#64748B'}
                      style={{ marginRight: 6 }}
                    />
                    <Text style={[styles.meetingTypeText, isSelected && styles.meetingTypeTextSelected]}>
                      {opt.label}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </View>

            {/* Category & Link to Goal Row */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <CategoryPickerField
                  value={selectedCategory}
                  onChange={setSelectedCategory}
                />
              </View>

              <View style={styles.col}>
                <GoalPickerField
                  value={selectedGoal}
                  onChange={setSelectedGoal}
                  label="Link to Goal"
                />
              </View>
            </View>

            {/* Set Reminder */}
            <View style={{ marginBottom: 16 }}>
              <Text style={styles.label}>Set Reminder</Text>
              <TouchableOpacity
                style={styles.dropdownSelect}
                onPress={() => setShowReminderModal(true)}
                activeOpacity={0.7}
              >
                <Feather name="bell" size={16} color="#64748B" style={styles.fieldIcon} />
                <Text style={styles.dropdownText}>{reminder}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>

            {/* Attachments */}

            <AttachmentPicker attachments={attachments} onChange={setAttachments} />
          </View>

          {/* Action Buttons */}
          <View style={styles.actionRow}>
            <TouchableOpacity style={styles.cancelBtn} onPress={onBack} activeOpacity={0.8}>
              <Text style={styles.cancelBtnText}>Cancel</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.createBtn, !title.trim() && styles.createBtnDisabled]}
              onPress={handleSave}
              disabled={!title.trim()}
              activeOpacity={0.88}
            >
              <Text style={styles.createBtnText}>{existingEvent ? 'Update Event' : 'Create Event'}</Text>
            </TouchableOpacity>
          </View>

          <SelectionModal
            visible={showDurationModal}
            onClose={() => setShowDurationModal(false)}
            title="Select Duration"
            options={durationOptions}
            selectedValue={duration}
            onSelect={setDuration}
          />
          <SelectionModal
            visible={showReminderModal}
            onClose={() => setShowReminderModal(false)}
            title="Set Reminder"
            options={[
              { label: 'None', value: 'None' },
              { label: '15 Min', value: '15 Min' },
              { label: '30 Min', value: '30 Min' },
              { label: '1 Hour', value: '1 Hour' },
            ]}
            selectedValue={reminder}
            onSelect={setReminder}
          />
          <SelectionModal
            visible={showRepeatModal}
            onClose={() => setShowRepeatModal(false)}
            title="Repeat"
            options={[
              { label: 'Does not repeat', value: 'Does not repeat' },
              { label: 'Daily', value: 'Daily' },
              { label: 'Weekly', value: 'Weekly' },
              { label: 'Monthly', value: 'Monthly' },
            ]}
            selectedValue={repeat}
            onSelect={setRepeat}
          />
        </ScrollView>
        <CreationSuccessModal
          visible={showSuccessModal}
          itemType="Event"
          itemData={createdItem}
          onClose={() => {
            setShowSuccessModal(false);
          }}
          onViewTask={() => {
            setShowSuccessModal(false);
            navigation.navigate('Schedule');
          }}
        />
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  flex: {
    flex: 1,
  },
  scrollContent: {
    paddingHorizontal: 16,
    paddingTop: 35,
    paddingBottom: 32,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 18,
  },
  backButton: {
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
    fontSize: 20,
    fontWeight: '800',
    color: '#0F172A',
  },
  headerSubtitle: {
    fontSize: 12,
    color: '#64748B',
  },
  avatarCircle: {
    width: 34,
    height: 34,
    borderRadius: 17,
    backgroundColor: '#66C400',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  aiBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#F0FDF4',
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#DCFCE7',
    paddingHorizontal: 14,
    paddingVertical: 12,
    marginBottom: 18,
  },
  aiBannerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    marginRight: 10,
  },
  aiIconWrap: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#DCFCE7',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  aiTextWrap: {
    flex: 1,
  },
  aiTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: '#15803D',
  },
  aiSubtitle: {
    fontSize: 11,
    color: '#166534',
    lineHeight: 15,
  },
  useAiBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#DCFCE7',
    borderRadius: 14,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  useAiBtnText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#15803D',
  },
  formSection: {
    backgroundColor: '#FFFFFF',
  },
  label: {
    fontSize: 13,
    fontWeight: '700',
    color: '#1E293B',
    marginBottom: 6,
  },
  requiredStar: {
    color: '#EF4444',
  },
  optionalText: {
    fontSize: 11,
    color: '#94A3B8',
    fontWeight: '400',
  },
  textInput: {
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 14,
    color: '#0F172A',
    marginBottom: 16,
  },
  textAreaWrap: {
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingVertical: 12,
    height: 100,
    marginBottom: 16,
  },
  textArea: {
    flex: 1,
    fontSize: 14,
    color: '#0F172A',
  },
  charCount: {
    fontSize: 11,
    color: '#94A3B8',
    textAlign: 'right',
  },
  twoColRow: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 16,
  },
  col: {
    flex: 1,
  },
  dropdownSelect: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 12,
    paddingVertical: 12,
  },
  fieldIcon: {
    marginRight: 8,
  },
  dropdownText: {
    flex: 1,
    fontSize: 13,
    color: '#0F172A',
    fontWeight: '500',
  },
  inputIconWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 12,
    paddingVertical: 10,
    marginBottom: 16,
  },
  textInputWithIcon: {
    flex: 1,
    fontSize: 14,
    color: '#0F172A',
  },
  meetingTypeRow: {
    flexDirection: 'row',
    gap: 10,
    marginBottom: 16,
  },
  meetingTypePill: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingVertical: 10,
    paddingHorizontal: 6,
  },
  meetingTypePillSelected: {
    borderColor: '#16A34A',
    backgroundColor: '#F0FDF4',
    borderWidth: 1.5,
  },
  meetingTypeText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#475569',
  },
  meetingTypeTextSelected: {
    color: '#1E293B',
    fontWeight: '700',
  },
  actionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    marginTop: 10,
  },
  cancelBtn: {
    flex: 1,
    backgroundColor: '#F8FAFC',
    borderRadius: 16,
    paddingVertical: 14,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  cancelBtnText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#475569',
  },
  createBtn: {
    flex: 1.3,
    backgroundColor: '#66C400',
    borderRadius: 16,
    paddingVertical: 14,
    alignItems: 'center',
    justifyContent: 'center',
  },
  createBtnDisabled: {
    backgroundColor: '#CBD5E1',
  },
  createBtnText: {
    fontSize: 14,
    fontWeight: '800',
    color: '#FFFFFF',
  },
});



