import React, { useState } from 'react';
import { DatePickerField, TimePickerField, PrioritySelector, SelectionModal, SubtaskManager, AttachmentPicker, SelectionOption, CategoryPickerField, GoalPickerField } from '../components/forms';
import { CreationSuccessModal } from '../components/ui/CreationSuccessModal';

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
} from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';

import { useEvents } from '../hooks/useEvents';

interface AddHealthScreenProps {
  onBack?: () => void;
  onSubmit?: (data: any) => void;
}

export const AddHealthScreen: React.FC<AddHealthScreenProps> = ({ onBack, onSubmit }) => {
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

  const { addEvent } = useEvents();
  const [healthTitle, setHealthTitle] = useState('');
  const [description, setDescription] = useState('');
  const [healthType, setHealthType] = useState<'workout' | 'nutrition' | 'medical' | 'sleep' | 'checkup' | 'mental'>('workout');
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [selectedTime, setSelectedTime] = useState<Date>(new Date());
  const [duration, setDuration] = useState('30 Min');
  const [location, setLocation] = useState('');
  const [meetingType, setMeetingType] = useState<'in_person' | 'online' | 'phone'>('in_person');
  const [intensity, setIntensity] = useState('Moderate');
  const [goalConnection, setGoalConnection] = useState('Stay Fit');
  const [selectedMetric, setSelectedMetric] = useState('Steps');
  const [notes, setNotes] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('Health');
  const [reminder, setReminder] = useState('No reminder');
  const [repeat, setRepeat] = useState('Does not repeat');
  const [attachments, setAttachments] = useState<any[]>([]);

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
    if (!healthTitle.trim()) return;
    const data = {
      healthTitle: healthTitle.trim(),
      description: description.trim(),
      healthType,
      date: selectedDate,
      time: selectedTime,
      duration,
      location: location.trim(),
      meetingType,
      intensity,
      goalConnection,
      selectedMetric,
      notes,
      attachments,
    };

    await addEvent({
      title: healthTitle.trim(),
      description: description.trim() || `Type: ${healthType}`,
      date: selectedDate.toISOString(),
      startTime: selectedTime.toISOString(),
      category: 'Health',
    });

    if (onSubmit) {
      onSubmit(data);
    }
    
    setCreatedItem({ 
      title: healthTitle.trim(), 
      date: selectedDate, 
      time: selectedTime, 
      category: selectedCategory 
    });
    setShowSuccessModal(true);
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
              <Text style={styles.headerTitle}>Add Health Activity</Text>
              <Text style={styles.headerSubtitle}>Log workouts, wellness, & health checkups.</Text>
            </View>

            <TouchableOpacity style={styles.avatarCircle} onPress={() => navigation.navigate('Profile')}>
              <Text style={styles.avatarText}>R</Text>
            </TouchableOpacity>
          </View>

          {/* Activity Title */}
          <Text style={styles.label}>Activity Title <Text style={styles.requiredStar}>*</Text></Text>
          <View style={styles.inputBox}>
            <TextInput
              style={styles.textInput}
              placeholder="e.g. Morning Workout / Health Checkup"
              placeholderTextColor="#CBD5E1"
              value={healthTitle}
              onChangeText={setHealthTitle}
            />
          </View>

          {/* Description */}
          <Text style={styles.label}>Notes <Text style={styles.optionalText}>(optional)</Text></Text>
          <View style={styles.textAreaBox}>
            <TextInput
              style={styles.textArea}
              placeholder="Add details, metrics, or notes..."
              placeholderTextColor="#CBD5E1"
              value={notes}
              onChangeText={setNotes}
              multiline
              numberOfLines={4}
              textAlignVertical="top"
            />
          </View>

          {/* Date & Time Row 1 */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <DatePickerField value={selectedDate} onChange={setSelectedDate} />
            </View>
            <View style={styles.colHalf}>
              <TimePickerField value={selectedTime} onChange={setSelectedTime} />
            </View>
          </View>

          {/* Duration & Repeat Row 2 */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <Text style={styles.label}>Duration</Text>
              <TouchableOpacity
                style={styles.dropdownBox}
                onPress={() => setShowDurationModal(true)}
                activeOpacity={0.7}
              >
                <Feather name="clock" size={15} color="#64748B" style={{ marginRight: 6 }} />
                <Text style={styles.dropdownText}>{duration}</Text>
                <Feather name="chevron-down" size={14} color="#94A3B8" style={{ marginLeft: 'auto' }} />
              </TouchableOpacity>
            </View>
            <View style={styles.colHalf}>
              <Text style={styles.label}>Repeat</Text>
              <TouchableOpacity
                style={styles.dropdownBox}
                onPress={() => setShowRepeatModal(true)}
                activeOpacity={0.7}
              >
                <Feather name="repeat" size={15} color="#64748B" style={{ marginRight: 6 }} />
                <Text style={styles.dropdownText} numberOfLines={1}>{repeat}</Text>
                <Feather name="chevron-down" size={14} color="#94A3B8" style={{ marginLeft: 'auto' }} />
              </TouchableOpacity>
            </View>
          </View>

          {/* Location */}
          <Text style={styles.label}>
            Location <Text style={styles.optionalText}>(optional)</Text>
          </Text>
          <View style={styles.inputBoxWithIcon}>
            <Feather name="map-pin" size={16} color="#64748B" style={{ marginRight: 8 }} />
            <TextInput
              style={[styles.textInput, { flex: 1, borderWidth: 0, paddingHorizontal: 0, marginBottom: 0 }]}
              placeholder="e.g. Gym, Clinic, Park"
              placeholderTextColor="#CBD5E1"
              value={location}
              onChangeText={setLocation}
            />
          </View>

          {/* Meeting Type / Format */}
          <Text style={styles.label}>Format / Location Type</Text>
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
                    color={isSelected ? '#66C400' : '#64748B'}
                    style={{ marginRight: 6 }}
                  />
                  <Text style={[styles.meetingTypeText, isSelected && styles.meetingTypeTextSelected]}>
                    {opt.label}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>

          {/* Category & Link to Goal */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <CategoryPickerField
                value={selectedCategory}
                onChange={setSelectedCategory}
                options={[
                  { label: 'Health', value: 'Health', icon: 'heart', iconColor: '#E11D48', iconBgColor: '#FFE4E6' },
                  { label: 'Workout', value: 'Workout', icon: 'activity', iconColor: '#16A34A', iconBgColor: '#DCFCE7' },
                  { label: 'Nutrition', value: 'Nutrition', icon: 'coffee', iconColor: '#EA580C', iconBgColor: '#FFEDD5' },
                ]}
              />
            </View>
            <View style={styles.colHalf}>
              <GoalPickerField
                value={goalConnection}
                onChange={setGoalConnection}
                label="Link to Goal"
                options={[
                  { label: 'Stay Fit', value: 'Stay Fit', icon: 'activity', iconColor: '#E11D48', iconBgColor: '#FFE4E6' },
                  { label: 'Build Muscle', value: 'Build Muscle', icon: 'award', iconColor: '#7C3AED', iconBgColor: '#F3E8FF' },
                  { label: 'None', value: 'None', icon: 'slash', iconColor: '#94A3B8', iconBgColor: '#F1F5F9' },
                ]}
              />
            </View>
          </View>

          {/* Attachments */}

          <AttachmentPicker attachments={attachments} onChange={setAttachments} />

          <View style={{ height: 40 }} />

          {/* Action Buttons */}
          <View style={styles.actionRow}>
            <TouchableOpacity style={styles.cancelBtn} onPress={onBack} activeOpacity={0.8}>
              <Text style={styles.cancelBtnText}>Cancel</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.createBtn, !healthTitle.trim() && styles.createBtnDisabled]}
              onPress={handleSave}
              disabled={!healthTitle.trim()}
              activeOpacity={0.88}
            >
              <Text style={styles.createBtnText}>Save Activity</Text>
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
            visible={showRepeatModal}
            onClose={() => setShowRepeatModal(false)}
            title="Repeat"
            options={[
              { label: 'Does not repeat', value: 'Does not repeat' },
              { label: 'Daily', value: 'Daily' },
              { label: 'Weekly', value: 'Weekly' },
            ]}
            selectedValue={repeat}
            onSelect={setRepeat}
          />
        </ScrollView>
        <CreationSuccessModal
          visible={showSuccessModal}
          itemType="Health"
          itemData={createdItem}
          onClose={() => {
            setShowSuccessModal(false);
          }}
          onViewTask={() => {
            setShowSuccessModal(false);
            navigation.navigate('MainTabs', { screen: 'Plan' });
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
  inputBox: {
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingVertical: 12,
    marginBottom: 16,
  },
  inputBoxWithIcon: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingVertical: 12,
    marginBottom: 16,
  },
  textInput: {
    fontSize: 14,
    color: '#0F172A',
    padding: 0,
  },
  textAreaBox: {
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingVertical: 12,
    marginBottom: 16,
    height: 90,
  },
  textArea: {
    flex: 1,
    fontSize: 14,
    color: '#0F172A',
    padding: 0,
  },
  twoColRow: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 16,
  },
  colHalf: {
    flex: 1,
  },
  dropdownBox: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 12,
    paddingVertical: 12,
  },
  dropdownText: {
    fontSize: 13,
    color: '#0F172A',
    fontWeight: '500',
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
    borderColor: '#66C400',
    backgroundColor: '#F7FEE7',
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


