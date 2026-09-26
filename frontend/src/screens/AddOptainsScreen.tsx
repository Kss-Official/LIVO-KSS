import React, { useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  Platform,
  Image,
} from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';
import { useFocusEffect, useNavigation } from '@react-navigation/native';
import { useProfile } from '../hooks/useProfile';

interface AddHubScreenProps {
  onBack?: () => void;
  onSelectOption?: (option: string) => void;
}

export const AddOptainsScreen: React.FC<AddHubScreenProps> = ({ onBack, onSelectOption }) => {
  const navigation = useNavigation<any>();
  const { profile, refreshProfile } = useProfile();

  useFocusEffect(
    useCallback(() => {
      refreshProfile?.();
    }, [refreshProfile])
  );

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  const handleNavigate = (screenName: string, params?: any) => {
    if (onSelectOption) {
      onSelectOption(screenName);
    }
    navigation.navigate(screenName, params);
  };

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
          <View style={styles.headerLeftWrap}>
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
              <Text style={styles.logoSubtitle}>A BETTER YOU</Text>
            </View>
          </View>

          <View style={styles.headerRightActions}>
            <TouchableOpacity
              style={styles.iconBtn}
              onPress={() => navigation.navigate('Search')}
              activeOpacity={0.7}
            >
              <Feather name="search" size={18} color="#0F172A" />
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.iconBtn}
              onPress={() => navigation.navigate('Notifications')}
              activeOpacity={0.7}
            >
              <Ionicons name="notifications-outline" size={18} color="#0F172A" />
              <View style={styles.notificationBadge} />
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

        {/* Title Section */}
        <View style={styles.titleSection}>
          <View style={styles.titleLeft}>
            <Text style={styles.mainTitle}>Add</Text>
            <Text style={styles.mainSubtitle}>
              Capture today. Build a better tomorrow.
            </Text>
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
            onPress={() => handleNavigate('Ai')}
            activeOpacity={0.8}
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
            onPress={() => handleNavigate('AddTask')}
            activeOpacity={0.7}
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
            onPress={() => handleNavigate('AddEvent')}
            activeOpacity={0.7}
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
            onPress={() => handleNavigate('AddGoal')}
            activeOpacity={0.7}
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
            onPress={() => handleNavigate('AddHabit')}
            activeOpacity={0.7}
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
            onPress={() => handleNavigate('AddExpense')}
            activeOpacity={0.7}
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
            onPress={() => handleNavigate('AddTrip')}
            activeOpacity={0.7}
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
            onPress={() => handleNavigate('AddLearning')}
            activeOpacity={0.7}
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
            onPress={() => handleNavigate('AddHealth')}
            activeOpacity={0.7}
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

        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          style={styles.quickAddChipsScroll}
          contentContainerStyle={styles.quickAddChipsRow}
        >
          <TouchableOpacity
            style={[styles.quickChip, { backgroundColor: '#F0FDF4', borderColor: '#DCFCE7' }]}
            onPress={() => handleNavigate('AddTask')}
            activeOpacity={0.7}
          >
            <Ionicons name="flash-outline" size={14} color="#16A34A" style={{ marginRight: 6 }} />
            <Text style={[styles.quickChipText, { color: '#16A34A' }]}>Quick Task</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.quickChip, { backgroundColor: '#EFF6FF', borderColor: '#DBEAFE' }]}
            onPress={() => handleNavigate('AddEvent')}
            activeOpacity={0.7}
          >
            <Feather name="calendar" size={14} color="#2563EB" style={{ marginRight: 6 }} />
            <Text style={[styles.quickChipText, { color: '#2563EB' }]}>Quick Event</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.quickChip, { backgroundColor: '#FEF2F2', borderColor: '#FEE2E2' }]}
            onPress={() => handleNavigate('AddExpense')}
            activeOpacity={0.7}
          >
            <Ionicons name="card-outline" size={14} color="#DC2626" style={{ marginRight: 6 }} />
            <Text style={[styles.quickChipText, { color: '#DC2626' }]}>Log Expense</Text>
          </TouchableOpacity>
        </ScrollView>

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

          <TouchableOpacity
            style={styles.voiceBtn}
            onPress={() => handleNavigate('VoiceRecording')}
            activeOpacity={0.7}
          >
            <Ionicons name="mic-outline" size={15} color="#7C3AED" style={{ marginRight: 4 }} />
            <Text style={styles.voiceBtnText}>Tap to record</Text>
          </TouchableOpacity>
        </View>

        {/* Scan & Add Banner */}
        <TouchableOpacity
          style={[styles.bannerCard, { backgroundColor: '#F8FAFC', borderColor: '#E2E8F0' }]}
          onPress={() => handleNavigate('ScanAndAdd')}
          activeOpacity={0.7}
        >
          <View style={styles.bannerLeft}>
            <View style={[styles.bannerIconBox, { backgroundColor: '#F1F5F9' }]}>
              <Feather name="maximize" size={16} color="#475569" />
            </View>
            <View style={styles.bannerTextWrap}>
              <Text style={styles.bannerTitle}>Scan & Add</Text>
              <Text style={styles.bannerSub}>Add from a photo, document or card</Text>
            </View>
          </View>

          <View style={styles.scanBtn}>
            <Feather name="camera" size={15} color="#475569" style={{ marginRight: 4 }} />
            <Text style={styles.scanBtnText}>Scan</Text>
          </View>
        </TouchableOpacity>
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
    paddingBottom: 24,
  },

  /* Header */
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  headerLeftWrap: {
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
  logoSubtitle: {
    fontSize: 7.5,
    fontWeight: '700',
    color: '#94A3B8',
    letterSpacing: 1.3,
    marginTop: 1,
    marginLeft: 5,
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

  /* Title Section */
  titleSection: {
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

  /* AI Card */
  aiCard: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#F5EFFF',
    borderRadius: 16,
    padding: 12,
    borderWidth: 1,
    borderColor: '#EDE9FE',
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
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 14,
  },
  tryAiText: {
    fontSize: 11.5,
    fontWeight: '700',
    color: '#7C3AED',
  },

  /* Section Header */
  sectionHeaderRow: {
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
    marginRight: 4,
  },
  gridCardTitle: {
    fontSize: 13,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 2,
  },
  gridCardSub: {
    fontSize: 10,
    color: '#64748B',
    lineHeight: 13,
  },

  /* Quick Add */
  quickAddChipsScroll: {
    marginBottom: 20,
  },
  quickAddChipsRow: {
    flexDirection: 'row',
  },
  quickChip: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 20,
    borderWidth: 1,
    marginRight: 8,
  },
  quickChipText: {
    fontSize: 12,
    fontWeight: '700',
  },

  /* Banner Cards */
  bannerCard: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 12,
  },
  bannerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    marginRight: 8,
  },
  bannerIconBox: {
    width: 36,
    height: 36,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  bannerTextWrap: {
    flex: 1,
  },
  bannerTitle: {
    fontSize: 13,
    fontWeight: '800',
    color: '#0F172A',
  },
  bannerSub: {
    fontSize: 11,
    color: '#64748B',
    marginTop: 1,
  },
  voiceBtn: {
    backgroundColor: '#F5EFFF',
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#EDE9FE',
  },
  voiceBtnText: {
    fontSize: 11.5,
    fontWeight: '700',
    color: '#7C3AED',
  },
  scanBtn: {
    backgroundColor: '#F1F5F9',
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 14,
  },
  scanBtnText: {
    fontSize: 11.5,
    fontWeight: '700',
    color: '#475569',
  },
});
