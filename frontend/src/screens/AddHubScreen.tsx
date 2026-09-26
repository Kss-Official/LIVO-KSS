import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  Image,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  Platform,
  Modal,
  TouchableWithoutFeedback,
  Alert,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { useFocusEffect, useNavigation } from '@react-navigation/native';
import { useTasks } from '../hooks/useTasks';
import { useProfile } from '../hooks/useProfile';
import { SelectionModal } from '../components/forms/SelectionModal';

interface AddHubScreenProps {
  onBack?: () => void;
}

type FilterTab = 'All' | 'Today' | 'Upcoming' | 'Completed';
type Priority = 'High' | 'Medium' | 'Low';

interface Task {
  id: string;
  title: string;
  time: string;
  tag: string;
  tagColor: string;
  tagBg: string;
  priority: Priority;
  completed: boolean;
  meetLink?: string;
  icon: 'clock' | 'calendar';
}

const TASKS: Task[] = [
  { id: '1', title: 'Finish UI Design', time: 'Today, 2:00 PM', tag: 'Work', tagColor: '#16A34A', tagBg: '#DCFCE7', priority: 'High', completed: false, icon: 'clock' },
  { id: '2', title: 'Prepare Client Presentation', time: 'Tomorrow, 10:00 AM', tag: 'Work', tagColor: '#16A34A', tagBg: '#DCFCE7', priority: 'High', completed: false, icon: 'calendar' },
  { id: '3', title: 'Review Project Updates', time: 'Today, 5:00 PM', tag: 'Work', tagColor: '#16A34A', tagBg: '#DCFCE7', priority: 'Medium', completed: false, icon: 'clock' },
  { id: '4', title: 'Read React Chapter', time: 'Tomorrow', tag: 'Learning', tagColor: '#7C3AED', tagBg: '#EDE9FE', priority: 'Medium', completed: false, icon: 'calendar' },
  { id: '5', title: 'Plan Weekend Trip', time: 'Sun, 8 Sep', tag: 'Personal', tagColor: '#D97706', tagBg: '#FEF3C7', priority: 'Low', completed: false, icon: 'calendar' },
  { id: '6', title: 'Team Sync', time: 'Today, 11:30 AM', tag: 'Work', tagColor: '#16A34A', tagBg: '#DCFCE7', priority: 'High', completed: true, meetLink: 'Google Meet', icon: 'clock' },
];

const PRIORITY_CONFIG: Record<Priority, { color: string; bg: string; label: string }> = {
  High: { color: '#DC2626', bg: '#FEE2E2', label: 'High Priority' },
  Medium: { color: '#D97706', bg: '#FEF3C7', label: 'Medium Priority' },
  Low: { color: '#2563EB', bg: '#DBEAFE', label: 'Low Priority' },
};

export const AddHubScreen: React.FC<AddHubScreenProps> = ({ onBack }) => {
  const navigation = useNavigation<any>();
  const { profile, refreshProfile } = useProfile();
  const [activeTab, setActiveTab] = useState<FilterTab>('All');
  const [collapsedSections, setCollapsedSections] = useState<Set<string>>(new Set());
  const [showAiBanner, setShowAiBanner] = useState(true);

  // Filters State
  const [selectedPriorityFilter, setSelectedPriorityFilter] = useState('All');
  const [selectedCategoryFilter, setSelectedCategoryFilter] = useState('All');
  const [selectedGoalFilter, setSelectedGoalFilter] = useState('All');
  const [sortBy, setSortBy] = useState('Due Date');

  const [showPriorityModal, setShowPriorityModal] = useState(false);
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const [showGoalModal, setShowGoalModal] = useState(false);
  const [showSortModal, setShowSortModal] = useState(false);

  // Hook for tasks
  const { tasks: realTasks, toggleTask, addTask, updateTask, deleteTask, refreshTasks } = useTasks();

  // Task Options Modal State
  const [isActionModalOpen, setIsActionModalOpen] = useState(false);
  const [selectedTaskForAction, setSelectedTaskForAction] = useState<Task | null>(null);
  const [showChangePriorityModal, setShowChangePriorityModal] = useState(false);
  const [showChangeDateModal, setShowChangeDateModal] = useState(false);

  // Task Action Handlers
  const handleEditTask = () => {
    if (!selectedTaskForAction) return;
    const raw = realTasks.find((t) => t.id === selectedTaskForAction.id);
    setIsActionModalOpen(false);
    navigation.navigate('AddTask', { existingTask: raw || selectedTaskForAction });
  };

  const handleToggleComplete = async () => {
    if (!selectedTaskForAction) return;
    const taskId = selectedTaskForAction.id;
    setIsActionModalOpen(false);
    await toggleTask(taskId);
  };

  const handleChangeDate = async (type: 'today' | 'tomorrow' | 'nextWeek' | 'custom') => {
    if (!selectedTaskForAction) return;
    const raw = realTasks.find((t) => t.id === selectedTaskForAction.id);
    if (!raw) return;

    if (type === 'custom') {
      setShowChangeDateModal(false);
      navigation.navigate('AddTask', { existingTask: raw });
      return;
    }

    const d = new Date();
    if (type === 'tomorrow') {
      d.setDate(d.getDate() + 1);
    } else if (type === 'nextWeek') {
      d.setDate(d.getDate() + 7);
    }
    const isoDate = d.toISOString();
    setShowChangeDateModal(false);
    await updateTask({
      ...raw,
      date: isoDate,
      dueDate: isoDate,
    });
  };

  const handleChangePriority = async (newPriority: 'High' | 'Medium' | 'Low') => {
    if (!selectedTaskForAction) return;
    const raw = realTasks.find((t) => t.id === selectedTaskForAction.id);
    if (!raw) return;
    setShowChangePriorityModal(false);
    await updateTask({
      ...raw,
      priority: newPriority,
    });
  };

  const handleDuplicateTask = async () => {
    if (!selectedTaskForAction) return;
    const raw = realTasks.find((t) => t.id === selectedTaskForAction.id);
    setIsActionModalOpen(false);
    if (raw) {
      await addTask({
        title: `${raw.title} (Copy)`,
        description: raw.description || '',
        date: raw.date || new Date().toISOString(),
        time: raw.time,
        priority: raw.priority || 'Medium',
        category: raw.category || 'Work',
        duration: raw.duration,
        location: raw.location,
        subtasks: raw.subtasks || [],
        attachments: raw.attachments || [],
      });
    }
  };

  const handleDeleteTask = () => {
    if (!selectedTaskForAction) return;
    const taskId = selectedTaskForAction.id;
    const title = selectedTaskForAction.title;
    setIsActionModalOpen(false);

    Alert.alert(
      'Delete Task',
      `Are you sure you want to delete "${title}"?`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            await deleteTask(taskId);
          },
        },
      ]
    );
  };

  useFocusEffect(
    useCallback(() => {
      refreshTasks?.();
      refreshProfile?.();
    }, [refreshTasks, refreshProfile])
  );

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('Home');
    }
  };

  const formatISO = (isoStr?: string, mode: 'date' | 'time' = 'date') => {
    if (!isoStr) return '';
    try {
      const d = new Date(isoStr);
      if (isNaN(d.getTime())) return isoStr;
      if (mode === 'time') return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      return d.toLocaleDateString('en-US', { day: 'numeric', month: 'short', year: 'numeric' });
    } catch {
      return isoStr;
    }
  };

  // Transform realTasks or fallback to TASKS
  const allTasksList =
    realTasks.length > 0
      ? realTasks.map((t) => {
          const formattedDate = formatISO(t.date || t.dueDate, 'date');
          const formattedTime = formatISO(t.time, 'time');
          return {
            id: t.id,
            title: t.title,
            time: formattedDate ? (formattedTime ? `${formattedDate}, ${formattedTime}` : formattedDate) : 'Today',
            tag: t.category || 'Work',
            tagColor: t.category === 'Learning' ? '#7C3AED' : t.category === 'Personal' ? '#D97706' : '#16A34A',
            tagBg: t.category === 'Learning' ? '#EDE9FE' : t.category === 'Personal' ? '#FEF3C7' : '#DCFCE7',
            priority: (t.priority || 'High') as Priority,
            completed: t.completed,
            icon: 'clock' as const,
          };
        })
      : TASKS;

  const toggleCheck = (id: string) => {
    toggleTask(id);
  };

  const toggleSection = (key: string) => {
    setCollapsedSections((prev) => {
      const next = new Set(prev);
      next.has(key) ? next.delete(key) : next.add(key);
      return next;
    });
  };

  const filteredTasks = allTasksList.filter((t) => {
    if (activeTab === 'Completed') return t.completed;
    if (activeTab === 'Today' && !t.time.toLowerCase().includes('today')) return false;
    if (activeTab === 'Upcoming' && t.time.toLowerCase().includes('today')) return false;
    if (selectedPriorityFilter !== 'All' && t.priority !== selectedPriorityFilter) return false;
    if (selectedCategoryFilter !== 'All' && t.tag !== selectedCategoryFilter) return false;
    return true;
  });

  const completedTasks = allTasksList.filter((t) => t.completed);
  const todayTotal = allTasksList.filter((t) => t.time.toLowerCase().includes('today')).length || 3;
  const todayDone = allTasksList.filter((t) => t.time.toLowerCase().includes('today') && t.completed).length || 1;
  const remaining = Math.max(0, todayTotal - todayDone);

  const priorities: Priority[] = ['High', 'Medium', 'Low'];

  const renderTask = (task: Task) => {
    const isChecked = task.completed;
    const cfg = PRIORITY_CONFIG[task.priority] || PRIORITY_CONFIG.Medium;
    return (
      <View key={task.id} style={styles.taskRow}>
        <TouchableOpacity
          onPress={() => toggleCheck(task.id)}
          style={[styles.checkbox, isChecked && styles.checkboxChecked]}
        >
          {isChecked && <Feather name="check" size={11} color="#FFFFFF" />}
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.taskInfo}
          onPress={() => navigation.navigate('AddTask', { taskId: task.id })}
          activeOpacity={0.7}
        >
          <Text style={[styles.taskTitle, isChecked && styles.taskTitleDone]}>{task.title}</Text>
          <View style={styles.taskMeta}>
            {task.icon === 'clock' ? (
              <Ionicons name="time-outline" size={11} color="#94A3B8" style={{ marginRight: 3 }} />
            ) : (
              <Feather name="calendar" size={11} color="#94A3B8" style={{ marginRight: 3 }} />
            )}
            <Text style={styles.taskTime}>{task.time}</Text>
            {task.meetLink && (
              <>
                <MaterialCommunityIcons name="google" size={11} color="#94A3B8" style={{ marginLeft: 6, marginRight: 2 }} />
                <Text style={styles.taskTime}>{task.meetLink}</Text>
              </>
            )}
          </View>
        </TouchableOpacity>
        <View style={[styles.tagBadge, { backgroundColor: task.tagBg }]}>
          <Text style={[styles.tagText, { color: task.tagColor }]}>{task.tag}</Text>
        </View>
        {!isChecked && (
          <View style={[styles.priorityBadge, { backgroundColor: cfg.bg }]}>
            <Text style={[styles.priorityText, { color: cfg.color }]}>{task.priority}</Text>
          </View>
        )}
        <TouchableOpacity
          style={styles.moreBtn}
          hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
          onPress={() => {
            setSelectedTaskForAction(task);
            setIsActionModalOpen(true);
          }}
        >
          <Feather name="more-vertical" size={16} color="#CBD5E1" />
        </TouchableOpacity>
      </View>
    );
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.contentContainer}
        showsVerticalScrollIndicator={false}
      >
        {/* Header */}
        <View style={styles.headerRow}>
          <View style={styles.logoWrap}>
            <TouchableOpacity
              onPress={handleBack}
              style={styles.backBtn}
              hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
              activeOpacity={0.7}
            >
              <Feather name="arrow-left" size={22} color="#0F172A" />
            </TouchableOpacity>
            <View>
              <Image
                source={require('../../assets/livo_logo.png')}
                style={styles.logoImage}
                resizeMode="contain"
              />
              <Text style={styles.logoSub}>A BETTER YOU</Text>
            </View>
          </View>

          <View style={styles.headerActions}>
            <TouchableOpacity
              style={styles.iconBtn}
              onPress={() => navigation.navigate('Search')}
              activeOpacity={0.7}
            >
              <Feather name="search" size={20} color="#0F172A" />
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.iconBtn}
              onPress={() => navigation.navigate('Notifications')}
              activeOpacity={0.7}
            >
              <Ionicons name="notifications-outline" size={20} color="#0F172A" />
              <View style={styles.notifDot} />
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.avatarCircle}
              onPress={() => navigation.navigate('Profile')}
              activeOpacity={0.8}
            >
              <Text style={styles.avatarText}>{profile?.name?.charAt(0)?.toUpperCase() || 'R'}</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Title + Badge */}
        <View style={styles.titleSection}>
          <View style={styles.titleLeft}>
            <Text style={styles.mainTitle}>Tasks</Text>
            <Text style={styles.mainSubtitle}>Turn your plans into progress.</Text>
          </View>
          <View style={styles.ovalBadge}>
            <Text style={styles.ovalLine}>Small tasks</Text>
            <Text style={styles.ovalLine}>Big progress.</Text>
            <Feather name="edit-2" size={11} color="#2D6A00" style={styles.pencilIcon} />
          </View>
        </View>

        {/* Filter Tabs + Add Task */}
        <View style={styles.tabsRow}>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.tabsScroll}>
            {(['All', 'Today', 'Upcoming', 'Completed'] as FilterTab[]).map((tab) => (
              <TouchableOpacity
                key={tab}
                style={[styles.tabBtn, activeTab === tab && styles.tabBtnActive]}
                onPress={() => setActiveTab(tab)}
              >
                <Text style={[styles.tabText, activeTab === tab && styles.tabTextActive]}>{tab}</Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
          <TouchableOpacity
            style={styles.addTaskBtn}
            onPress={() => navigation.navigate('AddOptains')}
            activeOpacity={0.85}
          >
            <Feather name="plus" size={14} color="#FFFFFF" style={{ marginRight: 4 }} />
            <Text style={styles.addTaskText}>Add Task</Text>
          </TouchableOpacity>
        </View>

        {/* Secondary Filters */}
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          style={styles.filterScrollView}
          contentContainerStyle={styles.filterRow}
        >
          <TouchableOpacity style={styles.filterChip} onPress={() => setShowPriorityModal(true)}>
            <Text style={styles.filterChipText}>Priority: {selectedPriorityFilter}</Text>
            <Feather name="chevron-down" size={12} color="#64748B" style={{ marginLeft: 2 }} />
          </TouchableOpacity>

          <TouchableOpacity style={styles.filterChip} onPress={() => setShowCategoryModal(true)}>
            <Text style={styles.filterChipText}>Category: {selectedCategoryFilter}</Text>
            <Feather name="chevron-down" size={12} color="#64748B" style={{ marginLeft: 2 }} />
          </TouchableOpacity>

          <TouchableOpacity style={styles.filterChip} onPress={() => setShowGoalModal(true)}>
            <Text style={styles.filterChipText}>Goal: {selectedGoalFilter}</Text>
            <Feather name="chevron-down" size={12} color="#64748B" style={{ marginLeft: 2 }} />
          </TouchableOpacity>

          <TouchableOpacity style={styles.sortChip} onPress={() => setShowSortModal(true)}>
            <MaterialCommunityIcons name="sort" size={13} color="#64748B" style={{ marginRight: 3 }} />
            <Text style={styles.filterChipText}>Sort by: </Text>
            <Text style={[styles.filterChipText, { fontWeight: '700', color: '#0F172A' }]}>{sortBy}</Text>
          </TouchableOpacity>
        </ScrollView>

        {/* Today's Progress Card */}
        <View style={styles.progressCard}>
          <View style={styles.progressCircle}>
            <Text style={styles.progressNum}>{todayDone}</Text>
            <Text style={styles.progressDen}>/{todayTotal}</Text>
          </View>
          <View style={styles.progressTextWrap}>
            <Text style={styles.progressTitle}>Today's Tasks</Text>
            <Text style={styles.progressSub}>
              {remaining > 0
                ? `You're doing great! ${remaining} more to go.`
                : 'All done! Great work today! 🎉'}
            </Text>
          </View>
          <TouchableOpacity onPress={() => navigation.navigate('Insights')}>
            <Text style={styles.insightsLink}>View insights →</Text>
          </TouchableOpacity>
        </View>

        {/* Priority Sections */}
        {priorities.map((priority) => {
          const tasks = filteredTasks.filter((t) => t.priority === priority && !t.completed);
          if (tasks.length === 0) return null;
          const cfg = PRIORITY_CONFIG[priority] || PRIORITY_CONFIG.Medium;
          const isCollapsed = collapsedSections.has(priority);
          const dotColor = priority === 'High' ? '#DC2626' : priority === 'Medium' ? '#D97706' : '#2563EB';
          return (
            <View key={priority} style={styles.section}>
              <TouchableOpacity style={styles.sectionHeader} onPress={() => toggleSection(priority)}>
                <View style={styles.sectionLeft}>
                  <View style={[styles.priorityDot, { backgroundColor: dotColor }]} />
                  <Text style={styles.sectionTitle}>{cfg.label}</Text>
                </View>
                <View style={styles.sectionRight}>
                  <Text style={styles.taskCount}>{tasks.length} tasks</Text>
                  <Feather name={isCollapsed ? 'chevron-down' : 'chevron-up'} size={16} color="#94A3B8" style={{ marginLeft: 6 }} />
                </View>
              </TouchableOpacity>
              {!isCollapsed && tasks.map(renderTask)}
            </View>
          );
        })}

        {/* Completed Section */}
        {completedTasks.length > 0 && (activeTab === 'All' || activeTab === 'Completed') && (
          <View style={styles.section}>
            <TouchableOpacity style={styles.sectionHeader} onPress={() => toggleSection('completed')}>
              <View style={styles.sectionLeft}>
                <View style={styles.completedDot}>
                  <Feather name="check" size={10} color="#FFFFFF" />
                </View>
                <Text style={styles.sectionTitle}>Completed</Text>
              </View>
              <View style={styles.sectionRight}>
                <Text style={styles.taskCount}>{completedTasks.length} tasks</Text>
                <Feather name={collapsedSections.has('completed') ? 'chevron-down' : 'chevron-up'} size={16} color="#94A3B8" style={{ marginLeft: 6 }} />
              </View>
            </TouchableOpacity>
            {!collapsedSections.has('completed') && completedTasks.map(renderTask)}
          </View>
        )}

        {/* AI Banner */}
        {showAiBanner && (
          <View style={styles.aiBanner}>
            <View style={styles.aiBannerLeft}>
              <View style={styles.aiIconBox}>
                <Ionicons name="sparkles" size={15} color="#7C3AED" />
              </View>
              <View style={styles.aiBannerText}>
                <Text style={styles.aiBannerTitle}>Break big tasks into smaller steps</Text>
                <Text style={styles.aiBannerSub}>
                  Want me to break down "Finish UI Design" into smaller tasks?
                </Text>
              </View>
            </View>
            <TouchableOpacity onPress={() => navigation.navigate('Ai')}>
              <Text style={styles.askLivo}>Ask LIVO</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.closeBanner} onPress={() => setShowAiBanner(false)}>
              <Feather name="x" size={14} color="#94A3B8" />
            </TouchableOpacity>
          </View>
        )}

        <View style={{ height: 24 }} />
      </ScrollView>

      {/* Filter Modals */}
      <SelectionModal
        visible={showPriorityModal}
        onClose={() => setShowPriorityModal(false)}
        title="Filter by Priority"
        options={[
          { label: 'All Priorities', value: 'All' },
          { label: 'High Priority', value: 'High' },
          { label: 'Medium Priority', value: 'Medium' },
          { label: 'Low Priority', value: 'Low' },
        ]}
        selectedValue={selectedPriorityFilter}
        onSelect={setSelectedPriorityFilter}
      />

      <SelectionModal
        visible={showCategoryModal}
        onClose={() => setShowCategoryModal(false)}
        title="Filter by Category"
        options={[
          { label: 'All Categories', value: 'All', icon: 'grid' },
          { label: 'Work', value: 'Work', icon: 'briefcase' },
          { label: 'Personal', value: 'Personal', icon: 'user' },
          { label: 'Learning', value: 'Learning', icon: 'book' },
          { label: 'Health', value: 'Health', icon: 'heart' },
        ]}
        selectedValue={selectedCategoryFilter}
        onSelect={setSelectedCategoryFilter}
      />

      <SelectionModal
        visible={showGoalModal}
        onClose={() => setShowGoalModal(false)}
        title="Filter by Goal"
        options={[
          { label: 'All Goals', value: 'All', icon: 'target' },
          { label: 'Build a strong portfolio', value: 'Build a strong portfolio', icon: 'disc' },
          { label: 'Learn React Native', value: 'Learn React Native', icon: 'disc' },
        ]}
        selectedValue={selectedGoalFilter}
        onSelect={setSelectedGoalFilter}
      />

      <SelectionModal
        visible={showSortModal}
        onClose={() => setShowSortModal(false)}
        title="Sort Tasks"
        options={[
          { label: 'Due Date', value: 'Due Date', icon: 'calendar' },
          { label: 'Priority', value: 'Priority', icon: 'alert-circle' },
          { label: 'Alphabetical', value: 'Alphabetical', icon: 'type' },
        ]}
        selectedValue={sortBy}
        onSelect={setSortBy}
      />

      {/* Task Options Bottom Sheet Modal */}
      <Modal
        visible={isActionModalOpen}
        transparent
        animationType="slide"
        onRequestClose={() => setIsActionModalOpen(false)}
      >
        <TouchableWithoutFeedback onPress={() => setIsActionModalOpen(false)}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>
        <View style={styles.taskOptionsModalContent}>
          {/* Header */}
          <View style={styles.taskOptionsHeader}>
            <View style={styles.taskOptionsHeaderTop}>
              <Text style={styles.taskOptionsTitle}>Task Options</Text>
              <TouchableOpacity
                onPress={() => setIsActionModalOpen(false)}
                hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
              >
                <Feather name="x" size={20} color="#64748B" />
              </TouchableOpacity>
            </View>
            {selectedTaskForAction && (
              <View style={styles.taskOptionsSubtitleRow}>
                <Text style={styles.taskOptionsTaskTitle} numberOfLines={1}>
                  {selectedTaskForAction.title}
                </Text>
                <View style={[styles.taskOptionsBadge, { backgroundColor: selectedTaskForAction.completed ? '#DCFCE7' : '#F1F5F9' }]}>
                  <Text style={[styles.taskOptionsBadgeText, { color: selectedTaskForAction.completed ? '#16A34A' : '#64748B' }]}>
                    {selectedTaskForAction.completed ? 'Completed' : `${selectedTaskForAction.priority} Priority`}
                  </Text>
                </View>
              </View>
            )}
          </View>

          <View style={styles.taskOptionsDivider} />

          {/* Options List */}
          <View style={styles.taskOptionsList}>
            {/* 1. Edit Task */}
            <TouchableOpacity style={styles.taskOptionRow} onPress={handleEditTask} activeOpacity={0.7}>
              <View style={[styles.taskOptionIconBox, { backgroundColor: '#EBF3FF' }]}>
                <Feather name="edit-2" size={16} color="#2563EB" />
              </View>
              <View style={styles.taskOptionTextCol}>
                <Text style={styles.taskOptionMainText}>Edit Task</Text>
                <Text style={styles.taskOptionSubText}>Update details, title, or category</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>

            {/* 2. Mark as Completed / Incomplete */}
            <TouchableOpacity style={styles.taskOptionRow} onPress={handleToggleComplete} activeOpacity={0.7}>
              <View style={[styles.taskOptionIconBox, { backgroundColor: selectedTaskForAction?.completed ? '#F1F5F9' : '#DCFCE7' }]}>
                <Feather
                  name={selectedTaskForAction?.completed ? 'rotate-ccw' : 'check-circle'}
                  size={16}
                  color={selectedTaskForAction?.completed ? '#64748B' : '#16A34A'}
                />
              </View>
              <View style={styles.taskOptionTextCol}>
                <Text style={styles.taskOptionMainText}>
                  {selectedTaskForAction?.completed ? 'Mark as Incomplete' : 'Mark as Completed'}
                </Text>
                <Text style={styles.taskOptionSubText}>
                  {selectedTaskForAction?.completed ? 'Reopen this task' : 'Move to completed section'}
                </Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>

            {/* 3. Change Date & Time */}
            <TouchableOpacity
              style={styles.taskOptionRow}
              onPress={() => {
                setIsActionModalOpen(false);
                setTimeout(() => setShowChangeDateModal(true), 250);
              }}
              activeOpacity={0.7}
            >
              <View style={[styles.taskOptionIconBox, { backgroundColor: '#FEF3C7' }]}>
                <Feather name="calendar" size={16} color="#D97706" />
              </View>
              <View style={styles.taskOptionTextCol}>
                <Text style={styles.taskOptionMainText}>Change Date & Time</Text>
                <Text style={styles.taskOptionSubText}>Reschedule to Today, Tomorrow, or pick date</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>

            {/* 4. Change Priority */}
            <TouchableOpacity
              style={styles.taskOptionRow}
              onPress={() => {
                setIsActionModalOpen(false);
                setTimeout(() => setShowChangePriorityModal(true), 250);
              }}
              activeOpacity={0.7}
            >
              <View style={[styles.taskOptionIconBox, { backgroundColor: '#F3E8FF' }]}>
                <Feather name="flag" size={16} color="#7C3AED" />
              </View>
              <View style={styles.taskOptionTextCol}>
                <Text style={styles.taskOptionMainText}>Change Priority</Text>
                <Text style={styles.taskOptionSubText}>Set to High, Medium, or Low</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>

            {/* 5. Duplicate Task */}
            <TouchableOpacity style={styles.taskOptionRow} onPress={handleDuplicateTask} activeOpacity={0.7}>
              <View style={[styles.taskOptionIconBox, { backgroundColor: '#E0F2FE' }]}>
                <Feather name="copy" size={16} color="#0284C7" />
              </View>
              <View style={styles.taskOptionTextCol}>
                <Text style={styles.taskOptionMainText}>Duplicate Task</Text>
                <Text style={styles.taskOptionSubText}>Create a copy of this task</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>

            {/* 6. Delete Task */}
            <TouchableOpacity style={[styles.taskOptionRow, { borderBottomWidth: 0 }]} onPress={handleDeleteTask} activeOpacity={0.7}>
              <View style={[styles.taskOptionIconBox, { backgroundColor: '#FEE2E2' }]}>
                <Feather name="trash-2" size={16} color="#DC2626" />
              </View>
              <View style={styles.taskOptionTextCol}>
                <Text style={[styles.taskOptionMainText, { color: '#DC2626' }]}>Delete Task</Text>
                <Text style={styles.taskOptionSubText}>Permanently remove this task</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#DC2626" />
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* Change Date & Time Sub-Modal */}
      <Modal
        visible={showChangeDateModal}
        transparent
        animationType="fade"
        onRequestClose={() => setShowChangeDateModal(false)}
      >
        <TouchableWithoutFeedback onPress={() => setShowChangeDateModal(false)}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>
        <View style={styles.dropdownModalContent}>
          <Text style={styles.dropdownModalTitle}>Change Date & Time</Text>
          <TouchableOpacity style={styles.dropdownItem} onPress={() => handleChangeDate('today')}>
            <View style={{ flexDirection: 'row', alignItems: 'center' }}>
              <Feather name="sun" size={16} color="#D97706" style={{ marginRight: 10 }} />
              <Text style={styles.dropdownItemText}>Today</Text>
            </View>
            <Feather name="chevron-right" size={14} color="#94A3B8" />
          </TouchableOpacity>

          <TouchableOpacity style={styles.dropdownItem} onPress={() => handleChangeDate('tomorrow')}>
            <View style={{ flexDirection: 'row', alignItems: 'center' }}>
              <Feather name="sunrise" size={16} color="#2563EB" style={{ marginRight: 10 }} />
              <Text style={styles.dropdownItemText}>Tomorrow</Text>
            </View>
            <Feather name="chevron-right" size={14} color="#94A3B8" />
          </TouchableOpacity>

          <TouchableOpacity style={styles.dropdownItem} onPress={() => handleChangeDate('nextWeek')}>
            <View style={{ flexDirection: 'row', alignItems: 'center' }}>
              <Feather name="calendar" size={16} color="#16A34A" style={{ marginRight: 10 }} />
              <Text style={styles.dropdownItemText}>Next Week (+7 days)</Text>
            </View>
            <Feather name="chevron-right" size={14} color="#94A3B8" />
          </TouchableOpacity>

          <TouchableOpacity style={[styles.dropdownItem, { borderBottomWidth: 0 }]} onPress={() => handleChangeDate('custom')}>
            <View style={{ flexDirection: 'row', alignItems: 'center' }}>
              <Feather name="clock" size={16} color="#7C3AED" style={{ marginRight: 10 }} />
              <Text style={[styles.dropdownItemText, { color: '#7C3AED', fontWeight: '700' }]}>Pick Custom Date / Time</Text>
            </View>
            <Feather name="chevron-right" size={14} color="#7C3AED" />
          </TouchableOpacity>
        </View>
      </Modal>

      {/* Change Priority Sub-Modal */}
      <Modal
        visible={showChangePriorityModal}
        transparent
        animationType="fade"
        onRequestClose={() => setShowChangePriorityModal(false)}
      >
        <TouchableWithoutFeedback onPress={() => setShowChangePriorityModal(false)}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>
        <View style={styles.dropdownModalContent}>
          <Text style={styles.dropdownModalTitle}>Change Priority</Text>
          <TouchableOpacity style={styles.dropdownItem} onPress={() => handleChangePriority('High')}>
            <View style={{ flexDirection: 'row', alignItems: 'center' }}>
              <View style={{ width: 10, height: 10, borderRadius: 5, backgroundColor: '#EF4444', marginRight: 10 }} />
              <Text style={styles.dropdownItemText}>High Priority</Text>
            </View>
            {selectedTaskForAction?.priority === 'High' && <Feather name="check" size={16} color="#EF4444" />}
          </TouchableOpacity>

          <TouchableOpacity style={styles.dropdownItem} onPress={() => handleChangePriority('Medium')}>
            <View style={{ flexDirection: 'row', alignItems: 'center' }}>
              <View style={{ width: 10, height: 10, borderRadius: 5, backgroundColor: '#D97706', marginRight: 10 }} />
              <Text style={styles.dropdownItemText}>Medium Priority</Text>
            </View>
            {selectedTaskForAction?.priority === 'Medium' && <Feather name="check" size={16} color="#D97706" />}
          </TouchableOpacity>

          <TouchableOpacity style={[styles.dropdownItem, { borderBottomWidth: 0 }]} onPress={() => handleChangePriority('Low')}>
            <View style={{ flexDirection: 'row', alignItems: 'center' }}>
              <View style={{ width: 10, height: 10, borderRadius: 5, backgroundColor: '#2563EB', marginRight: 10 }} />
              <Text style={styles.dropdownItemText}>Low Priority</Text>
            </View>
            {selectedTaskForAction?.priority === 'Low' && <Feather name="check" size={16} color="#2563EB" />}
          </TouchableOpacity>
        </View>
      </Modal>
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
    backgroundColor: '#F8FAF5',
  },
  contentContainer: {
    paddingHorizontal: 16,
    paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) + 10 : 16,
    paddingBottom: 24,
  },

  /* Header */
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  logoWrap: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  backBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 6,
    marginLeft: -4,
  },
  logoImage: {
    width: 77,
    height: 32,
  },
  logoSub: {
    fontSize: 7.5,
    fontWeight: '700',
    color: '#94A3B8',
    letterSpacing: 1.3,
    marginTop: 1,
    marginLeft: 5,
  },
  headerActions: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  iconBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#F1F5F9',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 8,
    position: 'relative',
  },
  notifDot: {
    position: 'absolute',
    top: 8,
    right: 8,
    width: 7,
    height: 7,
    borderRadius: 3.5,
    backgroundColor: '#EF4444',
  },
  avatarCircle: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#E2F7C5',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 10,
  },
  avatarText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#2D6A00',
  },

  /* Title Section */
  titleSection: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 14,
  },
  titleLeft: {
    flex: 1,
  },
  mainTitle: {
    fontSize: 28,
    fontWeight: '900',
    color: '#0F172A',
  },
  mainSubtitle: {
    fontSize: 13,
    color: '#64748B',
    marginTop: 2,
  },
  ovalBadge: {
    backgroundColor: '#E2F7C5',
    borderRadius: 40,
    paddingHorizontal: 16,
    paddingVertical: 8,
    alignItems: 'center',
    transform: [{ rotate: '-3deg' }],
  },
  ovalLine: {
    fontSize: 12,
    fontWeight: '700',
    color: '#2D6A00',
    lineHeight: 15,
  },
  pencilIcon: {
    marginTop: 2,
  },

  /* Filter Tabs */
  tabsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  tabsScroll: {
    flexDirection: 'row',
  },
  tabBtn: {
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 20,
    marginRight: 6,
    backgroundColor: 'transparent',
  },
  tabBtnActive: {
    backgroundColor: '#0F172A',
  },
  tabText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#64748B',
  },
  tabTextActive: {
    color: '#FFFFFF',
  },
  addTaskBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#66C400',
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 20,
    marginLeft: 6,
  },
  addTaskText: {
    fontSize: 12.5,
    fontWeight: '700',
    color: '#FFFFFF',
  },

  /* Filter Scroll */
  filterScrollView: {
    marginBottom: 14,
  },
  filterRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  filterChip: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 16,
    paddingHorizontal: 10,
    paddingVertical: 5,
    marginRight: 6,
  },
  sortChip: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 16,
    paddingHorizontal: 10,
    paddingVertical: 5,
    marginRight: 6,
  },
  filterChipText: {
    fontSize: 12,
    color: '#64748B',
  },

  /* Progress Card */
  progressCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 16,
  },
  progressCircle: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: '#E2F7C5',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  progressNum: {
    fontSize: 16,
    fontWeight: '900',
    color: '#2D6A00',
  },
  progressDen: {
    fontSize: 12,
    fontWeight: '700',
    color: '#66C400',
  },
  progressTextWrap: {
    flex: 1,
  },
  progressTitle: {
    fontSize: 13.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  progressSub: {
    fontSize: 11.5,
    color: '#64748B',
    marginTop: 2,
  },
  insightsLink: {
    fontSize: 11.5,
    fontWeight: '700',
    color: '#66C400',
  },

  /* Sections */
  section: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 12,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  sectionLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  priorityDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 8,
  },
  completedDot: {
    width: 14,
    height: 14,
    borderRadius: 7,
    backgroundColor: '#66C400',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  sectionTitle: {
    fontSize: 13.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  sectionRight: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  taskCount: {
    fontSize: 11.5,
    color: '#94A3B8',
  },

  /* Task Rows */
  taskRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 9,
    borderTopWidth: 1,
    borderTopColor: '#F1F5F9',
  },
  checkbox: {
    width: 18,
    height: 18,
    borderRadius: 5,
    borderWidth: 1.5,
    borderColor: '#CBD5E1',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  checkboxChecked: {
    backgroundColor: '#66C400',
    borderColor: '#66C400',
  },
  taskInfo: {
    flex: 1,
    marginRight: 8,
  },
  taskTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
  },
  taskTitleDone: {
    textDecorationLine: 'line-through',
    color: '#94A3B8',
  },
  taskMeta: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 2,
  },
  taskTime: {
    fontSize: 11,
    color: '#94A3B8',
  },
  tagBadge: {
    paddingHorizontal: 7,
    paddingVertical: 3,
    borderRadius: 6,
    marginRight: 6,
  },
  tagText: {
    fontSize: 10.5,
    fontWeight: '700',
  },
  priorityBadge: {
    paddingHorizontal: 7,
    paddingVertical: 3,
    borderRadius: 6,
    marginRight: 4,
  },
  priorityText: {
    fontSize: 10.5,
    fontWeight: '700',
  },
  moreBtn: {
    padding: 2,
  },

  /* AI Banner */
  aiBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#F5EFFF',
    borderRadius: 16,
    padding: 12,
    borderWidth: 1,
    borderColor: '#EDE9FE',
    marginTop: 4,
  },
  aiBannerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    marginRight: 8,
  },
  aiIconBox: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: '#E9D5FF',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  aiBannerText: {
    flex: 1,
  },
  aiBannerTitle: {
    fontSize: 12.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  aiBannerSub: {
    fontSize: 11,
    color: '#64748B',
    marginTop: 1,
  },
  askLivo: {
    fontSize: 11.5,
    fontWeight: '700',
    color: '#7C3AED',
    paddingHorizontal: 6,
    paddingVertical: 4,
  },
  closeBanner: {
    padding: 4,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.45)',
  },
  dropdownModalContent: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 16,
    marginHorizontal: 30,
    marginTop: 'auto',
    marginBottom: 'auto',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 10,
    elevation: 6,
  },
  dropdownModalTitle: {
    fontSize: 15,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 12,
  },
  dropdownItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 11,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  dropdownItemText: {
    fontSize: 13.5,
    color: '#475569',
    fontWeight: '600',
  },
  taskOptionsModalContent: {
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: Platform.OS === 'ios' ? 36 : 24,
    marginTop: 'auto',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -4 },
    shadowOpacity: 0.12,
    shadowRadius: 12,
    elevation: 10,
  },
  taskOptionsHeader: {
    marginBottom: 12,
  },
  taskOptionsHeaderTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
  },
  taskOptionsTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#0F172A',
  },
  taskOptionsSubtitleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  taskOptionsTaskTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#64748B',
    flex: 1,
    marginRight: 10,
  },
  taskOptionsBadge: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 8,
  },
  taskOptionsBadgeText: {
    fontSize: 11,
    fontWeight: '700',
  },
  taskOptionsDivider: {
    height: 1,
    backgroundColor: '#F1F5F9',
    marginBottom: 8,
  },
  taskOptionsList: {
    gap: 2,
  },
  taskOptionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 11,
    borderBottomWidth: 1,
    borderBottomColor: '#F8FAFC',
  },
  taskOptionIconBox: {
    width: 36,
    height: 36,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 14,
  },
  taskOptionTextCol: {
    flex: 1,
  },
  taskOptionMainText: {
    fontSize: 14.5,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 2,
  },
  taskOptionSubText: {
    fontSize: 12,
    color: '#94A3B8',
  },
});
