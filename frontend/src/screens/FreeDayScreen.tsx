import React from 'react';
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
import { Feather, Ionicons, MaterialIcons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';

interface FreeDayScreenProps {
  onBack?: () => void;
  onAddTask?: () => void;
  onExploreSuggestions?: () => void;
}

const { width } = Dimensions.get('window');

export const FreeDayScreen: React.FC<FreeDayScreenProps> = ({
  onBack,
  onAddTask,
  onExploreSuggestions,
}) => {
  const navigation = useNavigation<any>();

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  const handleAddTask = () => {
    if (onAddTask) {
      onAddTask();
    } else {
      navigation.navigate('AddTask');
    }
  };

  const handleExploreSuggestions = () => {
    if (onExploreSuggestions) {
      onExploreSuggestions();
    } else {
      navigation.navigate('AddOptains');
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
            source={require('../../assets/free_day.png')}
            style={styles.illustrationImage}
            resizeMode="contain"
          />
        </View>

        {/* 3. Title & Subtitle */}
        <View style={styles.textContainer}>
          <Text style={styles.title}>A free day!</Text>
          <Text style={styles.subtitle}>
            You don’t have any scheduled tasks today. Take a moment to relax or plan something new.
          </Text>
        </View>

        {/* 4. Action Buttons */}
        <View style={styles.buttonsContainer}>
          {/* Button 1: Add a Task */}
          <TouchableOpacity
            style={styles.primaryButton}
            onPress={handleAddTask}
            activeOpacity={0.85}
          >
            <Feather name="calendar" size={18} color="#FFFFFF" style={styles.btnIcon} />
            <Text style={styles.primaryButtonText}>Add a Task</Text>
          </TouchableOpacity>

          {/* Button 2: Explore Suggestions */}
          <TouchableOpacity
            style={styles.secondaryButton}
            onPress={handleExploreSuggestions}
            activeOpacity={0.75}
          >
            <MaterialIcons
              name="auto-fix-high"
              size={18}
              color="#0F172A"
              style={styles.btnIcon}
            />
            <Text style={styles.secondaryButtonText}>Explore Suggestions</Text>
          </TouchableOpacity>
        </View>

        {/* 5. Tip from LIVO */}
        <View style={styles.tipCard}>
          <View style={styles.tipIconBadge}>
            <Ionicons name="bulb-outline" size={20} color="#22C55E" />
          </View>
          <View style={styles.tipTextWrapper}>
            <Text style={styles.tipTitle}>Tip from LIVO</Text>
            <Text style={styles.tipDescription}>
              Use your free time to plan ahead and stay productive!
            </Text>
          </View>
          <Feather name="chevron-right" size={18} color="#94A3B8" />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

// Export alias for convenience
export const EmptyDayScreen = FreeDayScreen;

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
    paddingTop: 10,
    paddingBottom: 36,
    alignItems: 'center',
  },

  /* Illustration */
  illustrationContainer: {
    width: '100%',
    height: Math.min(width * 0.72, 280),
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: 10,
  },
  illustrationImage: {
    width: '100%',
    height: '100%',
  },

  /* Text */
  textContainer: {
    alignItems: 'center',
    marginBottom: 26,
    paddingHorizontal: 12,
  },
  title: {
    fontSize: 26,
    fontWeight: '800',
    color: '#0F172A',
    textAlign: 'center',
    marginBottom: 8,
    letterSpacing: -0.3,
  },
  subtitle: {
    fontSize: 14,
    color: '#64748B',
    textAlign: 'center',
    lineHeight: 21,
  },

  /* Buttons */
  buttonsContainer: {
    width: '100%',
    gap: 12,
    marginBottom: 24,
  },
  primaryButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#28A745',
    height: 54,
    borderRadius: 27,
    width: '100%',
    shadowColor: '#28A745',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.32,
    shadowRadius: 10,
    elevation: 5,
  },
  primaryButtonText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  secondaryButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#EEF2F6',
    height: 54,
    borderRadius: 27,
    width: '100%',
  },
  secondaryButtonText: {
    fontSize: 15.5,
    fontWeight: '700',
    color: '#0F172A',
  },
  btnIcon: {
    marginRight: 9,
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
