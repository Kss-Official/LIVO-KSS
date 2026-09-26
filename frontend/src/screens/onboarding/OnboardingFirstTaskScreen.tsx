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
import { taskService } from '../../services/taskService';

interface OnboardingFirstTaskScreenProps {
  onNext: () => void;
  onBack: () => void;
}

export const OnboardingFirstTaskScreen: React.FC<OnboardingFirstTaskScreenProps> = ({
  onNext,
  onBack,
}) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priority, setPriority] = useState<'Low' | 'Medium' | 'High' | 'Urgent'>('Medium');
  const [dueDate, setDueDate] = useState<Date>(new Date());
  const [dueTime, setDueTime] = useState<Date>(new Date());
  const [selectedTag, setSelectedTag] = useState<string>('Personal');

  const priorities: Array<{ label: 'Low' | 'Medium' | 'High' | 'Urgent'; color: string }> = [
    { label: 'Low', color: '#22C55E' },
    { label: 'Medium', color: '#EAB308' },
    { label: 'High', color: '#EF4444' },
    { label: 'Urgent', color: '#DC2626' },
  ];

  const tags = ['Work', 'Personal', 'Study', 'Health', 'Finance'];

  const handleSaveAndNext = async () => {
    if (title.trim()) {
      try {
        await taskService.saveTask({
          title: title.trim(),
          description: description.trim(),
          category: selectedTag,
          priority,
          dueDate: dueDate.toISOString().split('T')[0],
        });
      } catch (err) {
        console.error('Failed to save onboarding task:', err);
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
          iconName="checkbox-outline"
          title="Add your first task"
          subtitle="What do you need to get done? Let's add a task to get you started."
        />

        <View style={styles.formContainer}>
          {/* Task Title */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>Task title</Text>
            <View style={styles.inputCard}>
              <TextInput
                style={styles.textInput}
                placeholder="e.g. Complete project proposal"
                placeholderTextColor="#94A3B8"
                value={title}
                onChangeText={setTitle}
              />
            </View>
          </View>

          {/* Description */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>Description (optional)</Text>
            <View style={[styles.inputCard, styles.multilineCard]}>
              <TextInput
                style={[styles.textInput, styles.multilineInput]}
                placeholder="Add more details..."
                placeholderTextColor="#94A3B8"
                value={description}
                onChangeText={setDescription}
                multiline
                numberOfLines={3}
                maxLength={200}
              />
            </View>
            <Text style={styles.charCount}>{description.length}/200</Text>
          </View>

          {/* Priority */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>Priority</Text>
            <View style={styles.priorityRow}>
              {priorities.map((item) => {
                const isSelected = priority === item.label;
                return (
                  <TouchableOpacity
                    key={item.label}
                    style={[
                      styles.priorityPill,
                      isSelected && styles.priorityPillSelected,
                    ]}
                    onPress={() => setPriority(item.label)}
                    activeOpacity={0.7}
                  >
                    <View style={[styles.dot, { backgroundColor: item.color }]} />
                    <Text style={[styles.priorityText, isSelected && styles.priorityTextSelected]}>
                      {item.label}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </View>
          </View>

          {/* Due date & Time */}
          <View style={styles.twoColRow}>
            <View style={styles.col}>
              <DatePickerField
                label="Due date"
                value={dueDate}
                onChange={setDueDate}
              />
            </View>
            <View style={styles.col}>
              <TimePickerField
                label="Time (optional)"
                value={dueTime}
                onChange={setDueTime}
              />
            </View>
          </View>

          {/* Tags */}
          <View style={styles.fieldGroup}>
            <Text style={styles.fieldLabel}>Tags (optional)</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.tagsScroll}>
              {tags.map((tag) => {
                const isSelected = selectedTag === tag;
                return (
                  <TouchableOpacity
                    key={tag}
                    style={[styles.tagPill, isSelected && styles.tagPillSelected]}
                    onPress={() => setSelectedTag(tag)}
                    activeOpacity={0.7}
                  >
                    <Text style={[styles.tagText, isSelected && styles.tagTextSelected]}>
                      {tag}
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
            <Text style={styles.primaryBtnText}>Add Task</Text>
            <Ionicons name="arrow-forward" size={20} color="#0F172A" style={{ marginLeft: 6 }} />
          </TouchableOpacity>

          <TouchableOpacity style={styles.skipBtn} onPress={onNext} activeOpacity={0.7}>
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
  priorityRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 8,
  },
  priorityPill: {
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
  priorityPillSelected: {
    borderColor: '#84CC16',
    backgroundColor: '#F7FEE7',
    borderWidth: 1.5,
  },
  dot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 6,
  },
  priorityText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#475569',
  },
  priorityTextSelected: {
    color: '#1E293B',
    fontWeight: '700',
  },
  twoColRow: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 18,
  },
  col: {
    flex: 1,
  },
  tagsScroll: {
    flexDirection: 'row',
  },
  tagPill: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 18,
    paddingVertical: 10,
    marginRight: 10,
  },
  tagPillSelected: {
    backgroundColor: '#E4F5CB',
    borderColor: '#84CC16',
  },
  tagText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#475569',
  },
  tagTextSelected: {
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
