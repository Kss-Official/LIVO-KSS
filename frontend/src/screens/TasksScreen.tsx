import React, { useState, useMemo } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  Platform,
  Modal,
  TextInput,
  TouchableWithoutFeedback,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { Colors } from '../theme/colors';
import { AddTaskScreen } from './AddTaskScreen';
import { AddOptainsScreen } from './AddOptainsScreen';

export type TaskPriority = 'High' | 'Medium' | 'Low';
export type TaskCategory = 'Work' | 'Learning' | 'Personal' | 'Health' | 'Finance';

export interface TaskItem {
  id: string;
  title: string;
  dueDate: string;
  time?: string;
  isCalendar?: boolean; // true for calendar icon, false for clock icon
  category: TaskCategory;
  priority: TaskPriority;
  completed: boolean;
  integration?: string; // e.g. "Google Meet"
}

interface TasksScreenProps {
  onBack?: () => void;
  autoOpenAddModal?: boolean;
}

export const TasksScreen: React.FC<TasksScreenProps> = ({ onBack, autoOpenAddModal = false }) => {
  const [showAddTaskScreen, setShowAddTaskScreen] = useState(false);
  const [showAddOptainsScreen, setShowAddOptainsScreen] = useState(autoOpenAddModal);
  // Task Data State initialized with exact items from the image mockup
  const [tasks, setTasks] = useState<TaskItem[]>([
    {
      id: '1',
      title: 'Finish UI Design',
      dueDate: 'Today, 2:00 PM',
      isCalendar: false,
      category: 'Work',
      priority: 'High',
      completed: false,
    },
    {
      id: '2',
      title: 'Prepare Client Presentation',
      dueDate: 'Tomorrow, 10:00 AM',
      isCalendar: true,
      category: 'Work',
      priority: 'High',
      completed: false,
    },
    {
      id: '3',
      title: 'Review Project Updates',
      dueDate: 'Today, 5:00 PM',
      isCalendar: false,
      category: 'Work',
      priority: 'Medium',
      completed: false,
    },
    {
      id: '4',
      title: 'Read React Chapter',
      dueDate: 'Tomorrow',
      isCalendar: true,
      category: 'Learning',
      priority: 'Medium',
      completed: false,
    },
    {
      id: '5',
      title: 'Plan Weekend Trip',
      dueDate: 'Sun, 8 Sep',
      isCalendar: true,
      category: 'Personal',
      priority: 'Low',
      completed: false,
    },
    {
      id: '6',
      title: 'Team Sync',
      dueDate: 'Today, 11:30 AM',
      isCalendar: false,
      category: 'Work',
      priority: 'Medium',
      completed: true,
      integration: 'Google Meet',
    },
  ]);

  // Tab state: All | Today | Upcoming | Completed
  const [activeTab, setActiveTab] = useState<'All' | 'Today' | 'Upcoming' | 'Completed'>('All');

  // Priority filter state
  const [selectedPriorityFilter, setSelectedPriorityFilter] = useState<string>('All');
  const [showPriorityDropdown, setShowPriorityDropdown] = useState(false);

  // Category filter state
  const [selectedCategoryFilter, setSelectedCategoryFilter] = useState<string>('All');
  const [showCategoryDropdown, setShowCategoryDropdown] = useState(false);

  // Goal filter state
  const [selectedGoalFilter, setSelectedGoalFilter] = useState<string>('All');
  const [showGoalDropdown, setShowGoalDropdown] = useState(false);

  // Sort state
  const [sortBy, setSortBy] = useState<'Due Date' | 'Priority' | 'Title'>('Due Date');
  const [showSortDropdown, setShowSortDropdown] = useState(false);

  // Collapsible sections
  const [collapsedSections, setCollapsedSections] = useState<Record<string, boolean>>({
    High: false,
    Medium: false,
    Low: false,
    Completed: false,
  });

  // AI Suggestion Banner visibility
  const [showAiBanner, setShowAiBanner] = useState(true);

  if (showAddOptainsScreen) {
    return <AddOptainsScreen onBack={() => setShowAddOptainsScreen(false)} />;
  }

  if (showAddTaskScreen) {
    return (
      <AddTaskScreen
        onBack={() => setShowAddTaskScreen(false)}
        onCreateTask={(createdTask: any) => {
          if (createdTask?.title) {
            const newTaskItem: TaskItem = {
              id: Date.now().toString(),
              title: createdTask.title,
              dueDate: createdTask.date ? `${createdTask.date}, ${createdTask.time || ''}` : 'Today',
              isCalendar: false,
              category: (createdTask.category as TaskCategory) || 'Work',
              priority: (createdTask.priority as TaskPriority) || 'High',
              completed: false,
            };
            setTasks((prev) => [newTaskItem, ...prev]);
          }
          setShowAddTaskScreen(false);
        }}
      />
    );
  }

  // Add Task Modal State
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [newTaskTitle, setNewTaskTitle] = useState('');
  const [newTaskCategory, setNewTaskCategory] = useState<TaskCategory>('Work');
  const [newTaskPriority, setNewTaskPriority] = useState<TaskPriority>('High');
  const [newTaskDueDate, setNewTaskDueDate] = useState('Today, 3:00 PM');

  // Toggle Task Completion
  const toggleTaskCompleted = (id: string) => {
    setTasks((prev) =>
      prev.map((t) => (t.id === id ? { ...t, completed: !t.completed } : t))
    );
  };

  // Toggle Section Collapse
  const toggleSection = (sectionKey: string) => {
    setCollapsedSections((prev) => ({
      ...prev,
      [sectionKey]: !prev[sectionKey],
    }));
  };

  // Add New Task Handler
  const handleAddNewTask = () => {
    if (!newTaskTitle.trim()) return;

    const newTask: TaskItem = {
      id: Date.now().toString(),
      title: newTaskTitle.trim(),
      dueDate: newTaskDueDate || 'Today',
      isCalendar: newTaskDueDate.toLowerCase().includes('tomorrow') || newTaskDueDate.toLowerCase().includes('sun'),
      category: newTaskCategory,
      priority: newTaskPriority,
      completed: false,
    };

    setTasks((prev) => [newTask, ...prev]);
    setNewTaskTitle('');
    setIsAddModalOpen(false);
  };

  // Filter tasks based on tabs and dropdown filters
  const filteredTasks = useMemo(() => {
    return tasks.filter((task) => {
      // Tab filter
      if (activeTab === 'Today' && !task.dueDate.toLowerCase().includes('today')) return false;
      if (activeTab === 'Upcoming' && task.dueDate.toLowerCase().includes('today')) return false;
      if (activeTab === 'Completed' && !task.completed) return false;

      // Priority filter dropdown
      if (selectedPriorityFilter !== 'All' && task.priority !== selectedPriorityFilter) return false;

      // Category filter dropdown
      if (selectedCategoryFilter !== 'All' && task.category !== selectedCategoryFilter) return false;

      return true;
    });
  }, [tasks, activeTab, selectedPriorityFilter, selectedCategoryFilter]);

  // Group tasks by section
  const highPriorityTasks = filteredTasks.filter((t) => !t.completed && t.priority === 'High');
  const mediumPriorityTasks = filteredTasks.filter((t) => !t.completed && t.priority === 'Medium');
  const lowPriorityTasks = filteredTasks.filter((t) => !t.completed && t.priority === 'Low');
  const completedTasks = filteredTasks.filter((t) => t.completed);

  // Today progress counts
  const todayTasks = tasks.filter((t) => t.dueDate.toLowerCase().includes('today'));
  const totalToday = todayTasks.length || 5;
  const completedToday = todayTasks.filter((t) => t.completed).length || 3;
  const remainingToday = Math.max(0, totalToday - completedToday);

  // Render Category Tag Badge
  const renderCategoryBadge = (category: TaskCategory) => {
    let bg = '#DCFCE7';
    let text = '#166534';

    if (category === 'Learning') {
      bg = '#DBEAFE';
      text = '#1E40AF';
    } else if (category === 'Personal') {
      bg = '#F3E8FF';
      text = '#7E22CE';
    } else if (category === 'Health') {
      bg = '#FCE7F3';
      text = '#9D174D';
    } else if (category === 'Finance') {
      bg = '#FEF3C7';
      text = '#92400E';
    }

    return (
      <View style={[styles.categoryBadge, { backgroundColor: bg }]}>
        <Text style={[styles.categoryBadgeText, { color: text }]}>{category}</Text>
      </View>
    );
  };

  // Render Priority Badge
  const renderPriorityBadge = (priority: TaskPriority) => {
    let bg = '#FEE2E2';
    let text = '#EF4444';

    if (priority === 'Medium') {
      bg = '#EEF2FF';
      text = '#4F46E5';
    } else if (priority === 'Low') {
      bg = '#F1F5F9';
      text = '#64748B';
    }

    return (
      <View style={[styles.priorityTag, { backgroundColor: bg }]}>
        <Text style={[styles.priorityTagText, { color: text }]}>{priority}</Text>
      </View>
    );
  };

  // Render Single Task Item Row
  const renderTaskItem = (task: TaskItem) => {
    return (
      <View key={task.id} style={styles.taskCard}>
        {/* Left Checkbox */}
        <TouchableOpacity
          style={[styles.checkbox, task.completed && styles.checkboxChecked]}
          onPress={() => toggleTaskCompleted(task.id)}
          activeOpacity={0.7}
        >
          {task.completed && <Feather name="check" size={14} color="#FFFFFF" />}
        </TouchableOpacity>

        {/* Middle Content */}
        <View style={styles.taskContent}>
          <Text style={[styles.taskTitle, task.completed && styles.taskTitleCompleted]}>
            {task.title}
          </Text>

          <View style={styles.taskSubRow}>
            <View style={styles.dateTimeWrap}>
              <Feather
                name={task.isCalendar ? 'calendar' : 'clock'}
                size={12}
                color="#94A3B8"
                style={{ marginRight: 4 }}
              />
              <Text style={styles.dateTimeText}>{task.dueDate}</Text>
            </View>

            {renderCategoryBadge(task.category)}

            {task.integration && (
              <View style={styles.integrationWrap}>
                <Feather name="video" size={12} color="#475569" style={{ marginRight: 4 }} />
                <Text style={styles.integrationText}>{task.integration}</Text>
              </View>
            )}
          </View>
        </View>

        {/* Right Actions */}
        <View style={styles.taskRightActions}>
          {!task.completed && renderPriorityBadge(task.priority)}

          <TouchableOpacity style={styles.moreBtn} hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}>
            <Feather name="more-vertical" size={16} color="#94A3B8" />
          </TouchableOpacity>
        </View>
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
        {/* 1. Top Header Row */}
        <View style={styles.headerRow}>
          <View style={{ flexDirection: 'row', alignItems: 'center' }}>
            {onBack && (
              <TouchableOpacity
                onPress={onBack}
                style={{ marginRight: 10, padding: 4 }}
                hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
              >
                <Feather name="arrow-left" size={22} color="#0F172A" />
              </TouchableOpacity>
            )}
            <View>
              <View style={styles.logoRow}>
                <Text style={styles.logoText}>LIVO</Text>
                <View style={styles.logoDot} />
              </View>
              <Text style={styles.logoSubtitle}>A BETTER YOU</Text>
            </View>
          </View>

          <View style={styles.headerRightActions}>
            <TouchableOpacity style={styles.iconBtn}>
              <Feather name="search" size={20} color="#0F172A" />
            </TouchableOpacity>

            <TouchableOpacity style={styles.iconBtn}>
              <Feather name="bell" size={20} color="#0F172A" />
              <View style={styles.notificationBadge} />
            </TouchableOpacity>

            <TouchableOpacity style={styles.avatarCircle}>
              <Text style={styles.avatarText}>R</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* 2. Main Title Row & "Small tasks Big progress" callout graphic */}
        <View style={styles.titleRow}>
          <View style={styles.titleTextColumn}>
            <Text style={styles.pageTitle}>Tasks</Text>
            <Text style={styles.pageSubtitle}>Turn your plans into progress.</Text>
          </View>

          <View style={styles.calloutBanner}>
            <Text style={styles.calloutText1}>Small tasks</Text>
            <Text style={styles.calloutText2}>Big progress.</Text>
            <View style={styles.calloutUnderline} />
          </View>
        </View>

        {/* 3. Filter Tabs & "+ Add Task" Button Row */}
        <View style={styles.tabsRow}>
          <View style={styles.tabContainer}>
            {(['All', 'Today', 'Upcoming', 'Completed'] as const).map((tab) => {
              const isActive = activeTab === tab;
              return (
                <TouchableOpacity
                  key={tab}
                  style={[styles.tabBtn, isActive && styles.tabBtnActive]}
                  onPress={() => setActiveTab(tab)}
                  activeOpacity={0.8}
                >
                  <Text style={[styles.tabText, isActive && styles.tabTextActive]}>
                    {tab}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>

          <TouchableOpacity
            style={styles.addTaskBtn}
            onPress={() => setShowAddOptainsScreen(true)}
            activeOpacity={0.85}
          >
            <Feather name="plus" size={16} color="#FFFFFF" style={{ marginRight: 4 }} />
            <Text style={styles.addTaskBtnText}>Add Task</Text>
          </TouchableOpacity>
        </View>

        {/* 4. Filter & Sort Dropdowns Row */}
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          style={styles.filtersScrollView}
          contentContainerStyle={styles.filtersScrollContent}
        >
          {/* Priority Dropdown */}
          <TouchableOpacity
            style={styles.dropdownChip}
            onPress={() => setShowPriorityDropdown(true)}
          >
            <Text style={styles.dropdownChipText}>
              Priority{selectedPriorityFilter !== 'All' ? `: ${selectedPriorityFilter}` : ''}
            </Text>
            <Feather name="chevron-down" size={14} color="#64748B" style={{ marginLeft: 4 }} />
          </TouchableOpacity>

          {/* Category Dropdown */}
          <TouchableOpacity
            style={styles.dropdownChip}
            onPress={() => setShowCategoryDropdown(true)}
          >
            <Text style={styles.dropdownChipText}>
              Category{selectedCategoryFilter !== 'All' ? `: ${selectedCategoryFilter}` : ''}
            </Text>
            <Feather name="chevron-down" size={14} color="#64748B" style={{ marginLeft: 4 }} />
          </TouchableOpacity>

          {/* Goal Dropdown */}
          <TouchableOpacity
            style={styles.dropdownChip}
            onPress={() => setShowGoalDropdown(true)}
          >
            <Text style={styles.dropdownChipText}>
              Goal{selectedGoalFilter !== 'All' ? `: ${selectedGoalFilter}` : ''}
            </Text>
            <Feather name="chevron-down" size={14} color="#64748B" style={{ marginLeft: 4 }} />
          </TouchableOpacity>

          {/* Sort By Dropdown */}
          <TouchableOpacity
            style={[styles.dropdownChip, styles.sortDropdownChip]}
            onPress={() => setShowSortDropdown(true)}
          >
            <Ionicons name="swap-vertical" size={13} color="#64748B" style={{ marginRight: 4 }} />
            <Text style={styles.dropdownChipText}>Sort by: {sortBy}</Text>
            <Feather name="chevron-down" size={14} color="#64748B" style={{ marginLeft: 4 }} />
          </TouchableOpacity>
        </ScrollView>

        {/* 5. Today's Tasks Progress Card */}
        <View style={styles.progressCard}>
          {/* Circular Ring Progress */}
          <View style={styles.progressRingContainer}>
            <View style={styles.progressRingOuter}>
              <Text style={styles.progressRingText}>
                {completedToday}/{totalToday}
              </Text>
            </View>
          </View>

          {/* Text Summary */}
          <View style={styles.progressCardContent}>
            <Text style={styles.progressCardTitle}>Today's Tasks</Text>
            <Text style={styles.progressCardSubtitle}>
              {remainingToday === 0
                ? "Awesome work! All done for today 🎉"
                : `You're doing great! ${remainingToday} more to go.`}
            </Text>
          </View>

          {/* View Insights Link */}
          <TouchableOpacity style={styles.viewInsightsLink} activeOpacity={0.7}>
            <Text style={styles.viewInsightsText}>View insights</Text>
            <Feather name="arrow-right" size={14} color="#66C400" style={{ marginLeft: 4 }} />
          </TouchableOpacity>
        </View>

        {/* 6. Tasks List Grouped by Priority */}

        {/* HIGH PRIORITY SECTION */}
        {(activeTab === 'All' || activeTab === 'Today' || activeTab === 'Upcoming') && (
          <View style={styles.sectionContainer}>
            <TouchableOpacity
              style={styles.sectionHeader}
              onPress={() => toggleSection('High')}
              activeOpacity={0.7}
            >
              <View style={styles.sectionHeaderLeft}>
                <View style={[styles.sectionIconCircle, { backgroundColor: '#EF4444' }]}>
                  <Text style={styles.sectionIconExclamation}>!</Text>
                </View>
                <Text style={styles.sectionTitle}>High Priority</Text>
                <View style={styles.taskCountBadge}>
                  <Text style={styles.taskCountText}>{highPriorityTasks.length} tasks</Text>
                </View>
              </View>

              <Feather
                name={collapsedSections['High'] ? 'chevron-down' : 'chevron-up'}
                size={18}
                color="#64748B"
              />
            </TouchableOpacity>

            {!collapsedSections['High'] && (
              <View style={styles.sectionBody}>
                {highPriorityTasks.length > 0 ? (
                  highPriorityTasks.map(renderTaskItem)
                ) : (
                  <Text style={styles.emptySectionText}>No high priority tasks</Text>
                )}
              </View>
            )}
          </View>
        )}

        {/* MEDIUM PRIORITY SECTION */}
        {(activeTab === 'All' || activeTab === 'Today' || activeTab === 'Upcoming') && (
          <View style={styles.sectionContainer}>
            <TouchableOpacity
              style={styles.sectionHeader}
              onPress={() => toggleSection('Medium')}
              activeOpacity={0.7}
            >
              <View style={styles.sectionHeaderLeft}>
                <View style={[styles.sectionIconCircle, { backgroundColor: '#F59E0B' }]}>
                  <Text style={styles.sectionIconExclamation}>!</Text>
                </View>
                <Text style={styles.sectionTitle}>Medium Priority</Text>
                <View style={styles.taskCountBadge}>
                  <Text style={styles.taskCountText}>{mediumPriorityTasks.length} tasks</Text>
                </View>
              </View>

              <Feather
                name={collapsedSections['Medium'] ? 'chevron-down' : 'chevron-up'}
                size={18}
                color="#64748B"
              />
            </TouchableOpacity>

            {!collapsedSections['Medium'] && (
              <View style={styles.sectionBody}>
                {mediumPriorityTasks.length > 0 ? (
                  mediumPriorityTasks.map(renderTaskItem)
                ) : (
                  <Text style={styles.emptySectionText}>No medium priority tasks</Text>
                )}
              </View>
            )}
          </View>
        )}

        {/* LOW PRIORITY SECTION */}
        {(activeTab === 'All' || activeTab === 'Today' || activeTab === 'Upcoming') && (
          <View style={styles.sectionContainer}>
            <TouchableOpacity
              style={styles.sectionHeader}
              onPress={() => toggleSection('Low')}
              activeOpacity={0.7}
            >
              <View style={styles.sectionHeaderLeft}>
                <View style={[styles.sectionIconCircle, { backgroundColor: '#64748B' }]}>
                  <Text style={styles.sectionIconExclamation}>!</Text>
                </View>
                <Text style={styles.sectionTitle}>Low Priority</Text>
                <View style={styles.taskCountBadge}>
                  <Text style={styles.taskCountText}>{lowPriorityTasks.length} tasks</Text>
                </View>
              </View>

              <Feather
                name={collapsedSections['Low'] ? 'chevron-down' : 'chevron-up'}
                size={18}
                color="#64748B"
              />
            </TouchableOpacity>

            {!collapsedSections['Low'] && (
              <View style={styles.sectionBody}>
                {lowPriorityTasks.length > 0 ? (
                  lowPriorityTasks.map(renderTaskItem)
                ) : (
                  <Text style={styles.emptySectionText}>No low priority tasks</Text>
                )}
              </View>
            )}
          </View>
        )}

        {/* COMPLETED SECTION */}
        <View style={styles.sectionContainer}>
          <TouchableOpacity
            style={styles.sectionHeader}
            onPress={() => toggleSection('Completed')}
            activeOpacity={0.7}
          >
            <View style={styles.sectionHeaderLeft}>
              <View style={[styles.sectionIconCircle, { backgroundColor: '#66C400' }]}>
                <Feather name="check" size={12} color="#FFFFFF" />
              </View>
              <Text style={styles.sectionTitle}>Completed</Text>
              <View style={styles.taskCountBadge}>
                <Text style={styles.taskCountText}>{completedTasks.length} tasks</Text>
              </View>
            </View>

            <Feather
              name={collapsedSections['Completed'] ? 'chevron-down' : 'chevron-up'}
              size={18}
              color="#64748B"
            />
          </TouchableOpacity>

          {!collapsedSections['Completed'] && (
            <View style={styles.sectionBody}>
              {completedTasks.length > 0 ? (
                completedTasks.map(renderTaskItem)
              ) : (
                <Text style={styles.emptySectionText}>No completed tasks yet</Text>
              )}
            </View>
          )}
        </View>

        {/* 7. Bottom Floating AI Suggestion Banner */}
        {showAiBanner && (
          <View style={styles.aiBannerCard}>
            <TouchableOpacity
              style={styles.aiBannerCloseBtn}
              onPress={() => setShowAiBanner(false)}
              hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
            >
              <Feather name="x" size={14} color="#94A3B8" />
            </TouchableOpacity>

            <View style={styles.aiBannerContent}>
              <View style={styles.aiSparkleIconCircle}>
                <Ionicons name="sparkles" size={16} color="#7C3AED" />
              </View>

              <View style={styles.aiBannerTextWrap}>
                <Text style={styles.aiBannerTitle}>Break big tasks into smaller steps</Text>
                <Text style={styles.aiBannerSub}>
                  Want me to break down "Finish UI Design" into smaller tasks?
                </Text>
              </View>

              <TouchableOpacity style={styles.askLivoBtn} activeOpacity={0.8}>
                <Text style={styles.askLivoText}>Ask LIVO</Text>
              </TouchableOpacity>
            </View>
          </View>
        )}
      </ScrollView>

      {/* 8. Modal: Add Task */}
      <Modal
        visible={isAddModalOpen}
        transparent
        animationType="slide"
        onRequestClose={() => setIsAddModalOpen(false)}
      >
        <TouchableWithoutFeedback onPress={() => setIsAddModalOpen(false)}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>

        <View style={styles.modalContent}>
          <View style={styles.modalHeader}>
            <Text style={styles.modalTitle}>Add New Task</Text>
            <TouchableOpacity onPress={() => setIsAddModalOpen(false)}>
              <Feather name="x" size={20} color="#64748B" />
            </TouchableOpacity>
          </View>

          <Text style={styles.inputLabel}>Task Title</Text>
          <TextInput
            style={styles.modalInput}
            placeholder="e.g. Design wireframes for LIVO"
            placeholderTextColor="#94A3B8"
            value={newTaskTitle}
            onChangeText={setNewTaskTitle}
            autoFocus
          />

          <Text style={styles.inputLabel}>Category</Text>
          <View style={styles.optionsRow}>
            {(['Work', 'Learning', 'Personal', 'Health', 'Finance'] as TaskCategory[]).map((cat) => (
              <TouchableOpacity
                key={cat}
                style={[
                  styles.optionChip,
                  newTaskCategory === cat && styles.optionChipActive,
                ]}
                onPress={() => setNewTaskCategory(cat)}
              >
                <Text
                  style={[
                    styles.optionChipText,
                    newTaskCategory === cat && styles.optionChipTextActive,
                  ]}
                >
                  {cat}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          <Text style={styles.inputLabel}>Priority</Text>
          <View style={styles.optionsRow}>
            {(['High', 'Medium', 'Low'] as TaskPriority[]).map((prio) => (
              <TouchableOpacity
                key={prio}
                style={[
                  styles.optionChip,
                  newTaskPriority === prio && styles.optionChipActivePriority,
                ]}
                onPress={() => setNewTaskPriority(prio)}
              >
                <Text
                  style={[
                    styles.optionChipText,
                    newTaskPriority === prio && styles.optionChipTextActivePriority,
                  ]}
                >
                  {prio}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          <Text style={styles.inputLabel}>Due Date / Time</Text>
          <TextInput
            style={styles.modalInput}
            placeholder="e.g. Today, 4:00 PM"
            placeholderTextColor="#94A3B8"
            value={newTaskDueDate}
            onChangeText={setNewTaskDueDate}
          />

          <TouchableOpacity style={styles.submitTaskBtn} onPress={handleAddNewTask}>
            <Text style={styles.submitTaskText}>Create Task</Text>
          </TouchableOpacity>
        </View>
      </Modal>

      {/* Priority Dropdown Selector Modal */}
      <Modal
        visible={showPriorityDropdown}
        transparent
        animationType="fade"
        onRequestClose={() => setShowPriorityDropdown(false)}
      >
        <TouchableWithoutFeedback onPress={() => setShowPriorityDropdown(false)}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>
        <View style={styles.dropdownModalContent}>
          <Text style={styles.dropdownModalTitle}>Filter by Priority</Text>
          {['All', 'High', 'Medium', 'Low'].map((item) => (
            <TouchableOpacity
              key={item}
              style={styles.dropdownItem}
              onPress={() => {
                setSelectedPriorityFilter(item);
                setShowPriorityDropdown(false);
              }}
            >
              <Text style={[styles.dropdownItemText, selectedPriorityFilter === item && styles.dropdownItemTextSelected]}>
                {item}
              </Text>
              {selectedPriorityFilter === item && <Feather name="check" size={16} color="#66C400" />}
            </TouchableOpacity>
          ))}
        </View>
      </Modal>

      {/* Category Dropdown Selector Modal */}
      <Modal
        visible={showCategoryDropdown}
        transparent
        animationType="fade"
        onRequestClose={() => setShowCategoryDropdown(false)}
      >
        <TouchableWithoutFeedback onPress={() => setShowCategoryDropdown(false)}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>
        <View style={styles.dropdownModalContent}>
          <Text style={styles.dropdownModalTitle}>Filter by Category</Text>
          {['All', 'Work', 'Learning', 'Personal', 'Health', 'Finance'].map((item) => (
            <TouchableOpacity
              key={item}
              style={styles.dropdownItem}
              onPress={() => {
                setSelectedCategoryFilter(item);
                setShowCategoryDropdown(false);
              }}
            >
              <Text style={[styles.dropdownItemText, selectedCategoryFilter === item && styles.dropdownItemTextSelected]}>
                {item}
              </Text>
              {selectedCategoryFilter === item && <Feather name="check" size={16} color="#66C400" />}
            </TouchableOpacity>
          ))}
        </View>
      </Modal>

      {/* Goal Dropdown Selector Modal */}
      <Modal
        visible={showGoalDropdown}
        transparent
        animationType="fade"
        onRequestClose={() => setShowGoalDropdown(false)}
      >
        <TouchableWithoutFeedback onPress={() => setShowGoalDropdown(false)}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>
        <View style={styles.dropdownModalContent}>
          <Text style={styles.dropdownModalTitle}>Filter by Goal</Text>
          {['All', 'Launch Product', 'Fitness Target', 'Skill Mastery'].map((item) => (
            <TouchableOpacity
              key={item}
              style={styles.dropdownItem}
              onPress={() => {
                setSelectedGoalFilter(item);
                setShowGoalDropdown(false);
              }}
            >
              <Text style={[styles.dropdownItemText, selectedGoalFilter === item && styles.dropdownItemTextSelected]}>
                {item}
              </Text>
              {selectedGoalFilter === item && <Feather name="check" size={16} color="#66C400" />}
            </TouchableOpacity>
          ))}
        </View>
      </Modal>

      {/* Sort Dropdown Selector Modal */}
      <Modal
        visible={showSortDropdown}
        transparent
        animationType="fade"
        onRequestClose={() => setShowSortDropdown(false)}
      >
        <TouchableWithoutFeedback onPress={() => setShowSortDropdown(false)}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>
        <View style={styles.dropdownModalContent}>
          <Text style={styles.dropdownModalTitle}>Sort Tasks By</Text>
          {(['Due Date', 'Priority', 'Title'] as const).map((item) => (
            <TouchableOpacity
              key={item}
              style={styles.dropdownItem}
              onPress={() => {
                setSortBy(item);
                setShowSortDropdown(false);
              }}
            >
              <Text style={[styles.dropdownItemText, sortBy === item && styles.dropdownItemTextSelected]}>
                {item}
              </Text>
              {sortBy === item && <Feather name="check" size={16} color="#66C400" />}
            </TouchableOpacity>
          ))}
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
    backgroundColor: '#FAFBFD',
  },
  contentContainer: {
    paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) + 10 : 16,
    paddingHorizontal: 16,
    paddingBottom: 40,
  },

  /* 1. Header */
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  logoRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  logoText: {
    fontSize: 24,
    fontWeight: '900',
    color: '#0F172A',
    letterSpacing: 0.5,
  },
  logoDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#66C400',
    marginLeft: 2,
    marginTop: 6,
  },
  logoSubtitle: {
    fontSize: 9,
    fontWeight: '700',
    color: '#94A3B8',
    letterSpacing: 1.2,
    marginTop: -2,
  },
  headerRightActions: {
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
  notificationBadge: {
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

  /* 2. Main Title Row */
  titleRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  titleTextColumn: {
    flex: 1,
  },
  pageTitle: {
    fontSize: 32,
    fontWeight: '900',
    color: '#0F172A',
    letterSpacing: -0.5,
  },
  pageSubtitle: {
    fontSize: 13.5,
    color: '#64748B',
    marginTop: 2,
  },

  /* Green callout graphic badge */
  calloutBanner: {
    backgroundColor: '#EFF9E8',
    borderRadius: 22,
    paddingHorizontal: 16,
    paddingVertical: 8,
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
    transform: [{ rotate: '-2deg' }],
  },
  calloutText1: {
    fontSize: 16,
    fontFamily: Platform.OS === 'web' ? 'Caveat, cursive' : 'Caveat_700Bold',
    fontWeight: '700',
    color: '#2D6A00',
  },
  calloutText2: {
    fontSize: 17,
    fontFamily: Platform.OS === 'web' ? 'Caveat, cursive' : 'Caveat_700Bold',
    fontWeight: '700',
    color: '#2D6A00',
    marginTop: -4,
  },
  calloutUnderline: {
    height: 2.5,
    width: '85%',
    backgroundColor: '#2D6A00',
    borderRadius: 2,
    marginTop: 0,
  },

  /* 3. Filter Tabs & Add Button Row */
  tabsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  tabContainer: {
    flexDirection: 'row',
    backgroundColor: '#F1F5F9',
    borderRadius: 22,
    padding: 3,
    flex: 1,
    marginRight: 10,
  },
  tabBtn: {
    flex: 1,
    paddingVertical: 7,
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: 18,
  },
  tabBtnActive: {
    backgroundColor: '#E2F7C5',
  },
  tabText: {
    fontSize: 12.5,
    fontWeight: '600',
    color: '#64748B',
  },
  tabTextActive: {
    color: '#2D6A00',
    fontWeight: '800',
  },
  addTaskBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#66C400',
    paddingHorizontal: 14,
    paddingVertical: 9,
    borderRadius: 20,
    shadowColor: '#66C400',
    shadowOffset: { width: 0, height: 3 },
    shadowOpacity: 0.25,
    shadowRadius: 5,
    elevation: 3,
  },
  addTaskBtnText: {
    color: '#FFFFFF',
    fontWeight: '700',
    fontSize: 13,
  },

  /* 4. Dropdowns Row */
  filtersScrollView: {
    marginBottom: 16,
  },
  filtersScrollContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  dropdownChip: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 7,
    marginRight: 8,
  },
  sortDropdownChip: {
    marginLeft: 4,
  },
  dropdownChipText: {
    fontSize: 12.5,
    fontWeight: '600',
    color: '#475569',
  },

  /* 5. Today's Tasks Progress Card */
  progressCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 16,
    borderWidth: 1,
    borderColor: '#F1F5F9',
    marginBottom: 18,
    shadowColor: '#0F172A',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.04,
    shadowRadius: 8,
    elevation: 2,
  },
  progressRingContainer: {
    marginRight: 14,
  },
  progressRingOuter: {
    width: 48,
    height: 48,
    borderRadius: 24,
    borderWidth: 4,
    borderColor: '#66C400',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FFFFFF',
  },
  progressRingText: {
    fontSize: 14,
    fontWeight: '900',
    color: '#0F172A',
  },
  progressCardContent: {
    flex: 1,
  },
  progressCardTitle: {
    fontSize: 15,
    fontWeight: '800',
    color: '#0F172A',
  },
  progressCardSubtitle: {
    fontSize: 12,
    color: '#64748B',
    marginTop: 2,
  },
  viewInsightsLink: {
    flexDirection: 'row',
    alignItems: 'center',
    marginLeft: 6,
  },
  viewInsightsText: {
    fontSize: 12.5,
    fontWeight: '700',
    color: '#66C400',
  },

  /* 6. Section Containers */
  sectionContainer: {
    marginBottom: 16,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderWidth: 1,
    borderColor: '#F1F5F9',
  },
  sectionHeaderLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  sectionIconCircle: {
    width: 20,
    height: 20,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  sectionIconExclamation: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '900',
  },
  sectionTitle: {
    fontSize: 15,
    fontWeight: '800',
    color: '#0F172A',
    marginRight: 10,
  },
  taskCountBadge: {
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingHorizontal: 8,
    paddingVertical: 2,
  },
  taskCountText: {
    fontSize: 11,
    fontWeight: '600',
    color: '#64748B',
  },
  sectionBody: {
    marginTop: 8,
  },
  emptySectionText: {
    fontSize: 12.5,
    color: '#94A3B8',
    fontStyle: 'italic',
    paddingHorizontal: 16,
    paddingVertical: 8,
  },

  /* Task Card Item */
  taskCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    padding: 12,
    marginBottom: 8,
    borderWidth: 1,
    borderColor: '#F1F5F9',
  },
  checkbox: {
    width: 20,
    height: 20,
    borderRadius: 6,
    borderWidth: 1.5,
    borderColor: '#CBD5E1',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  checkboxChecked: {
    backgroundColor: '#66C400',
    borderColor: '#66C400',
  },
  taskContent: {
    flex: 1,
  },
  taskTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: '#0F172A',
  },
  taskTitleCompleted: {
    textDecorationLine: 'line-through',
    color: '#94A3B8',
  },
  taskSubRow: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
    marginTop: 4,
  },
  dateTimeWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    marginRight: 8,
  },
  dateTimeText: {
    fontSize: 11.5,
    color: '#64748B',
  },
  categoryBadge: {
    borderRadius: 6,
    paddingHorizontal: 7,
    paddingVertical: 2,
    marginRight: 8,
  },
  categoryBadgeText: {
    fontSize: 11,
    fontWeight: '700',
  },
  integrationWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F8FAFC',
    borderRadius: 6,
    paddingHorizontal: 6,
    paddingVertical: 2,
  },
  integrationText: {
    fontSize: 11,
    color: '#475569',
    fontWeight: '600',
  },

  taskRightActions: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  priorityTag: {
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 2,
    marginRight: 6,
  },
  priorityTagText: {
    fontSize: 11,
    fontWeight: '800',
  },
  moreBtn: {
    padding: 4,
  },

  /* 7. Bottom AI Banner */
  aiBannerCard: {
    backgroundColor: '#F5EFFF',
    borderRadius: 18,
    padding: 14,
    marginTop: 12,
    borderWidth: 1,
    borderColor: '#EDE9FE',
    position: 'relative',
  },
  aiBannerCloseBtn: {
    position: 'absolute',
    top: 10,
    right: 10,
    zIndex: 10,
  },
  aiBannerContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  aiSparkleIconCircle: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#ECE6FF',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  aiBannerTextWrap: {
    flex: 1,
    paddingRight: 6,
  },
  aiBannerTitle: {
    fontSize: 13.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  aiBannerSub: {
    fontSize: 11.5,
    color: '#64748B',
    marginTop: 2,
  },
  askLivoBtn: {
    backgroundColor: '#ECE6FF',
    borderRadius: 16,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderWidth: 1,
    borderColor: '#DDD6FE',
  },
  askLivoText: {
    fontSize: 12,
    fontWeight: '800',
    color: '#7C3AED',
  },

  /* Modal Styles */
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.4)',
  },
  modalContent: {
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 20,
    maxHeight: '80%',
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#0F172A',
  },
  inputLabel: {
    fontSize: 12.5,
    fontWeight: '700',
    color: '#475569',
    marginBottom: 6,
    marginTop: 10,
  },
  modalInput: {
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 10,
    fontSize: 14,
    color: '#0F172A',
  },
  optionsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginBottom: 4,
  },
  optionChip: {
    backgroundColor: '#F1F5F9',
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 6,
    marginRight: 8,
    marginBottom: 8,
  },
  optionChipActive: {
    backgroundColor: '#DCFCE7',
  },
  optionChipText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#64748B',
  },
  optionChipTextActive: {
    color: '#166534',
    fontWeight: '800',
  },
  optionChipActivePriority: {
    backgroundColor: '#FEE2E2',
  },
  optionChipTextActivePriority: {
    color: '#EF4444',
    fontWeight: '800',
  },
  submitTaskBtn: {
    backgroundColor: '#66C400',
    borderRadius: 14,
    paddingVertical: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 20,
    marginBottom: 10,
  },
  submitTaskText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: '800',
  },

  /* Dropdown Selector Modals */
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
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  dropdownItemText: {
    fontSize: 13.5,
    color: '#475569',
    fontWeight: '600',
  },
  dropdownItemTextSelected: {
    color: '#66C400',
    fontWeight: '800',
  },
});
