import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  SafeAreaView,
  StatusBar,
} from 'react-native';
import { Feather } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { Colors } from '../theme/colors';

interface NotificationsScreenProps {
  onBack?: () => void;
}

interface NotificationItem {
  id: string;
  title: string;
  message: string;
  time: string;
  unread: boolean;
  type: 'task' | 'goal' | 'habit' | 'system' | 'ai';
  icon: string;
  color: string;
  bg: string;
}

export const NotificationsScreen: React.FC<NotificationsScreenProps> = ({ onBack }) => {
  const navigation = useNavigation<any>();
  const [filter, setFilter] = useState<'All' | 'Unread' | 'Reminders'>('All');

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  const [notifications, setNotifications] = useState<NotificationItem[]>([
    {
      id: 'n1',
      title: 'Task Reminder',
      message: 'Finish UI Design is due today at 9:00 AM. Keep up the great work!',
      time: '10 mins ago',
      unread: true,
      type: 'task',
      icon: 'clock',
      color: '#2D6A00',
      bg: '#EBF9DB',
    },
    {
      id: 'n2',
      title: 'Goal Progress Update',
      message: 'You achieved 60% progress on your "Weekly Fitness Goal".',
      time: '1 hour ago',
      unread: true,
      type: 'goal',
      icon: 'target',
      color: '#6D28D9',
      bg: '#F3EBFB',
    },
    {
      id: 'n3',
      title: 'Habit Streak Fire! 🔥',
      message: 'You have completed 5 consecutive days of "Morning Gym". Don\'t break the chain!',
      time: '3 hours ago',
      unread: false,
      type: 'habit',
      icon: 'zap',
      color: '#C2410C',
      bg: '#FFF3E5',
    },
    {
      id: 'n4',
      title: 'LIVO AI Tip',
      message: 'Based on your recent schedule, blocking 30 mins after lunch will increase your focus by 20%.',
      time: 'Yesterday',
      unread: false,
      type: 'ai',
      icon: 'cpu',
      color: '#7C3AED',
      bg: '#F5EFFF',
    },
    {
      id: 'n5',
      title: 'Welcome to LIVO!',
      message: 'Your personal AI productivity assistant is set up and ready to help you reach your goals.',
      time: '2 days ago',
      unread: false,
      type: 'system',
      icon: 'check-circle',
      color: '#1D4ED8',
      bg: '#EBF3FF',
    },
  ]);

  const handleMarkAllRead = () => {
    setNotifications(prev => prev.map(item => ({ ...item, unread: false })));
  };

  const handleToggleRead = (id: string) => {
    setNotifications(prev =>
      prev.map(item => (item.id === id ? { ...item, unread: !item.unread } : item))
    );
  };

  const filteredNotifications = notifications.filter(item => {
    if (filter === 'Unread') return item.unread;
    if (filter === 'Reminders') return item.type === 'task' || item.type === 'habit';
    return true;
  });

  const unreadCount = notifications.filter(item => item.unread).length;

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      {/* Top Header */}
      <View style={styles.header}>
        <TouchableOpacity style={styles.backBtn} onPress={handleBack} activeOpacity={0.7}>
          <Feather name="arrow-left" size={22} color="#0F172A" />
        </TouchableOpacity>

        <View style={styles.headerTitleContainer}>
          <Text style={styles.headerTitle}>Notifications</Text>
          {unreadCount > 0 && (
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{unreadCount}</Text>
            </View>
          )}
        </View>

        {unreadCount > 0 && (
          <TouchableOpacity onPress={handleMarkAllRead} activeOpacity={0.7}>
            <Text style={styles.markReadText}>Mark all read</Text>
          </TouchableOpacity>
        )}
      </View>

      {/* Filter Tabs */}
      <View style={styles.tabsRow}>
        {(['All', 'Unread', 'Reminders'] as const).map(tab => {
          const isActive = filter === tab;
          return (
            <TouchableOpacity
              key={tab}
              style={[styles.tabBtn, isActive && styles.tabBtnActive]}
              onPress={() => setFilter(tab)}
              activeOpacity={0.7}
            >
              <Text style={[styles.tabText, isActive && styles.tabTextActive]}>{tab}</Text>
            </TouchableOpacity>
          );
        })}
      </View>

      {/* Notifications List */}
      <ScrollView style={styles.content} contentContainerStyle={styles.contentInner} showsVerticalScrollIndicator={false}>
        {filteredNotifications.length > 0 ? (
          filteredNotifications.map(item => (
            <TouchableOpacity
              key={item.id}
              style={[styles.notificationCard, item.unread && styles.unreadCard]}
              onPress={() => handleToggleRead(item.id)}
              activeOpacity={0.8}
            >
              <View style={[styles.iconBox, { backgroundColor: item.bg }]}>
                <Feather name={item.icon as any} size={20} color={item.color} />
              </View>

              <View style={styles.cardContent}>
                <View style={styles.cardHeaderRow}>
                  <Text style={styles.cardTitle}>{item.title}</Text>
                  <Text style={styles.timeText}>{item.time}</Text>
                </View>
                <Text style={styles.messageText}>{item.message}</Text>
              </View>

              {item.unread && <View style={styles.unreadDot} />}
            </TouchableOpacity>
          ))
        ) : (
          <View style={styles.emptyContainer}>
            <View style={styles.emptyIconBox}>
              <Feather name="bell-off" size={32} color="#94A3B8" />
            </View>
            <Text style={styles.emptyTitle}>No notifications</Text>
            <Text style={styles.emptySubtitle}>You're all caught up! Check back later for new updates.</Text>
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingTop: 40,
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  backBtn: {
    padding: 4,
  },
  headerTitleContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#0F172A',
  },
  badge: {
    backgroundColor: Colors.primary,
    borderRadius: 10,
    paddingHorizontal: 7,
    paddingVertical: 2,
    marginLeft: 8,
  },
  badgeText: {
    color: '#FFFFFF',
    fontSize: 11,
    fontWeight: '700',
  },
  markReadText: {
    fontSize: 13,
    fontWeight: '600',
    color: Colors.primary,
  },
  tabsRow: {
    flexDirection: 'row',
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
    gap: 8,
  },
  tabBtn: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    backgroundColor: '#F1F5F9',
  },
  tabBtnActive: {
    backgroundColor: Colors.primary,
  },
  tabText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#64748B',
  },
  tabTextActive: {
    color: '#FFFFFF',
  },
  content: {
    flex: 1,
  },
  contentInner: {
    padding: 16,
  },
  notificationCard: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    backgroundColor: '#FFFFFF',
    padding: 14,
    borderRadius: 14,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: '#F1F5F9',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.02,
    shadowRadius: 3,
    elevation: 1,
  },
  unreadCard: {
    borderColor: '#EBF9DB',
    backgroundColor: '#FAFDF6',
  },
  iconBox: {
    width: 42,
    height: 42,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  cardContent: {
    flex: 1,
  },
  cardHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  cardTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: '#0F172A',
  },
  timeText: {
    fontSize: 11,
    color: '#94A3B8',
  },
  messageText: {
    fontSize: 13,
    color: '#475569',
    lineHeight: 18,
  },
  unreadDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: Colors.primary,
    marginLeft: 8,
    marginTop: 4,
  },
  emptyContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 80,
  },
  emptyIconBox: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: '#F1F5F9',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 16,
  },
  emptyTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 6,
  },
  emptySubtitle: {
    fontSize: 13,
    color: '#64748B',
    textAlign: 'center',
    paddingHorizontal: 20,
  },
});
