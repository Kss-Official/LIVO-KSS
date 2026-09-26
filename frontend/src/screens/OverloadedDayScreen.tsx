import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
  StatusBar,
  Platform,
  Image,
  Dimensions,
  Alert,
  ActivityIndicator,
} from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';
import { useNavigation, useRoute } from '@react-navigation/native';
import { taskService } from '../services/taskService';

interface OverloadedDayScreenProps {
  onBack?: () => void;
  plannedWork?: string;
  availableTime?: string;
  onRebalance?: () => Promise<void> | void;
  onHandleManually?: () => void;
}

const { width } = Dimensions.get('window');

export const OverloadedDayScreen: React.FC<OverloadedDayScreenProps> = ({
  onBack,
  plannedWork: propPlanned,
  availableTime: propAvailable,
  onRebalance,
  onHandleManually,
}) => {
  const navigation = useNavigation<any>();
  const route = useRoute<any>();

  const plannedWork = route?.params?.plannedWork || propPlanned || '7h 30m';
  const availableTime = route?.params?.availableTime || propAvailable || '5h 45m';

  const [isRebalancing, setIsRebalancing] = useState(false);

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  const handleRebalance = async () => {
    setIsRebalancing(true);
    try {
      // Real task rebalance from storage
      const allTasks = await taskService.getTasks();
      const pendingTasks = allTasks.filter((t) => !t.completed);

      let movedTaskTitles: string[] = [];
      if (pendingTasks.length > 0) {
        // Find lower priority tasks or last 2 tasks
        const lowPriority = pendingTasks
          .filter((t) => t.priority === 'Low' || t.priority === 'Medium' || t.priority === 'LOW' || t.priority === 'MEDIUM')
          .slice(0, 2);

        const tasksToMove = lowPriority.length > 0 ? lowPriority : pendingTasks.slice(-2);

        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        const tomorrowIso = tomorrow.toISOString();

        for (const task of tasksToMove) {
          await taskService.updateTask({
            ...task,
            date: tomorrowIso,
            dueDate: tomorrowIso,
          });
          movedTaskTitles.push(task.title);
        }
      }

      if (onRebalance) {
        await onRebalance();
      } else {
        await new Promise((resolve) => setTimeout(resolve, 1000));
      }

      setIsRebalancing(false);

      const message =
        movedTaskTitles.length > 0
          ? `LIVO moved "${movedTaskTitles.join('" and "')}" to tomorrow to give you focused deep work today.`
          : 'LIVO adjusted your tasks and schedule to fit cleanly within available time.';

      Alert.alert('Schedule Rebalanced! 🎯', message, [
        {
          text: 'View Updated Plan',
          onPress: () => navigation.navigate('MainTabs', { screen: 'Plan' }),
        },
      ]);
    } catch (e) {
      setIsRebalancing(false);
      Alert.alert('Error', 'Could not rebalance schedule automatically.');
    }
  };

  const handleManual = () => {
    if (onHandleManually) {
      onHandleManually();
    } else {
      navigation.navigate('MainTabs', { screen: 'Plan' });
    }
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      {/* 1. Header */}
      <View style={styles.headerRow}>
        <TouchableOpacity
          onPress={handleBack}
          style={styles.backBtn}
          hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
          activeOpacity={0.7}
        >
          <Feather name="arrow-left" size={24} color="#0F172A" />
        </TouchableOpacity>

        <Image
          source={require('../../assets/livo_logo.png')}
          style={styles.logoImage}
          resizeMode="contain"
        />
      </View>

      <ScrollView
        style={styles.scrollView}
        contentContainerStyle={styles.contentContainer}
        showsVerticalScrollIndicator={false}
      >
        {/* 2. Illustration */}
        <View style={styles.illustrationContainer}>
          <Image
            source={require('../../assets/overloaded_day.png')}
            style={styles.illustrationImage}
            resizeMode="contain"
          />
        </View>

        {/* 3. Title & Subtitle */}
        <View style={styles.textContainer}>
          <Text style={styles.title}>Your day is overloaded</Text>
          <Text style={styles.subtitle}>
            You have <Text style={styles.boldSubtitle}>{plannedWork}</Text> of planned work but only{' '}
            <Text style={styles.boldSubtitle}>{availableTime}</Text> available.
          </Text>
        </View>

        {/* 4. Two Stats Mini Cards */}
        <View style={styles.statsRow}>
          {/* Left: Planned work */}
          <View style={styles.plannedCard}>
            <View style={styles.statTopRow}>
              <View style={styles.clockIconBadge}>
                <Feather name="clock" size={14} color="#EF4444" />
              </View>
              <Text style={styles.statLabel}>Planned work</Text>
            </View>
            <Text style={styles.statValue}>{plannedWork}</Text>
            <View style={styles.plannedBarTrack}>
              <View style={styles.plannedBarFill} />
            </View>
          </View>

          {/* Right: Available time */}
          <View style={styles.availableCard}>
            <View style={styles.statTopRow}>
              <View style={styles.calIconBadge}>
                <Feather name="calendar" size={14} color="#22C55E" />
              </View>
              <Text style={styles.statLabel}>Available time</Text>
            </View>
            <Text style={styles.statValue}>{availableTime}</Text>
            <View style={styles.availableBarTrack}>
              <View style={styles.availableBarFill} />
            </View>
          </View>
        </View>

        {/* 5. LIVO SUGGESTS Card */}
        <View style={styles.suggestsCard}>
          <View style={styles.suggestsIconBadge}>
            <Ionicons name="sparkles" size={18} color="#16A34A" />
          </View>
          <View style={styles.suggestsTextWrapper}>
            <Text style={styles.suggestsTag}>LIVO SUGGESTS</Text>
            <Text style={styles.suggestsHeadline}>Let me rebalance your day</Text>
            <Text style={styles.suggestsSubtext}>
              I can automatically adjust lower priority tasks, find better time slots and keep your goals on track.
            </Text>
          </View>
        </View>

        {/* 6. Buttons Section */}
        <View style={styles.buttonsContainer}>
          {/* Button 1: Let LIVO rebalance */}
          <TouchableOpacity
            style={[styles.rebalanceButton, isRebalancing && styles.rebalanceButtonDisabled]}
            onPress={handleRebalance}
            activeOpacity={0.85}
            disabled={isRebalancing}
          >
            {isRebalancing ? (
              <ActivityIndicator size="small" color="#FFFFFF" style={{ marginRight: 8 }} />
            ) : (
              <Ionicons name="sparkles" size={17} color="#FFFFFF" style={styles.btnIcon} />
            )}
            <Text style={styles.rebalanceButtonText}>
              {isRebalancing ? 'Rebalancing Schedule...' : 'Let LIVO rebalance'}
            </Text>
          </TouchableOpacity>

          {/* Button 2: I'll handle it */}
          <TouchableOpacity
            style={styles.secondaryButton}
            onPress={handleManual}
            activeOpacity={0.75}
            disabled={isRebalancing}
          >
            <Feather name="calendar" size={17} color="#0F172A" style={styles.btnIcon} />
            <Text style={styles.secondaryButtonText}>I'll handle it</Text>
          </TouchableOpacity>
        </View>

        {/* 7. Tip from LIVO */}
        <TouchableOpacity
          style={styles.tipCard}
          onPress={() => {
            Alert.alert(
              'LIVO Focus Tip 💡',
              'Limiting high-cognitive tasks to 3 per day prevents mental fatigue and produces higher quality output.'
            );
          }}
          activeOpacity={0.8}
        >
          <View style={styles.tipIconBadge}>
            <Ionicons name="bulb-outline" size={20} color="#22C55E" />
          </View>
          <View style={styles.tipTextWrapper}>
            <Text style={styles.tipTitle}>Tip from LIVO</Text>
            <Text style={styles.tipDescription}>
              A more focused schedule helps you get more done with less stress.
            </Text>
          </View>
          <Feather name="chevron-right" size={18} color="#94A3B8" />
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
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) + 8 : 12,
    paddingBottom: 8,
    backgroundColor: '#FFFFFF',
  },
  backBtn: {
    width: 36,
    height: 36,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
    marginLeft: -4,
  },
  logoImage: {
    width: 86,
    height: 32,
  },
  scrollView: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  contentContainer: {
    paddingHorizontal: 20,
    paddingTop: 8,
    paddingBottom: 36,
    alignItems: 'center',
  },

  /* Illustration */
  illustrationContainer: {
    width: '100%',
    height: Math.min(width * 0.62, 240),
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: 4,
  },
  illustrationImage: {
    width: '100%',
    height: '100%',
  },

  /* Text */
  textContainer: {
    alignItems: 'center',
    marginBottom: 16,
    paddingHorizontal: 12,
  },
  title: {
    fontSize: 25,
    fontWeight: '800',
    color: '#0F172A',
    textAlign: 'center',
    marginBottom: 6,
    letterSpacing: -0.3,
  },
  subtitle: {
    fontSize: 13.5,
    color: '#64748B',
    textAlign: 'center',
    lineHeight: 20,
  },
  boldSubtitle: {
    fontWeight: '800',
    color: '#0F172A',
  },

  /* Stats Row */
  statsRow: {
    flexDirection: 'row',
    width: '100%',
    gap: 12,
    marginBottom: 14,
  },
  plannedCard: {
    flex: 1,
    backgroundColor: '#FFF5F5',
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#FEE2E2',
    padding: 12,
  },
  availableCard: {
    flex: 1,
    backgroundColor: '#F0FDF4',
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#DCFCE7',
    padding: 12,
  },
  statTopRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 4,
  },
  clockIconBadge: {
    width: 22,
    height: 22,
    borderRadius: 11,
    backgroundColor: '#FEE2E2',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 6,
  },
  calIconBadge: {
    width: 22,
    height: 22,
    borderRadius: 11,
    backgroundColor: '#DCFCE7',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 6,
  },
  statLabel: {
    fontSize: 11,
    fontWeight: '600',
    color: '#64748B',
  },
  statValue: {
    fontSize: 17,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 8,
  },
  plannedBarTrack: {
    height: 4,
    backgroundColor: '#FEE2E2',
    borderRadius: 2,
    overflow: 'hidden',
  },
  plannedBarFill: {
    width: '68%',
    height: '100%',
    backgroundColor: '#F87171',
    borderRadius: 2,
  },
  availableBarTrack: {
    height: 4,
    backgroundColor: '#DCFCE7',
    borderRadius: 2,
    overflow: 'hidden',
  },
  availableBarFill: {
    width: '52%',
    height: '100%',
    backgroundColor: '#22C55E',
    borderRadius: 2,
  },

  /* Suggests Card */
  suggestsCard: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    backgroundColor: '#EDF8EE',
    borderRadius: 18,
    borderWidth: 1,
    borderColor: '#D4F4DC',
    padding: 14,
    width: '100%',
    marginBottom: 16,
  },
  suggestsIconBadge: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#DCFCE7',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
    marginTop: 2,
  },
  suggestsTextWrapper: {
    flex: 1,
  },
  suggestsTag: {
    fontSize: 11,
    fontWeight: '800',
    color: '#16A34A',
    letterSpacing: 0.5,
    marginBottom: 2,
  },
  suggestsHeadline: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 3,
  },
  suggestsSubtext: {
    fontSize: 12,
    color: '#64748B',
    lineHeight: 17,
  },

  /* Buttons */
  buttonsContainer: {
    width: '100%',
    gap: 10,
    marginBottom: 16,
  },
  rebalanceButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#28A745',
    height: 52,
    borderRadius: 26,
    width: '100%',
    shadowColor: '#28A745',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.32,
    shadowRadius: 10,
    elevation: 5,
  },
  rebalanceButtonDisabled: {
    opacity: 0.8,
  },
  rebalanceButtonText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  secondaryButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#EEF2F6',
    height: 52,
    borderRadius: 26,
    width: '100%',
  },
  secondaryButtonText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
  },
  btnIcon: {
    marginRight: 8,
  },

  /* Tip Card */
  tipCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#EDF8EE',
    borderRadius: 18,
    borderWidth: 1,
    borderColor: '#D4F4DC',
    padding: 14,
    width: '100%',
  },
  tipIconBadge: {
    width: 38,
    height: 38,
    borderRadius: 19,
    backgroundColor: '#D7F5D8',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  tipTextWrapper: {
    flex: 1,
  },
  tipTitle: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 2,
  },
  tipDescription: {
    fontSize: 11.5,
    color: '#64748B',
    lineHeight: 16,
  },
});
