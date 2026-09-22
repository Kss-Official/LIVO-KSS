import React, { useState } from 'react';
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

interface AddEventScreenProps {
  onBack?: () => void;
  onSubmit?: (data: any) => void;
}

export const AddEventScreen: React.FC<AddEventScreenProps> = ({ onBack, onSubmit }) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [date, setDate] = useState('Mon, 2 Sep 2024');
  const [time, setTime] = useState('2:00 PM');
  const [duration, setDuration] = useState('30 Min');
  const [repeat, setRepeat] = useState('Does not repeat');
  const [location, setLocation] = useState('');
  const [priority, setPriority] = useState<'high' | 'medium' | 'low'>('high');
  const [meetingType, setMeetingType] = useState<'in_person' | 'online' | 'phone'>('online');
  const [category, setCategory] = useState('Work');
  const [reminder, setReminder] = useState('30 Min before');

  const handleSave = () => {
    const data = {
      title,
      description,
      date,
      time,
      duration,
      repeat,
      location,
      priority,
      meetingType,
      category,
      reminder,
    };
    if (onSubmit) {
      onSubmit(data);
    } else if (onBack) {
      onBack();
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
            <TouchableOpacity style={styles.backButton} onPress={onBack} activeOpacity={0.7}>
              <Feather name="arrow-left" size={22} color="#0F172A" />
            </TouchableOpacity>

            <View style={styles.headerTitleWrap}>
              <Text style={styles.headerTitle}>Add Event</Text>
              <Text style={styles.headerSubtitle}>Plan it. Make space for what matters.</Text>
            </View>

            <View style={styles.avatarCircle}>
              <Text style={styles.avatarText}>R</Text>
            </View>
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
            <TouchableOpacity style={styles.useAiBtn} activeOpacity={0.8}>
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
            <Text style={styles.label}>Description</Text>
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

            {/* Date & Time */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <Text style={styles.label}>Date</Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Feather name="calendar" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{date}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>

              <View style={styles.col}>
                <Text style={styles.label}>Time</Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Feather name="clock" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{time}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>
            </View>

            {/* Duration & Repeat */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <Text style={styles.label}>Duration</Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Feather name="clock" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{duration}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>

              <View style={styles.col}>
                <Text style={styles.label}>Repeat</Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Feather name="repeat" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{repeat}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>
            </View>

            {/* Location */}
            <Text style={styles.label}>Location</Text>
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

            {/* Priority */}
            <Text style={styles.label}>Priority</Text>
            <View style={styles.priorityRow}>
              <TouchableOpacity
                style={[styles.priorityPill, priority === 'high' && styles.priorityHighActive]}
                onPress={() => setPriority('high')}
                activeOpacity={0.8}
              >
                <Feather name="flag" size={14} color={priority === 'high' ? '#DC2626' : '#64748B'} style={{ marginRight: 6 }} />
                <Text style={[styles.priorityText, priority === 'high' && styles.priorityHighText]}>High</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.priorityPill, priority === 'medium' && styles.priorityMediumActive]}
                onPress={() => setPriority('medium')}
                activeOpacity={0.8}
              >
                <Feather name="flag" size={14} color={priority === 'medium' ? '#D97706' : '#64748B'} style={{ marginRight: 6 }} />
                <Text style={[styles.priorityText, priority === 'medium' && styles.priorityMediumText]}>Medium</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.priorityPill, priority === 'low' && styles.priorityLowActive]}
                onPress={() => setPriority('low')}
                activeOpacity={0.8}
              >
                <Feather name="flag" size={14} color={priority === 'low' ? '#475569' : '#64748B'} style={{ marginRight: 6 }} />
                <Text style={[styles.priorityText, priority === 'low' && styles.priorityLowText]}>Low</Text>
              </TouchableOpacity>
            </View>

            {/* Meeting Type / Format */}
            <Text style={styles.label}>Meeting Type / Format</Text>
            <View style={styles.formatRow}>
              <TouchableOpacity
                style={[styles.formatPill, meetingType === 'in_person' && styles.formatActive]}
                onPress={() => setMeetingType('in_person')}
                activeOpacity={0.8}
              >
                <Feather name="user" size={15} color={meetingType === 'in_person' ? '#16A34A' : '#475569'} style={{ marginRight: 6 }} />
                <Text style={[styles.formatText, meetingType === 'in_person' && styles.formatActiveText]}>In person</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.formatPill, meetingType === 'online' && styles.formatActive]}
                onPress={() => setMeetingType('online')}
                activeOpacity={0.8}
              >
                <Feather name="video" size={15} color={meetingType === 'online' ? '#16A34A' : '#475569'} style={{ marginRight: 6 }} />
                <Text style={[styles.formatText, meetingType === 'online' && styles.formatActiveText]}>Online</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.formatPill, meetingType === 'phone' && styles.formatActive]}
                onPress={() => setMeetingType('phone')}
                activeOpacity={0.8}
              >
                <Feather name="phone" size={15} color={meetingType === 'phone' ? '#16A34A' : '#475569'} style={{ marginRight: 6 }} />
                <Text style={[styles.formatText, meetingType === 'phone' && styles.formatActiveText]}>Phone</Text>
              </TouchableOpacity>
            </View>

            {/* Category & Reminder */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <Text style={styles.label}>Category</Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Feather name="briefcase" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{category}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>

              <View style={styles.col}>
                <Text style={styles.label}>Reminder</Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Feather name="bell" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{reminder}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>
            </View>

            {/* Add Notes / Attachments */}
            <Text style={styles.label}>Add Notes / Attachments <Text style={styles.optionalText}>(optional)</Text></Text>
            <TouchableOpacity style={styles.attachmentButton} activeOpacity={0.7}>
              <View style={styles.attachmentLeft}>
                <Feather name="paperclip" size={16} color="#64748B" style={{ marginRight: 10 }} />
                <Text style={styles.attachmentText}>Add file, image or link</Text>
              </View>
              <Feather name="chevron-right" size={18} color="#94A3B8" />
            </TouchableOpacity>
          </View>

          {/* Action Buttons */}
          <View style={styles.actionRow}>
            <TouchableOpacity style={styles.cancelBtn} onPress={onBack} activeOpacity={0.8}>
              <Text style={styles.cancelBtnText}>Cancel</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.createBtn} onPress={handleSave} activeOpacity={0.88}>
              <Text style={styles.createBtnText}>Create Event</Text>
            </TouchableOpacity>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  flex: { flex: 1 },
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  scrollContent: {
    paddingHorizontal: 20,
    paddingTop: 40,
    paddingBottom: 40,
  },

  /* Header */
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 20,
  },
  backButton: {
    width: 38,
    height: 38,
    borderRadius: 19,
    backgroundColor: '#F8FAFC',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  headerTitleWrap: {
    flex: 1,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: '800',
    color: '#0F172A',
  },
  headerSubtitle: {
    fontSize: 13,
    color: '#64748B',
    marginTop: 2,
  },
  avatarCircle: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#DCFCE7',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#15803D',
  },

  /* AI Banner */
  aiBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#F4FBF4',
    borderWidth: 1,
    borderColor: '#DCFCE7',
    borderRadius: 16,
    padding: 14,
    marginBottom: 24,
  },
  aiBannerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    paddingRight: 10,
  },
  aiIconWrap: {
    width: 38,
    height: 38,
    borderRadius: 19,
    backgroundColor: '#DCFCE7',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  aiTextWrap: {
    flex: 1,
  },
  aiTitle: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
  },
  aiSubtitle: {
    fontSize: 11.5,
    color: '#64748B',
    marginTop: 2,
    lineHeight: 16,
  },
  useAiBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#CBD5E1',
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  useAiBtnText: {
    fontSize: 12.5,
    fontWeight: '700',
    color: '#0F172A',
  },

  /* Form controls */
  formSection: {
    marginBottom: 20,
  },
  label: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 8,
    marginTop: 14,
  },
  requiredStar: {
    color: '#DC2626',
  },
  optionalText: {
    fontWeight: '400',
    color: '#64748B',
  },
  textInput: {
    height: 50,
    backgroundColor: '#FFFFFF',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 14,
    paddingHorizontal: 16,
    fontSize: 14.5,
    color: '#0F172A',
  },
  textAreaWrap: {
    backgroundColor: '#FFFFFF',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 14,
    padding: 14,
  },
  textArea: {
    height: 90,
    fontSize: 14,
    color: '#0F172A',
  },
  charCount: {
    alignSelf: 'flex-end',
    fontSize: 11.5,
    color: '#94A3B8',
    marginTop: 4,
  },

  twoColRow: {
    flexDirection: 'row',
    gap: 12,
  },
  col: {
    flex: 1,
  },
  dropdownSelect: {
    height: 50,
    backgroundColor: '#FFFFFF',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 14,
    paddingHorizontal: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  fieldIcon: {
    marginRight: 8,
  },
  dropdownText: {
    flex: 1,
    fontSize: 13.5,
    fontWeight: '500',
    color: '#0F172A',
  },

  inputIconWrap: {
    height: 50,
    backgroundColor: '#FFFFFF',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 14,
    paddingHorizontal: 14,
    flexDirection: 'row',
    alignItems: 'center',
  },
  textInputWithIcon: {
    flex: 1,
    fontSize: 14.5,
    color: '#0F172A',
  },

  /* Priority */
  priorityRow: {
    flexDirection: 'row',
    gap: 10,
  },
  priorityPill: {
    flex: 1,
    height: 44,
    borderRadius: 12,
    backgroundColor: '#F8FAFC',
    borderWidth: 1.5,
    borderColor: '#F1F5F9',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  priorityHighActive: {
    backgroundColor: '#FEF2F2',
    borderColor: '#FECACA',
  },
  priorityMediumActive: {
    backgroundColor: '#FFFBEB',
    borderColor: '#FDE68A',
  },
  priorityLowActive: {
    backgroundColor: '#F8FAFC',
    borderColor: '#CBD5E1',
  },
  priorityText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#64748B',
  },
  priorityHighText: {
    color: '#DC2626',
    fontWeight: '700',
  },
  priorityMediumText: {
    color: '#D97706',
    fontWeight: '700',
  },
  priorityLowText: {
    color: '#475569',
    fontWeight: '700',
  },

  /* Format */
  formatRow: {
    flexDirection: 'row',
    gap: 10,
  },
  formatPill: {
    flex: 1,
    height: 44,
    borderRadius: 12,
    backgroundColor: '#F8FAFC',
    borderWidth: 1.5,
    borderColor: '#F1F5F9',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  formatActive: {
    backgroundColor: '#F0FDF4',
    borderColor: '#86EFAC',
  },
  formatText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#475569',
  },
  formatActiveText: {
    color: '#16A34A',
    fontWeight: '700',
  },

  attachmentButton: {
    height: 50,
    backgroundColor: '#FFFFFF',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 14,
    paddingHorizontal: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  attachmentLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  attachmentText: {
    fontSize: 13.5,
    color: '#94A3B8',
  },

  /* Actions */
  actionRow: {
    flexDirection: 'row',
    gap: 12,
    marginTop: 10,
  },
  cancelBtn: {
    flex: 1,
    height: 52,
    backgroundColor: '#F1F5F9',
    borderRadius: 26,
    alignItems: 'center',
    justifyContent: 'center',
  },
  cancelBtnText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
  },
  createBtn: {
    flex: 1,
    height: 52,
    backgroundColor: '#66C400',
    borderRadius: 26,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#66C400',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 4,
  },
  createBtnText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#FFFFFF',
  },
});
