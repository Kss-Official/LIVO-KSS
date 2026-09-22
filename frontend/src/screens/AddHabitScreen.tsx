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
  Switch,
} from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';

interface AddHabitScreenProps {
  onBack?: () => void;
  onSubmit?: (data: any) => void;
}

export const AddHabitScreen: React.FC<AddHabitScreenProps> = ({ onBack, onSubmit }) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [selectedColor, setSelectedColor] = useState('#66C400');
  const [frequency, setFrequency] = useState<'daily' | 'weekly' | 'custom' | 'specific'>('daily');
  const [target, setTarget] = useState('1 times per day');
  const [timeOfDay, setTimeOfDay] = useState('9:00 AM');
  const [startDate, setStartDate] = useState('Mon, 2 Sep 2024');
  const [endDate, setEndDate] = useState('No end date');
  const [sendReminder, setSendReminder] = useState(true);
  const [reminderTime, setReminderTime] = useState('9:00 AM');
  const [linkedGoal, setLinkedGoal] = useState('Select a goal');
  const [motivationNote, setMotivationNote] = useState('');

  const colorOptions = [
    '#66C400',
    '#3B82F6',
    '#8B5CF6',
    '#EC4899',
    '#F97316',
    '#EAB308',
    '#14B8A6',
    '#94A3B8',
  ];

  const handleSave = () => {
    const data = {
      title,
      description,
      color: selectedColor,
      frequency,
      target,
      timeOfDay,
      startDate,
      endDate,
      sendReminder,
      reminderTime,
      linkedGoal,
      motivationNote,
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
              <Text style={styles.headerTitle}>Add Habit</Text>
              <Text style={styles.headerSubtitle}>Build small habits for a better you.</Text>
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
                <Text style={styles.aiTitle}>Need help creating this habit?</Text>
                <Text style={styles.aiSubtitle}>
                  Tell LIVO your goal, and I'll suggest the best routine for you.
                </Text>
              </View>
            </View>
            <TouchableOpacity style={styles.useAiBtn} activeOpacity={0.8}>
              <Ionicons name="sparkles-outline" size={14} color="#15803D" style={{ marginRight: 4 }} />
              <Text style={styles.useAiBtnText}>Use AI</Text>
            </TouchableOpacity>
          </View>

          {/* Form Section */}
          <View style={styles.formSection}>
            {/* Habit Title */}
            <Text style={styles.label}>Habit Title <Text style={styles.requiredStar}>*</Text></Text>
            <TextInput
              style={styles.textInput}
              placeholder="e.g. Drink 2L of water"
              placeholderTextColor="#94A3B8"
              value={title}
              onChangeText={setTitle}
            />

            {/* Description */}
            <Text style={styles.label}>Description <Text style={styles.optionalText}>(optional)</Text></Text>
            <View style={styles.textAreaWrap}>
              <TextInput
                style={styles.textArea}
                placeholder="Why do you want to build this habit?"
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

            {/* Icon & Color */}
            <Text style={styles.label}>Icon & Color</Text>
            <View style={styles.iconColorRow}>
              <TouchableOpacity style={styles.changeIconBtn} activeOpacity={0.8}>
                <Ionicons name="water-outline" size={18} color="#0284C7" style={{ marginRight: 6 }} />
                <Text style={styles.changeIconText}>Change Icon</Text>
                <Feather name="chevron-right" size={14} color="#64748B" style={{ marginLeft: 4 }} />
              </TouchableOpacity>

              <View style={styles.colorPaletteRow}>
                {colorOptions.map((color, idx) => (
                  <TouchableOpacity
                    key={idx}
                    style={[
                      styles.colorDot,
                      { backgroundColor: color },
                      selectedColor === color && styles.colorDotSelected,
                    ]}
                    onPress={() => setSelectedColor(color)}
                    activeOpacity={0.8}
                  />
                ))}
              </View>
            </View>

            {/* Frequency */}
            <Text style={styles.label}>Frequency</Text>
            <View style={styles.freqRow}>
              <TouchableOpacity
                style={[styles.freqPill, frequency === 'daily' && styles.freqActive]}
                onPress={() => setFrequency('daily')}
                activeOpacity={0.8}
              >
                <Feather name="calendar" size={14} color={frequency === 'daily' ? '#16A34A' : '#475569'} style={{ marginRight: 6 }} />
                <Text style={[styles.freqText, frequency === 'daily' && styles.freqActiveText]}>Daily</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.freqPill, frequency === 'weekly' && styles.freqActive]}
                onPress={() => setFrequency('weekly')}
                activeOpacity={0.8}
              >
                <Feather name="calendar" size={14} color={frequency === 'weekly' ? '#16A34A' : '#475569'} style={{ marginRight: 6 }} />
                <Text style={[styles.freqText, frequency === 'weekly' && styles.freqActiveText]}>Weekly</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.freqPill, frequency === 'custom' && styles.freqActive]}
                onPress={() => setFrequency('custom')}
                activeOpacity={0.8}
              >
                <Feather name="repeat" size={14} color={frequency === 'custom' ? '#16A34A' : '#475569'} style={{ marginRight: 6 }} />
                <Text style={[styles.freqText, frequency === 'custom' && styles.freqActiveText]}>Custom</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.freqPill, frequency === 'specific' && styles.freqActive]}
                onPress={() => setFrequency('specific')}
                activeOpacity={0.8}
              >
                <Feather name="calendar" size={14} color={frequency === 'specific' ? '#16A34A' : '#475569'} style={{ marginRight: 6 }} />
                <Text style={[styles.freqText, frequency === 'specific' && styles.freqActiveText]}>Specific Days</Text>
              </TouchableOpacity>
            </View>

            {/* Target & Time of Day */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <Text style={styles.label}>Target</Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Ionicons name="disc-outline" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{target}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>

              <View style={styles.col}>
                <Text style={styles.label}>Time of Day <Text style={styles.optionalText}>(optional)</Text></Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Feather name="clock" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{timeOfDay}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>
            </View>

            {/* Start Date & End Date */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <Text style={styles.label}>Start Date</Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Feather name="calendar" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{startDate}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>

              <View style={styles.col}>
                <Text style={styles.label}>End Date <Text style={styles.optionalText}>(optional)</Text></Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Feather name="calendar" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{endDate}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>
            </View>

            {/* Reminder & Reminder Time */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <Text style={styles.label}>Reminder</Text>
                <View style={styles.switchBox}>
                  <Feather name="bell" size={16} color="#64748B" style={{ marginRight: 6 }} />
                  <Text style={styles.switchLabel}>Send a reminder</Text>
                  <Switch
                    value={sendReminder}
                    onValueChange={setSendReminder}
                    trackColor={{ false: '#CBD5E1', true: '#86EFAC' }}
                    thumbColor={sendReminder ? '#66C400' : '#F1F5F9'}
                  />
                </View>
              </View>

              <View style={styles.col}>
                <Text style={styles.label}>Reminder Time</Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Feather name="clock" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{reminderTime}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>
            </View>

            {/* Link to Goal */}
            <Text style={styles.label}>Link to Goal <Text style={styles.optionalText}>(optional)</Text></Text>
            <TouchableOpacity style={styles.fullWidthSelect} activeOpacity={0.7}>
              <Ionicons name="disc-outline" size={16} color="#64748B" style={styles.fieldIcon} />
              <Text style={styles.dropdownText}>{linkedGoal}</Text>
              <Feather name="chevron-down" size={16} color="#64748B" />
            </TouchableOpacity>

            {/* Motivation Note */}
            <Text style={styles.label}>Add a Motivation Note <Text style={styles.optionalText}>(optional)</Text></Text>
            <View style={styles.inputIconWrap}>
              <Feather name="paperclip" size={16} color="#64748B" style={styles.fieldIcon} />
              <TextInput
                style={styles.textInputWithIcon}
                placeholder="e.g. A healthier me, a happier life"
                placeholderTextColor="#94A3B8"
                value={motivationNote}
                onChangeText={setMotivationNote}
              />
            </View>
          </View>

          {/* Action Buttons */}
          <View style={styles.actionRow}>
            <TouchableOpacity style={styles.cancelBtn} onPress={onBack} activeOpacity={0.8}>
              <Text style={styles.cancelBtnText}>Cancel</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.createBtn} onPress={handleSave} activeOpacity={0.88}>
              <Text style={styles.createBtnText}>Create Habit</Text>
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

  /* Form */
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

  iconColorRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
  },
  changeIconBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 14,
    paddingHorizontal: 12,
    height: 44,
  },
  changeIconText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#0F172A',
  },
  colorPaletteRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    flex: 1,
    justifyContent: 'flex-end',
  },
  colorDot: {
    width: 22,
    height: 22,
    borderRadius: 11,
  },
  colorDotSelected: {
    borderWidth: 3,
    borderColor: '#FFFFFF',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 3,
    elevation: 3,
  },

  freqRow: {
    flexDirection: 'row',
    gap: 8,
  },
  freqPill: {
    flex: 1,
    height: 42,
    borderRadius: 12,
    backgroundColor: '#F8FAFC',
    borderWidth: 1.5,
    borderColor: '#F1F5F9',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 4,
  },
  freqActive: {
    backgroundColor: '#F0FDF4',
    borderColor: '#86EFAC',
  },
  freqText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#475569',
  },
  freqActiveText: {
    color: '#16A34A',
    fontWeight: '700',
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
  fullWidthSelect: {
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
  switchBox: {
    height: 50,
    backgroundColor: '#FFFFFF',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 14,
    paddingHorizontal: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  switchLabel: {
    fontSize: 12,
    color: '#475569',
    fontWeight: '500',
    flex: 1,
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
