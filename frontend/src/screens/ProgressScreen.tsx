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
import { Feather, Ionicons } from '@expo/vector-icons';

interface ProgressScreenProps {
  onBack?: () => void;
}

export const ProgressScreen: React.FC<ProgressScreenProps> = ({ onBack }) => {
  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      {/* Header Bar */}
      <View style={styles.headerBar}>
        <TouchableOpacity
          style={styles.backBtn}
          onPress={onBack}
          hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
        >
          <Feather name="arrow-left" size={22} color="#0F172A" />
        </TouchableOpacity>

        <View style={styles.headerTitleWrap}>
          <Text style={styles.headerTitle}>Today's Progress</Text>
          <Text style={styles.headerSub}>
            Track your small steps, see the bigger picture.
          </Text>
        </View>
      </View>

      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.contentContainer}
        showsVerticalScrollIndicator={false}
      >
        {/* Date & Filter Row */}
        <View style={styles.dateBarRow}>
          <TouchableOpacity style={styles.dateNavBtn}>
            <Feather name="chevron-left" size={16} color="#475569" />
          </TouchableOpacity>

          <TouchableOpacity style={styles.datePickerPill}>
            <Feather name="calendar" size={14} color="#64748B" style={{ marginRight: 6 }} />
            <Text style={styles.datePickerText}>Today, Mon 2 Sep</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.dateNavBtn}>
            <Feather name="chevron-right" size={16} color="#475569" />
          </TouchableOpacity>

          <TouchableOpacity style={styles.filterDropdownPill}>
            <Text style={styles.filterDropdownText}>This Week</Text>
            <Feather name="chevron-down" size={14} color="#2D6A00" style={{ marginLeft: 4 }} />
          </TouchableOpacity>
        </View>

        {/* Motivational Banner */}
        <View style={styles.bannerCard}>
          <View style={styles.bannerLeft}>
            <View style={styles.bannerIconCircle}>
              <Ionicons name="sparkles" size={16} color="#7C3AED" />
            </View>
            <View style={styles.bannerTextWrap}>
              <Text style={styles.bannerTitle}>You're making progress!</Text>
              <Text style={styles.bannerSub}>
                Keep going, small steps{'\n'}every day lead to big results.
              </Text>
            </View>
          </View>

          {/* Handwritten callout */}
          <View style={styles.handwrittenBadge}>
            <Text style={styles.handwrittenText}>Progress</Text>
            <Text style={styles.handwrittenText2}>looks good!</Text>
            <View style={styles.handwrittenUnderline} />
          </View>
        </View>

        {/* Overall Progress Card */}
        <View style={styles.overallCard}>
          <Text style={styles.cardTitle}>Overall Progress</Text>

          <View style={styles.overallBodyRow}>
            {/* Left Circular Ring Gauge */}
            <View style={styles.gaugeWrap}>
              <View style={styles.gaugeOuterRing}>
                <View style={styles.gaugeInnerCircle}>
                  <Text style={styles.gaugePercent}>67%</Text>
                  <Text style={styles.gaugeLabel}>Done today</Text>
                </View>
              </View>
            </View>

            {/* Right Side Breakdown */}
            <View style={styles.breakdownCol}>
              {/* Tasks row */}
              <View style={styles.breakdownRow}>
                <View style={styles.breakdownLabelRow}>
                  <View style={[styles.miniIconBadge, { backgroundColor: '#E2F7C5' }]}>
                    <Feather name="check" size={10} color="#2D6A00" />
                  </View>
                  <Text style={styles.breakdownLabel}>Tasks</Text>
                </View>
                <Text style={styles.breakdownRatio}>3/5</Text>
              </View>
              <View style={styles.miniTrack}>
                <View style={[styles.miniFill, { width: '60%', backgroundColor: '#66C400' }]} />
              </View>

              {/* Habits row */}
              <View style={styles.breakdownRow}>
                <View style={styles.breakdownLabelRow}>
                  <View style={[styles.miniIconBadge, { backgroundColor: '#FFEDD5' }]}>
                    <Ionicons name="stats-chart" size={10} color="#C2410C" />
                  </View>
                  <Text style={styles.breakdownLabel}>Habits</Text>
                </View>
                <Text style={styles.breakdownRatio}>2/4</Text>
              </View>
              <View style={styles.miniTrack}>
                <View style={[styles.miniFill, { width: '50%', backgroundColor: '#F97316' }]} />
              </View>

              {/* Goals row */}
              <View style={styles.breakdownRow}>
                <View style={styles.breakdownLabelRow}>
                  <View style={[styles.miniIconBadge, { backgroundColor: '#F3E8FF' }]}>
                    <Ionicons name="disc" size={10} color="#7C3AED" />
                  </View>
                  <Text style={styles.breakdownLabel}>Goals</Text>
                </View>
                <Text style={styles.breakdownRatio}>1/3</Text>
              </View>
              <View style={styles.miniTrack}>
                <View style={[styles.miniFill, { width: '33%', backgroundColor: '#8B5CF6' }]} />
              </View>
            </View>
          </View>
        </View>

        {/* Details Section */}
        <View style={styles.detailsHeaderRow}>
          <Text style={styles.detailsTitle}>Details</Text>
          <TouchableOpacity style={styles.seeAllBtn}>
            <Text style={styles.seeAllText}>See all</Text>
            <Feather name="arrow-right" size={14} color="#66C400" style={{ marginLeft: 2 }} />
          </TouchableOpacity>
        </View>

        {/* Card 1: Tasks */}
        <TouchableOpacity style={styles.detailCard}>
          <View style={styles.detailHeaderRow}>
            <View style={[styles.detailIconCircle, { backgroundColor: '#E2F7C5' }]}>
              <Feather name="check" size={18} color="#66C400" />
            </View>
            <View style={styles.detailTitleWrap}>
              <Text style={styles.detailItemTitle}>Tasks</Text>
              <Text style={styles.detailItemSub}>3 of 5 tasks completed</Text>
            </View>
            <Feather name="chevron-right" size={18} color="#94A3B8" />
          </View>

          <View style={styles.detailProgressRow}>
            <View style={styles.detailTrack}>
              <View style={[styles.detailFill, { width: '60%', backgroundColor: '#66C400' }]} />
            </View>
            <Text style={styles.detailRatioText}>3/5</Text>
          </View>
        </TouchableOpacity>

        {/* Card 2: Habits */}
        <TouchableOpacity style={styles.detailCard}>
          <View style={styles.detailHeaderRow}>
            <View style={[styles.detailIconCircle, { backgroundColor: '#FFEDD5' }]}>
              <Ionicons name="stats-chart" size={18} color="#F97316" />
            </View>
            <View style={styles.detailTitleWrap}>
              <Text style={styles.detailItemTitle}>Habits</Text>
              <Text style={styles.detailItemSub}>2 of 4 habits done</Text>
            </View>
            <Feather name="chevron-right" size={18} color="#94A3B8" />
          </View>

          <View style={styles.detailProgressRow}>
            <View style={styles.detailTrack}>
              <View style={[styles.detailFill, { width: '50%', backgroundColor: '#F97316' }]} />
            </View>
            <Text style={styles.detailRatioText}>2/4</Text>
          </View>
        </TouchableOpacity>

        {/* Card 3: Goals */}
        <TouchableOpacity style={styles.detailCard}>
          <View style={styles.detailHeaderRow}>
            <View style={[styles.detailIconCircle, { backgroundColor: '#F3E8FF' }]}>
              <Ionicons name="disc-outline" size={18} color="#8B5CF6" />
            </View>
            <View style={styles.detailTitleWrap}>
              <Text style={styles.detailItemTitle}>Goals</Text>
              <Text style={styles.detailItemSub}>1 of 3 goals progressing</Text>
            </View>
            <Feather name="chevron-right" size={18} color="#94A3B8" />
          </View>

          <View style={styles.detailProgressRow}>
            <View style={styles.detailTrack}>
              <View style={[styles.detailFill, { width: '33%', backgroundColor: '#8B5CF6' }]} />
            </View>
            <Text style={styles.detailRatioText}>1/3</Text>
          </View>
        </TouchableOpacity>

        {/* Quote Card */}
        <View style={styles.quoteCard}>
          <Text style={styles.quoteMark}>“</Text>
          <View style={styles.quoteContent}>
            <Text style={styles.quoteText}>
              Small steps every day lead to big results.
            </Text>
            <View style={styles.quoteUnderline} />
          </View>
        </View>

        <View style={{ height: 30 }} />
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#F8FAF5',
  },

  /* Header Bar */
  headerBar: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 12,
    paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) + 8 : 12,
    backgroundColor: '#F8FAF5',
  },
  backBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  headerTitleWrap: {
    flex: 1,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: '800',
    color: '#0F172A',
  },
  headerSub: {
    fontSize: 13,
    color: '#64748B',
    marginTop: 1,
  },

  container: {
    flex: 1,
  },
  contentContainer: {
    paddingHorizontal: 16,
    paddingTop: 8,
  },

  /* Date Bar */
  dateBarRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  dateNavBtn: {
    width: 34,
    height: 34,
    borderRadius: 12,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    alignItems: 'center',
    justifyContent: 'center',
  },
  datePickerPill: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 14,
    paddingVertical: 8,
    marginHorizontal: 8,
  },
  datePickerText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
  },
  filterDropdownPill: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#E2F7C5',
    borderRadius: 14,
    paddingHorizontal: 12,
    paddingVertical: 8,
    marginLeft: 4,
  },
  filterDropdownText: {
    fontSize: 12.5,
    fontWeight: '700',
    color: '#2D6A00',
  },

  /* Motivational Banner */
  bannerCard: {
    backgroundColor: '#F3F0FF',
    borderRadius: 18,
    padding: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderWidth: 1,
    borderColor: '#E9D5FF',
    marginBottom: 16,
  },
  bannerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  bannerIconCircle: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#E9D5FF',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  bannerTextWrap: {
    flex: 1,
  },
  bannerTitle: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0F172A',
  },
  bannerSub: {
    fontSize: 11.5,
    color: '#64748B',
    marginTop: 2,
    lineHeight: 16,
  },
  handwrittenBadge: {
    alignItems: 'center',
    transform: [{ rotate: '-3deg' }],
  },
  handwrittenText: {
    fontSize: 13,
    fontFamily: Platform.OS === 'web' ? 'Caveat, cursive' : 'Caveat_700Bold',
    fontWeight: '700',
    color: '#2D6A00',
  },
  handwrittenText2: {
    fontSize: 13,
    fontFamily: Platform.OS === 'web' ? 'Caveat, cursive' : 'Caveat_700Bold',
    fontWeight: '700',
    color: '#2D6A00',
    marginTop: -4,
  },
  handwrittenUnderline: {
    width: '80%',
    height: 2,
    backgroundColor: '#2D6A00',
    borderRadius: 1,
    marginTop: 0,
  },

  /* Overall Progress Card */
  overallCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    padding: 16,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 20,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 14,
  },
  overallBodyRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  gaugeWrap: {
    width: 105,
    height: 105,
    marginRight: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  gaugeOuterRing: {
    width: 100,
    height: 100,
    borderRadius: 50,
    borderWidth: 9,
    borderColor: '#66C400',
    borderTopColor: '#E2E8F0',
    alignItems: 'center',
    justifyContent: 'center',
    transform: [{ rotate: '-45deg' }],
  },
  gaugeInnerCircle: {
    transform: [{ rotate: '45deg' }],
    alignItems: 'center',
  },
  gaugePercent: {
    fontSize: 20,
    fontWeight: '900',
    color: '#0F172A',
  },
  gaugeLabel: {
    fontSize: 10,
    color: '#94A3B8',
    marginTop: -2,
  },

  breakdownCol: {
    flex: 1,
  },
  breakdownRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  breakdownLabelRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  miniIconBadge: {
    width: 16,
    height: 16,
    borderRadius: 4,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 6,
  },
  breakdownLabel: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
  },
  breakdownRatio: {
    fontSize: 12,
    fontWeight: '700',
    color: '#64748B',
  },
  miniTrack: {
    height: 6,
    backgroundColor: '#F1F5F9',
    borderRadius: 3,
    marginBottom: 10,
    overflow: 'hidden',
  },
  miniFill: {
    height: '100%',
    borderRadius: 3,
  },

  /* Details Section */
  detailsHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  detailsTitle: {
    fontSize: 17,
    fontWeight: '800',
    color: '#0F172A',
  },
  seeAllBtn: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  seeAllText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#66C400',
  },

  /* Detail Card */
  detailCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 12,
  },
  detailHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  detailIconCircle: {
    width: 38,
    height: 38,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  detailTitleWrap: {
    flex: 1,
  },
  detailItemTitle: {
    fontSize: 14.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  detailItemSub: {
    fontSize: 12,
    color: '#94A3B8',
    marginTop: 1,
  },

  detailProgressRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  detailTrack: {
    flex: 1,
    height: 8,
    backgroundColor: '#F1F5F9',
    borderRadius: 4,
    marginRight: 10,
    overflow: 'hidden',
  },
  detailFill: {
    height: '100%',
    borderRadius: 4,
  },
  detailRatioText: {
    fontSize: 12.5,
    fontWeight: '800',
    color: '#64748B',
  },

  /* Quote Box */
  quoteCard: {
    backgroundColor: '#F1F9E8',
    borderRadius: 16,
    padding: 14,
    borderWidth: 1,
    borderColor: '#E2F2D0',
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 8,
  },
  quoteMark: {
    fontSize: 28,
    color: '#66C400',
    fontWeight: '900',
    marginRight: 8,
    lineHeight: 30,
  },
  quoteContent: {
    flex: 1,
  },
  quoteText: {
    fontSize: 15,
    fontFamily: Platform.OS === 'web' ? 'Caveat, cursive' : 'Caveat_700Bold',
    fontWeight: '700',
    color: '#2D6A00',
  },
  quoteUnderline: {
    width: '60%',
    height: 2,
    backgroundColor: '#2D6A00',
    borderRadius: 1,
    marginTop: 1,
  },
});
