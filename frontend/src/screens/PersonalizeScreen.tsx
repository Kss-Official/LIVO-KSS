import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  ScrollView,
  Modal,
  TouchableWithoutFeedback,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { SelectionModal } from '../components/forms/SelectionModal';

interface PersonalizeScreenProps {
  onBack?: () => void;
  onComplete?: () => void;
  onSkip?: () => void;
}

export const PersonalizeScreen: React.FC<PersonalizeScreenProps> = ({
  onBack,
  onComplete,
  onSkip,
}: PersonalizeScreenProps) => {
  // Preference values state
  const [language, setLanguage] = useState('English');
  const [timeZone, setTimeZone] = useState('(GMT+05:30)');
  const [timeZoneCountry, setTimeZoneCountry] = useState('India');
  const [notifications, setNotifications] = useState('Enabled');
  const [appearance, setAppearance] = useState('Light');
  const [aiPreference, setAiPreference] = useState('Personalized');

  // Active modal state
  const [activeModal, setActiveModal] = useState<'language' | 'timezone' | 'notifications' | 'appearance' | 'ai' | null>(null);

  const prefs = [
    {
      id: 'language',
      icon: <Ionicons name="globe-outline" size={22} color="#1A202C" />,
      title: 'Language',
      subtitle: 'Choose your preferred language',
      value: language,
      onPress: () => setActiveModal('language'),
    },
    {
      id: 'timezone',
      icon: <Feather name="clock" size={22} color="#1A202C" />,
      title: 'Time zone',
      subtitle: 'Set your local time zone',
      value: timeZone,
      valueSecondLine: timeZoneCountry,
      onPress: () => setActiveModal('timezone'),
    },
    {
      id: 'notifications',
      icon: <Ionicons name="notifications-outline" size={22} color="#1A202C" />,
      title: 'Notifications',
      subtitle: 'Stay updated with what matters',
      value: notifications,
      onPress: () => setActiveModal('notifications'),
    },
    {
      id: 'appearance',
      icon: <MaterialCommunityIcons name="palette-outline" size={22} color="#1A202C" />,
      title: 'Appearance',
      subtitle: 'Choose your theme and style',
      value: appearance,
      onPress: () => setActiveModal('appearance'),
    },
    {
      id: 'ai',
      icon: <MaterialCommunityIcons name="asterisk" size={22} color="#1A202C" />,
      title: 'AI Preferences',
      subtitle: 'Get smarter, more relevant suggestions',
      value: aiPreference,
      onPress: () => setActiveModal('ai'),
    },
  ];

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      {/* Background glow elements */}
      <View style={[styles.bgGlow, { top: -180, right: -60, width: 480, height: 480, borderRadius: 240, backgroundColor: '#C8F07A', opacity: 0.20 }]} />
      <View style={[styles.bgGlow, { top: -120, right: -10, width: 350, height: 350, borderRadius: 175, backgroundColor: '#B5E84E', opacity: 0.14 }]} />
      <View style={[styles.bgGlow, { top: -320, right: -320, width: 800, height: 800, borderRadius: 400, backgroundColor: '#D7F59E', opacity: 0.06 }]} />
      <View style={[styles.bgGlow, { bottom: -200, left: -160, width: 560, height: 560, borderRadius: 280, backgroundColor: '#C8F07A', opacity: 0.18 }]} />
      <View style={[styles.bgGlow, { bottom: -140, left: -100, width: 400, height: 400, borderRadius: 200, backgroundColor: '#B5E84E', opacity: 0.13 }]} />
      <View style={[styles.bgGlow, { bottom: -280, left: -280, width: 720, height: 720, borderRadius: 360, backgroundColor: '#D7F59E', opacity: 0.06 }]} />
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
              <View style={styles.stepBarActive} />
            </View>
            <Text style={styles.stepText}>Step 3 of 3</Text>
          </View>

          <View style={{ width: 38 }} />
        </View>

        {/* Title & Subtitle */}
        <Text style={styles.title}>Let's personalize{'\n'}your experience</Text>
        <Text style={styles.subtitle}>
          Set a few preferences to get the most out of{'\n'}LIVO.
        </Text>

        {/* Preference Cards */}
        <View style={styles.prefList}>
          {prefs.map((item) => (
            <TouchableOpacity
              key={item.id}
              style={styles.prefCard}
              activeOpacity={0.78}
              onPress={item.onPress}
            >
              {/* Icon */}
              <View style={styles.prefIconWrap}>
                {item.icon}
              </View>

              {/* Title + Subtitle */}
              <View style={styles.prefText}>
                <Text style={styles.prefTitle}>{item.title}</Text>
                <Text style={styles.prefSubtitle}>{item.subtitle}</Text>
              </View>

              {/* Value + Chevron */}
              <View style={styles.prefRight}>
                <View style={styles.prefValueWrap}>
                  <Text style={styles.prefValue}>{item.value}</Text>
                  {item.valueSecondLine && (
                    <Text style={styles.prefValueBold}>{item.valueSecondLine}</Text>
                  )}
                </View>
                <Feather name="chevron-right" size={16} color="#718096" style={styles.chevron} />
              </View>
            </TouchableOpacity>
          ))}
        </View>

        <View style={{ height: 120 }} />
      </ScrollView>

      {/* Fixed Footer Area */}
      <View style={styles.footerWrap}>
        <View style={styles.buttonContainer}>
          <TouchableOpacity style={styles.completeButton} onPress={onComplete} activeOpacity={0.88}>
            <Text style={styles.completeText}>Complete Setup</Text>
            <Feather name="arrow-right" size={18} color="#0F172A" />
          </TouchableOpacity>


        </View>

        <TouchableOpacity onPress={onSkip} activeOpacity={0.7} style={styles.skipTouchable}>
          <Text style={styles.skipText}>I'll do this later</Text>
        </TouchableOpacity>
      </View>

      {/* Selection Modals */}
      <SelectionModal
        visible={activeModal === 'language'}
        onClose={() => setActiveModal(null)}
        title="Choose Language"
        options={[
          { label: 'English', value: 'English', icon: 'globe' },
          { label: 'Hindi (हिंदी)', value: 'Hindi', icon: 'globe' },
          { label: 'Spanish (Español)', value: 'Spanish', icon: 'globe' },
          { label: 'French (Français)', value: 'French', icon: 'globe' },
          { label: 'German (Deutsch)', value: 'German', icon: 'globe' },
        ]}
        selectedValue={language}
        onSelect={setLanguage}
      />

      <SelectionModal
        visible={activeModal === 'timezone'}
        onClose={() => setActiveModal(null)}
        title="Set Time Zone"
        options={[
          { label: '(GMT+05:30) India', value: '(GMT+05:30)' },
          { label: '(GMT-05:00) US Eastern Time', value: '(GMT-05:00)' },
          { label: '(GMT+00:00) UTC / London', value: '(GMT+00:00)' },
          { label: '(GMT+01:00) Central Europe', value: '(GMT+01:00)' },
          { label: '(GMT+09:00) Tokyo / Japan', value: '(GMT+09:00)' },
        ]}
        selectedValue={timeZone}
        onSelect={(val) => {
          setTimeZone(val);
          if (val === '(GMT+05:30)') setTimeZoneCountry('India');
          else if (val === '(GMT-05:00)') setTimeZoneCountry('USA');
          else if (val === '(GMT+00:00)') setTimeZoneCountry('UK');
          else if (val === '(GMT+01:00)') setTimeZoneCountry('Europe');
          else setTimeZoneCountry('Japan');
        }}
      />

      <SelectionModal
        visible={activeModal === 'notifications'}
        onClose={() => setActiveModal(null)}
        title="Notifications"
        options={[
          { label: 'Enabled', value: 'Enabled', icon: 'bell' },
          { label: 'Disabled', value: 'Disabled', icon: 'bell-off' },
          { label: 'Quiet Hours (Night Only)', value: 'Quiet Hours', icon: 'moon' },
        ]}
        selectedValue={notifications}
        onSelect={setNotifications}
      />

      <SelectionModal
        visible={activeModal === 'appearance'}
        onClose={() => setActiveModal(null)}
        title="Choose Theme"
        options={[
          { label: 'Light Theme', value: 'Light', icon: 'sun' },
          { label: 'Dark Theme', value: 'Dark', icon: 'moon' },
          { label: 'System Default', value: 'System', icon: 'monitor' },
        ]}
        selectedValue={appearance}
        onSelect={setAppearance}
      />

      <SelectionModal
        visible={activeModal === 'ai'}
        onClose={() => setActiveModal(null)}
        title="AI Preferences"
        options={[
          { label: 'Personalized (Recommended)', value: 'Personalized' },
          { label: 'Standard AI Suggestions', value: 'Standard' },
          { label: 'Minimal AI Interaction', value: 'Minimal' },
        ]}
        selectedValue={aiPreference}
        onSelect={setAiPreference}
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },

  /* Background */
  bgGlow: { position: 'absolute' },
  bgArcCircle: {
    position: 'absolute',
    top: 10,
    right: -100,
    width: 380,
    height: 380,
    borderRadius: 190,
    borderWidth: 1,
    borderColor: 'rgba(149, 230, 18, 0.18)',
  },

  scrollContent: {
    paddingHorizontal: 22,
    paddingBottom: 24,
  },

  /* Top Bar */
  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: 36,
    marginBottom: 18,
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
  stepIndicatorContainer: { alignItems: 'center' },
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
  stepText: {
    fontSize: 10.5,
    color: '#6B7280',
    fontWeight: '500',
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
    marginBottom: 18,
    fontWeight: '400',
  },

  /* Preference List — compact box cards */
  prefList: {
    gap: 9,
  },
  prefCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    borderWidth: 1.2,
    borderColor: '#EFF3E9',
    paddingHorizontal: 20,
    paddingVertical: 11,
    gap: 10,
    shadowColor: '#94A3B8',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 5,
    elevation: 1,
  },
  prefIconWrap: {
    width: 38,
    height: 38,
    borderRadius: 10,
    backgroundColor: '#E7F9BF',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  prefText: {
    flex: 1,
  },
  prefTitle: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 1,
  },
  prefSubtitle: {
    fontSize: 11,
    color: '#8E9BAE',
    lineHeight: 14,
    fontWeight: '400',
  },
  prefRight: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
  },
  prefValueWrap: {
    alignItems: 'flex-end',
  },
  prefValue: {
    fontSize: 12,
    color: '#718096',
    fontWeight: '500',
  },
  prefValueBold: {
    fontSize: 12,
    color: '#1A202C',
    fontWeight: '700',
  },
  chevron: {
    marginLeft: 1,
  },

  /* Footer Wrap — fixed at bottom 34 */
  footerWrap: {
    position: 'absolute',
    bottom: 60,
    left: 22,
    right: 22,
    alignItems: 'center',
  },
  buttonContainer: {
    width: '100%',
    position: 'relative',
    alignItems: 'center',
  },
  completeButton: {
    width: '100%',
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
  completeText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
  },
  skipTouchable: {
    marginTop: 12,
    paddingVertical: 4,
  },
  skipText: {
    fontSize: 13,
    color: '#718096',
    fontWeight: '500',
  },

  /* Decorative organic bubble badge matching reference image */
  badge: {
    position: 'absolute',
    right: -8,
    bottom: -60,
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderRadius: 28,
    borderTopLeftRadius: 32,
    borderBottomLeftRadius: 18,
    borderTopRightRadius: 26,
    borderBottomRightRadius: 28,
    backgroundColor: 'rgba(243, 253, 232, 0.75)',
    borderWidth: 1.5,
    borderColor: '#95E612',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#84CC16',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.12,
    shadowRadius: 6,
    elevation: 4,
    zIndex: 10,
  },
  badgeTextLine1: {
    fontSize: 10.5,
    color: '#4D8000',
    fontWeight: '700',
    textAlign: 'center',
    lineHeight: 13,
  },
  badgeTextLine2: {
    fontSize: 10.5,
    color: '#4D8000',
    fontWeight: '700',
    textAlign: 'center',
    lineHeight: 13,
  },
  badgeUnderlineWrap: {
    alignItems: 'center',
  },
  badgeTextLine3: {
    fontSize: 10.5,
    color: '#4D8000',
    fontWeight: '700',
    textAlign: 'center',
    lineHeight: 13,
  },
  badgeUnderline: {
    width: 48,
    height: 2,
    backgroundColor: '#6DBE00',
    borderRadius: 1,
  },
});
