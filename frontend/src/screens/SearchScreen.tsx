import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  SafeAreaView,
  StatusBar,
} from 'react-native';
import { Feather } from '@expo/vector-icons';
import { Colors } from '../theme/colors';
import { useNavigation } from '@react-navigation/native';
import { useTasks } from '../hooks/useTasks';
import { useHabits } from '../hooks/useHabits';
import { useGoals } from '../hooks/useGoals';

interface SearchScreenProps {
  onBack?: () => void;
}

type CategoryType = 'All' | 'Tasks' | 'Goals' | 'Habits' | 'Events';

export const SearchScreen: React.FC<SearchScreenProps> = ({ onBack }) => {
  const navigation = useNavigation<any>();
  const [query, setQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<CategoryType>('All');

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  const { tasks } = useTasks();
  const { habits } = useHabits();
  const { goals } = useGoals();

  const mockEvents = [
    { id: 'e1', title: 'Team Sync Meeting', subtitle: 'Event | 10:00 AM', category: 'Events', icon: 'calendar', color: '#1D4ED8', bg: '#EBF3FF' },
    { id: 'e2', title: 'Design Review', subtitle: 'Event | 2:30 PM', category: 'Events', icon: 'layers', color: '#1D4ED8', bg: '#EBF3FF' },
  ];

  const categories: CategoryType[] = ['All', 'Tasks', 'Goals', 'Habits', 'Events'];

  const recentSearches = ['Finish UI Design', 'Client Presentation', 'Morning Gym', 'Read Books'];

  // Filter tasks
  const filteredTasks = tasks.map(t => ({
    id: t.id,
    title: t.title,
    subtitle: t.category ? `Category: ${t.category}` : 'Task',
    category: 'Tasks',
    icon: 'check-square',
    color: '#2D6A00',
    bg: '#EBF9DB',
  }));

  // Filter goals
  const filteredGoals = goals.map(g => ({
    id: g.id,
    title: g.title,
    subtitle: `${g.progressPercentage || 0}% completed`,
    category: 'Goals',
    icon: 'target',
    color: '#6D28D9',
    bg: '#F3EBFB',
  }));

  // Filter habits
  const filteredHabits = habits.map(h => ({
    id: h.id,
    title: h.title,
    subtitle: `${h.streakCount || 0} day streak`,
    category: 'Habits',
    icon: 'repeat',
    color: '#C2410C',
    bg: '#FFF3E5',
  }));

  const allItems = [...filteredTasks, ...filteredGoals, ...filteredHabits, ...mockEvents];

  const filteredItems = allItems.filter(item => {
    const matchesQuery = item.title.toLowerCase().includes(query.trim().toLowerCase());
    const matchesCategory = selectedCategory === 'All' || item.category === selectedCategory;
    return matchesQuery && matchesCategory;
  });

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      {/* Header Bar */}
      <View style={styles.header}>
        <TouchableOpacity style={styles.backBtn} onPress={handleBack} activeOpacity={0.7}>
          <Feather name="arrow-left" size={22} color="#0F172A" />
        </TouchableOpacity>

        <View style={styles.searchBarContainer}>
          <Feather name="search" size={18} color="#64748B" style={styles.searchIcon} />
          <TextInput
            style={styles.searchInput}
            placeholder="Search tasks, goals, habits, events..."
            placeholderTextColor="#94A3B8"
            value={query}
            onChangeText={setQuery}
            autoFocus
          />
          {query.length > 0 && (
            <TouchableOpacity onPress={() => setQuery('')} style={styles.clearBtn}>
              <Feather name="x" size={16} color="#64748B" />
            </TouchableOpacity>
          )}
        </View>
      </View>

      {/* Category Pills */}
      <View style={styles.pillsContainer}>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.pillsScroll}>
          {categories.map(cat => {
            const isSelected = selectedCategory === cat;
            return (
              <TouchableOpacity
                key={cat}
                style={[styles.pill, isSelected && styles.pillActive]}
                onPress={() => setSelectedCategory(cat)}
                activeOpacity={0.7}
              >
                <Text style={[styles.pillText, isSelected && styles.pillTextActive]}>{cat}</Text>
              </TouchableOpacity>
            );
          })}
        </ScrollView>
      </View>

      <ScrollView style={styles.content} contentContainerStyle={styles.contentInner} showsVerticalScrollIndicator={false}>
        {query.trim() === '' ? (
          <View style={styles.recentContainer}>
            <Text style={styles.sectionTitle}>Recent Searches</Text>
            <View style={styles.recentChipsRow}>
              {recentSearches.map((term, index) => (
                <TouchableOpacity
                  key={index}
                  style={styles.recentChip}
                  onPress={() => setQuery(term)}
                  activeOpacity={0.7}
                >
                  <Feather name="clock" size={13} color="#64748B" style={{ marginRight: 6 }} />
                  <Text style={styles.recentChipText}>{term}</Text>
                </TouchableOpacity>
              ))}
            </View>

            <Text style={[styles.sectionTitle, { marginTop: 24 }]}>Suggested for You</Text>
            {allItems.slice(0, 4).map(item => (
              <TouchableOpacity key={item.id} style={styles.resultCard} activeOpacity={0.7}>
                <View style={[styles.cardIconBox, { backgroundColor: item.bg }]}>
                  <Feather name={item.icon as any} size={18} color={item.color} />
                </View>
                <View style={styles.cardInfo}>
                  <Text style={styles.cardTitle}>{item.title}</Text>
                  <Text style={styles.cardSubtitle}>{item.subtitle}</Text>
                </View>
                <View style={[styles.categoryBadge, { backgroundColor: item.bg }]}>
                  <Text style={[styles.categoryBadgeText, { color: item.color }]}>{item.category}</Text>
                </View>
              </TouchableOpacity>
            ))}
          </View>
        ) : filteredItems.length > 0 ? (
          <View>
            <Text style={styles.sectionTitle}>Results ({filteredItems.length})</Text>
            {filteredItems.map(item => (
              <TouchableOpacity key={item.id} style={styles.resultCard} activeOpacity={0.7}>
                <View style={[styles.cardIconBox, { backgroundColor: item.bg }]}>
                  <Feather name={item.icon as any} size={18} color={item.color} />
                </View>
                <View style={styles.cardInfo}>
                  <Text style={styles.cardTitle}>{item.title}</Text>
                  <Text style={styles.cardSubtitle}>{item.subtitle}</Text>
                </View>
                <View style={[styles.categoryBadge, { backgroundColor: item.bg }]}>
                  <Text style={[styles.categoryBadgeText, { color: item.color }]}>{item.category}</Text>
                </View>
              </TouchableOpacity>
            ))}
          </View>
        ) : (
          <View style={styles.emptyContainer}>
            <View style={styles.emptyIconBox}>
              <Feather name="search" size={32} color="#94A3B8" />
            </View>
            <Text style={styles.emptyTitle}>No results found</Text>
            <Text style={styles.emptySubtitle}>We couldn't find anything matching "{query}"</Text>
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
    paddingTop: 40,
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  backBtn: {
    padding: 6,
    marginRight: 8,
  },
  searchBarContainer: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F8FAFC',
    borderRadius: 12,
    paddingHorizontal: 12,
    height: 44,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  searchIcon: {
    marginRight: 8,
  },
  searchInput: {
    flex: 1,
    fontSize: 15,
    color: '#0F172A',
  },
  clearBtn: {
    padding: 4,
  },
  pillsContainer: {
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  pillsScroll: {
    paddingHorizontal: 16,
    gap: 8,
  },
  pill: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    backgroundColor: '#F1F5F9',
    marginRight: 6,
  },
  pillActive: {
    backgroundColor: Colors.primary,
  },
  pillText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#64748B',
  },
  pillTextActive: {
    color: '#FFFFFF',
  },
  content: {
    flex: 1,
  },
  contentInner: {
    padding: 16,
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: '#64748B',
    marginBottom: 12,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  recentContainer: {},
  recentChipsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  recentChip: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F8FAFC',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  recentChipText: {
    fontSize: 13,
    color: '#334155',
  },
  resultCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    padding: 14,
    borderRadius: 12,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: '#F1F5F9',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.03,
    shadowRadius: 3,
    elevation: 1,
  },
  cardIconBox: {
    width: 40,
    height: 40,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  cardInfo: {
    flex: 1,
  },
  cardTitle: {
    fontSize: 15,
    fontWeight: '600',
    color: '#0F172A',
    marginBottom: 2,
  },
  cardSubtitle: {
    fontSize: 12,
    color: '#64748B',
  },
  categoryBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
  },
  categoryBadgeText: {
    fontSize: 11,
    fontWeight: '600',
  },
  emptyContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 60,
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
  },
});
