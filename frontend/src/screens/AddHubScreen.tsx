import React, { useState, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  Platform,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { useFocusEffect } from '@react-navigation/native';
import { AddOptainsScreen } from './AddOptainsScreen';
import { AddTaskScreen } from './AddTaskScreen';

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
  const [currentView, setCurrentView] = useState<'hub' | 'options' | 'addTask'>('hub');
  const [activeTab, setActiveTab] = useState<FilterTab>('All');
  const [checkedTasks, setCheckedTasks] = useState<Set<string>>(new Set(['6']));
  const [collapsedSections, setCollapsedSections] = useState<Set<string>>(new Set());
  const [showAiBanner, setShowAiBanner] = useState(true);

  useFocusEffect(
    useCallback(() => {
      setCurrentView('hub');
    }, [])
  );

  if (currentView === 'options') {
    return <AddOptainsScreen onBack={() => setCurrentView('hub')} />;
  }

  if (currentView === 'addTask') {
    return <AddTaskScreen onBack={() => setCurrentView('hub')} />;
  }

  const toggleCheck = (id: string) => {
    setCheckedTasks(prev => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  };

  const toggleSection = (key: string) => {
    setCollapsedSections(prev => {
      const next = new Set(prev);
      next.has(key) ? next.delete(key) : next.add(key);
      return next;
    });
  };

  const filteredTasks = TASKS.filter(t => {
    if (activeTab === 'All') return true;
    if (activeTab === 'Completed') return checkedTasks.has(t.id);
    if (activeTab === 'Today') return t.time.startsWith('Today');
    if (activeTab === 'Upcoming') return t.time.startsWith('Tomorrow') || t.time.startsWith('Sun');
    return true;
  });

  const completedTasks = TASKS.filter(t => checkedTasks.has(t.id));
  const todayTotal = TASKS.filter(t => t.time.startsWith('Today')).length;
  const todayDone = TASKS.filter(t => t.time.startsWith('Today') && checkedTasks.has(t.id)).length;
  const remaining = todayTotal - todayDone;

  const priorities: Priority[] = ['High', 'Medium', 'Low'];

  const renderTask = (task: Task) => {
    const isChecked = checkedTasks.has(task.id);
    const cfg = PRIORITY_CONFIG[task.priority];
    return (
      <View key={task.id} style={styles.taskRow}>
        <TouchableOpacity
          onPress={() => toggleCheck(task.id)}
          style={[styles.checkbox, isChecked && styles.checkboxChecked]}
        >
          {isChecked && <Feather name="check" size={11} color="#FFFFFF" />}
        </TouchableOpacity>
        <View style={styles.taskInfo}>
          <Text style={[styles.taskTitle, isChecked && styles.taskTitleDone]}>{task.title}</Text>
          <View style={styles.taskMeta}>
            {task.icon === 'clock'
              ? <Ionicons name="time-outline" size={11} color="#94A3B8" style={{ marginRight: 3 }} />
              : <Feather name="calendar" size={11} color="#94A3B8" style={{ marginRight: 3 }} />}
            <Text style={styles.taskTime}>{task.time}</Text>
            {task.meetLink && (
              <>
                <MaterialCommunityIcons name="google" size={11} color="#94A3B8" style={{ marginLeft: 6, marginRight: 2 }} />
                <Text style={styles.taskTime}>{task.meetLink}</Text>
              </>
            )}
          </View>
        </View>
        <View style={[styles.tagBadge, { backgroundColor: task.tagBg }]}>
          <Text style={[styles.tagText, { color: task.tagColor }]}>{task.tag}</Text>
        </View>
        {!isChecked && (
          <View style={[styles.priorityBadge, { backgroundColor: cfg.bg }]}>
            <Text style={[styles.priorityText, { color: cfg.color }]}>{task.priority}</Text>
          </View>
        )}
        <TouchableOpacity style={styles.moreBtn}>
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
            <Text style={styles.logoText}>LIVO</Text>
            <View style={styles.logoDot} />
            <Text style={styles.logoSub}>  A BETTER YOU</Text>
          </View>
          <View style={styles.headerActions}>
            <TouchableOpacity style={styles.iconBtn}>
              <Feather name="search" size={20} color="#0F172A" />
            </TouchableOpacity>
            <TouchableOpacity style={styles.iconBtn}>
              <Ionicons name="notifications-outline" size={20} color="#0F172A" />
              <View style={styles.notifDot} />
            </TouchableOpacity>
            <View style={styles.avatarCircle}>
              <Text style={styles.avatarText}>R</Text>
            </View>
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
            {(['All', 'Today', 'Upcoming', 'Completed'] as FilterTab[]).map(tab => (
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
            onPress={() => setCurrentView('options')}
          >
            <Feather name="plus" size={14} color="#FFFFFF" style={{ marginRight: 4 }} />
            <Text style={styles.addTaskText}>Add Task</Text>
          </TouchableOpacity>
        </View>

        {/* Secondary Filters */}
        <View style={styles.filterRow}>
          {['Priority', 'Category', 'Goal'].map(f => (
            <TouchableOpacity key={f} style={styles.filterChip}>
              <Text style={styles.filterChipText}>{f}</Text>
              <Feather name="chevron-down" size={12} color="#64748B" style={{ marginLeft: 2 }} />
            </TouchableOpacity>
          ))}
          <View style={styles.sortChip}>
            <MaterialCommunityIcons name="sort" size={13} color="#64748B" style={{ marginRight: 3 }} />
            <Text style={styles.filterChipText}>Sort by: </Text>
            <Text style={[styles.filterChipText, { fontWeight: '700', color: '#0F172A' }]}>Due Date</Text>
          </View>
        </View>

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
          <TouchableOpacity>
            <Text style={styles.insightsLink}>View insights →</Text>
          </TouchableOpacity>
        </View>

        {/* Priority Sections */}
        {priorities.map(priority => {
          const tasks = filteredTasks.filter(t => t.priority === priority && !checkedTasks.has(t.id));
          if (tasks.length === 0) return null;
          const cfg = PRIORITY_CONFIG[priority];
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
            <TouchableOpacity>
              <Text style={styles.askLivo}>Ask LIVO</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.closeBanner} onPress={() => setShowAiBanner(false)}>
              <Feather name="x" size={14} color="#94A3B8" />
            </TouchableOpacity>
          </View>
        )}

        <View style={{ height: 24 }} />
      </ScrollView>
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
    paddingBottom: 20,
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
  logoText: {
    fontSize: 22,
    fontWeight: '900',
    color: '#0F172A',
    letterSpacing: 0.5,
  },
  logoDot: {
    width: 7,
    height: 7,
    borderRadius: 4,
    backgroundColor: '#66C400',
    marginLeft: 2,
    marginTop: 4,
  },
  logoSub: {
    fontSize: 9,
    fontWeight: '700',
    color: '#94A3B8',
    letterSpacing: 1.1,
  },
  headerActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  iconBtn: {
    position: 'relative',
    padding: 4,
  },
  notifDot: {
    position: 'absolute',
    top: 4,
    right: 4,
    width: 7,
    height: 7,
    borderRadius: 4,
    backgroundColor: '#EF4444',
    borderWidth: 1,
    borderColor: '#FFFFFF',
  },
  avatarCircle: {
    width: 34,
    height: 34,
    borderRadius: 17,
    backgroundColor: '#E2F7C5',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#2D6A00',
  },

  /* Title */
  titleSection: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 16,
  },
  titleLeft: { flex: 1 },
  mainTitle: {
    fontSize: 30,
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
    paddingHorizontal: 14,
    paddingVertical: 10,
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
    transform: [{ rotate: '-3deg' }],
  },
  ovalLine: {
    fontSize: 12,
    fontWeight: '700',
    color: '#2D6A00',
    lineHeight: 16,
  },
  pencilIcon: {
    position: 'absolute',
    bottom: 5,
    right: 7,
  },

  /* Tabs */
  tabsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
  },
  tabsScroll: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingRight: 8,
  },
  tabBtn: {
    paddingHorizontal: 14,
    paddingVertical: 7,
    borderRadius: 20,
    backgroundColor: '#F1F5F9',
  },
  tabBtnActive: {
    backgroundColor: '#E2F7C5',
  },
  tabText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#64748B',
  },
  tabTextActive: {
    color: '#2D6A00',
    fontWeight: '700',
  },
  addTaskBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#66C400',
    borderRadius: 20,
    paddingHorizontal: 14,
    paddingVertical: 8,
    marginLeft: 'auto',
  },
  addTaskText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#FFFFFF',
  },

  /* Filters */
  filterRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginBottom: 14,
    flexWrap: 'wrap',
  },
  filterChip: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  sortChip: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginLeft: 'auto',
  },
  filterChipText: {
    fontSize: 12,
    color: '#64748B',
    fontWeight: '600',
  },

  /* Progress */
  progressCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 14,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 4,
    elevation: 2,
  },
  progressCircle: {
    width: 44,
    height: 44,
    borderRadius: 22,
    borderWidth: 3,
    borderColor: '#66C400',
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    marginRight: 12,
  },
  progressNum: {
    fontSize: 14,
    fontWeight: '900',
    color: '#0F172A',
  },
  progressDen: {
    fontSize: 11,
    fontWeight: '600',
    color: '#94A3B8',
  },
  progressTextWrap: { flex: 1 },
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
    fontSize: 12,
    fontWeight: '700',
    color: '#66C400',
  },

  /* Sections */
  section: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 12,
    overflow: 'hidden',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.03,
    shadowRadius: 3,
    elevation: 1,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  sectionLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  priorityDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    marginRight: 8,
  },
  completedDot: {
    width: 18,
    height: 18,
    borderRadius: 9,
    backgroundColor: '#66C400',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0F172A',
  },
  sectionRight: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  taskCount: {
    fontSize: 12,
    color: '#94A3B8',
    fontWeight: '600',
  },

  /* Task Row */
  taskRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#F8FAFC',
  },
  checkbox: {
    width: 20,
    height: 20,
    borderRadius: 6,
    borderWidth: 2,
    borderColor: '#CBD5E1',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  checkboxChecked: {
    backgroundColor: '#66C400',
    borderColor: '#66C400',
  },
  taskInfo: { flex: 1 },
  taskTitle: {
    fontSize: 13.5,
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
    marginTop: 3,
  },
  taskTime: {
    fontSize: 11,
    color: '#94A3B8',
  },
  tagBadge: {
    borderRadius: 8,
    paddingHorizontal: 7,
    paddingVertical: 3,
    marginRight: 4,
  },
  tagText: {
    fontSize: 10.5,
    fontWeight: '700',
  },
  priorityBadge: {
    borderRadius: 8,
    paddingHorizontal: 7,
    paddingVertical: 3,
    marginRight: 4,
  },
  priorityText: {
    fontSize: 10.5,
    fontWeight: '700',
  },
  moreBtn: {
    padding: 4,
  },

  /* AI Banner */
  aiBanner: {
    backgroundColor: '#F5F3FF',
    borderRadius: 16,
    padding: 14,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#DDD6FE',
    marginTop: 4,
  },
  aiBannerLeft: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    flex: 1,
    marginRight: 8,
  },
  aiIconBox: {
    width: 30,
    height: 30,
    borderRadius: 15,
    backgroundColor: '#EDE9FE',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  aiBannerText: { flex: 1 },
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
  askLivo: {
    fontSize: 12,
    fontWeight: '700',
    color: '#7C3AED',
    marginRight: 8,
  },
  closeBanner: {
    padding: 4,
  },
});
