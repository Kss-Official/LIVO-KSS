import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  ScrollView,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';

interface AllSetScreenProps {
  onGoHome?: () => void;
}

export const AllSetScreen: React.FC<AllSetScreenProps> = ({ onGoHome }: AllSetScreenProps) => {
  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      {/* Background — Softly blurred & feathered green glows on pure white base */}
      {/* Top-Right Feathered Glow */}
      <View style={[styles.bgGlow, { top: -160, right: -100, width: 440, height: 440, borderRadius: 220, backgroundColor: '#D6F5A0', opacity: 0.05 }]} />
      <View style={[styles.bgGlow, { top: -130, right: -70, width: 380, height: 380, borderRadius: 190, backgroundColor: '#D6F5A0', opacity: 0.08 }]} />
      <View style={[styles.bgGlow, { top: -95, right: -35, width: 300, height: 300, borderRadius: 150, backgroundColor: '#D6F5A0', opacity: 0.09 }]} />

      {/* Bottom-Left Feathered Glow */}
      <View style={[styles.bgGlow, { bottom: -130, left: -90, width: 340, height: 340, borderRadius: 170, backgroundColor: '#D6F5A0', opacity: 0.04 }]} />
      <View style={[styles.bgGlow, { bottom: -100, left: -60, width: 280, height: 280, borderRadius: 140, backgroundColor: '#D6F5A0', opacity: 0.07 }]} />
      <View style={[styles.bgGlow, { bottom: -70, left: -30, width: 210, height: 210, borderRadius: 105, backgroundColor: '#D6F5A0', opacity: 0.07 }]} />
      {/* Delicate Top-Right Arc Line */}
      <View style={styles.bgArcCircle} />

      <ScrollView
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        {/* Top Header / Graphic Area */}
        <View style={styles.illustrationArea}>
          {/* Top-left casual badge */}
          <View style={styles.badgeWrap}>
            <Text style={styles.badgeTextLine1}>You're</Text>
            <View style={styles.badgeUnderlineWrap}>
              <Text style={styles.badgeTextLine2}>all set!</Text>
              <View style={styles.badgeUnderline1} />
              <View style={styles.badgeUnderline2} />
            </View>
          </View>

          {/* Central circle graphic with tick marks & white check card */}
          <View style={styles.circleOuter}>
            {/* Exact tick marks & dots matching reference image */}
            {/* Top-left angled pill */}
            <View style={[styles.tickPill, { width: 14, height: 6, top: 12, left: 16, transform: [{ rotate: '-35deg' }] }]} />
            {/* Top small dot */}
            <View style={[styles.tickPill, { width: 7, height: 7, borderRadius: 3.5, top: 6, left: 68 }]} />
            {/* Right horizontal pill */}
            <View style={[styles.tickPill, { width: 12, height: 5, top: 68, right: -4 }]} />
            {/* Bottom-right diamond (tilted square) on arc line */}
            <View style={[styles.tickPill, { width: 12, height: 12, borderRadius: 2.5, bottom: -4, right: 12, transform: [{ rotate: '45deg' }] }]} />
            {/* Bottom-right vertical pill */}
            <View style={[styles.tickPill, { width: 6, height: 14, bottom: 12, right: 38 }]} />
            {/* Bottom-left vertical pill */}
            <View style={[styles.tickPill, { width: 6, height: 14, bottom: 12, left: 38 }]} />
            {/* Left horizontal pill */}
            <View style={[styles.tickPill, { width: 12, height: 5, top: 68, left: -4 }]} />

            {/* Inner White Check Card */}
            <View style={styles.checkCard}>
              <Feather name="check" size={32} color="#66C400" />
            </View>
          </View>
        </View>

        {/* Title & Subtitle */}
        <Text style={styles.title}>Welcome to LIVO!</Text>
        <Text style={styles.subtitle}>
          You're ready to build a more{'\n'}organized, focused and fulfilling life.
        </Text>

        {/* Journey Card */}
        <View style={styles.journeyCard}>
          {/* Card Header */}
          <View style={styles.cardHeaderRow}>
            <MaterialCommunityIcons name="star-four-points" size={20} color="#0F172A" style={styles.sparkleIcon} />
            <View>
              <Text style={styles.cardHeaderTitle}>Your journey starts now</Text>
              <Text style={styles.cardHeaderSubtitle}>Here's what you can do next:</Text>
            </View>
          </View>

          {/* Action List */}
          <View style={styles.actionList}>
            {/* Item 1 */}
            <View style={styles.actionRow}>
              <View style={styles.iconPill}>
                <Ionicons name="disc-outline" size={20} color="#2D6A00" />
              </View>
              <View style={styles.actionTextWrap}>
                <Text style={styles.actionTitle}>Create your first goal</Text>
                <Text style={styles.actionDesc}>Turn your dreams into actionable steps</Text>
              </View>
            </View>

            {/* Item 2 */}
            <View style={styles.actionRow}>
              <View style={styles.iconPill}>
                <Feather name="check-square" size={19} color="#2D6A00" />
              </View>
              <View style={styles.actionTextWrap}>
                <Text style={styles.actionTitle}>Add a task</Text>
                <Text style={styles.actionDesc}>Get things done, one step at a time</Text>
              </View>
            </View>

            {/* Item 3 */}
            <View style={styles.actionRow}>
              <View style={styles.iconPill}>
                <Feather name="calendar" size={19} color="#2D6A00" />
              </View>
              <View style={styles.actionTextWrap}>
                <Text style={styles.actionTitle}>Plan your schedule</Text>
                <Text style={styles.actionDesc}>Stay on top of your time</Text>
              </View>
            </View>

            {/* Item 4 */}
            <View style={styles.actionRow}>
              <View style={styles.iconPill}>
                <Ionicons name="heart-outline" size={19} color="#2D6A00" />
              </View>
              <View style={styles.actionTextWrap}>
                <Text style={styles.actionTitle}>Build a habit</Text>
                <Text style={styles.actionDesc}>Small steps make a big difference</Text>
              </View>
            </View>
          </View>
        </View>

        {/* Space for bottom fixed footer */}
        <View style={{ height: 110 }} />
      </ScrollView>

      {/* Fixed Bottom Button */}
      <View style={styles.footer}>
        <TouchableOpacity style={styles.homeButton} onPress={onGoHome} activeOpacity={0.88}>
          <Text style={styles.homeButtonText}>Go to Home</Text>
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
  bgGlow: { position: 'absolute' },
  bgArcCircle: {
    position: 'absolute',
    top: -10,
    right: -80,
    width: 380,
    height: 380,
    borderRadius: 190,
    borderWidth: 1,
    borderColor: 'rgba(156, 230, 18, 0.25)',
  },

  scrollContent: {
    paddingHorizontal: 22,
    paddingTop: 24,
  },

  /* Illustration Area */
  illustrationArea: {
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 20,
    marginBottom: 20,
    position: 'relative',
    minHeight: 170,
  },

  /* Badge top-left of graphic */
  badgeWrap: {
    position: 'absolute',
    left: 12,
    top: 10,
    alignItems: 'flex-start',
    zIndex: 5,
  },
  badgeTextLine1: {
    fontFamily: 'Caveat_700Bold',
    fontSize: 21,
    color: '#4D8000',
    lineHeight: 22,
  },
  badgeUnderlineWrap: {
    alignItems: 'flex-start',
  },
  badgeTextLine2: {
    fontFamily: 'Caveat_700Bold',
    fontSize: 21,
    color: '#4D8000',
    lineHeight: 22,
  },
  badgeUnderline1: {
    width: 48,
    height: 2.5,
    backgroundColor: '#66C400',
    borderRadius: 1.5,
    marginTop: 2,
  },
  badgeUnderline2: {
    width: 42,
    height: 2.5,
    backgroundColor: '#66C400',
    borderRadius: 1.5,
    marginTop: 2,
  },

  /* Outer soft green ring graphic */
  circleOuter: {
    width: 148,
    height: 148,
    borderRadius: 74,
    backgroundColor: '#EEFAD6',
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
    borderWidth: 1,
    borderColor: '#E2F6C2',
  },

  /* Decorative pills & dots around circle */
  tickPill: {
    position: 'absolute',
    backgroundColor: '#66C400',
  },

  /* Inner White Card */
  checkCard: {
    width: 66,
    height: 66,
    borderRadius: 22,
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#66C400',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.16,
    shadowRadius: 10,
    elevation: 5,
  },

  /* Title & Subtitle */
  title: {
    fontSize: 27,
    fontWeight: '800',
    color: '#0F172A',
    textAlign: 'center',
    letterSpacing: -0.4,
    marginBottom: 6,
  },
  subtitle: {
    fontSize: 13,
    color: '#6B7280',
    textAlign: 'center',
    lineHeight: 19,
    marginBottom: 22,
    fontWeight: '400',
  },

  /* Journey Card */
  journeyCard: {
    backgroundColor: '#F6FCED',
    borderRadius: 22,
    paddingHorizontal: 16,
    paddingVertical: 18,
    borderWidth: 1.2,
    borderColor: '#EFF8E0',
  },
  cardHeaderRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 16,
  },
  sparkleIcon: {
    marginRight: 10,
    marginTop: 2,
  },
  cardHeaderTitle: {
    fontSize: 14.5,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 2,
  },
  cardHeaderSubtitle: {
    fontSize: 12,
    color: '#6B7280',
    fontWeight: '400',
  },

  /* Action List */
  actionList: {
    gap: 12,
  },
  actionRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  iconPill: {
    width: 38,
    height: 38,
    borderRadius: 11,
    backgroundColor: '#E5F8B8',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  actionTextWrap: {
    flex: 1,
  },
  actionTitle: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 1,
  },
  actionDesc: {
    fontSize: 11.5,
    color: '#6B7280',
    fontWeight: '400',
  },

  /* Footer */
  footer: {
    position: 'absolute',
    bottom: 34,
    left: 22,
    right: 22,
  },
  homeButton: {
    height: 50,
    backgroundColor: '#9CE612',
    borderRadius: 25,
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
  homeButtonText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#0F172A',
  },
});
