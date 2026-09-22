import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Dimensions,
  SafeAreaView,
  StatusBar,
  Image,
} from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';

const { width } = Dimensions.get('window');

interface WelcomeScreenProps {
  onGetStarted?: () => void;
  onLogin?: () => void;
}

export const WelcomeScreen: React.FC<WelcomeScreenProps> = ({
  onGetStarted,
  onLogin,
}: WelcomeScreenProps) => {
  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
      <View style={styles.container}>
        {/* Background — heavily diffused soft glow, pure white base */}
        {/* Top-right: many large low-opacity circles simulate blur */}
        <View style={[styles.bgGlow, { top: -320, right: -320, width: 800, height: 800, borderRadius: 400, backgroundColor: '#C8F07A', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { top: -260, right: -260, width: 680, height: 680, borderRadius: 340, backgroundColor: '#B5E84E', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { top: -200, right: -200, width: 560, height: 560, borderRadius: 280, backgroundColor: '#AEED44', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { top: -150, right: -150, width: 440, height: 440, borderRadius: 220, backgroundColor: '#A4E83A', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { top: -110, right: -110, width: 340, height: 340, borderRadius: 170, backgroundColor: '#99E030', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { top: -80, right: -80, width: 250, height: 250, borderRadius: 125, backgroundColor: '#95E612', opacity: 0.04 }]} />
        {/* Bottom-left: same diffusion approach */}
        <View style={[styles.bgGlow, { bottom: -280, left: -280, width: 720, height: 720, borderRadius: 360, backgroundColor: '#D7F59E', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { bottom: -220, left: -220, width: 580, height: 580, borderRadius: 290, backgroundColor: '#C8F07A', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { bottom: -170, left: -170, width: 460, height: 460, borderRadius: 230, backgroundColor: '#BCE84A', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { bottom: -130, left: -130, width: 360, height: 360, borderRadius: 180, backgroundColor: '#B0E040', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { bottom: -100, left: -100, width: 270, height: 270, borderRadius: 135, backgroundColor: '#A8DC38', opacity: 0.04 }]} />
        {/* Barely-visible arc ring */}
        <View style={styles.bgArcCircle} />

        {/* Top Logo & Subtitle */}
        <View style={styles.headerContainer}>
          <Image
            source={require('../../assets/livo_logo.png')}
            style={styles.logoImage}
            resizeMode="contain"
          />

          <Text style={styles.taglineLine1}>Your Life.</Text>
          <Text style={styles.taglineLine2}>Organized Intelligently.</Text>
        </View>

        {/* Central Graphic Area with Stacked 3D Ovals & Floating Chips */}
        <View style={styles.graphicContainer}>
          {/* Stacked 3D Glass Discs Image */}
          <Image
            source={require('../../assets/stacked_discs.png')}
            style={styles.heroDiscsImage}
            resizeMode="contain"
          />

          {/* Floating Feature Pills */}
          {/* Top Row */}
          <View style={[styles.chip, styles.chipTasks]}>
            <Feather name="check-square" size={15} color="#2D3748" />
            <Text style={styles.chipText}>Tasks</Text>
          </View>

          <View style={[styles.chip, styles.chipGoals]}>
            <Feather name="bar-chart-2" size={15} color="#2D3748" />
            <Text style={styles.chipText}>Goals</Text>
          </View>

          {/* Mid Upper Row */}
          <View style={[styles.chip, styles.chipCalendar]}>
            <Feather name="calendar" size={15} color="#2D3748" />
            <Text style={styles.chipText}>Calendar</Text>
          </View>

          <View style={[styles.chip, styles.chipHabits]}>
            <Ionicons name="heart-outline" size={16} color="#2D3748" />
            <Text style={styles.chipText}>Habits</Text>
          </View>

          {/* Mid Lower Row */}
          <View style={[styles.chip, styles.chipLearning]}>
            <Feather name="book-open" size={15} color="#2D3748" />
            <Text style={styles.chipText}>Learning</Text>
          </View>

          <View style={[styles.chip, styles.chipFinance]}>
            <Feather name="credit-card" size={15} color="#2D3748" />
            <Text style={styles.chipText}>Finance</Text>
          </View>

          {/* Bottom Row */}
          <View style={[styles.chip, styles.chipTravel]}>
            <Feather name="send" size={14} color="#2D3748" />
            <Text style={styles.chipText}>Travel</Text>
          </View>

          <View style={[styles.chip, styles.chipInsights]}>
            <Ionicons name="stats-chart-outline" size={15} color="#2D3748" />
            <Text style={styles.chipText}>Insights</Text>
          </View>
        </View>

        {/* Carousel Indicator Dots */}
        <View style={styles.paginationContainer}>
          <View style={styles.activeDot} />
          <View style={styles.inactiveDot} />
          <View style={styles.inactiveDot} />
        </View>

        {/* Action Buttons Section */}
        <View style={styles.footerContainer}>
          <TouchableOpacity
            style={styles.getStartedButton}
            activeOpacity={0.88}
            onPress={onGetStarted}
          >
            <Text style={styles.getStartedText}>Get Started</Text>
            <Feather name="arrow-right" size={20} color="#1A202C" style={styles.arrowIcon} />
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.loginContainer}
            activeOpacity={0.7}
            onPress={onLogin}
          >
            <Text style={styles.loginText}>I already have an account</Text>
          </TouchableOpacity>
        </View>
      </View>
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
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 16,
    overflow: 'hidden',
  },

  /* Background Ambient Glows & Arcs */
  bgGlow: {
    position: 'absolute',
  },
  bgArcCircle: {
    position: 'absolute',
    top: 30,
    right: -110,
    width: 400,
    height: 400,
    borderRadius: 200,
    borderWidth: 1,
    borderColor: 'rgba(149, 230, 18, 0.18)',
  },
  /* Header Section */
  headerContainer: {
    alignItems: 'center',
    marginTop: 24,
    zIndex: 10,
  },
  logoImage: {
    width: 160,
    height: 60,
    marginBottom: 14,
  },
  taglineLine1: {
    fontSize: 20,
    fontWeight: '600',
    color: '#334155',
    textAlign: 'center',
    lineHeight: 26,
  },
  taglineLine2: {
    fontSize: 20,
    fontWeight: '600',
    color: '#334155',
    textAlign: 'center',
    lineHeight: 26,
  },

  /* Central Artwork & Feature Chips Container */
  graphicContainer: {
    width: width * 0.9,
    height: 320,
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
    marginVertical: 10,
  },

  /* 3D Stacked Translucent Green Discs Image */
  heroDiscsImage: {
    width: 250,
    height: 250,
  },

  /* Floating Chips Styling */
  chip: {
    position: 'absolute',
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 15,
    paddingVertical: 9,
    borderRadius: 22,
    gap: 7,

    // Soft Card Elevation Shadow
    shadowColor: '#1E293B',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 10,
    elevation: 5,
    borderWidth: 1,
    borderColor: '#F1F5F9',
  },
  chipText: {
    fontSize: 13.5,
    fontWeight: '600',
    color: '#334155',
  },

  /* Absolute Chip Positioning (Matching Image exact locations) */
  chipTasks: {
    top: 10,
    left: 12,
  },
  chipGoals: {
    top: 10,
    right: 18,
  },
  chipCalendar: {
    top: 85,
    left: -4,
  },
  chipHabits: {
    top: 88,
    right: 8,
  },
  chipLearning: {
    top: 162,
    left: -4,
  },
  chipFinance: {
    top: 165,
    right: 5,
  },
  chipTravel: {
    top: 236,
    left: 36,
  },
  chipInsights: {
    top: 236,
    right: 28,
  },

  /* Pagination Dots */
  paginationContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 7,
    marginTop: 6,
    marginBottom: 16,
  },
  activeDot: {
    width: 24,
    height: 7,
    borderRadius: 4,
    backgroundColor: '#95E612',
  },
  inactiveDot: {
    width: 7,
    height: 7,
    borderRadius: 3.5,
    backgroundColor: '#CBD5E1',
  },

  /* Footer Actions */
  footerContainer: {
    width: '100%',
    paddingHorizontal: 28,
    alignItems: 'center',
    marginBottom: 20,
  },
  getStartedButton: {
    width: '100%',
    height: 56,
    backgroundColor: '#95E612',
    borderRadius: 28,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,

    // Glowing Button Shadow
    shadowColor: '#84CC16',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.45,
    shadowRadius: 14,
    elevation: 8,
  },
  getStartedText: {
    fontSize: 17,
    fontWeight: '700',
    color: '#0F172A',
  },
  arrowIcon: {
    marginTop: 1,
  },
  loginContainer: {
    marginTop: 18,
    paddingVertical: 8,
    paddingHorizontal: 16,
  },
  loginText: {
    fontSize: 14.5,
    fontWeight: '500',
    color: '#64748B',
  },
});
