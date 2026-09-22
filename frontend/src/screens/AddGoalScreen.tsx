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

interface AddGoalScreenProps {
  onBack?: () => void;
  onSubmit?: (data: any) => void;
}

export const AddGoalScreen: React.FC<AddGoalScreenProps> = ({ onBack, onSubmit }) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [goalType, setGoalType] = useState<'personal' | 'career' | 'health' | 'learning' | 'finance' | 'travel'>('personal');
  const [targetDate, setTargetDate] = useState('Mon, 2 Sep 2024');
  const [priority, setPriority] = useState<'high' | 'medium' | 'low'>('high');
  const [milestone, setMilestone] = useState('');
  const [progressTracking, setProgressTracking] = useState('Percentage (%)');
  const [targetValue, setTargetValue] = useState('');
  const [relatedArea, setRelatedArea] = useState('Select a life area');
  const [reminder, setReminder] = useState('Weekly reminder');
  const [repeat, setRepeat] = useState('Every week');
  const [subgoal, setSubgoal] = useState('');

  const handleSave = () => {
    const data = {
      title,
      description,
      goalType,
      targetDate,
      priority,
      milestone,
      progressTracking,
      targetValue,
      relatedArea,
      reminder,
      repeat,
      subgoal,
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
              <Text style={styles.headerTitle}>Add Goal</Text>
              <Text style={styles.headerSubtitle}>Set a goal. Turn your vision into progress.</Text>
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
                <Text style={styles.aiTitle}>Need help setting this goal?</Text>
                <Text style={styles.aiSubtitle}>
                  Tell LIVO what you want to achieve, and I'll help you break it down into clear steps.
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
            {/* Goal Title */}
            <Text style={styles.label}>Goal Title <Text style={styles.requiredStar}>*</Text></Text>
            <TextInput
              style={styles.textInput}
              placeholder="e.g. Learn UI/UX Design"
              placeholderTextColor="#94A3B8"
              value={title}
              onChangeText={setTitle}
            />

            {/* Description */}
            <Text style={styles.label}>Description</Text>
            <View style={styles.textAreaWrap}>
              <TextInput
                style={styles.textArea}
                placeholder="Why is this goal important to you?"
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

            {/* Goal Type */}
            <Text style={styles.label}>Goal Type</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.typesRow}>
              <TouchableOpacity
                style={[styles.typeCard, goalType === 'personal' && styles.typeCardActive, { backgroundColor: '#F0FDF4' }]}
                onPress={() => setGoalType('personal')}
                activeOpacity={0.8}
              >
                <Feather name="user" size={18} color="#16A34A" />
                <Text style={[styles.typeText, { color: '#16A34A' }]}>Personal</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.typeCard, goalType === 'career' && styles.typeCardActive, { backgroundColor: '#EFF6FF' }]}
                onPress={() => setGoalType('career')}
                activeOpacity={0.8}
              >
                <Feather name="briefcase" size={18} color="#2563EB" />
                <Text style={[styles.typeText, { color: '#2563EB' }]}>Career</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.typeCard, goalType === 'health' && styles.typeCardActive, { backgroundColor: '#FDF2F8' }]}
                onPress={() => setGoalType('health')}
                activeOpacity={0.8}
              >
                <Ionicons name="heart-outline" size={18} color="#DB2777" />
                <Text style={[styles.typeText, { color: '#DB2777' }]}>Health</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.typeCard, goalType === 'learning' && styles.typeCardActive, { backgroundColor: '#F3E8FF' }]}
                onPress={() => setGoalType('learning')}
                activeOpacity={0.8}
              >
                <Feather name="book-open" size={18} color="#9333EA" />
                <Text style={[styles.typeText, { color: '#9333EA' }]}>Learning</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.typeCard, goalType === 'finance' && styles.typeCardActive, { backgroundColor: '#FEF3C7' }]}
                onPress={() => setGoalType('finance')}
                activeOpacity={0.8}
              >
                <Ionicons name="bar-chart-outline" size={18} color="#D97706" />
                <Text style={[styles.typeText, { color: '#D97706' }]}>Finance</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[styles.typeCard, goalType === 'travel' && styles.typeCardActive, { backgroundColor: '#FFEDD5' }]}
                onPress={() => setGoalType('travel')}
                activeOpacity={0.8}
              >
                <Ionicons name="airplane-outline" size={18} color="#EA580C" />
                <Text style={[styles.typeText, { color: '#EA580C' }]}>Travel</Text>
              </TouchableOpacity>
            </ScrollView>

            {/* Target Date & Priority */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <Text style={styles.label}>Target Date</Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Feather name="calendar" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{targetDate}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>

              <View style={styles.col}>
                <Text style={styles.label}>Priority</Text>
                <View style={styles.priorityRow}>
                  <TouchableOpacity
                    style={[styles.priorityPill, priority === 'high' && styles.priorityHighActive]}
                    onPress={() => setPriority('high')}
                    activeOpacity={0.8}
                  >
                    <Feather name="flag" size={13} color={priority === 'high' ? '#DC2626' : '#64748B'} style={{ marginRight: 4 }} />
                    <Text style={[styles.priorityText, priority === 'high' && styles.priorityHighText]}>High</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={[styles.priorityPill, priority === 'medium' && styles.priorityMediumActive]}
                    onPress={() => setPriority('medium')}
                    activeOpacity={0.8}
                  >
                    <Text style={styles.priorityText}>Medium</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={[styles.priorityPill, priority === 'low' && styles.priorityLowActive]}
                    onPress={() => setPriority('low')}
                    activeOpacity={0.8}
                  >
                    <Text style={styles.priorityText}>Low</Text>
                  </TouchableOpacity>
                </View>
              </View>
            </View>

            {/* Milestone / Target */}
            <Text style={styles.label}>Milestone / Target</Text>
            <View style={styles.inputIconWrap}>
              <Ionicons name="disc-outline" size={16} color="#64748B" style={styles.fieldIcon} />
              <TextInput
                style={styles.textInputWithIcon}
                placeholder="e.g. Complete the course, lose 5 kg, save ₹50,000"
                placeholderTextColor="#94A3B8"
                value={milestone}
                onChangeText={setMilestone}
              />
            </View>

            {/* Progress Tracking & Target Value */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <Text style={styles.label}>Progress Tracking</Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Ionicons name="bar-chart-outline" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{progressTracking}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>

              <View style={styles.col}>
                <Text style={styles.label}>Target Value</Text>
                <View style={styles.inputIconWrap}>
                  <Text style={{ color: '#64748B', fontWeight: '700', marginRight: 6 }}>#</Text>
                  <TextInput
                    style={styles.textInputWithIcon}
                    placeholder="e.g. 100"
                    placeholderTextColor="#94A3B8"
                    keyboardType="numeric"
                    value={targetValue}
                    onChangeText={setTargetValue}
                  />
                </View>
              </View>
            </View>

            {/* Related Area */}
            <Text style={styles.label}>Related Area <Text style={styles.optionalText}>(optional)</Text></Text>
            <TouchableOpacity style={styles.fullWidthSelect} activeOpacity={0.7}>
              <Feather name="grid" size={16} color="#64748B" style={styles.fieldIcon} />
              <Text style={styles.dropdownText}>{relatedArea}</Text>
              <Feather name="chevron-down" size={16} color="#64748B" />
            </TouchableOpacity>

            {/* Reminder & Repeat */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <Text style={styles.label}>Add a Reminder <Text style={styles.optionalText}>(optional)</Text></Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Feather name="bell" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{reminder}</Text>
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

            {/* Add Subgoals */}
            <Text style={styles.label}>Add Subgoals <Text style={styles.optionalText}>(optional)</Text></Text>
            <View style={styles.subgoalRow}>
              <View style={[styles.inputIconWrap, { flex: 1 }]}>
                <Feather name="plus-circle" size={16} color="#66C400" style={styles.fieldIcon} />
                <TextInput
                  style={styles.textInputWithIcon}
                  placeholder="Add a subgoal"
                  placeholderTextColor="#94A3B8"
                  value={subgoal}
                  onChangeText={setSubgoal}
                />
              </View>

              <TouchableOpacity style={styles.generateAiBtn} activeOpacity={0.8}>
                <Ionicons name="sparkles" size={14} color="#16A34A" style={{ marginRight: 4 }} />
                <Text style={styles.generateAiText}>Generate with AI</Text>
              </TouchableOpacity>
            </View>

            {/* Add Attachments */}
            <Text style={styles.label}>Add Attachments <Text style={styles.optionalText}>(optional)</Text></Text>
            <TouchableOpacity style={styles.attachmentButton} activeOpacity={0.7}>
              <View style={styles.attachmentLeft}>
                <Feather name="paperclip" size={16} color="#64748B" style={{ marginRight: 10 }} />
                <Text style={styles.attachmentText}>Attach file, image or link</Text>
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
              <Text style={styles.createBtnText}>Create Goal</Text>
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

  typesRow: {
    flexDirection: 'row',
    gap: 10,
    paddingBottom: 4,
  },
  typeCard: {
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    width: 76,
    height: 70,
    borderRadius: 14,
    borderWidth: 1.5,
    borderColor: 'transparent',
    marginRight: 10,
    gap: 4,
  },
  typeCardActive: {
    borderColor: '#66C400',
    borderWidth: 2,
  },
  typeText: {
    fontSize: 11.5,
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
    paddingHorizontal: 12,
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
  fieldIcon: {
    marginRight: 8,
  },
  dropdownText: {
    flex: 1,
    fontSize: 13,
    fontWeight: '500',
    color: '#0F172A',
  },

  priorityRow: {
    flexDirection: 'row',
    gap: 6,
  },
  priorityPill: {
    flex: 1,
    height: 50,
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
    fontSize: 12,
    fontWeight: '600',
    color: '#64748B',
  },
  priorityHighText: {
    color: '#DC2626',
    fontWeight: '700',
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
    fontSize: 14,
    color: '#0F172A',
  },

  subgoalRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  generateAiBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F0FDF4',
    borderWidth: 1,
    borderColor: '#86EFAC',
    paddingHorizontal: 12,
    height: 50,
    borderRadius: 14,
  },
  generateAiText: {
    fontSize: 12.5,
    fontWeight: '700',
    color: '#16A34A',
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
