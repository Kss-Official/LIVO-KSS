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
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';

import { useEvents } from '../hooks/useEvents';

interface AddTripScreenProps {
  onBack?: () => void;
  onSubmit?: (data: any) => void;
}

export const AddTripScreen: React.FC<AddTripScreenProps> = ({ onBack, onSubmit }) => {
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
  const [tripTitle, setTripTitle] = useState('');
  const [description, setDescription] = useState('');
  const [destination, setDestination] = useState('');
  const [tripType, setTripType] = useState('Leisure');
  const [selectedStartDate, setSelectedStartDate] = useState<Date>(new Date());
  const [selectedEndDate, setSelectedEndDate] = useState<Date>(new Date(Date.now() + 86400000 * 3));
  const [duration, setDuration] = useState('3 Days');
  const [travelWith, setTravelWith] = useState<'solo' | 'partner' | 'family' | 'friends' | 'work'>('solo');
  const [budget, setBudget] = useState('');
  const [currency, setCurrency] = useState('INR (₹)');
  const [travelMode, setTravelMode] = useState<'flight' | 'train' | 'bus' | 'car' | 'other'>('flight');
  const [accommodation, setAccommodation] = useState('');
  const [location, setLocation] = useState('');
  const [meetingType, setMeetingType] = useState<'in_person' | 'online' | 'phone'>('in_person');
  const [selectedCategory, setSelectedCategory] = useState('Travel');
  const [selectedGoal, setSelectedGoal] = useState('Build a strong portfolio');
  const [reminder, setReminder] = useState('1 Day before');
  const [repeat, setRepeat] = useState('Does not repeat');
  const [notes, setNotes] = useState('');
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
    { label: '1 Day', value: '1 Day' },
    { label: '2 Days', value: '2 Days' },
    { label: '3 Days', value: '3 Days' },
    { label: '5 Days', value: '5 Days' },
    { label: '1 Week', value: '1 Week' },
    { label: '2 Weeks', value: '2 Weeks' },
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
    if (!tripTitle.trim()) return;
    const data = {
      tripTitle: tripTitle.trim(),
      description: description.trim(),
      destination: destination.trim(),
      tripType,
      startDate: selectedStartDate,
      endDate: selectedEndDate,
      duration,
      location: location.trim() || destination.trim(),
      meetingType,
      travelWith,
      budget,
      currency,
      travelMode,
      accommodation,
      linkedGoal: selectedGoal,
      notes,
      category: selectedCategory,
      reminder,
      repeat,
      attachments,
    };

    await addEvent({
      title: tripTitle.trim(),
      description: description.trim() || `Destination: ${destination}`,
      date: selectedStartDate.toISOString(),
      startTime: selectedStartDate.toISOString(),
      category: 'Trip',
    });

    if (onSubmit) {
      onSubmit(data);
    }
    
    setCreatedItem({ 
      title: tripTitle.trim(), 
      date: selectedStartDate, 
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
              <Text style={styles.headerTitle}>Add Trip</Text>
              <Text style={styles.headerSubtitle}>Plan your journey. Make it memorable.</Text>
            </View>

            <TouchableOpacity style={styles.avatarCircle} onPress={() => navigation.navigate('Profile')}>
              <Text style={styles.avatarText}>R</Text>
            </TouchableOpacity>
          </View>

          {/* AI Banner */}
          <View style={styles.aiBanner}>
            <View style={styles.aiBannerLeft}>
              <View style={styles.aiIconWrap}>
                <Ionicons name="sparkles" size={18} color="#2563EB" />
              </View>
              <View style={styles.aiTextWrap}>
                <Text style={styles.aiTitle}>Need an itinerary?</Text>
                <Text style={styles.aiSubtitle}>
                  Tell LIVO where you're going and I'll build a daily plan.
                </Text>
              </View>
            </View>
            <TouchableOpacity style={styles.useAiBtn} activeOpacity={0.8} onPress={() => navigation.navigate('ChatWithLivo')}>
              <Text style={styles.useAiBtnText}>Use AI</Text>
            </TouchableOpacity>
          </View>

          {/* Trip Title */}
          <Text style={styles.label}>Trip Name <Text style={styles.requiredStar}>*</Text></Text>
          <View style={styles.inputBox}>
            <TextInput
              style={styles.textInput}
              placeholder="e.g. Summer Vacation in Bali"
              placeholderTextColor="#CBD5E1"
              value={tripTitle}
              onChangeText={setTripTitle}
            />
          </View>

          {/* Destination */}
          <Text style={styles.label}>Destination <Text style={styles.requiredStar}>*</Text></Text>
          <View style={styles.inputBoxWithIcon}>
            <Feather name="map-pin" size={16} color="#64748B" style={{ marginRight: 8 }} />
            <TextInput
              style={[styles.textInput, { flex: 1, borderWidth: 0, paddingHorizontal: 0, marginBottom: 0 }]}
              placeholder="e.g. Paris, Tokyo, Bali"
              placeholderTextColor="#CBD5E1"
              value={destination}
              onChangeText={(t) => {
                setDestination(t);
                if (!location) setLocation(t);
              }}
            />
          </View>

          {/* Description */}
          <Text style={styles.label}>Description / Notes <Text style={styles.optionalText}>(optional)</Text></Text>
          <View style={styles.textAreaBox}>
            <TextInput
              style={styles.textArea}
              placeholder="Add details, itinerary ideas, or notes..."
              placeholderTextColor="#CBD5E1"
              value={description}
              onChangeText={setDescription}
              multiline
              numberOfLines={4}
              textAlignVertical="top"
            />
          </View>

          {/* Start Date & End Date Row 1 */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <DatePickerField label="Start Date" value={selectedStartDate} onChange={setSelectedStartDate} />
            </View>
            <View style={styles.colHalf}>
              <DatePickerField label="End Date" value={selectedEndDate} onChange={setSelectedEndDate} />
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

          {/* Format / Context Type */}
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
              />
            </View>
            <View style={styles.colHalf}>
              <GoalPickerField
                value={selectedGoal}
                onChange={setSelectedGoal}
                label="Link to Goal"
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
              style={[styles.createBtn, !tripTitle.trim() && styles.createBtnDisabled]}
              onPress={handleSave}
              disabled={!tripTitle.trim()}
              activeOpacity={0.88}
            >
              <Text style={styles.createBtnText}>Save Trip</Text>
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
              { label: 'Yearly', value: 'Yearly' },
            ]}
            selectedValue={repeat}
            onSelect={setRepeat}
          />
        </ScrollView>
        <CreationSuccessModal
          visible={showSuccessModal}
          itemType="Trip"
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
  aiBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#EFF6FF',
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#DBEAFE',
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
    backgroundColor: '#DBEAFE',
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
    color: '#1D4ED8',
  },
  aiSubtitle: {
    fontSize: 11,
    color: '#1E40AF',
    lineHeight: 15,
  },
  useAiBtn: {
    backgroundColor: '#2563EB',
    borderRadius: 14,
    paddingHorizontal: 14,
    paddingVertical: 8,
  },
  useAiBtnText: {
    fontSize: 12,
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


