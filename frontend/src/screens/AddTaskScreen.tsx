import React, { useState, useEffect, useMemo, useRef } from 'react';
import { DatePickerField } from '../components/forms/DatePickerField';
import { TimePickerField } from '../components/forms/TimePickerField';
import { SelectionModal, SelectionOption } from '../components/forms/SelectionModal';
import { PrioritySelector } from '../components/forms/PrioritySelector';
import { SubtaskManager } from '../components/forms/SubtaskManager';
import { AttachmentPicker } from '../components/forms/AttachmentPicker';
import { CategoryPickerField } from '../components/forms/CategoryPickerField';
import { GoalPickerField } from '../components/forms/GoalPickerField';
import { CreationSuccessModal } from '../components/ui/CreationSuccessModal';
import { useTasks } from '../hooks/useTasks';
import { useEvents } from '../hooks/useEvents';
import { useProfile } from '../hooks/useProfile';
import { Task } from '../types';
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
  Alert,
} from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';
import { useNavigation, useRoute } from '@react-navigation/native';

export interface AddTaskScreenProps {
  onBack?: () => void;
  onCreateTask?: (task: any) => void;
  onViewTask?: (task?: any) => void;
  existingTask?: any;
  route?: any;
}

// Helper to safely parse task date
const parseTaskDate = (task: any): Date => {
  if (!task) return new Date();
  const d = task.date || task.dueDate;
  if (!d) return new Date();
  if (d instanceof Date) return isNaN(d.getTime()) ? new Date() : d;
  if (typeof d === 'string') {
    const lower = d.trim().toLowerCase();
    if (lower.startsWith('today')) return new Date();
    if (lower.startsWith('tomorrow')) {
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      return tomorrow;
    }
    const parsed = new Date(d);
    if (!isNaN(parsed.getTime())) return parsed;
    const match = d.match(/^(\d{4})[-/](\d{1,2})[-/](\d{1,2})/);
    if (match) {
      return new Date(parseInt(match[1], 10), parseInt(match[2], 10) - 1, parseInt(match[3], 10));
    }
  }
  return new Date();
};

// Helper to safely parse task time
const parseTaskTime = (task: any): Date => {
  if (!task) return new Date();
  const t = task.time || task.startTime;
  if (!t) return new Date();
  if (t instanceof Date) return isNaN(t.getTime()) ? new Date() : t;
  if (typeof t === 'string') {
    if (t.includes('T') || t.includes('Z')) {
      const parsed = new Date(t);
      if (!isNaN(parsed.getTime())) return parsed;
    }
    const timeMatch = t.match(/(\d{1,2}):(\d{2})(?::\d{2})?\s*(AM|PM)?/i);
    if (timeMatch) {
      let hours = parseInt(timeMatch[1], 10);
      const minutes = parseInt(timeMatch[2], 10);
      const ampm = timeMatch[3]?.toUpperCase();
      if (ampm === 'PM' && hours < 12) hours += 12;
      if (ampm === 'AM' && hours === 12) hours = 0;
      const d = new Date();
      d.setHours(hours, minutes, 0, 0);
      return d;
    }
  }
  return new Date();
};

export const AddTaskScreen: React.FC<AddTaskScreenProps> = (props) => {
  const navigation = useNavigation<any>();
  const route = useRoute<any>();
  const { profile } = useProfile();
  const { tasks, addTask, updateTask } = useTasks();
  const { events } = useEvents();

  // Resolve target task from props, route params, or taskId lookup
  const initialTask = useMemo(() => {
    const fromProps = props.existingTask;
    const fromRoute = route?.params?.existingTask || route?.params?.task;
    const fromId = route?.params?.taskId || route?.params?.id;
    if (fromProps) return fromProps;
    if (fromRoute) return fromRoute;
    if (fromId) return tasks.find((t) => t.id === fromId) || null;
    return null;
  }, [props.existingTask, route?.params, tasks]);

  const [activeTask, setActiveTask] = useState<any>(initialTask);
  const isEditMode = Boolean(activeTask?.id);

  // Track if we've initialized the form values from targetTask
  const loadedTaskIdRef = useRef<string | null>(null);

  const [taskTitle, setTaskTitle] = useState(initialTask?.title || '');
  const [description, setDescription] = useState(initialTask?.description || '');
  const [selectedDate, setSelectedDate] = useState<Date>(() => parseTaskDate(initialTask));
  const [selectedTime, setSelectedTime] = useState<Date>(() => parseTaskTime(initialTask));
  const [duration, setDuration] = useState(initialTask?.duration || '30 Min');
  const [selectedPriority, setSelectedPriority] = useState<'High' | 'Medium' | 'Low'>(() => {
    const p = initialTask?.priority;
    if (p) {
      const up = p.toString().toUpperCase();
      if (up === 'HIGH' || up === 'URGENT') return 'High';
      if (up === 'MEDIUM') return 'Medium';
      if (up === 'LOW') return 'Low';
    }
    return 'High';
  });
  const [selectedCategory, setSelectedCategory] = useState(initialTask?.category || 'Work');
  const [selectedGoal, setSelectedGoal] = useState(initialTask?.goal || 'Build a strong portfolio');
  const [reminder, setReminder] = useState(initialTask?.reminder || '30 Min');
  const [repeat, setRepeat] = useState(initialTask?.repeat || 'Does not repeat');
  const [location, setLocation] = useState(initialTask?.location || '');
  const [meetingType, setMeetingType] = useState<'in_person' | 'online' | 'phone'>(
    initialTask?.meetingType || 'online'
  );
  const [subtasks, setSubtasks] = useState<string[]>(initialTask?.subtasks || []);
  const [attachments, setAttachments] = useState<any[]>(initialTask?.attachments || []);

  // Update form state when initialTask or route params change
  useEffect(() => {
    const taskToLoad = initialTask;
    if (taskToLoad && taskToLoad.id !== loadedTaskIdRef.current) {
      loadedTaskIdRef.current = taskToLoad.id;
      setActiveTask(taskToLoad);
      setTaskTitle(taskToLoad.title || '');
      setDescription(taskToLoad.description || '');
      setSelectedDate(parseTaskDate(taskToLoad));
      setSelectedTime(parseTaskTime(taskToLoad));
      setDuration(taskToLoad.duration || (taskToLoad.estimatedMinutes ? `${taskToLoad.estimatedMinutes} Min` : '30 Min'));
      
      const p = taskToLoad.priority;
      if (p) {
        const up = p.toString().toUpperCase();
        if (up === 'HIGH' || up === 'URGENT') setSelectedPriority('High');
        else if (up === 'MEDIUM') setSelectedPriority('Medium');
        else if (up === 'LOW') setSelectedPriority('Low');
      } else {
        setSelectedPriority('High');
      }

      setSelectedCategory(taskToLoad.category || 'Work');
      setSelectedGoal(taskToLoad.goal || 'Build a strong portfolio');
      setReminder(taskToLoad.reminder || '30 Min');
      setRepeat(taskToLoad.repeat || 'Does not repeat');
      setLocation(taskToLoad.location || '');
      setMeetingType(taskToLoad.meetingType || 'online');
      setSubtasks(Array.isArray(taskToLoad.subtasks) ? taskToLoad.subtasks : []);
      setAttachments(Array.isArray(taskToLoad.attachments) ? taskToLoad.attachments : []);
    }
  }, [initialTask]);

  const handleBack = () => {
    if (props.onBack) {
      props.onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  // Modal visibilities
  const [showDurationModal, setShowDurationModal] = useState(false);
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

  const handleSaveOrUpdateTask = async () => {
    if (!taskTitle.trim()) {
      Alert.alert('Required Field', 'Please enter a task title.');
      return;
    }

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

    // Check for schedule conflict only when creating a new task
    if (!isEditMode) {
      const conflictingEvent = events.find((e) => {
        if (!isSameDate(e.date || e.startTime, selectedDate)) return false;
        if (!e.startTime) return false;
        const eTime = new Date(e.startTime);
        const diffMins = Math.abs(
          eTime.getHours() * 60 + eTime.getMinutes() - (selectedTime.getHours() * 60 + selectedTime.getMinutes())
        );
        return diffMins < 45;
      });

      if (conflictingEvent) {
        const formatTimeStr = (d: Date) => {
          let h = d.getHours();
          const m = d.getMinutes();
          const ampm = h >= 12 ? 'PM' : 'AM';
          h = h % 12 || 12;
          return `${h}:${String(m).padStart(2, '0')} ${ampm}`;
        };

        const nextSuggestedDate = new Date(selectedTime.getTime() + 60 * 60 * 1000);
        const suggestedTimeStr = `${formatTimeStr(nextSuggestedDate)} – ${formatTimeStr(
          new Date(nextSuggestedDate.getTime() + 60 * 60 * 1000)
        )}`;

        navigation.navigate('ScheduleConflict', {
          existingEvent: {
            title: conflictingEvent.title,
            time: conflictingEvent.startTime ? formatTimeStr(new Date(conflictingEvent.startTime)) : '11:00 AM',
            category: conflictingEvent.category || 'General',
            badge: 'Locked',
          },
          newEvent: {
            title: taskTitle.trim(),
            time: `${formatTimeStr(selectedTime)} – ${formatTimeStr(
              new Date(selectedTime.getTime() + 45 * 60 * 1000)
            )}`,
            category: selectedCategory,
            badge: 'New',
          },
          suggestedTime: suggestedTimeStr,
          suggestionReason: 'Your calendar is free and it fits before your next commitment.',
        });
        return;
      }
    }

    const taskPayload = {
      title: taskTitle.trim(),
      description: description.trim(),
      date: selectedDate.toISOString(),
      time: selectedTime.toISOString(),
      duration,
      category: selectedCategory as any,
      priority: selectedPriority as any,
      goal: selectedGoal,
      reminder,
      repeat,
      location: location.trim(),
      meetingType,
      subtasks,
      attachments,
    };

    if (isEditMode && activeTask?.id) {
      // Update existing task
      const updatedItem: Task = {
        ...activeTask,
        ...taskPayload,
        id: activeTask.id,
        createdAt: activeTask.createdAt || new Date().toISOString(),
        completed: activeTask.completed ?? false,
        status: activeTask.status || (activeTask.completed ? 'COMPLETED' : 'PENDING'),
      };
      await updateTask(updatedItem);
      props.onCreateTask?.(updatedItem);
      handleBack();
    } else {
      // Create new task
      const created = await addTask(taskPayload);
      props.onCreateTask?.({
        ...taskPayload,
        date: selectedDate,
        time: selectedTime,
      });
      setCreatedItem(
        created || {
          title: taskTitle,
          date: selectedDate,
          time: selectedTime,
          priority: selectedPriority,
          category: selectedCategory,
        }
      );
      setShowSuccessModal(true);
    }
  };

  const userInitial = profile?.name ? profile.name.charAt(0).toUpperCase() : 'R';

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
            onPress={handleBack}
            hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
          >
            <Feather name="arrow-left" size={22} color="#0F172A" />
          </TouchableOpacity>

          <View style={styles.headerTitleWrap}>
            <Text style={styles.headerTitle}>{isEditMode ? 'Edit Task' : 'Add Task'}</Text>
            <Text style={styles.headerSub}>Turn your thoughts into action.</Text>
          </View>

          <TouchableOpacity
            style={styles.avatarCircle}
            onPress={() => navigation.navigate('Profile')}
            hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
          >
            <Text style={styles.avatarText}>{userInitial}</Text>
          </TouchableOpacity>
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
            <TouchableOpacity
              style={styles.useAiBtn}
              onPress={() => navigation.navigate('ChatWithLivo')}
            >
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

          {/* Date & Time Section: Row 1 (Date | Time) */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <DatePickerField value={selectedDate} onChange={setSelectedDate} />
            </View>

            <View style={styles.colHalf}>
              <TimePickerField value={selectedTime} onChange={setSelectedTime} />
            </View>
          </View>

          {/* Date & Time Section: Row 2 (Duration | Repeat) */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <Text style={styles.fieldLabel}>Duration</Text>
              <TouchableOpacity
                style={styles.dropdownBox}
                onPress={() => setShowDurationModal(true)}
                activeOpacity={0.7}
              >
                <Feather name="clock" size={15} color="#64748B" style={{ marginRight: 6 }} />
                <Text style={styles.dropdownText}>{duration}</Text>
                <Feather
                  name="chevron-down"
                  size={14}
                  color="#94A3B8"
                  style={{ marginLeft: 'auto' }}
                />
              </TouchableOpacity>
            </View>

            <View style={styles.colHalf}>
              <Text style={styles.fieldLabel}>Repeat</Text>
              <TouchableOpacity
                style={styles.dropdownBox}
                onPress={() => setShowRepeatModal(true)}
                activeOpacity={0.7}
              >
                <Feather name="repeat" size={15} color="#64748B" style={{ marginRight: 6 }} />
                <Text style={styles.dropdownText} numberOfLines={1}>
                  {repeat}
                </Text>
                <Feather
                  name="chevron-down"
                  size={14}
                  color="#94A3B8"
                  style={{ marginLeft: 'auto' }}
                />
              </TouchableOpacity>
            </View>
          </View>

          {/* Location */}
          <Text style={styles.fieldLabelLight}>
            Location <Text style={styles.optionalText}>(optional)</Text>
          </Text>
          <View style={styles.inputBoxWithIcon}>
            <Feather name="map-pin" size={16} color="#64748B" style={{ marginRight: 8 }} />
            <TextInput
              style={[styles.textInput, { flex: 1 }]}
              placeholder="e.g. Office, Home, Google Meet"
              placeholderTextColor="#CBD5E1"
              value={location}
              onChangeText={setLocation}
            />
          </View>

          {/* Meeting Type / Format */}
          <Text style={styles.fieldLabel}>Meeting Type / Format</Text>
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
                  <Text
                    style={[
                      styles.meetingTypeText,
                      isSelected && styles.meetingTypeTextSelected,
                    ]}
                  >
                    {opt.label}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>

          {/* Priority */}
          <Text style={styles.fieldLabel}>Priority</Text>
          <PrioritySelector selectedPriority={selectedPriority} onSelect={setSelectedPriority} />

          {/* Category & Add to Goal Row */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <CategoryPickerField value={selectedCategory} onChange={setSelectedCategory} />
            </View>

            <View style={styles.colHalf}>
              <GoalPickerField value={selectedGoal} onChange={setSelectedGoal} />
            </View>
          </View>

          {/* Set Reminder */}
          <View style={{ marginBottom: 16 }}>
            <Text style={styles.fieldLabel}>Set Reminder</Text>
            <TouchableOpacity
              style={styles.dropdownBox}
              onPress={() => setShowReminderModal(true)}
              activeOpacity={0.7}
            >
              <Feather name="bell" size={15} color="#64748B" style={{ marginRight: 6 }} />
              <Text style={styles.dropdownText}>{reminder}</Text>
              <Feather
                name="chevron-down"
                size={14}
                color="#94A3B8"
                style={{ marginLeft: 'auto' }}
              />
            </TouchableOpacity>
          </View>

          {/* Add Subtasks */}
          <Text style={styles.fieldLabel}>Add Subtasks</Text>
          <SubtaskManager subtasks={subtasks} onChange={setSubtasks} />

          <AttachmentPicker attachments={attachments} onChange={setAttachments} />

          <View style={{ height: 100 }} />

          {/* Selection Modals */}
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
              { label: '5 Min', value: '5 Min' },
              { label: '10 Min', value: '10 Min' },
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

        {/* Bottom Bar */}
        <View style={styles.bottomBar}>
          <TouchableOpacity style={styles.cancelBtn} onPress={handleBack}>
            <Text style={styles.cancelBtnText}>Cancel</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.createBtn, !taskTitle.trim() && styles.createBtnDisabled]}
            onPress={handleSaveOrUpdateTask}
            disabled={!taskTitle.trim()}
          >
            <Text style={styles.createBtnText}>{isEditMode ? 'Save Changes' : 'Create Task'}</Text>
          </TouchableOpacity>
        </View>
        <CreationSuccessModal
          visible={showSuccessModal}
          itemType="Task"
          itemData={createdItem}
          onClose={() => {
            setShowSuccessModal(false);
          }}
          onViewTask={() => {
            setShowSuccessModal(false);
            if (props.onViewTask) {
              props.onViewTask(createdItem);
            } else {
              navigation.navigate('Tasks');
            }
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
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  contentContainer: {
    paddingHorizontal: 16,
    paddingTop: 16,
    paddingBottom: 24,
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
    marginRight: 8,
  },
  headerTitleWrap: {
    flex: 1,
  },
  headerTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
  },
  headerSub: {
    fontSize: 11,
    color: '#64748B',
    marginTop: -1,
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
    fontSize: 13,
    fontWeight: '700',
    color: '#2D6A00',
  },

  /* AI Banner */
  aiBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#F5EFFF',
    borderRadius: 16,
    padding: 14,
    marginBottom: 20,
    borderWidth: 1,
    borderColor: '#EDE9FE',
  },
  aiBannerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  aiIconCircle: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  aiTextWrap: {
    flex: 1,
  },
  aiBannerTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
  },
  aiBannerSub: {
    fontSize: 11,
    color: '#64748B',
    marginTop: 2,
    lineHeight: 15,
  },
  useAiBtn: {
    backgroundColor: '#7C3AED',
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 14,
    marginLeft: 8,
  },
  useAiText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#FFFFFF',
  },

  /* Fields */
  fieldLabel: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 6,
  },
  fieldLabelLight: {
    fontSize: 13,
    fontWeight: '600',
    color: '#0F172A',
    marginBottom: 6,
  },
  requiredStar: {
    color: '#EF4444',
  },
  optionalText: {
    fontSize: 12,
    fontWeight: '400',
    color: '#94A3B8',
  },
  inputBox: {
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 10,
    marginBottom: 16,
  },
  inputBoxWithIcon: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 10,
    marginBottom: 16,
  },
  textInput: {
    fontSize: 13.5,
    color: '#0F172A',
    padding: 0,
  },
  textAreaBox: {
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 10,
    marginBottom: 16,
  },
  textArea: {
    fontSize: 13.5,
    color: '#0F172A',
    height: 70,
    padding: 0,
  },
  charCount: {
    fontSize: 10.5,
    color: '#94A3B8',
    textAlign: 'right',
    marginTop: 4,
  },

  /* Rows */
  twoColRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 16,
  },
  colHalf: {
    width: '48%',
  },
  dropdownBox: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 10,
  },
  dropdownText: {
    fontSize: 13,
    color: '#0F172A',
    fontWeight: '600',
  },

  /* Meeting Type */
  meetingTypeRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 16,
  },
  meetingTypePill: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    width: '31%',
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingVertical: 9,
  },
  meetingTypePillSelected: {
    backgroundColor: '#F0FDF4',
    borderColor: '#66C400',
  },
  meetingTypeText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#64748B',
  },
  meetingTypeTextSelected: {
    color: '#16A34A',
    fontWeight: '700',
  },

  /* Bottom Bar */
  bottomBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 12,
    backgroundColor: '#FFFFFF',
    borderTopWidth: 1,
    borderTopColor: '#F1F5F9',
  },
  cancelBtn: {
    width: '48%',
    backgroundColor: '#F1F5F9',
    borderRadius: 14,
    paddingVertical: 14,
    alignItems: 'center',
  },
  cancelBtnText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#64748B',
  },
  createBtn: {
    width: '48%',
    backgroundColor: '#66C400',
    borderRadius: 14,
    paddingVertical: 14,
    alignItems: 'center',
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
