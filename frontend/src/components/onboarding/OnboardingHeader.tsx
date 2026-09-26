import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

interface OnboardingHeaderProps {
  onBack?: () => void;
  iconName: keyof typeof Ionicons.glyphMap;
  title: string;
  subtitle: string;
  quoteText?: string;
  quoteDecoration?: boolean;
}

export const OnboardingHeader: React.FC<OnboardingHeaderProps> = ({
  onBack,
  iconName,
  title,
  subtitle,
  quoteText,
}) => {
  return (
    <View style={styles.container}>
      {/* Decorative Top-Right Arc Background */}
      <View style={styles.topArc} />

      {/* Top Navigation Row */}
      <View style={styles.topRow}>
        <TouchableOpacity style={styles.backBtn} onPress={onBack} hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}>
          <Ionicons name="chevron-back" size={24} color="#1E293B" />
        </TouchableOpacity>

        {/* Top Right Cursive Quote */}
        {!!quoteText && (
          <View style={styles.quoteContainer}>
            <Text style={styles.quoteText}>{quoteText}</Text>
            <View style={styles.underlineAccent} />
          </View>
        )}
      </View>

      {/* Badge Icon */}
      <View style={styles.iconBadgeContainer}>
        <View style={styles.iconBadge}>
          <Ionicons name={iconName} size={28} color="#2D5B18" />
        </View>
      </View>

      {/* Title & Subtitle */}
      <Text style={styles.title}>{title}</Text>
      <Text style={styles.subtitle}>{subtitle}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: 20,
    paddingTop: 12,
    paddingBottom: 16,
    position: 'relative',
  },
  topArc: {
    position: 'absolute',
    top: -60,
    right: -60,
    width: 240,
    height: 240,
    borderRadius: 120,
    backgroundColor: '#E2F4C5',
    opacity: 0.55,
  },
  topRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 20,
    zIndex: 2,
  },
  backBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
  },
  quoteContainer: {
    alignItems: 'flex-end',
    maxWidth: 200,
  },
  quoteText: {
    fontFamily: 'Caveat_600SemiBold',
    fontSize: 21,
    color: '#3B6B1F',
    textAlign: 'right',
    lineHeight: 23,
    transform: [{ rotate: '-3deg' }],
  },
  underlineAccent: {
    width: 80,
    height: 3,
    backgroundColor: '#74C011',
    borderRadius: 2,
    marginTop: 2,
  },
  iconBadgeContainer: {
    marginBottom: 16,
    zIndex: 2,
  },
  iconBadge: {
    width: 54,
    height: 54,
    borderRadius: 16,
    backgroundColor: '#E4F5CB',
    borderWidth: 1.5,
    borderColor: '#C7EA96',
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    fontSize: 32,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 8,
    letterSpacing: -0.5,
    zIndex: 2,
  },
  subtitle: {
    fontSize: 15,
    color: '#64748B',
    lineHeight: 22,
    fontWeight: '400',
    zIndex: 2,
  },
});
