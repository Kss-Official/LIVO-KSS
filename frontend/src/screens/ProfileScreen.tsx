import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  Image,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  Platform,
  Modal,
  TextInput,
  TouchableWithoutFeedback,
  Switch,
  Alert,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { useNavigation, useFocusEffect } from '@react-navigation/native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useProfile } from '../hooks/useProfile';

type ProfileModalType =
  | 'editProfile'
  | 'lifeAreas'
  | 'aiPreferences'
  | 'notifications'
  | 'appearance'
  | 'language'
  | 'connectedServices'
  | 'privacy'
  | 'help'
  | 'about'
  | 'signOut'
  | null;

export const ProfileScreen: React.FC = () => {
  const navigation = useNavigation<any>();
  const [activeModal, setActiveModal] = useState<ProfileModalType>(null);

  const { profile, updateProfile, refreshProfile } = useProfile();

  useFocusEffect(
    React.useCallback(() => {
      refreshProfile();
    }, [refreshProfile])
  );

  // Temporary edit states
  const [editName, setEditName] = useState(profile.name);
  const [editEmail, setEditEmail] = useState(profile.email);
  const [editBio, setEditBio] = useState(profile.bio);

  // Form error states
  const [nameError, setNameError] = useState('');
  const [emailError, setEmailError] = useState('');
  const [bioError, setBioError] = useState('');

  // Sync temp states when modal opens or profile changes
  useEffect(() => {
    setEditName(profile.name);
    setEditEmail(profile.email);
    setEditBio(profile.bio);
    setNameError('');
    setEmailError('');
    setBioError('');
  }, [profile, activeModal]);

  const handleSaveProfile = async () => {
    let hasError = false;

    // Name validation
    const trimmedName = editName.trim();
    const nameRegex = /^[A-Za-z\s]+$/;
    if (!trimmedName || !nameRegex.test(trimmedName)) {
      setNameError('Please enter a valid name.');
      hasError = true;
    } else {
      setNameError('');
    }

    // Email validation
    const trimmedEmail = editEmail.trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!trimmedEmail || !emailRegex.test(trimmedEmail)) {
      setEmailError('Please enter a valid email address.');
      hasError = true;
    } else {
      setEmailError('');
    }

    // Bio validation
    const trimmedBio = editBio.trim();
    if (!trimmedBio || trimmedBio.length > 150) {
      setBioError('Please enter a valid bio.');
      hasError = true;
    } else {
      setBioError('');
    }

    if (hasError) return;

    await updateProfile({
      name: trimmedName,
      email: trimmedEmail,
      bio: trimmedBio,
    });
    closeModal();
  };

  // Preferences states
  const [selectedLanguage, setSelectedLanguage] = useState('English (US)');
  const [selectedTheme, setSelectedTheme] = useState<'Light' | 'Dark' | 'System'>('Light');
  const [notificationsEnabled, setNotificationsEnabled] = useState(true);
  const [aiSuggestionsEnabled, setAiSuggestionsEnabled] = useState(true);
  const [smartScheduling, setSmartScheduling] = useState(true);
  const [biometricLock, setBiometricLock] = useState(false);

  // Life areas
  const [lifeAreas, setLifeAreas] = useState<Record<string, boolean>>({
    'Health & Fitness': true,
    'Career & Growth': true,
    'Finance & Wealth': true,
    'Personal & Hobbies': true,
    'Learning & Mindset': true,
    'Relationships': false,
  });

  // Connected Services
  const [connectedServices, setConnectedServices] = useState<Record<string, boolean>>({
    'Google Calendar': true,
    'Apple Health': true,
    'Notion': false,
    'Slack': false,
  });

  const toggleLifeArea = (key: string) => {
    setLifeAreas((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  const toggleService = (key: string) => {
    setConnectedServices((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  const closeModal = () => setActiveModal(null);

  const handleSignOut = () => {
    Alert.alert('Sign Out', 'Are you sure you want to sign out?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Sign Out',
        style: 'destructive',
        onPress: async () => {
          closeModal();
          try {
            await AsyncStorage.removeItem('@livo_onboarding_completed');
          } catch (e) {
            console.error('Failed to clear onboarding state on signout:', e);
          }
          navigation.reset({
            index: 0,
            routes: [{ name: 'Welcome' }],
          });
        },
      },
    ]);
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.contentContainer}
        showsVerticalScrollIndicator={false}
      >
        {/* 1. Top Header Row */}
        <View style={styles.headerRow}>
          <View style={{ flexDirection: 'row', alignItems: 'center' }}>
            {navigation.canGoBack() && (
              <TouchableOpacity
                style={{ marginRight: 10, padding: 4 }}
                onPress={() => navigation.goBack()}
                activeOpacity={0.7}
              >
                <Feather name="arrow-left" size={22} color="#0F172A" />
              </TouchableOpacity>
            )}
            <View>
              <Image
                source={require('../../assets/livo_logo.png')}
                style={styles.logoImage}
                resizeMode="contain"
              />
              <Text style={styles.logoSubtitle}>A BETTER YOU</Text>
            </View>
          </View>

          <View style={styles.headerRightActions}>
            <TouchableOpacity style={styles.iconBtn} onPress={() => navigation.navigate('Notifications')}>
              <Feather name="bell" size={20} color="#0F172A" />
              {notificationsEnabled && <View style={styles.notificationBadge} />}
            </TouchableOpacity>

            <TouchableOpacity style={styles.avatarCircle} onPress={() => setActiveModal('editProfile')}>
              <Text style={styles.avatarText}>{profile.name.charAt(0).toUpperCase()}</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* 2. Profile Header Row with Title & Illustration */}
        <View style={styles.profileHeaderRow}>
          <View style={styles.titleSection}>
            <Text style={styles.mainTitle}>Profile</Text>
            <Text style={styles.mainSubtitle}>Your space. Your growth.</Text>
          </View>

          <View style={styles.headerIllustrationGroup}>
            {/* Stacked Handwritten Slogan on Left */}
            <View style={styles.sloganCol}>
              <Text style={styles.sloganLine}>Small</Text>
              <Text style={styles.sloganLine}>Steps</Text>
              <Text style={styles.sloganLine}>Big You!</Text>
              <View style={styles.sloganUnderlineWrap}>
                <View style={styles.sloganUnderline1} />
                <View style={styles.sloganUnderline2} />
              </View>
            </View>

            {/* Boy Illustration overlapping light-green circular background */}
            <View style={styles.boyIllustrationContainer}>
              <View style={styles.boyBgCircle} />
              <Image
                source={require('../../assets/boy_illustration.png')}
                style={styles.boyImage}
                resizeMode="contain"
              />
            </View>
          </View>
        </View>

        {/* 3. User Profile Card */}
        <View style={styles.userCard}>
          <View style={styles.avatarWrap}>
            <View style={styles.userAvatarCircle}>
              <Text style={styles.userAvatarText}>{profile.name.charAt(0).toUpperCase()}</Text>
            </View>
            <View style={styles.statusDot} />
          </View>

          <View style={styles.userInfoCol}>
            <Text style={styles.userNameText}>{profile.name}</Text>
            <Text style={styles.userEmailText}>{profile.email}</Text>
            <Text style={styles.userBioText}>{profile.bio}</Text>
          </View>

          <TouchableOpacity style={styles.editProfileBtn} onPress={() => setActiveModal('editProfile')}>
            <Feather name="edit-2" size={13} color="#0F172A" style={{ marginRight: 4 }} />
            <Text style={styles.editProfileText}>Edit</Text>
          </TouchableOpacity>
        </View>

        {/* 4. Stats Bar */}
        <View style={styles.statsRow}>
          {/* Card 1: Tasks Completed */}
          <View style={styles.statCard}>
            <View style={[styles.statIconBox, { backgroundColor: '#EBF9DB' }]}>
              <Feather name="check" size={12} color="#2D6A00" />
            </View>
            <Text style={styles.statValueText}>18</Text>
            <Text style={styles.statLabelText}>Tasks Done</Text>
            <Text style={[styles.statSubText, { color: '#66C400' }]}>↑ +12%</Text>
          </View>

          {/* Card 2: Active Goals */}
          <View style={styles.statCard}>
            <View style={[styles.statIconBox, { backgroundColor: '#F3EBFB' }]}>
              <Ionicons name="disc-outline" size={12} color="#6D28D9" />
            </View>
            <Text style={styles.statValueText}>3</Text>
            <Text style={styles.statLabelText}>Active Goals</Text>
            <Text style={[styles.statSubText, { color: '#8B5CF6' }]}>↑ +1</Text>
          </View>

          {/* Card 3: Day Streak */}
          <View style={styles.statCard}>
            <View style={[styles.statIconBox, { backgroundColor: '#FFF3E5' }]}>
              <Ionicons name="flame-outline" size={12} color="#C2410C" />
            </View>
            <Text style={styles.statValueText}>12</Text>
            <Text style={styles.statLabelText}>Day Streak</Text>
            <Text style={[styles.statSubText, { color: '#F97316' }]}>↑ +3</Text>
          </View>

          {/* Card 4: Learning This Week */}
          <View style={styles.statCard}>
            <View style={[styles.statIconBox, { backgroundColor: '#EBF3FF' }]}>
              <Feather name="book-open" size={12} color="#1D4ED8" />
            </View>
            <Text style={styles.statValueText}>4h 30m</Text>
            <Text style={styles.statLabelText}>Learning</Text>
            <Text style={[styles.statSubText, { color: '#3B82F6' }]}>↑ +1h</Text>
          </View>
        </View>

        {/* 5. Personalization Section */}
        <View style={styles.groupSection}>
          <Text style={styles.groupTitle}>Personalization</Text>

          <View style={styles.optionsCard}>
            {/* Item 1 */}
            <TouchableOpacity style={styles.optionRow} onPress={() => setActiveModal('lifeAreas')}>
              <View style={[styles.optionIconBox, { backgroundColor: '#EBF9DB' }]}>
                <Feather name="grid" size={16} color="#2D6A00" />
              </View>
              <View style={styles.optionTextCol}>
                <Text style={styles.optionTitle}>My Life Areas</Text>
                <Text style={styles.optionSub}>Choose what matters to you</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>

            <View style={styles.rowDivider} />

            {/* Item 2 */}
            <TouchableOpacity style={styles.optionRow} onPress={() => setActiveModal('aiPreferences')}>
              <View style={[styles.optionIconBox, { backgroundColor: '#F3EBFB' }]}>
                <Ionicons name="sparkles" size={16} color="#7C3AED" />
              </View>
              <View style={styles.optionTextCol}>
                <Text style={styles.optionTitle}>AI Preferences</Text>
                <Text style={styles.optionSub}>Customize how LIVO helps you</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>

            <View style={styles.rowDivider} />

            {/* Item 3 */}
            <TouchableOpacity style={styles.optionRow} onPress={() => setActiveModal('notifications')}>
              <View style={[styles.optionIconBox, { backgroundColor: '#FFEBEB' }]}>
                <Feather name="bell" size={16} color="#DC2626" />
              </View>
              <View style={styles.optionTextCol}>
                <Text style={styles.optionTitle}>Notifications</Text>
                <Text style={styles.optionSub}>Manage your alerts</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>

            <View style={styles.rowDivider} />

            {/* Item 4 */}
            <TouchableOpacity style={styles.optionRow} onPress={() => setActiveModal('appearance')}>
              <View style={[styles.optionIconBox, { backgroundColor: '#FFF3E5' }]}>
                <Feather name="moon" size={16} color="#C2410C" />
              </View>
              <View style={styles.optionTextCol}>
                <Text style={styles.optionTitle}>Appearance</Text>
                <Text style={styles.optionSub}>{selectedTheme} mode</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>

            <View style={styles.rowDivider} />

            {/* Item 5 */}
            <TouchableOpacity style={styles.optionRow} onPress={() => setActiveModal('language')}>
              <View style={[styles.optionIconBox, { backgroundColor: '#EBF3FF' }]}>
                <Feather name="globe" size={16} color="#1D4ED8" />
              </View>
              <View style={styles.optionTextCol}>
                <Text style={styles.optionTitle}>Language</Text>
                <Text style={styles.optionSub}>{selectedLanguage}</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>
          </View>
        </View>

        {/* 6. Account Section */}
        <View style={styles.groupSection}>
          <Text style={styles.groupTitle}>Account</Text>

          <View style={styles.optionsCard}>
            {/* Item 1 */}
            <TouchableOpacity style={styles.optionRow} onPress={() => setActiveModal('connectedServices')}>
              <View style={[styles.optionIconBox, { backgroundColor: '#EBF9DB' }]}>
                <Feather name="link" size={16} color="#2D6A00" />
              </View>
              <View style={styles.optionTextCol}>
                <Text style={styles.optionTitle}>Connected Services</Text>
                <Text style={styles.optionSub}>Link your tools (Google, Calendar, etc.)</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>

            <View style={styles.rowDivider} />

            {/* Item 2 */}
            <TouchableOpacity style={styles.optionRow} onPress={() => setActiveModal('privacy')}>
              <View style={[styles.optionIconBox, { backgroundColor: '#F3EBFB' }]}>
                <Feather name="shield" size={16} color="#7C3AED" />
              </View>
              <View style={styles.optionTextCol}>
                <Text style={styles.optionTitle}>Privacy & Security</Text>
                <Text style={styles.optionSub}>Manage your data and security</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>
          </View>
        </View>

        {/* 7. Support & System Section */}
        <View style={styles.groupSection}>
          <Text style={styles.groupTitle}>System & Updates</Text>

          <View style={styles.optionsCard}>
            {/* Sync & Update */}
            <TouchableOpacity style={styles.optionRow} onPress={() => navigation.navigate('UpdatingLivo')}>
              <View style={[styles.optionIconBox, { backgroundColor: '#EBF9DB' }]}>
                <Feather name="refresh-cw" size={16} color="#2D6A00" />
              </View>
              <View style={styles.optionTextCol}>
                <Text style={styles.optionTitle}>Sync & Update LIVO</Text>
                <Text style={styles.optionSub}>Sync database & fetch updates</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>

            <View style={styles.rowDivider} />

            {/* Offline Status */}
            <TouchableOpacity style={styles.optionRow} onPress={() => navigation.navigate('Offline')}>
              <View style={[styles.optionIconBox, { backgroundColor: '#FFF3E5' }]}>
                <Feather name="wifi-off" size={16} color="#C2410C" />
              </View>
              <View style={styles.optionTextCol}>
                <Text style={styles.optionTitle}>Offline Mode & Sync</Text>
                <Text style={styles.optionSub}>View offline cache & connection status</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>

            <View style={styles.rowDivider} />

            {/* Item 1 */}
            <TouchableOpacity style={styles.optionRow} onPress={() => setActiveModal('help')}>
              <View style={[styles.optionIconBox, { backgroundColor: '#EBF3FF' }]}>
                <Feather name="help-circle" size={16} color="#1D4ED8" />
              </View>
              <View style={styles.optionTextCol}>
                <Text style={styles.optionTitle}>Help & Support</Text>
                <Text style={styles.optionSub}>Get help or contact us</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>

            <View style={styles.rowDivider} />

            {/* Item 2 */}
            <TouchableOpacity style={styles.optionRow} onPress={() => setActiveModal('about')}>
              <View style={[styles.optionIconBox, { backgroundColor: '#FFEBEB' }]}>
                <Feather name="info" size={16} color="#DC2626" />
              </View>
              <View style={styles.optionTextCol}>
                <Text style={styles.optionTitle}>About LIVO</Text>
                <Text style={styles.optionSub}>Version 1.0.0</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>
          </View>
        </View>

        {/* 8. Sign Out Button */}
        <TouchableOpacity style={styles.signOutBtn} onPress={handleSignOut}>
          <Feather name="log-out" size={16} color="#EF4444" style={{ marginRight: 8 }} />
          <Text style={styles.signOutText}>Sign Out</Text>
        </TouchableOpacity>

        <View style={{ height: 40 }} />
      </ScrollView>

      {/* Profile Setting Detail Modals */}
      <Modal visible={activeModal !== null} transparent animationType="fade" onRequestClose={closeModal}>
        <TouchableWithoutFeedback onPress={closeModal}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>

        <View style={styles.modalContent}>
          {/* Edit Profile */}
          {activeModal === 'editProfile' && (
            <View>
              <Text style={styles.modalTitle}>Edit Profile</Text>

              <Text style={styles.inputLabel}>Name</Text>
              <TextInput
                style={[styles.modalInput, !!nameError && styles.modalInputError]}
                value={editName}
                onChangeText={(text) => {
                  setEditName(text);
                  if (nameError) setNameError('');
                }}
                placeholder="Enter your name"
                placeholderTextColor="#94A3B8"
              />
              {!!nameError && <Text style={styles.errorText}>{nameError}</Text>}

              <Text style={styles.inputLabel}>Email</Text>
              <TextInput
                style={[styles.modalInput, !!emailError && styles.modalInputError]}
                value={editEmail}
                onChangeText={(text) => {
                  setEditEmail(text);
                  if (emailError) setEmailError('');
                }}
                keyboardType="email-address"
                autoCapitalize="none"
                placeholder="name@example.com"
                placeholderTextColor="#94A3B8"
              />
              {!!emailError && <Text style={styles.errorText}>{emailError}</Text>}

              <View style={styles.bioLabelRow}>
                <Text style={styles.inputLabel}>Bio</Text>
                <Text style={styles.charCountText}>{editBio.length}/150</Text>
              </View>
              <TextInput
                style={[styles.modalInput, !!bioError && styles.modalInputError]}
                value={editBio}
                maxLength={150}
                onChangeText={(text) => {
                  setEditBio(text);
                  if (bioError) setBioError('');
                }}
                placeholder="Tell us about yourself"
                placeholderTextColor="#94A3B8"
                multiline
              />
              {!!bioError && <Text style={styles.errorText}>{bioError}</Text>}

              <TouchableOpacity style={styles.modalSaveBtn} onPress={handleSaveProfile} activeOpacity={0.85}>
                <Text style={styles.modalSaveText}>Save Changes</Text>
              </TouchableOpacity>
            </View>
          )}

          {/* My Life Areas */}
          {activeModal === 'lifeAreas' && (
            <View>
              <Text style={styles.modalTitle}>My Life Areas</Text>
              <Text style={styles.modalSub}>Select the domains of life you want LIVO to help you manage:</Text>
              {Object.keys(lifeAreas).map((area) => (
                <View key={area} style={styles.switchRow}>
                  <Text style={styles.switchLabel}>{area}</Text>
                  <Switch
                    value={lifeAreas[area]}
                    onValueChange={() => toggleLifeArea(area)}
                    trackColor={{ false: '#CBD5E1', true: '#66C400' }}
                    thumbColor="#f8f7f8ff"
                  />
                </View>
              ))}
              <TouchableOpacity style={styles.modalSaveBtn} onPress={closeModal}>
                <Text style={styles.modalSaveText}>Done</Text>
              </TouchableOpacity>
            </View>
          )}

          {/* AI Preferences */}
          {activeModal === 'aiPreferences' && (
            <View>
              <Text style={styles.modalTitle}>AI Preferences</Text>
              <View style={styles.switchRow}>
                <Text style={styles.switchLabel}>AI Suggestions Banner</Text>
                <Switch value={aiSuggestionsEnabled} onValueChange={setAiSuggestionsEnabled} trackColor={{ false: '#CBD5E1', true: '#66C400' }} thumbColor="#f8f7f8ff" />
              </View>
              <View style={styles.switchRow}>
                <Text style={styles.switchLabel}>Smart Auto-Scheduling</Text>
                <Switch value={smartScheduling} onValueChange={setSmartScheduling} trackColor={{ false: '#CBD5E1', true: '#66C400' }} thumbColor="#f8f7f8ff" />
              </View>
              <TouchableOpacity style={styles.modalSaveBtn} onPress={closeModal}>
                <Text style={styles.modalSaveText}>Save Preferences</Text>
              </TouchableOpacity>
            </View>
          )}

          {/* Notifications */}
          {activeModal === 'notifications' && (
            <View>
              <Text style={styles.modalTitle}>Notifications</Text>
              <View style={styles.switchRow}>
                <Text style={styles.switchLabel}>Push Notifications</Text>
                <Switch value={notificationsEnabled} onValueChange={setNotificationsEnabled} trackColor={{ false: '#CBD5E1', true: '#66C400' }} thumbColor="#f8f7f8ff" />
              </View>
              <TouchableOpacity style={styles.modalSaveBtn} onPress={closeModal}>
                <Text style={styles.modalSaveText}>Done</Text>
              </TouchableOpacity>
            </View>
          )}

          {/* Appearance */}
          {activeModal === 'appearance' && (
            <View>
              <Text style={styles.modalTitle}>Appearance</Text>
              {(['Light', 'Dark', 'System'] as const).map((mode) => (
                <TouchableOpacity
                  key={mode}
                  style={[styles.radioItem, selectedTheme === mode && styles.radioItemSelected]}
                  onPress={() => setSelectedTheme(mode)}
                >
                  <Text style={[styles.radioText, selectedTheme === mode && styles.radioTextSelected]}>{mode} Mode</Text>
                  {selectedTheme === mode && <Feather name="check" size={18} color="#66C400" />}
                </TouchableOpacity>
              ))}
              <TouchableOpacity style={styles.modalSaveBtn} onPress={closeModal}>
                <Text style={styles.modalSaveText}>Apply</Text>
              </TouchableOpacity>
            </View>
          )}

          {/* Language */}
          {activeModal === 'language' && (
            <View>
              <Text style={styles.modalTitle}>Language</Text>
              {['English (US)', 'Hindi (हिंदी)', 'Spanish (Español)', 'French (Français)'].map((lang) => (
                <TouchableOpacity
                  key={lang}
                  style={[styles.radioItem, selectedLanguage === lang && styles.radioItemSelected]}
                  onPress={() => setSelectedLanguage(lang)}
                >
                  <Text style={[styles.radioText, selectedLanguage === lang && styles.radioTextSelected]}>{lang}</Text>
                  {selectedLanguage === lang && <Feather name="check" size={18} color="#66C400" />}
                </TouchableOpacity>
              ))}
              <TouchableOpacity style={styles.modalSaveBtn} onPress={closeModal}>
                <Text style={styles.modalSaveText}>Done</Text>
              </TouchableOpacity>
            </View>
          )}

          {/* Connected Services */}
          {activeModal === 'connectedServices' && (
            <View>
              <Text style={styles.modalTitle}>Connected Services</Text>
              {Object.keys(connectedServices).map((service) => (
                <View key={service} style={styles.switchRow}>
                  <Text style={styles.switchLabel}>{service}</Text>
                  <Switch value={connectedServices[service]} onValueChange={() => toggleService(service)} trackColor={{ false: '#CBD5E1', true: '#66C400' }} thumbColor="#f8f7f8ff" />
                </View>
              ))}
              <TouchableOpacity style={styles.modalSaveBtn} onPress={closeModal}>
                <Text style={styles.modalSaveText}>Done</Text>
              </TouchableOpacity>
            </View>
          )}

          {/* Privacy & Security */}
          {activeModal === 'privacy' && (
            <View>
              <Text style={styles.modalTitle}>Privacy & Security</Text>
              <View style={styles.switchRow}>
                <Text style={styles.switchLabel}>Biometric / Face ID Lock</Text>
                <Switch value={biometricLock} onValueChange={setBiometricLock} trackColor={{ false: '#CBD5E1', true: '#66C400' }} thumbColor="#f8f7f8ff" />
              </View>
              <TouchableOpacity style={styles.modalSaveBtn} onPress={closeModal}>
                <Text style={styles.modalSaveText}>Done</Text>
              </TouchableOpacity>
            </View>
          )}

          {/* Help & Support */}
          {activeModal === 'help' && (
            <View>
              <Text style={styles.modalTitle}>Help & Support</Text>
              <Text style={styles.modalSub}>Need help? Reach out to support@livo.app</Text>
              <TouchableOpacity style={styles.modalSaveBtn} onPress={closeModal}>
                <Text style={styles.modalSaveText}>Close</Text>
              </TouchableOpacity>
            </View>
          )}

          {/* About LIVO */}
          {activeModal === 'about' && (
            <View style={{ alignItems: 'center' }}>
              <Image source={require('../../assets/livo_logo.png')} style={{ width: 120, height: 36, marginBottom: 8 }} resizeMode="contain" />
              <Text style={styles.modalTitle}>LIVO Life OS</Text>
              <Text style={styles.modalSub}>Version 1.0.0 (Build 102)</Text>
              <Text style={[styles.modalSub, { marginTop: 8 }]}>Designed for peak productivity and digital well-being.</Text>
              <TouchableOpacity style={[styles.modalSaveBtn, { width: '100%' }]} onPress={closeModal}>
                <Text style={styles.modalSaveText}>Close</Text>
              </TouchableOpacity>
            </View>
          )}
        </View>
      </Modal>
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
    backgroundColor: '#F8FAFC',
  },
  contentContainer: {
    paddingHorizontal: 16,
    paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) + 14 : 20,
    paddingBottom: 40,
  },

  /* 1. Header */
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  logoImage: {
    width: 77,
    height: 32,

  },
  logoSubtitle: {
    fontSize: 7.5,
    fontWeight: '700',
    color: '#94A3B8',
    letterSpacing: 1.3,
    marginTop: 1,
    marginLeft: 5,
  },
  headerRightActions: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  iconBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#F1F5F9',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 8,
    position: 'relative',
  },
  notificationBadge: {
    position: 'absolute',
    top: 8,
    right: 8,
    width: 7,
    height: 7,
    borderRadius: 3.5,
    backgroundColor: '#EF4444',
  },
  avatarCircle: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#E8F6ED',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 10,
  },
  avatarText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#000000ff',
  },

  /* 2. Profile Header & Illustration */
  profileHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
    marginTop: 4,
    marginBottom: 8,
    minHeight: 104,
  },
  titleSection: {
    justifyContent: 'flex-start',
    paddingBottom: 14,
    flex: 1,
    marginRight: 4,
  },
  mainTitle: {
    fontSize: 28,
    fontWeight: '900',
    color: '#0F172A',
    marginBottom: 3,
  },
  mainSubtitle: {
    fontSize: 13.5,
    color: '#64748B',
  },
  headerIllustrationGroup: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-end',
    position: 'relative',
  },
  sloganCol: {
    alignItems: 'flex-start',
    marginRight: 4,
    marginBottom: 10,
    transform: [{ rotate: '-7deg' }],
  },
  sloganLine: {
    fontFamily: 'Caveat_700Bold',
    fontSize: 18,
    color: '#15803D',
    lineHeight: 18,
    letterSpacing: 0.2,
  },
  sloganUnderlineWrap: {
    marginTop: 3,
    width: 44,
  },
  sloganUnderline1: {
    width: 38,
    height: 2.2,
    backgroundColor: '#16A34A',
    borderRadius: 1.5,
    marginBottom: 2,
  },
  sloganUnderline2: {
    width: 26,
    height: 1.8,
    backgroundColor: '#16A34A',
    borderRadius: 1,
    marginLeft: 4,
  },
  boyIllustrationContainer: {
    width: 114,
    height: 100,
    position: 'relative',
    alignItems: 'center',
    justifyContent: 'flex-end',
  },
  boyBgCircle: {
    position: 'absolute',
    top: 4,
    right: 4,
    width: 90,
    height: 90,
    borderRadius: 45,
    backgroundColor: '#E6F6E3',
  },
  boyImage: {
    width: 116,
    height: 96,
  },

  /* 3. User Profile Card */
  userCard: {
    backgroundColor: '#F1F9E8',
    borderRadius: 18,
    padding: 14,
    borderWidth: 1,
    borderColor: '#E2F2D0',
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  avatarWrap: {
    position: 'relative',
    marginRight: 12,
  },
  userAvatarCircle: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: '#D4F7A5',
    alignItems: 'center',
    justifyContent: 'center',
  },
  userAvatarText: {
    fontSize: 20,
    fontWeight: '800',
    color: '#2D6A00',
  },
  statusDot: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    width: 12,
    height: 12,
    borderRadius: 6,
    backgroundColor: '#0F172A',
    borderWidth: 2,
    borderColor: '#FFFFFF',
  },
  userInfoCol: {
    flex: 1,
    paddingRight: 6,
  },
  userNameText: {
    fontSize: 17,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 2,
  },
  userEmailText: {
    fontSize: 12,
    color: '#64748B',
    marginBottom: 2,
  },
  userBioText: {
    fontSize: 11.5,
    color: '#475569',
  },
  editProfileBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  editProfileText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#0F172A',
  },

  /* 4. Stats Row */
  statsRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 20,
  },
  statCard: {
    backgroundColor: '#FFFFFF',
    width: '23.5%',
    borderRadius: 16,
    padding: 10,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  statIconBox: {
    width: 24,
    height: 24,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 6,
  },
  statValueText: {
    fontSize: 15,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 2,
  },
  statLabelText: {
    fontSize: 10,
    fontWeight: '600',
    color: '#64748B',
    marginBottom: 4,
  },
  statSubText: {
    fontSize: 10,
    fontWeight: '700',
  },

  /* 5, 6, 7. Group Sections */
  groupSection: {
    marginBottom: 18,
  },
  groupTitle: {
    fontSize: 15,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 10,
  },
  optionsCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    overflow: 'hidden',
  },
  optionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    paddingVertical: 12,
  },
  optionIconBox: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  optionTextCol: {
    flex: 1,
  },
  optionTitle: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 1,
  },
  optionSub: {
    fontSize: 11.5,
    color: '#64748B',
  },
  rowDivider: {
    height: 1,
    backgroundColor: '#F1F5F9',
    marginLeft: 58,
  },

  /* 8. Sign Out Button */
  signOutBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FFF5F5',
    borderRadius: 16,
    paddingVertical: 14,
    borderWidth: 1,
    borderColor: '#FFEBEB',
    marginTop: 4,
    marginBottom: 20,
  },
  signOutText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#EF4444',
  },

  /* Modals */
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.4)',
  },
  modalContent: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 24,
    paddingBottom: 40,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 6,
  },
  modalSub: {
    fontSize: 13,
    color: '#64748B',
    marginBottom: 16,
  },
  inputLabel: {
    fontSize: 12.5,
    fontWeight: '700',
    color: '#64748B',
    marginTop: 10,
    marginBottom: 4,
  },
  modalInput: {
    backgroundColor: '#F8FAFC',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 14,
    color: '#0F172A',
  },
  modalInputError: {
    borderColor: '#EF4444',
    backgroundColor: '#FEF2F2',
  },
  errorText: {
    fontSize: 12,
    color: '#EF4444',
    fontWeight: '600',
    marginTop: 4,
  },
  bioLabelRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 10,
    marginBottom: 4,
  },
  charCountText: {
    fontSize: 11.5,
    color: '#94A3B8',
    fontWeight: '500',
  },
  modalSaveBtn: {
    backgroundColor: '#66C400',
    borderRadius: 14,
    paddingVertical: 12,
    alignItems: 'center',
    marginTop: 20,
  },
  modalSaveText: {
    fontSize: 14,
    fontWeight: '800',
    color: '#FFFFFF',
  },
  switchRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#F8FAFC',
  },
  switchLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#334155',
  },
  radioItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: 12,
    borderRadius: 12,
    marginBottom: 6,
    backgroundColor: '#F8FAFC',
  },
  radioItemSelected: {
    backgroundColor: '#EBF9DB',
  },
  radioText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#334155',
  },
  radioTextSelected: {
    color: '#2D6A00',
    fontWeight: '700',
  },
});
