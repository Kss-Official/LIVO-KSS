import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  ScrollView,
  Dimensions,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';

const { width } = Dimensions.get('window');
const H_PAD = 20;
const GAP = 10;
const CARD_SIZE = (width - H_PAD * 2 - GAP * 2) / 3;

interface Category {
  id: string;
  icon: React.ReactNode;
  title: string;
  description: string;
}

const categories: Category[] = [
  {
    id: 'tasks',
    icon: <Feather name="check-square" size={24} color="#1A202C" />,
    title: 'Tasks',
    description: 'Stay on top of your work',
  },
  {
    id: 'calendar',
    icon: <Feather name="calendar" size={24} color="#1A202C" />,
    title: 'Calendar',
    description: 'Plan your time effectively',
  },
  {
    id: 'goals',
    icon: <Ionicons name="bar-chart-outline" size={24} color="#1A202C" />,
    title: 'Goals',
    description: 'Turn your dreams into progress',
  },
  {
    id: 'habits',
    icon: <Ionicons name="heart-outline" size={24} color="#1A202C" />,
    title: 'Habits',
    description: 'Build a better you',
  },
  {
    id: 'learning',
    icon: <Feather name="book-open" size={24} color="#1A202C" />,
    title: 'Learning',
    description: 'Keep growing every day',
  },
  {
    id: 'finance',
    icon: <MaterialCommunityIcons name="wallet-outline" size={24} color="#1A202C" />,
    title: 'Finance',
    description: 'Track and manage your money',
  },
  {
    id: 'travel',
    icon: <MaterialCommunityIcons name="compass-outline" size={24} color="#1A202C" />,
    title: 'Travel',
    description: 'Plan your next adventure',
  },
  {
    id: 'insights',
    icon: <Ionicons name="stats-chart-outline" size={24} color="#1A202C" />,
    title: 'Insights',
    description: 'Understand your progress',
  },
];

interface SelectCategoriesScreenProps {
  onBack?: () => void;
  onSkip?: () => void;
  onContinue?: (selected: string[]) => void;
}

export const SelectCategoriesScreen: React.FC<SelectCategoriesScreenProps> = ({
  onBack,
  onSkip,
  onContinue,
}: SelectCategoriesScreenProps) => {
  const [selected, setSelected] = useState<Set<string>>(
    new Set(['tasks', 'goals', 'learning'])
  );

  const toggleCategory = (id: string) => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const handleContinue = () => {
    if (onContinue) onContinue(Array.from(selected));
  };

  const mainCats = categories.slice(0, 6);
  const lastCats = categories.slice(6);

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      {/* Background glows */}
      <View style={[styles.bgGlow, { top: -180, right: -60, width: 520, height: 520, borderRadius: 260, backgroundColor: '#C8F07A', opacity: 0.22 }]} />
      <View style={[styles.bgGlow, { top: -130, right: -20, width: 400, height: 400, borderRadius: 200, backgroundColor: '#B8EC5A', opacity: 0.16 }]} />
      <View style={[styles.bgGlow, { top: -80, right: 20, width: 300, height: 300, borderRadius: 150, backgroundColor: '#AEED44', opacity: 0.12 }]} />
      <View style={[styles.bgGlow, { top: -320, right: -320, width: 800, height: 800, borderRadius: 400, backgroundColor: '#D7F59E', opacity: 0.06 }]} />
      <View style={[styles.bgGlow, { bottom: -200, left: -180, width: 600, height: 600, borderRadius: 300, backgroundColor: '#C8F07A', opacity: 0.14 }]} />
      <View style={[styles.bgGlow, { bottom: -140, left: -120, width: 440, height: 440, borderRadius: 220, backgroundColor: '#B5E84E', opacity: 0.10 }]} />
      <View style={[styles.bgGlow, { bottom: -280, left: -280, width: 720, height: 720, borderRadius: 360, backgroundColor: '#D7F59E', opacity: 0.06 }]} />
      {/* Arc ring */}
      <View style={styles.bgArcCircle} />

      <ScrollView
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        {/* Top Bar */}
        <View style={styles.topBar}>
          <TouchableOpacity style={styles.backButton} onPress={onBack} activeOpacity={0.7}>
            <Feather name="chevron-left" size={22} color="#1A202C" />
          </TouchableOpacity>

          <View style={styles.stepIndicatorContainer}>
            <View style={styles.stepBarsRow}>
              <View style={styles.stepBarActive} />
              <View style={styles.stepBarActive} />
              <View style={styles.stepBarInactive} />
            </View>
            <Text style={styles.stepText}>Step 2 of 3</Text>
          </View>

          <TouchableOpacity onPress={onSkip} activeOpacity={0.7}>
            <Text style={styles.skipText}>Skip</Text>
          </TouchableOpacity>
        </View>

        {/* Title & Subtitle */}
        <Text style={styles.title}>What would you like{'\n'}to manage with LIVO?</Text>
        <Text style={styles.subtitle}>
          Choose what matters to you.{'\n'}You can always change this later.
        </Text>

        {/* Main 3-col grid (first 6 cards) */}
        <View style={styles.grid}>
          {mainCats.map((cat) => {
            const isSelected = selected.has(cat.id);
            return (
              <TouchableOpacity
                key={cat.id}
                style={[styles.card, isSelected && styles.cardSelected]}
                onPress={() => toggleCategory(cat.id)}
                activeOpacity={0.82}
              >
                {isSelected && (
                  <View style={styles.checkBadge}>
                    <Feather name="check" size={10} color="#FFFFFF" />
                  </View>
                )}
                <View style={styles.cardIconWrap}>{cat.icon}</View>
                <Text style={styles.cardTitle}>{cat.title}</Text>
                <Text style={styles.cardDesc}>{cat.description}</Text>
              </TouchableOpacity>
            );
          })}
        </View>

        {/* Last row — 2 cards centered */}
        <View style={styles.lastRow}>
          {lastCats.map((cat) => {
            const isSelected = selected.has(cat.id);
            return (
              <TouchableOpacity
                key={cat.id}
                style={[styles.card, isSelected && styles.cardSelected]}
                onPress={() => toggleCategory(cat.id)}
                activeOpacity={0.82}
              >
                {isSelected && (
                  <View style={styles.checkBadge}>
                    <Feather name="check" size={10} color="#FFFFFF" />
                  </View>
                )}
                <View style={styles.cardIconWrap}>{cat.icon}</View>
                <Text style={styles.cardTitle}>{cat.title}</Text>
                <Text style={styles.cardDesc}>{cat.description}</Text>
              </TouchableOpacity>
            );
          })}
        </View>
      </ScrollView>

      {/* Continue Button — fixed at bottom */}
      <View style={styles.footer}>
        <TouchableOpacity
          style={[styles.continueButton, selected.size === 0 && styles.continueButtonDisabled]}
          onPress={handleContinue}
          activeOpacity={0.88}
          disabled={selected.size === 0}
        >
          <Text style={styles.continueText}>Continue</Text>
          <Feather name="arrow-right" size={18} color="#0F172A" />
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },

  /* Background */
  bgGlow: {
    position: 'absolute',
  },
  bgArcCircle: {
    position: 'absolute',
    top: 10,
    right: -90,
    width: 380,
    height: 380,
    borderRadius: 190,
    borderWidth: 1,
    borderColor: 'rgba(149, 230, 18, 0.20)',
  },

  scrollContent: {
    paddingHorizontal: H_PAD,
    paddingBottom: 90,
  },

  /* Top Bar */
  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: 48,
    marginBottom: 20,
  },
  backButton: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: 'rgba(255,255,255,0.85)',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#EEF2E6',
    elevation: 1,
  },
  stepIndicatorContainer: {
    alignItems: 'center',
  },
  stepBarsRow: {
    flexDirection: 'row',
    gap: 4,
    marginBottom: 3,
  },
  stepBarActive: {
    width: 28,
    height: 4,
    borderRadius: 2,
    backgroundColor: '#95E612',
  },
  stepBarInactive: {
    width: 18,
    height: 4,
    borderRadius: 2,
    backgroundColor: '#D1D5DB',
  },
  stepText: {
    fontSize: 10.5,
    color: '#6B7280',
    fontWeight: '500',
  },
  skipText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#6B7280',
  },

  /* Title */
  title: {
    fontSize: 25,
    fontWeight: '800',
    color: '#0F172A',
    letterSpacing: -0.4,
    lineHeight: 31,
    marginBottom: 6,
  },
  subtitle: {
    fontSize: 12.5,
    color: '#718096',
    lineHeight: 18,
    marginBottom: 24,
    fontWeight: '400',
  },

  /* Grid — first 6 cards in 3 columns */
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: GAP,
    marginTop: 4,
    marginBottom: GAP,
  },

  /* Last row — 2 cards, centered */
  lastRow: {
    flexDirection: 'row',
    justifyContent: 'center',
    gap: GAP,
  },

  /* Card */
  card: {
    width: CARD_SIZE,
    height: CARD_SIZE + 16,
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    borderWidth: 1.2,
    borderColor: '#EEF2E6',
    paddingVertical: 10,
    paddingHorizontal: 6,
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
    shadowColor: '#94A3B8',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 5,
    elevation: 1,
  },
  cardSelected: {
    borderColor: '#95E612',
    borderWidth: 1.8,
    backgroundColor: '#FAFFF3',
    shadowColor: '#84CC16',
    shadowOpacity: 0.12,
    shadowRadius: 6,
    elevation: 3,
  },
  checkBadge: {
    position: 'absolute',
    top: 6,
    right: 6,
    width: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: '#7DC900',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 10,
  },
  cardIconWrap: {
    marginBottom: 6,
    alignItems: 'center',
  },
  cardTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
    textAlign: 'center',
    marginBottom: 3,
  },
  cardDesc: {
    fontSize: 10,
    color: '#718096',
    textAlign: 'center',
    lineHeight: 13,
    fontWeight: '400',
  },

  /* Footer */
  footer: {
    position: 'absolute',
    bottom: 60,
    left: 0,
    right: 0,
    paddingHorizontal: H_PAD,
    backgroundColor: 'transparent',
  },
  continueButton: {
    height: 48,
    backgroundColor: '#9CE612',
    borderRadius: 24,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    shadowColor: '#7DC900',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 10,
    elevation: 6,
  },
  continueButtonDisabled: {
    opacity: 0.45,
    shadowOpacity: 0.08,
    elevation: 1,
  },
  continueText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
  },
});
