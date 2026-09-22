import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  Platform,
  TextInput,
  KeyboardAvoidingView,
} from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';

interface AddTaskScreenProps {
  onBack?: () => void;
  onCreateTask?: (task: any) => void;
}

export const AddTaskScreen: React.FC<AddTaskScreenProps> = ({ onBack, onCreateTask }) => {
  const [taskTitle, setTaskTitle] = useState('');
  const [description, setDescription] = useState('');
  const [selectedDate, setSelectedDate] = useState('Mon, 2 Sep 2024');
  const [selectedTime, setSelectedTime] = useState('2:00 PM');
  const [selectedPriority, setSelectedPriority] = useState<'High' | 'Medium' | 'Low'>('High');
  const [selectedCategory, setSelectedCategory] = useState('Work');
  const [selectedGoal, setSelectedGoal] = useState('Build a strong portfolio');
  const [reminder, setReminder] = useState('30 Min');
  const [repeat, setRepeat] = useState('Does not repeat');
  const [subtasks, setSubtasks] = useState<string[]>([]);
  const [newSubtask, setNewSubtask] = useState('');
  const [showSubtaskInput, setShowSubtaskInput] = useState(false);

  const handleCreateTask = () => {
    if (!taskTitle.trim()) return;
    onCreateTask?.({
      title: taskTitle,
      description,
      date: selectedDate,
      time: selectedTime,
      priority: selectedPriority,
      category: selectedCategory,
      goal: selectedGoal,
      reminder,
      repeat,
      subtasks,
    });
    onBack?.();
  };

  const addSubtask = () => {
    if (newSubtask.trim()) {
      setSubtasks((prev) => [...prev, newSubtask.trim()]);
      setNewSubtask('');
      setShowSubtaskInput(false);
    }
  };

  const priorityOptions: { label: 'High' | 'Medium' | 'Low'; color: string; bgColor: string }[] = [
    { label: 'High', color: '#DC2626', bgColor: '#FEE2E2' },
    { label: 'Medium', color: '#F59E0B', bgColor: '#FEF3C7' },
    { label: 'Low', color: '#64748B', bgColor: '#F1F5F9' },
  ];

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        {/* Header */}
        <View style={styles.headerBar}>
          <TouchableOpacity
            style={styles.backBtn}
            onPress={onBack}
            hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
          >
            <Feather name="arrow-left" size={22} color="#0F172A" />
          </TouchableOpacity>

          <View style={styles.headerTitleWrap}>
            <Text style={styles.headerTitle}>Add Task</Text>
            <Text style={styles.headerSub}>Turn your thoughts into action.</Text>
          </View>

          <View style={styles.avatarCircle}>
            <Text style={styles.avatarText}>R</Text>
          </View>
        </View>

        <ScrollView
          style={styles.container}
          contentContainerStyle={styles.contentContainer}
          showsVerticalScrollIndicator={false}
          keyboardShouldPersistTaps="handled"
        >
          {/* AI Help Banner */}
          <View style={styles.aiBanner}>
            <View style={styles.aiBannerLeft}>
              <View style={styles.aiIconCircle}>
                <Ionicons name="sparkles" size={16} color="#7C3AED" />
              </View>
              <View style={styles.aiTextWrap}>
                <Text style={styles.aiBannerTitle}>Need help writing this task?</Text>
                <Text style={styles.aiBannerSub}>
                  Tell LIVO what you want to do,{'\n'}and I'll help set it up.
                </Text>
              </View>
            </View>
            <TouchableOpacity style={styles.useAiBtn}>
              <Text style={styles.useAiText}>Use AI</Text>
            </TouchableOpacity>
          </View>

          {/* Task Title */}
          <Text style={styles.fieldLabel}>
            Task Title <Text style={styles.requiredStar}>*</Text>
          </Text>
          <View style={styles.inputBox}>
            <TextInput
              style={styles.textInput}
              placeholder="e.g. Finish UI Design"
              placeholderTextColor="#CBD5E1"
              value={taskTitle}
              onChangeText={setTaskTitle}
            />
          </View>

          {/* Description */}
          <Text style={styles.fieldLabel}>Description</Text>
          <View style={styles.textAreaBox}>
            <TextInput
              style={styles.textArea}
              placeholder="Add more details (optional)..."
              placeholderTextColor="#CBD5E1"
              value={description}
              onChangeText={(t) => {
                if (t.length <= 300) setDescription(t);
              }}
              multiline
              numberOfLines={4}
              textAlignVertical="top"
            />
            <Text style={styles.charCount}>{description.length}/300</Text>
          </View>

          {/* Date & Time Row */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <Text style={styles.fieldLabel}>Date</Text>
              <TouchableOpacity style={styles.dropdownBox}>
                <Feather name="calendar" size={15} color="#64748B" style={{ marginRight: 6 }} />
                <Text style={styles.dropdownText}>{selectedDate}</Text>
                <Feather name="chevron-down" size={14} color="#94A3B8" style={{ marginLeft: 'auto' }} />
              </TouchableOpacity>
            </View>

            <View style={styles.colHalf}>
              <Text style={styles.fieldLabel}>Time</Text>
              <TouchableOpacity style={styles.dropdownBox}>
                <Feather name="clock" size={15} color="#64748B" style={{ marginRight: 6 }} />
                <Text style={styles.dropdownText}>{selectedTime}</Text>
                <Feather name="chevron-down" size={14} color="#94A3B8" style={{ marginLeft: 'auto' }} />
              </TouchableOpacity>
            </View>
          </View>

          {/* Priority */}
          <Text style={styles.fieldLabel}>Priority</Text>
          <View style={styles.priorityRow}>
            {priorityOptions.map((opt) => {
              const isSelected = selectedPriority === opt.label;
              return (
                <TouchableOpacity
                  key={opt.label}
                  style={[
                    styles.priorityPill,
                    {
                      backgroundColor: isSelected ? opt.bgColor : '#F8FAFC',
                      borderColor: isSelected ? opt.color : '#E2E8F0',
                    },
                  ]}
                  onPress={() => setSelectedPriority(opt.label)}
                >
                  <View
                    style={[
                      styles.priorityDot,
                      { backgroundColor: opt.color },
                    ]}
                  />
                  <Text
                    style={[
                      styles.priorityText,
                      { color: isSelected ? opt.color : '#64748B' },
                    ]}
                  >
                    {opt.label}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>

          {/* Category & Add to Goal Row */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <Text style={styles.fieldLabel}>Category</Text>
              <TouchableOpacity style={styles.dropdownBox}>
                <View style={[styles.miniIconBadge, { backgroundColor: '#E2F7C5' }]}>
                  <Feather name="briefcase" size={12} color="#2D6A00" />
                </View>
                <Text style={styles.dropdownText}>{selectedCategory}</Text>
                <Feather name="chevron-down" size={14} color="#94A3B8" style={{ marginLeft: 'auto' }} />
              </TouchableOpacity>
            </View>

            <View style={styles.colHalf}>
              <Text style={styles.fieldLabelLight}>Add to Goal <Text style={styles.optionalText}>(optional)</Text></Text>
              <TouchableOpacity style={styles.dropdownBox}>
                <View style={[styles.miniIconBadge, { backgroundColor: '#F3E8FF' }]}>
                  <Ionicons name="disc-outline" size={12} color="#7C3AED" />
                </View>
                <Text style={styles.dropdownText} numberOfLines={1}>
                  {selectedGoal.length > 14 ? selectedGoal.substring(0, 14) + '...' : selectedGoal}
                </Text>
                <Feather name="chevron-down" size={14} color="#94A3B8" style={{ marginLeft: 'auto' }} />
              </TouchableOpacity>
            </View>
          </View>

          {/* Set Reminder & Repeat Row */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <Text style={styles.fieldLabel}>Set Reminder</Text>
              <TouchableOpacity style={styles.dropdownBox}>
                <Feather name="bell" size={15} color="#64748B" style={{ marginRight: 6 }} />
                <Text style={styles.dropdownText}>{reminder}</Text>
                <Feather name="chevron-down" size={14} color="#94A3B8" style={{ marginLeft: 'auto' }} />
              </TouchableOpacity>
            </View>

            <View style={styles.colHalf}>
              <Text style={styles.fieldLabel}>Repeat</Text>
              <TouchableOpacity style={styles.dropdownBox}>
                <Feather name="repeat" size={15} color="#64748B" style={{ marginRight: 6 }} />
                <Text style={styles.dropdownText} numberOfLines={1}>{repeat}</Text>
                <Feather name="chevron-down" size={14} color="#94A3B8" style={{ marginLeft: 'auto' }} />
              </TouchableOpacity>
            </View>
          </View>

          {/* Add Subtasks */}
          <Text style={styles.fieldLabel}>Add Subtasks</Text>
          <View style={styles.subtasksSection}>
            {subtasks.map((st, idx) => (
              <View key={idx} style={styles.subtaskRow}>
                <View style={styles.subtaskCheckbox}>
                  <Feather name="circle" size={14} color="#CBD5E1" />
                </View>
                <Text style={styles.subtaskText}>{st}</Text>
                <TouchableOpacity
                  onPress={() =>
                    setSubtasks((prev) => prev.filter((_, i) => i !== idx))
                  }
                >
                  <Feather name="x" size={14} color="#94A3B8" />
                </TouchableOpacity>
              </View>
            ))}

            {showSubtaskInput ? (
              <View style={styles.subtaskInputRow}>
                <TextInput
                  style={styles.subtaskInput}
                  placeholder="Enter subtask..."
                  placeholderTextColor="#CBD5E1"
                  value={newSubtask}
                  onChangeText={setNewSubtask}
                  onSubmitEditing={addSubtask}
                  autoFocus
                />
                <TouchableOpacity onPress={addSubtask} style={styles.subtaskAddIcon}>
                  <Feather name="check" size={16} color="#66C400" />
                </TouchableOpacity>
              </View>
            ) : null}

            <View style={styles.subtaskActionsRow}>
              <TouchableOpacity
                style={styles.subtaskActionBtn}
                onPress={() => setShowSubtaskInput(true)}
              >
                <View style={styles.addSubCircle}>
                  <Feather name="plus" size={14} color="#66C400" />
                </View>
                <Text style={styles.subtaskActionText}>Add a subtask</Text>
              </TouchableOpacity>

              <TouchableOpacity style={styles.subtaskActionBtn}>
                <Feather name="plus" size={14} color="#7C3AED" style={{ marginRight: 4 }} />
                <Text style={[styles.subtaskActionText, { color: '#7C3AED' }]}>
                  Generate with AI
                </Text>
              </TouchableOpacity>
            </View>
          </View>

          {/* Add Attachments */}
          <Text style={styles.fieldLabelLight}>
            Add Attachments <Text style={styles.optionalText}>(optional)</Text>
          </Text>
          <TouchableOpacity style={styles.attachmentRow}>
            <View style={styles.attachLeftRow}>
              <Feather name="paperclip" size={16} color="#64748B" style={{ marginRight: 8 }} />
              <Text style={styles.attachText}>Attach file, image or link</Text>
            </View>
            <Feather name="chevron-right" size={16} color="#94A3B8" />
          </TouchableOpacity>

          <View style={{ height: 100 }} />
        </ScrollView>

        {/* Bottom Bar */}
        <View style={styles.bottomBar}>
          <TouchableOpacity style={styles.cancelBtn} onPress={onBack}>
            <Text style={styles.cancelBtnText}>Cancel</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[
              styles.createBtn,
              !taskTitle.trim() && styles.createBtnDisabled,
            ]}
            onPress={handleCreateTask}
            disabled={!taskTitle.trim()}
          >
            <Text style={styles.createBtnText}>Create Task</Text>
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },

  /* Header */
  headerBar: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) + 8 : 12,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  backBtn: {
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
  headerSub: {
    fontSize: 12,
    color: '#94A3B8',
    marginTop: 1,
  },
  avatarCircle: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#E2F7C5',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#2D6A00',
  },

  container: {
    flex: 1,
    backgroundColor: '#F8FAF5',
  },
  contentContainer: {
    paddingHorizontal: 16,
    paddingTop: 14,
  },

  /* AI Banner */
  aiBanner: {
    backgroundColor: '#F3F0FF',
    borderRadius: 16,
    padding: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderWidth: 1,
    borderColor: '#E9D5FF',
    marginBottom: 20,
  },
  aiBannerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    marginRight: 8,
  },
  aiIconCircle: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#E9D5FF',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  aiTextWrap: {
    flex: 1,
  },
  aiBannerTitle: {
    fontSize: 13,
    fontWeight: '800',
    color: '#0F172A',
  },
  aiBannerSub: {
    fontSize: 11,
    color: '#64748B',
    marginTop: 2,
    lineHeight: 15,
  },
  useAiBtn: {
    backgroundColor: '#EDE9FE',
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 8,
  },
  useAiText: {
    fontSize: 12.5,
    fontWeight: '700',
    color: '#7C3AED',
  },

  /* Field Labels */
  fieldLabel: {
    fontSize: 13,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 6,
  },
  fieldLabelLight: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 6,
  },
  requiredStar: {
    color: '#DC2626',
  },
  optionalText: {
    color: '#94A3B8',
    fontWeight: '400',
    fontStyle: 'italic',
  },

  /* Text Input */
  inputBox: {
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
  },

  /* Text Area */
  textAreaBox: {
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingTop: 12,
    paddingBottom: 8,
    marginBottom: 16,
    minHeight: 100,
  },
  textArea: {
    fontSize: 14,
    color: '#0F172A',
    minHeight: 70,
  },
  charCount: {
    fontSize: 11,
    color: '#94A3B8',
    textAlign: 'right',
    marginTop: 4,
  },

  /* Two Column Row */
  twoColRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 16,
  },
  colHalf: {
    width: '48%',
  },

  /* Dropdown Box */
  dropdownBox: {
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 12,
    paddingVertical: 11,
    flexDirection: 'row',
    alignItems: 'center',
  },
  dropdownText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#0F172A',
    flex: 1,
  },

  /* Mini Icon Badge */
  miniIconBadge: {
    width: 22,
    height: 22,
    borderRadius: 6,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 6,
  },

  /* Priority */
  priorityRow: {
    flexDirection: 'row',
    marginBottom: 16,
  },
  priorityPill: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 9,
    borderWidth: 1,
    marginRight: 10,
  },
  priorityDot: {
    width: 10,
    height: 10,
    borderRadius: 3,
    marginRight: 6,
  },
  priorityText: {
    fontSize: 13,
    fontWeight: '700',
  },

  /* Subtasks */
  subtasksSection: {
    marginBottom: 16,
  },
  subtaskRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 12,
    paddingVertical: 10,
    marginBottom: 8,
  },
  subtaskCheckbox: {
    marginRight: 8,
  },
  subtaskText: {
    flex: 1,
    fontSize: 13,
    color: '#0F172A',
  },
  subtaskInputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#66C400',
    paddingHorizontal: 12,
    paddingVertical: 8,
    marginBottom: 8,
  },
  subtaskInput: {
    flex: 1,
    fontSize: 13,
    color: '#0F172A',
  },
  subtaskAddIcon: {
    padding: 4,
  },
  subtaskActionsRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  subtaskActionBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 12,
    paddingVertical: 8,
    marginRight: 10,
  },
  addSubCircle: {
    width: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: '#E2F7C5',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 6,
  },
  subtaskActionText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#475569',
  },

  /* Attachments */
  attachmentRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingVertical: 13,
    marginBottom: 16,
  },
  attachLeftRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  attachText: {
    fontSize: 13,
    color: '#94A3B8',
  },

  /* Bottom Bar */
  bottomBar: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: '#FFFFFF',
    borderTopWidth: 1,
    borderTopColor: '#F1F5F9',
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
    marginRight: 10,
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
