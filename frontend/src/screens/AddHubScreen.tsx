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
import { TasksScreen } from './TasksScreen';
import { AiScreen } from './AiScreen';
import { AddTaskScreen } from './AddTaskScreen';
import { AddEventScreen } from './AddEventScreen';
import { AddGoalScreen } from './AddGoalScreen';
import { AddHabitScreen } from './AddHabitScreen';
import { AddExpenseScreen } from './AddExpenseScreen';
import { AddTripScreen } from './AddTripScreen';
import { AddLearningScreen } from './AddLearningScreen';
import { AddHealthScreen } from './AddHealthScreen';

interface AddHubScreenProps {
  onSelectOption?: (option: string) => void;
}

export const AddHubScreen: React.FC<AddHubScreenProps> = ({ onSelectOption }) => {
  const [currentView, setCurrentView] = useState<'hub' | 'tasks' | 'ai' | 'addTask' | 'addEvent' | 'addGoal' | 'addHabit' | 'addExpense' | 'addTrip' | 'addLearning' | 'addHealth'>('hub');
  const [openModalOnTaskView, setOpenModalOnTaskView] = useState(false);

  useFocusEffect(
    useCallback(() => {
      setCurrentView('hub');
    }, [])
  );

  if (currentView === 'addTask') {
    return <AddTaskScreen onBack={() => setCurrentView('hub')} />;
  }

  if (currentView === 'addEvent') {
    return <AddEventScreen onBack={() => setCurrentView('hub')} />;
  }

  if (currentView === 'addGoal') {
    return <AddGoalScreen onBack={() => setCurrentView('hub')} />;
  }

  if (currentView === 'addHabit') {
    return <AddHabitScreen onBack={() => setCurrentView('hub')} />;
  }

  if (currentView === 'addExpense') {
    return <AddExpenseScreen onBack={() => setCurrentView('hub')} />;
  }

  if (currentView === 'addTrip') {
    return <AddTripScreen onBack={() => setCurrentView('hub')} />;
  }

  if (currentView === 'addLearning') {
    return <AddLearningScreen onBack={() => setCurrentView('hub')} />;
  }

  if (currentView === 'addHealth') {
    return <AddHealthScreen onBack={() => setCurrentView('hub')} />;
  }

  if (currentView === 'tasks') {
    return <TasksScreen onBack={() => setCurrentView('hub')} autoOpenAddModal={openModalOnTaskView} />;
  }

  if (currentView === 'ai') {
    return <AiScreen onBack={() => setCurrentView('hub')} />;
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.contentContainer}
        showsVerticalScrollIndicator={false}
      >
        {/* Top Header */}
        <View style={styles.headerRow}>
          <View>
            <View style={styles.logoRow}>
              <Text style={styles.logoText}>LIVO</Text>
              <View style={styles.logoDot} />
            </View>
            <Text style={styles.logoSubtitle}>A BETTER YOU</Text>
          </View>

          <View style={styles.avatarCircle}>
            <Text style={styles.avatarText}>R</Text>
          </View>
        </View>

        {/* Title Section + Handwritten Oval Badge */}
        <View style={styles.titleSection}>
          <View style={styles.titleLeft}>
            <Text style={styles.mainTitle}>Add</Text>
            <Text style={styles.mainSubtitle}>
              Capture today. Build a better tomorrow.
            </Text>
          </View>

          <View style={styles.ovalBadge}>
            <Text style={styles.ovalTextLine1}>Small</Text>
            <Text style={styles.ovalTextLine2}>steps</Text>
            <Text style={styles.ovalTextLine3}>big progress.</Text>
            <Feather name="edit-2" size={12} color="#2D6A00" style={styles.pencilIcon} />
          </View>
        </View>

        {/* AI Prompt Card */}
        <View style={styles.aiCard}>
          <View style={styles.aiLeft}>
            <View style={styles.aiIconCircle}>
              <Ionicons name="sparkles" size={16} color="#7C3AED" />
            </View>
            <View style={styles.aiTextWrap}>
              <Text style={styles.aiTitle}>Not sure what to add?</Text>
              <Text style={styles.aiSub}>
                Tell LIVO what's on your mind, and I'll help you create
              </Text>
            </View>
          </View>

          <TouchableOpacity
            style={styles.tryAiBtn}
            onPress={() => setCurrentView('ai')}
          >
            <Text style={styles.tryAiText}>Try with AI</Text>
            <Feather name="arrow-right" size={13} color="#7C3AED" style={{ marginLeft: 3 }} />
          </TouchableOpacity>
        </View>

        {/* Create New Section */}
        <View style={styles.sectionHeaderRow}>
          <Text style={styles.sectionTitle}>Create New</Text>
          <Text style={styles.sectionSub}>Choose what you want to add</Text>
        </View>

        {/* 2-Column Grid */}
        <View style={styles.gridContainer}>
          {/* Item 1: Task */}
          <TouchableOpacity
            style={styles.gridCard}
            onPress={() => setCurrentView('addTask')}
          >
            <View style={[styles.gridIconBox, { backgroundColor: '#E2F7C5' }]}>
              <Feather name="check-square" size={20} color="#2D6A00" />
            </View>
            <View style={styles.gridTextWrap}>
              <Text style={styles.gridCardTitle}>Task</Text>
              <Text style={styles.gridCardSub} numberOfLines={2}>
                Add a task to get things done
              </Text>
            </View>
            <Feather name="chevron-right" size={16} color="#CBD5E1" />
          </TouchableOpacity>

          {/* Item 2: Event */}
          <TouchableOpacity
            style={styles.gridCard}
            onPress={() => setCurrentView('addEvent')}
          >
            <View style={[styles.gridIconBox, { backgroundColor: '#DBEAFE' }]}>
              <Feather name="calendar" size={20} color="#2563EB" />
            </View>
            <View style={styles.gridTextWrap}>
              <Text style={styles.gridCardTitle}>Event</Text>
              <Text style={styles.gridCardSub} numberOfLines={2}>
                Add a meeting or event
              </Text>
            </View>
            <Feather name="chevron-right" size={16} color="#CBD5E1" />
          </TouchableOpacity>

          {/* Item 3: Goal */}
          <TouchableOpacity
            style={styles.gridCard}
            onPress={() => setCurrentView('addGoal')}
          >
            <View style={[styles.gridIconBox, { backgroundColor: '#F3E8FF' }]}>
              <Ionicons name="disc-outline" size={20} color="#7C3AED" />
            </View>
            <View style={styles.gridTextWrap}>
              <Text style={styles.gridCardTitle}>Goal</Text>
              <Text style={styles.gridCardSub} numberOfLines={2}>
                Set a goal and break it down
              </Text>
            </View>
            <Feather name="chevron-right" size={16} color="#CBD5E1" />
          </TouchableOpacity>

          {/* Item 4: Habit */}
          <TouchableOpacity
            style={styles.gridCard}
            onPress={() => setCurrentView('addHabit')}
          >
            <View style={[styles.gridIconBox, { backgroundColor: '#FFEDD5' }]}>
              <Ionicons name="stats-chart-outline" size={20} color="#C2410C" />
            </View>
            <View style={styles.gridTextWrap}>
              <Text style={styles.gridCardTitle}>Habit</Text>
              <Text style={styles.gridCardSub} numberOfLines={2}>
                Track a habit and build consistency
              </Text>
            </View>
            <Feather name="chevron-right" size={16} color="#CBD5E1" />
          </TouchableOpacity>

          {/* Item 5: Expense */}
          <TouchableOpacity
            style={styles.gridCard}
            onPress={() => setCurrentView('addExpense')}
          >
            <View style={[styles.gridIconBox, { backgroundColor: '#FFE4E6' }]}>
              <Ionicons name="card-outline" size={20} color="#E11D48" />
            </View>
            <View style={styles.gridTextWrap}>
              <Text style={styles.gridCardTitle}>Expense</Text>
              <Text style={styles.gridCardSub} numberOfLines={2}>
                Record your expenses and budget
              </Text>
            </View>
            <Feather name="chevron-right" size={16} color="#CBD5E1" />
          </TouchableOpacity>

          {/* Item 6: Trip */}
          <TouchableOpacity
            style={styles.gridCard}
            onPress={() => setCurrentView('addTrip')}
          >
            <View style={[styles.gridIconBox, { backgroundColor: '#CCFBF1' }]}>
              <Ionicons name="earth-outline" size={20} color="#0D9488" />
            </View>
            <View style={styles.gridTextWrap}>
              <Text style={styles.gridCardTitle}>Trip</Text>
              <Text style={styles.gridCardSub} numberOfLines={2}>
                Plan your trips and itineraries
              </Text>
            </View>
            <Feather name="chevron-right" size={16} color="#CBD5E1" />
          </TouchableOpacity>

          {/* Item 7: Learning */}
          <TouchableOpacity
            style={styles.gridCard}
            onPress={() => setCurrentView('addLearning')}
          >
            <View style={[styles.gridIconBox, { backgroundColor: '#E0E7FF' }]}>
              <Feather name="book-open" size={20} color="#4F46E5" />
            </View>
            <View style={styles.gridTextWrap}>
              <Text style={styles.gridCardTitle}>Learning</Text>
              <Text style={styles.gridCardSub} numberOfLines={2}>
                Add what you want to learn
              </Text>
            </View>
            <Feather name="chevron-right" size={16} color="#CBD5E1" />
          </TouchableOpacity>

          {/* Item 8: Health */}
          <TouchableOpacity
            style={styles.gridCard}
            onPress={() => setCurrentView('addHealth')}
          >
            <View style={[styles.gridIconBox, { backgroundColor: '#FCE7F3' }]}>
              <Ionicons name="heart-outline" size={20} color="#DB2777" />
            </View>
            <View style={styles.gridTextWrap}>
              <Text style={styles.gridCardTitle}>Health</Text>
              <Text style={styles.gridCardSub} numberOfLines={2}>
                Track workouts, water, health
              </Text>
            </View>
            <Feather name="chevron-right" size={16} color="#CBD5E1" />
          </TouchableOpacity>
        </View>

        {/* Quick Add Section */}
        <View style={styles.sectionHeaderRow}>
          <Text style={styles.sectionTitle}>Quick Add</Text>
          <Text style={styles.sectionSub}>Add in a few taps</Text>
        </View>

        <View style={styles.quickAddChipsRow}>
          <TouchableOpacity
            style={[styles.quickChip, { backgroundColor: '#F0FDF4', borderColor: '#DCFCE7' }]}
            onPress={() => setCurrentView('addTask')}
          >
            <Ionicons name="flash-outline" size={14} color="#16A34A" style={{ marginRight: 6 }} />
            <Text style={[styles.quickChipText, { color: '#16A34A' }]}>Quick Task</Text>
          </TouchableOpacity>

          <TouchableOpacity style={[styles.quickChip, { backgroundColor: '#EFF6FF', borderColor: '#DBEAFE' }]}>
            <Feather name="calendar" size={14} color="#2563EB" style={{ marginRight: 6 }} />
            <Text style={[styles.quickChipText, { color: '#2563EB' }]}>Quick Event</Text>
          </TouchableOpacity>

          <TouchableOpacity style={[styles.quickChip, { backgroundColor: '#FEF2F2', borderColor: '#FEE2E2' }]}>
            <Ionicons name="card-outline" size={14} color="#DC2626" style={{ marginRight: 6 }} />
            <Text style={[styles.quickChipText, { color: '#DC2626' }]}>Log Expense</Text>
          </TouchableOpacity>
        </View>

        {/* Voice Note Banner */}
        <View style={styles.bannerCard}>
          <View style={styles.bannerLeft}>
            <View style={[styles.bannerIconBox, { backgroundColor: '#EDE9FE' }]}>
              <Ionicons name="sparkles" size={16} color="#7C3AED" />
            </View>
            <View style={styles.bannerTextWrap}>
              <Text style={styles.bannerTitle}>Add with a voice note</Text>
              <Text style={styles.bannerSub}>Just speak. LIVO will create</Text>
            </View>
          </View>

          <TouchableOpacity style={styles.voiceBtn}>
            <Ionicons name="mic-outline" size={15} color="#7C3AED" style={{ marginRight: 4 }} />
            <Text style={styles.voiceBtnText}>Tap to record</Text>
          </TouchableOpacity>
        </View>

        {/* Scan & Add Banner */}
        <View style={[styles.bannerCard, { backgroundColor: '#F8FAFC', borderColor: '#E2E8F0' }]}>
          <View style={styles.bannerLeft}>
            <View style={[styles.bannerIconBox, { backgroundColor: '#F1F5F9' }]}>
              <Feather name="maximize" size={16} color="#475569" />
            </View>
            <View style={styles.bannerTextWrap}>
              <Text style={styles.bannerTitle}>Scan & Add</Text>
              <Text style={styles.bannerSub}>Add from a photo, document or card</Text>
            </View>
          </View>

          <TouchableOpacity style={styles.scanBtn}>
            <MaterialCommunityIcons name="view-grid-plus-outline" size={15} color="#475569" style={{ marginRight: 4 }} />
            <Text style={styles.scanBtnText}>Scan Now</Text>
          </TouchableOpacity>
        </View>

        <View style={{ height: 30 }} />
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
  avatarCircle: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#E2F7C5',
    alignItems: 'center',
    justifyContent: 'center',
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
    marginBottom: 16,
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

  /* Oval Handwritten Badge */
  ovalBadge: {
    backgroundColor: '#E2F7C5',
    borderRadius: 40,
    paddingHorizontal: 16,
    paddingVertical: 12,
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
    transform: [{ rotate: '-4deg' }],
  },
  ovalTextLine1: {
    fontSize: 12,
    fontFamily: Platform.OS === 'web' ? 'Caveat, cursive' : 'Caveat_700Bold',
    fontWeight: '700',
    color: '#2D6A00',
    lineHeight: 14,
  },
  ovalTextLine2: {
    fontSize: 13,
    fontFamily: Platform.OS === 'web' ? 'Caveat, cursive' : 'Caveat_700Bold',
    fontWeight: '700',
    color: '#2D6A00',
    lineHeight: 15,
  },
  ovalTextLine3: {
    fontSize: 13,
    fontFamily: Platform.OS === 'web' ? 'Caveat, cursive' : 'Caveat_700Bold',
    fontWeight: '700',
    color: '#2D6A00',
    lineHeight: 15,
  },
  pencilIcon: {
    position: 'absolute',
    bottom: 6,
    right: 8,
  },

  /* AI Card */
  aiCard: {
    backgroundColor: '#F3F0FF',
    borderRadius: 18,
    padding: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderWidth: 1,
    borderColor: '#E9D5FF',
    marginBottom: 20,
  },
  aiLeft: {
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
  aiTitle: {
    fontSize: 13.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  aiSub: {
    fontSize: 11,
    color: '#64748B',
    marginTop: 2,
  },
  tryAiBtn: {
    backgroundColor: '#EDE9FE',
    borderRadius: 14,
    paddingHorizontal: 12,
    paddingVertical: 8,
    flexDirection: 'row',
    alignItems: 'center',
  },
  tryAiText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#7C3AED',
  },

  /* Section Header */
  sectionHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'baseline',
    marginBottom: 12,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
  },
  sectionSub: {
    fontSize: 12,
    color: '#94A3B8',
  },

  /* Grid Layout */
  gridContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    marginBottom: 20,
  },
  gridCard: {
    width: '48.5%',
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 12,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 10,
  },
  gridIconBox: {
    width: 36,
    height: 36,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  gridTextWrap: {
    flex: 1,
    marginRight: 2,
  },
  gridCardTitle: {
    fontSize: 13.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  gridCardSub: {
    fontSize: 10.5,
    color: '#94A3B8',
    marginTop: 1,
    lineHeight: 14,
  },

  /* Quick Add Chips */
  quickAddChipsRow: {
    flexDirection: 'row',
    marginBottom: 20,
  },
  quickChip: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 18,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderWidth: 1,
    marginRight: 8,
  },
  quickChipText: {
    fontSize: 12,
    fontWeight: '700',
  },

  /* Banner Cards */
  bannerCard: {
    backgroundColor: '#F5F3FF',
    borderRadius: 18,
    padding: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderWidth: 1,
    borderColor: '#DDD6FE',
    marginBottom: 12,
  },
  bannerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    marginRight: 8,
  },
  bannerIconBox: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  bannerTextWrap: {
    flex: 1,
  },
  bannerTitle: {
    fontSize: 13.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  bannerSub: {
    fontSize: 11,
    color: '#64748B',
    marginTop: 2,
  },
  voiceBtn: {
    backgroundColor: '#EDE9FE',
    borderRadius: 14,
    paddingHorizontal: 12,
    paddingVertical: 8,
    flexDirection: 'row',
    alignItems: 'center',
  },
  voiceBtnText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#7C3AED',
  },
  scanBtn: {
    backgroundColor: '#F1F5F9',
    borderRadius: 14,
    paddingHorizontal: 12,
    paddingVertical: 8,
    flexDirection: 'row',
    alignItems: 'center',
  },
  scanBtnText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#475569',
  },
});
