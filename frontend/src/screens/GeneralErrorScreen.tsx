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
  ActivityIndicator,
  Dimensions,
  Alert,
} from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';

interface GeneralErrorScreenProps {
  onBack?: () => void;
  onTryAgain?: () => Promise<void> | void;
  onGoHome?: () => void;
  onContactSupport?: () => void;
  errorMessage?: string;
}

const { width } = Dimensions.get('window');

export const GeneralErrorScreen: React.FC<GeneralErrorScreenProps> = ({
  onBack,
  onTryAgain,
  onGoHome,
  onContactSupport,
  errorMessage,
}) => {
  const navigation = useNavigation<any>();
  const [isRetrying, setIsRetrying] = useState(false);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  const handleTryAgain = async () => {
    setIsRetrying(true);
    setStatusMessage(null);
    try {
      if (onTryAgain) {
        await onTryAgain();
      } else {
        // Simulated network / data reload attempt
        await new Promise((resolve) => setTimeout(resolve, 1200));
      }
      setStatusMessage('Reloading LIVO data...');
      setTimeout(() => {
        setIsRetrying(false);
        if (navigation.canGoBack()) {
          navigation.goBack();
        } else {
          navigation.navigate('MainTabs');
        }
      }, 600);
    } catch (e) {
      setIsRetrying(false);
      setStatusMessage('Unable to resolve error. Please try again.');
    }
  };

  const handleGoHome = () => {
    if (onGoHome) {
      onGoHome();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  const handleContactSupport = () => {
    if (onContactSupport) {
      onContactSupport();
    } else {
      Alert.alert(
        'LIVO Support',
        'Need assistance? Reach our support team anytime at support@livo.app or visit the help center in your Profile.',
        [{ text: 'OK' }]
      );
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
            source={require('../../assets/error_cloud.png')}
            style={styles.illustrationImage}
            resizeMode="contain"
          />
        </View>

        {/* 3. Title & Subtitle */}
        <View style={styles.textContainer}>
          <Text style={styles.title}>Something went wrong</Text>
          <Text style={styles.subtitle}>
            {errorMessage ||
              "We couldn’t load your LIVO data right now. Please check your connection or try again in a few moments."}
          </Text>
          {statusMessage && (
            <Text style={styles.statusText}>{statusMessage}</Text>
          )}
        </View>

        {/* 4. Action Buttons */}
        <View style={styles.buttonsContainer}>
          {/* Primary: Try Again */}
          <TouchableOpacity
            style={[styles.primaryButton, isRetrying && styles.primaryButtonDisabled]}
            onPress={handleTryAgain}
            disabled={isRetrying}
            activeOpacity={0.85}
          >
            {isRetrying ? (
              <ActivityIndicator size="small" color="#FFFFFF" style={{ marginRight: 8 }} />
            ) : (
              <Feather name="refresh-cw" size={18} color="#FFFFFF" style={styles.btnIcon} />
            )}
            <Text style={styles.primaryButtonText}>
              {isRetrying ? 'Retrying...' : 'Try Again'}
            </Text>
          </TouchableOpacity>

          {/* Secondary: Go to Home */}
          <TouchableOpacity
            style={styles.secondaryButton}
            onPress={handleGoHome}
            activeOpacity={0.75}
          >
            <Feather
              name="home"
              size={18}
              color="#0F172A"
              style={styles.btnIcon}
            />
            <Text style={styles.secondaryButtonText}>Go to Home</Text>
          </TouchableOpacity>
        </View>

        {/* 5. "Need help?" Info Card with chevron */}
        <TouchableOpacity
          style={styles.infoCard}
          onPress={handleContactSupport}
          activeOpacity={0.8}
        >
          <View style={styles.infoIconBadge}>
            <Ionicons name="bulb-outline" size={22} color="#22C55E" />
          </View>
          <View style={styles.infoTextWrapper}>
            <Text style={styles.infoTitle}>Need help?</Text>
            <Text style={styles.infoDescription}>
              If this keeps happening, you can contact our support team.
            </Text>
          </View>
          <Feather name="chevron-right" size={20} color="#94A3B8" style={styles.chevronIcon} />
        </TouchableOpacity>
      </ScrollView>
    </SafeAreaView>
  );
};

// Export alias for convenience
export const SomethingWentWrongScreen = GeneralErrorScreen;

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
    marginBottom: 28,
    paddingHorizontal: 12,
  },
  title: {
    fontSize: 26,
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
  statusText: {
    fontSize: 12.5,
    color: '#16A34A',
    marginTop: 10,
    fontWeight: '600',
    textAlign: 'center',
  },

  /* Buttons */
  buttonsContainer: {
    width: '100%',
    marginBottom: 26,
    gap: 12,
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
  primaryButtonDisabled: {
    opacity: 0.8,
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

  /* Need Help Info Card */
  infoCard: {
    flexDirection: 'row',
    alignItems: 'center',
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
  chevronIcon: {
    marginLeft: 8,
  },
});
