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
} from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { taskService } from '../services/taskService';
import { eventService } from '../services/eventService';
import { habitService } from '../services/habitService';

interface UpdatingLivoScreenProps {
  onBack?: () => void;
  onComplete?: () => void;
  initialProgress?: number;
  autoProgress?: boolean;
}

const { width } = Dimensions.get('window');

export const UpdatingLivoScreen: React.FC<UpdatingLivoScreenProps> = ({
  onBack,
  onComplete,
  initialProgress = 68,
  autoProgress = true,
}) => {
  const navigation = useNavigation<any>();
  const [progress, setProgress] = useState(initialProgress);
  const [activeStep, setActiveStep] = useState<1 | 2 | 3>(2);

  useEffect(() => {
    if (!autoProgress) return;

    let isMounted = true;
    const syncData = async () => {
      try {
        // Pre-fetch actual storage
        await Promise.all([
          taskService.getTasks(),
          eventService.getEvents(),
          habitService.getHabits(),
        ]);
      } catch (e) {
        // Continue
      }
    };

    syncData();

    const interval = setInterval(() => {
      setProgress((prev) => {
        const next = prev + 3;
        if (next >= 100) {
          clearInterval(interval);
          if (isMounted) {
            setActiveStep(3);
            setTimeout(() => {
              if (onComplete) {
                onComplete();
              } else {
                navigation.navigate('MainTabs');
              }
            }, 500);
          }
          return 100;
        }

        if (next > 85) {
          setActiveStep(3);
        } else if (next > 30) {
          setActiveStep(2);
        } else {
          setActiveStep(1);
        }
        return next;
      });
    }, 120);

    return () => {
      isMounted = false;
      clearInterval(interval);
    };
  }, [autoProgress, onComplete, navigation]);

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      {/* 1. Top Header */}
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
        {/* 2. Illustration Area */}
        <View style={styles.illustrationContainer}>
          <Image
            source={require('../../assets/updating_livo.png')}
            style={styles.illustrationImage}
            resizeMode="contain"
          />
        </View>

        {/* 3. Title & Subtitle */}
        <View style={styles.textContainer}>
          <Text style={styles.title}>Updating your LIVO...</Text>
          <Text style={styles.subtitle}>
            Please wait while we load your data. This won't take long.
          </Text>
        </View>

        {/* 4. Progress Bar Row */}
        <View style={styles.progressContainer}>
          <View style={styles.progressBarTrack}>
            <View style={[styles.progressBarFill, { width: `${Math.min(Math.max(progress, 0), 100)}%` }]} />
          </View>
          <Text style={styles.progressPercentageText}>{progress}%</Text>
        </View>

        {/* 5. Step Checklist */}
        <View style={styles.checklistContainer}>
          {/* Step 1: Connecting to LIVO */}
          <View style={styles.checklistItem}>
            <View style={activeStep >= 2 ? styles.checkCircleCompleted : styles.checkCircleActive}>
              {activeStep >= 2 ? (
                <Feather name="check" size={13} color="#22C55E" />
              ) : (
                <Feather name="refresh-cw" size={14} color="#22C55E" />
              )}
            </View>
            <Text style={activeStep >= 2 ? styles.checklistTextCompleted : styles.checklistTextActive}>
              Connecting to LIVO
            </Text>
          </View>

          {/* Step 2: Loading your data */}
          <View style={styles.checklistItem}>
            <View
              style={
                activeStep > 2
                  ? styles.checkCircleCompleted
                  : activeStep === 2
                  ? styles.checkCircleActive
                  : styles.checkCirclePending
              }
            >
              {activeStep > 2 ? (
                <Feather name="check" size={13} color="#22C55E" />
              ) : activeStep === 2 ? (
                <Feather name="refresh-cw" size={14} color="#22C55E" />
              ) : null}
            </View>
            <Text
              style={
                activeStep > 2
                  ? styles.checklistTextCompleted
                  : activeStep === 2
                  ? styles.checklistTextActive
                  : styles.checklistTextPending
              }
            >
              Loading your data
            </Text>
          </View>

          {/* Step 3: Almost there... */}
          <View style={styles.checklistItem}>
            <View
              style={
                activeStep === 3
                  ? styles.checkCircleActive
                  : styles.checkCirclePending
              }
            >
              {activeStep === 3 && progress >= 100 ? (
                <Feather name="check" size={13} color="#22C55E" />
              ) : activeStep === 3 ? (
                <Feather name="refresh-cw" size={14} color="#22C55E" />
              ) : null}
            </View>
            <Text
              style={
                activeStep === 3
                  ? styles.checklistTextActive
                  : styles.checklistTextPending
              }
            >
              Almost there...
            </Text>
          </View>
        </View>

        {/* 6. "Good things take a moment" Info Card */}
        <View style={styles.infoCard}>
          <View style={styles.infoIconBadge}>
            <Ionicons name="bulb-outline" size={22} color="#22C55E" />
          </View>
          <View style={styles.infoTextWrapper}>
            <Text style={styles.infoTitle}>Good things take a moment</Text>
            <Text style={styles.infoDescription}>
              We're setting up your personalized experience.
            </Text>
          </View>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

// Export alias for convenience
export const LoadingScreen = UpdatingLivoScreen;

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
    paddingHorizontal: 24,
    paddingTop: 16,
    paddingBottom: 32,
    alignItems: 'center',
  },

  /* Illustration */
  illustrationContainer: {
    width: '100%',
    height: Math.min(width * 0.72, 280),
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 8,
    marginBottom: 16,
  },
  illustrationImage: {
    width: '100%',
    height: '100%',
  },

  /* Text */
  textContainer: {
    alignItems: 'center',
    marginBottom: 24,
    paddingHorizontal: 12,
  },
  title: {
    fontSize: 24,
    fontWeight: '800',
    color: '#0F172A',
    textAlign: 'center',
    marginBottom: 10,
    letterSpacing: -0.3,
  },
  subtitle: {
    fontSize: 14,
    color: '#64748B',
    textAlign: 'center',
    lineHeight: 21,
  },

  /* Progress Bar */
  progressContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    width: '100%',
    marginBottom: 28,
  },
  progressBarTrack: {
    flex: 1,
    height: 7,
    backgroundColor: '#EEF2F6',
    borderRadius: 4,
    overflow: 'hidden',
    marginRight: 14,
  },
  progressBarFill: {
    height: '100%',
    backgroundColor: '#22C55E',
    borderRadius: 4,
  },
  progressPercentageText: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0F172A',
    width: 38,
    textAlign: 'right',
  },

  /* Checklist */
  checklistContainer: {
    width: '100%',
    marginBottom: 32,
    paddingHorizontal: 12,
    gap: 16,
  },
  checklistItem: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  checkCircleCompleted: {
    width: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: '#DCFCE7',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 14,
  },
  checklistTextCompleted: {
    fontSize: 14.5,
    fontWeight: '500',
    color: '#475569',
  },
  checkCircleActive: {
    width: 20,
    height: 20,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 14,
  },
  checklistTextActive: {
    fontSize: 14.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  checkCirclePending: {
    width: 18,
    height: 18,
    borderRadius: 9,
    borderWidth: 1.5,
    borderColor: '#CBD5E1',
    marginRight: 15,
    marginLeft: 1,
  },
  checklistTextPending: {
    fontSize: 14.5,
    fontWeight: '500',
    color: '#94A3B8',
  },

  /* Good things take a moment Info Card */
  infoCard: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    backgroundColor: '#EDF8EE',
    borderRadius: 20,
    borderWidth: 1,
    borderColor: '#D4F4DC',
    padding: 16,
    width: '100%',
  },
  infoIconBadge: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: '#D7F5D8',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 14,
    marginTop: 2,
  },
  infoTextWrapper: {
    flex: 1,
  },
  infoTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 3,
  },
  infoDescription: {
    fontSize: 12,
    color: '#64748B',
    lineHeight: 18,
  },
});
