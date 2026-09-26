import React, { useState, useEffect } from 'react';
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
import { Task } from '../types';
import { taskService } from '../services/taskService';

interface TaskOverdueScreenProps {
  onBack?: () => void;
  task?: Partial<Task> & { category?: string; project?: string };
  onReschedule?: () => void;
  onMarkComplete?: () => Promise<void> | void;
  onAskAi?: () => void;
}

const { width } = Dimensions.get('window');

export const TaskOverdueScreen: React.FC<TaskOverdueScreenProps> = ({
  onBack,
  task: initialTaskProp,
  onReschedule,
  onMarkComplete,
  onAskAi,
}) => {
  const navigation = useNavigation<any>();
  const route = useRoute<any>();

  const [activeTask, setActiveTask] = useState<Partial<Task> & { category?: string; project?: string }>(() => {
    return (
      route?.params?.task ||
      initialTaskProp || {
        id: '1',
        title: 'Finish UI Design',
        category: 'Design',
        project: 'LIVO Mobile App',
        dueDate: 'Yesterday, 5 Sep',
      }
    );
  });

  const [isCompleted, setIsCompleted] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);

  useEffect(() => {
    const loadRealTask = async () => {
      const taskId = route?.params?.taskId || activeTask?.id;
      if (taskId) {
        try {
          const tasks = await taskService.getTasks();
          const found = tasks.find((t) => t.id === taskId);
          if (found) {
            setActiveTask({
              ...found,
              project: found.goal || found.category || 'LIVO Project',
              dueDate: found.dueDate || found.date || 'Yesterday',
            });
            if (found.completed) {
              setIsCompleted(true);
            }
          }
        } catch (e) {
          // Keep current state
        }
      }
    };
    loadRealTask();
  }, [route?.params]);

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  const handleReschedule = () => {
    if (onReschedule) {
      onReschedule();
    } else {
      navigation.navigate('AddTask', {
        taskId: activeTask.id,
        isReschedule: true,
        taskData: activeTask,
      });
    }
  };

  const handleMarkComplete = async () => {
    setIsUpdating(true);
    try {
      if (activeTask.id) {
        // Real update in AsyncStorage
        await taskService.toggleTaskCompletion(activeTask.id);
      }
      if (onMarkComplete) {
        await onMarkComplete();
      }
      setIsCompleted(true);
      setIsUpdating(false);

      Alert.alert(
        'Task Completed! 🎉',
        `"${activeTask.title}" has been marked as complete. Great job catching up!`,
        [
          {
            text: 'View Tasks',
            onPress: () => navigation.navigate('MainTabs', { screen: 'Plan' }),
          },
        ]
      );
    } catch (e) {
      setIsUpdating(false);
      Alert.alert('Error', 'Could not update task. Please try again.');
    }
  };

  const handleAskLivo = () => {
    if (onAskAi) {
      onAskAi();
    } else {
      navigation.navigate('MainTabs', {
        screen: 'Home',
        params: { initialQuery: `How can I quickly catch up on my overdue task: ${activeTask.title}?` },
      });
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
            source={require('../../assets/task_overdue.png')}
            style={styles.illustrationImage}
            resizeMode="contain"
          />
        </View>

        {/* 3. Title & Subtitle */}
        <View style={styles.textContainer}>
          <Text style={styles.title}>Task overdue</Text>
          <Text style={styles.subtitle}>
            “{activeTask.title}” was due yesterday. Don’t worry — you can still take action now.
          </Text>
        </View>

        {/* 4. Overdue Task Card */}
        <TouchableOpacity
          style={styles.taskCard}
          onPress={handleReschedule}
          activeOpacity={0.8}
        >
          <View style={styles.taskIconBadge}>
            <Ionicons name="document-text" size={18} color="#EF4444" />
          </View>
          <View style={styles.taskTextWrapper}>
            <Text style={styles.taskTitle}>{activeTask.title}</Text>
            <Text style={styles.taskProject}>
              {activeTask.category || 'Design'} • {activeTask.project || 'LIVO Mobile App'}
            </Text>
            <Text style={styles.taskDueDate}>
              Due • {activeTask.dueDate || 'Yesterday, 5 Sep'}
            </Text>
          </View>
          <Feather name="chevron-right" size={18} color="#94A3B8" />
        </TouchableOpacity>

        {/* 5. Buttons Section */}
        <View style={styles.buttonsContainer}>
          {/* Button 1: Reschedule */}
          <TouchableOpacity
            style={styles.rescheduleButton}
            onPress={handleReschedule}
            activeOpacity={0.85}
          >
            <Feather name="calendar" size={18} color="#FFFFFF" style={styles.btnIcon} />
            <Text style={styles.rescheduleButtonText}>Reschedule</Text>
          </TouchableOpacity>

          {/* Button 2: Mark Complete */}
          <TouchableOpacity
            style={styles.secondaryButton}
            onPress={handleMarkComplete}
            activeOpacity={0.75}
            disabled={isCompleted || isUpdating}
          >
            {isUpdating ? (
              <ActivityIndicator size="small" color="#0F172A" style={styles.btnIcon} />
            ) : (
              <Feather
                name={isCompleted ? 'check-circle' : 'check-circle'}
                size={18}
                color={isCompleted ? '#16A34A' : '#0F172A'}
                style={styles.btnIcon}
              />
            )}
            <Text style={[styles.secondaryButtonText, isCompleted && { color: '#16A34A' }]}>
              {isCompleted ? 'Completed ✓' : 'Mark Complete'}
            </Text>
          </TouchableOpacity>

          {/* Button 3: Ask LIVO */}
          <TouchableOpacity
            style={styles.askLivoButton}
            onPress={handleAskLivo}
            activeOpacity={0.75}
          >
            <Text style={styles.askLivoTitle}>Ask LIVO</Text>
            <Text style={styles.askLivoSubtext}>Get suggestions to catch up</Text>
          </TouchableOpacity>
        </View>

        {/* 6. Tip from LIVO */}
        <TouchableOpacity
          style={styles.tipCard}
          onPress={() => {
            Alert.alert(
              'LIVO Planning Tip 💡',
              'Breaking large tasks into 15-minute subtasks lowers friction and helps build momentum immediately.'
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
              Break it into smaller steps so it's easier to get started.
            </Text>
          </View>
          <Feather name="chevron-right" size={18} color="#94A3B8" />
        </TouchableOpacity>
      </ScrollView>
    </SafeAreaView>
  );
};

// Export alias for convenience
export const MissedDeadlineScreen = TaskOverdueScreen;

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
    marginVertical: 6,
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

  /* Task Card */
  taskCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFF5F5',
    borderRadius: 18,
    borderWidth: 1,
    borderColor: '#FEE2E2',
    padding: 14,
    width: '100%',
    marginBottom: 18,
  },
  taskIconBadge: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#FEE2E2',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  taskTextWrapper: {
    flex: 1,
  },
  taskTitle: {
    fontSize: 15,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 2,
  },
  taskProject: {
    fontSize: 12,
    color: '#64748B',
    marginBottom: 2,
  },
  taskDueDate: {
    fontSize: 12,
    fontWeight: '600',
    color: '#EF4444',
  },

  /* Buttons */
  buttonsContainer: {
    width: '100%',
    gap: 10,
    marginBottom: 18,
  },
  rescheduleButton: {
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
  rescheduleButtonText: {
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
  askLivoButton: {
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#EEF2F6',
    paddingVertical: 9,
    borderRadius: 26,
    width: '100%',
  },
  askLivoTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
  },
  askLivoSubtext: {
    fontSize: 11,
    color: '#64748B',
    marginTop: 1,
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
