import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  Platform,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';

export const ProfileScreen: React.FC = () => {
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
          <View>
            <View style={styles.logoRow}>
              <Text style={styles.logoText}>LIVO</Text>
              <View style={styles.logoDot} />
            </View>
            <Text style={styles.logoSubtitle}>A BETTER YOU</Text>
          </View>

          <View style={styles.headerRightActions}>
            <TouchableOpacity style={styles.iconBtn}>
              <Feather name="bell" size={20} color="#0F172A" />
              <View style={styles.notificationBadge} />
            </TouchableOpacity>

            <TouchableOpacity style={styles.avatarCircle}>
              <Text style={styles.avatarText}>R</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* Caveat Handwritten Slogan Banner (Top Right Area) */}
        <View style={styles.sloganWrap}>
          <Text style={styles.sloganText}>Small Steps Big You!</Text>
          <View style={styles.sloganUnderline} />
        </View>

        {/* 2. Title Section */}
        <View style={styles.titleSection}>
          <Text style={styles.mainTitle}>Profile</Text>
          <Text style={styles.mainSubtitle}>Your space. Your growth.</Text>
        </View>

        {/* 3. User Profile Card */}
        <View style={styles.userCard}>
          <View style={styles.avatarWrap}>
            <View style={styles.userAvatarCircle}>
              <Text style={styles.userAvatarText}>M</Text>
            </View>
            <View style={styles.statusDot} />
          </View>

          <View style={styles.userInfoCol}>
            <Text style={styles.userNameText}>Mahesh</Text>
            <Text style={styles.userEmailText}>mahesh.k@example.com</Text>
            <Text style={styles.userBioText}>Designing a better me, everyday</Text>
          </View>

          <TouchableOpacity style={styles.editProfileBtn}>
            <Feather name="edit-2" size={13} color="#0F172A" style={{ marginRight: 4 }} />
            <Text style={styles.editProfileText}>Edit Profile</Text>
          </TouchableOpacity>
        </View>

        {/* 4. Stats Bar (4 Horizontal Stat Cards) */}
        <View style={styles.statsRow}>
          {/* Card 1: Tasks Completed */}
          <View style={styles.statCard}>
            <View style={[styles.statIconBox, { backgroundColor: '#EBF9DB' }]}>
              <Feather name="check" size={12} color="#2D6A00" />
            </View>
            <Text style={styles.statValueText}>18</Text>
            <Text style={styles.statLabelText}>Tasks Completed</Text>
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
            <Text style={styles.statLabelText}>Learning This Week</Text>
            <Text style={[styles.statSubText, { color: '#3B82F6' }]}>↑ +1h</Text>
          </View>
        </View>

        {/* 5. Personalization Section */}
        <View style={styles.groupSection}>
          <Text style={styles.groupTitle}>Personalization</Text>

          <View style={styles.optionsCard}>
            {/* Item 1 */}
            <TouchableOpacity style={styles.optionRow}>
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
            <TouchableOpacity style={styles.optionRow}>
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
            <TouchableOpacity style={styles.optionRow}>
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
            <TouchableOpacity style={styles.optionRow}>
              <View style={[styles.optionIconBox, { backgroundColor: '#FFF3E5' }]}>
                <Feather name="moon" size={16} color="#C2410C" />
              </View>
              <View style={styles.optionTextCol}>
                <Text style={styles.optionTitle}>Appearance</Text>
                <Text style={styles.optionSub}>Light, dark or system</Text>
              </View>
              <Feather name="chevron-right" size={16} color="#94A3B8" />
            </TouchableOpacity>

            <View style={styles.rowDivider} />

            {/* Item 5 */}
            <TouchableOpacity style={styles.optionRow}>
              <View style={[styles.optionIconBox, { backgroundColor: '#EBF3FF' }]}>
                <Feather name="globe" size={16} color="#1D4ED8" />
              </View>
              <View style={styles.optionTextCol}>
                <Text style={styles.optionTitle}>Language</Text>
                <Text style={styles.optionSub}>English (US)</Text>
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
            <TouchableOpacity style={styles.optionRow}>
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
            <TouchableOpacity style={styles.optionRow}>
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

        {/* 7. Support Section */}
        <View style={styles.groupSection}>
          <Text style={styles.groupTitle}>Support</Text>

          <View style={styles.optionsCard}>
            {/* Item 1 */}
            <TouchableOpacity style={styles.optionRow}>
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
            <TouchableOpacity style={styles.optionRow}>
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
        <TouchableOpacity style={styles.signOutBtn}>
          <Feather name="log-out" size={16} color="#EF4444" style={{ marginRight: 8 }} />
          <Text style={styles.signOutText}>Sign Out</Text>
        </TouchableOpacity>

        <View style={{ height: 40 }} />
      </ScrollView>
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
  logoRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  logoText: {
    fontSize: 24,
    fontWeight: '900',
    color: '#0F172A',
    letterSpacing: 0.5,
  },
  logoDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#66C400',
    marginLeft: 2,
    marginTop: 6,
  },
  logoSubtitle: {
    fontSize: 9,
    fontWeight: '700',
    color: '#94A3B8',
    letterSpacing: 1.2,
    marginTop: -2,
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
    backgroundColor: '#E2F7C5',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 10,
  },
  avatarText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#2D6A00',
  },

  /* Slogan Caveat Banner */
  sloganWrap: {
    alignSelf: 'flex-end',
    marginTop: -6,
    marginBottom: 10,
    marginRight: 6,
    alignItems: 'flex-start',
  },
  sloganText: {
    fontFamily: 'Caveat_700Bold',
    fontSize: 20,
    color: '#4D8000',
    lineHeight: 21,
  },
  sloganUnderline: {
    width: 130,
    height: 2.5,
    backgroundColor: '#66C400',
    borderRadius: 1.5,
    marginTop: 1,
  },

  /* 2. Title Section */
  titleSection: {
    marginBottom: 14,
  },
  mainTitle: {
    fontSize: 28,
    fontWeight: '900',
    color: '#0F172A',
    marginBottom: 2,
  },
  mainSubtitle: {
    fontSize: 13.5,
    color: '#64748B',
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
});
