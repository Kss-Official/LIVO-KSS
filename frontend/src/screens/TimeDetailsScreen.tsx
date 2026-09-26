import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  Platform,
  DimensionValue,
  Modal,
  TouchableWithoutFeedback,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';

interface TimeDetailsScreenProps {
  onBack?: () => void;
}

export const TimeDetailsScreen: React.FC<TimeDetailsScreenProps> = ({ onBack }) => {
  const navigation = useNavigation<any>();
  const [selectedTimeframe, setSelectedTimeframe] = useState<'This Week' | 'Last Week' | 'This Month'>('This Week');
  const [showTimeframeModal, setShowTimeframeModal] = useState(false);
  const [chartView, setChartView] = useState<'Daily' | 'Weekly' | 'Category'>('Daily');

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  // Legend & Donut category data
  const categories = [
    { label: 'Work', time: '11h 24m', percent: 40, color: '#66C400', icon: 'briefcase-outline' as const, bg: '#DCFCE7' },
    { label: 'Personal', time: '5h 42m', percent: 20, color: '#3B82F6', icon: 'person-outline' as const, bg: '#DBEAFE' },
    { label: 'Learning', time: '4h 16m', percent: 15, color: '#8B5CF6', icon: 'book-outline' as const, bg: '#F3E8FF' },
    { label: 'Health', time: '2h 51m', percent: 10, color: '#EF4444', icon: 'heart-outline' as const, bg: '#FCE7F3' },
    { label: 'Travel', time: '1h 25m', percent: 5, color: '#F97316', icon: 'airplane-outline' as const, bg: '#FFEDD5' },
    { label: 'Others', time: '2h 51m', percent: 10, color: '#94A3B8', icon: 'ellipsis-horizontal-outline' as const, bg: '#F1F5F9' },
  ];

  // Daily Stacked Bars Data for "Time Breakdown"
  // Each day has stack segments from bottom to top: Work, Personal, Learning, Health, Travel, Others
  const dailyStackedData = [
    { day: 'Mon', date: '2 Sep', segments: [2.0, 1.2, 0.8, 0.6, 0.4, 1.0] },
    { day: 'Tue', date: '3 Sep', segments: [1.8, 1.0, 0.9, 0.5, 0.3, 0.9] },
    { day: 'Wed', date: '4 Sep', segments: [2.4, 1.4, 1.0, 0.7, 0.3, 1.2] },
    { day: 'Thu', date: '5 Sep', segments: [2.8, 1.5, 1.2, 0.8, 0.4, 1.3] },
    { day: 'Fri', date: '6 Sep', segments: [3.0, 1.8, 1.3, 0.9, 0.5, 1.4] },
    { day: 'Sat', date: '7 Sep', segments: [1.2, 1.0, 0.6, 0.5, 0.2, 0.8] },
    { day: 'Sun', date: '8 Sep', segments: [1.5, 1.1, 0.7, 0.5, 0.3, 0.9] },
  ];

  // Colors for stacked bar segments (bottom to top)
  const segmentColors = ['#66C400', '#3B82F6', '#8B5CF6', '#EF4444', '#F97316', '#94A3B8'];

  // Calculate segment height percentage relative to max Y-axis 10h (160px height)
  const calculateSegmentHeight = (value: number): DimensionValue => {
    const maxHeight = 160; // total px height for 10h
    const px = (value / 10) * maxHeight;
    return Math.max(px, 3) as DimensionValue;
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.contentContainer}
        showsVerticalScrollIndicator={false}
      >
        {/* 1. Header Navigation Row */}
        <View style={styles.headerRow}>
          <TouchableOpacity
            style={styles.backButton}
            onPress={handleBack}
            activeOpacity={0.7}
            hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
          >
            <Feather name="arrow-left" size={22} color="#0F172A" />
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.timeframePill}
            onPress={() => setShowTimeframeModal(true)}
            activeOpacity={0.8}
          >
            <Text style={styles.timeframeText}>{selectedTimeframe}</Text>
            <Feather name="chevron-down" size={14} color="#64748B" style={{ marginLeft: 4 }} />
          </TouchableOpacity>
        </View>

        {/* 2. Main Page Title */}
        <View style={styles.titleSection}>
          <Text style={styles.mainTitle}>Your Time Details</Text>
          <Text style={styles.mainSubtitle}>See where your time went and find balance.</Text>
        </View>

        {/* 3. Top AI Summary Banner */}
        <View style={styles.aiBannerCard}>
          <View style={styles.aiBannerLeft}>
            <View style={styles.starIconBadge}>
              <Ionicons name="star" size={16} color="#7C3AED" />
            </View>
            <View style={styles.aiBannerTextWrap}>
              <Text style={styles.aiBannerTitle}>You spent most of your time on Work</Text>
              <Text style={styles.aiBannerSub}>
                That's 40% of your week. Great focus!
              </Text>
            </View>
          </View>

          <TouchableOpacity style={styles.askLivoBtn} activeOpacity={0.8}>
            <Text style={styles.askLivoText}>+ Ask LIVO</Text>
          </TouchableOpacity>
        </View>

        {/* 4. Total Time & Donut Grid Row */}
        <View style={styles.twoColumnGrid}>
          {/* Left Column Card: Total Time */}
          <View style={styles.totalTimeCard}>
            <Text style={styles.totalTimeLabel}>Total Time</Text>
            <Text style={styles.totalTimeValue}>28h 30m</Text>

            <View style={styles.trendRow}>
              <Text style={styles.trendGreenText}>↑ 12%</Text>
            </View>
            <Text style={styles.vsLastWeekText}>vs. last week (25h 20m)</Text>
          </View>

          {/* Right Column Card: Donut & Legend */}
          <View style={styles.donutCard}>
            <View style={styles.donutRow}>
              {/* Donut graphic */}
              <View style={styles.donutWrap}>
                <View style={styles.donutOuterRing}>
                  <View style={styles.donutInnerHole}>
                    <Text style={styles.donutCenterValue}>28h</Text>
                    <Text style={styles.donutCenterSub}>30m</Text>
                  </View>
                </View>
              </View>

              {/* Legend column */}
              <View style={styles.legendWrap}>
                {categories.map((c) => (
                  <View key={c.label} style={styles.legendRow}>
                    <View style={[styles.legendDot, { backgroundColor: c.color }]} />
                    <Text style={styles.legendLabelText}>{c.label}</Text>
                    <Text style={styles.legendPercentText}>{c.percent}%</Text>
                  </View>
                ))}
              </View>
            </View>
          </View>
        </View>

        {/* 5. "Time Breakdown" Stacked Bar Chart Card */}
        <View style={styles.whiteCardSection}>
          <View style={styles.cardHeaderRow}>
            <Text style={styles.cardTitle}>Time Breakdown</Text>

            <View style={styles.chartSegmentContainer}>
              {(['Daily', 'Weekly', 'Category'] as const).map((view) => {
                const isActive = chartView === view;
                return (
                  <TouchableOpacity
                    key={view}
                    style={[styles.chartSegmentBtn, isActive && styles.chartSegmentBtnActive]}
                    onPress={() => setChartView(view)}
                  >
                    <Text style={[styles.chartSegmentText, isActive && styles.chartSegmentTextActive]}>
                      {view}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </View>
          </View>

          {/* Y-Axis Grid & Stacked Bars */}
          <View style={styles.chartAreaWrapper}>
            {/* Horizontal Grid lines */}
            <View style={styles.gridLinesContainer}>
              {['10h', '8h', '6h', '4h', '2h', '0h'].map((label) => (
                <View key={label} style={styles.gridLineRow}>
                  <Text style={styles.yAxisLabel}>{label}</Text>
                  <View style={styles.gridDashLine} />
                </View>
              ))}
            </View>

            {/* Stacked Bars Row */}
            <View style={styles.barsRowContainer}>
              {dailyStackedData.map((item) => (
                <View key={item.day} style={styles.barColumn}>
                  <View style={styles.stackedBarTrack}>
                    {item.segments.map((val, idx) => (
                      <View
                        key={idx}
                        style={[
                          styles.barSegment,
                          {
                            height: calculateSegmentHeight(val),
                            backgroundColor: segmentColors[idx],
                            borderTopLeftRadius: idx === item.segments.length - 1 ? 6 : 0,
                            borderTopRightRadius: idx === item.segments.length - 1 ? 6 : 0,
                            borderBottomLeftRadius: idx === 0 ? 6 : 0,
                            borderBottomRightRadius: idx === 0 ? 6 : 0,
                          },
                        ]}
                      />
                    ))}
                  </View>

                  <Text style={styles.barDayText}>{item.day}</Text>
                  <Text style={styles.barDateText}>{item.date}</Text>
                </View>
              ))}
            </View>
          </View>
        </View>

        {/* 6. "Category Insights" List Card */}
        <View style={styles.whiteCardSection}>
          <Text style={styles.cardTitle}>Category Insights</Text>

          <View style={styles.categoryList}>
            {categories.map((cat) => (
              <View key={cat.label} style={styles.categoryRowItem}>
                <View style={[styles.categoryIconSquare, { backgroundColor: cat.bg }]}>
                  <Ionicons name={cat.icon} size={16} color={cat.color} />
                </View>

                <Text style={styles.categoryTitleText}>{cat.label}</Text>

                <Text style={styles.categoryTimeText}>{cat.time}</Text>
                <Text style={styles.categoryPercentText}>{cat.percent}%</Text>

                {/* Horizontal Progress Bar */}
                <View style={styles.categoryProgressTrack}>
                  <View
                    style={[
                      styles.categoryProgressFill,
                      { width: `${cat.percent}%`, backgroundColor: cat.color },
                    ]}
                  />
                </View>

                <Feather name="chevron-right" size={14} color="#CBD5E1" style={{ marginLeft: 6 }} />
              </View>
            ))}
          </View>
        </View>

        {/* 7. Bottom LIVO Insight Card */}
        <View style={styles.livoInsightBanner}>
          <View style={styles.insightHeaderRow}>
            <View style={styles.insightStarBadge}>
              <Ionicons name="star" size={14} color="#7C3AED" />
            </View>
            <Text style={styles.insightHeaderTitle}>LIVO Insight</Text>
          </View>

          <Text style={styles.insightBodyQuote}>
            “You're spending 40% of your time on work, Consider allocating a bit more time to health and personal activities to maintain balance.”
          </Text>

          <View style={styles.getSuggestionsWrap}>
            <TouchableOpacity style={styles.getSuggestionsBtn} activeOpacity={0.8}>
              <Text style={styles.getSuggestionsText}>Get Suggestions →</Text>
            </TouchableOpacity>
          </View>
        </View>

        <View style={{ height: 40 }} />
      </ScrollView>

      {/* Modal: Timeframe Selection */}
      <Modal
        visible={showTimeframeModal}
        transparent
        animationType="fade"
        onRequestClose={() => setShowTimeframeModal(false)}
      >
        <TouchableWithoutFeedback onPress={() => setShowTimeframeModal(false)}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>
        <View style={styles.dropdownModalContent}>
          <Text style={styles.dropdownModalTitle}>Select Timeframe</Text>
          {(['This Week', 'Last Week', 'This Month'] as const).map((item) => (
            <TouchableOpacity
              key={item}
              style={styles.dropdownItem}
              onPress={() => {
                setSelectedTimeframe(item);
                setShowTimeframeModal(false);
              }}
            >
              <Text
                style={[
                  styles.dropdownItemText,
                  selectedTimeframe === item && styles.dropdownItemTextSelected,
                ]}
              >
                {item}
              </Text>
              {selectedTimeframe === item && <Feather name="check" size={16} color="#66C400" />}
            </TouchableOpacity>
          ))}
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
    backgroundColor: '#FAFBFD',
  },
  contentContainer: {
    paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) + 10 : 16,
    paddingHorizontal: 16,
    paddingBottom: 40,
  },

  /* 1. Header */
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 14,
  },
  backButton: {
    width: 38,
    height: 38,
    borderRadius: 19,
    backgroundColor: '#F1F5F9',
    alignItems: 'center',
    justifyContent: 'center',
  },
  timeframePill: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 20,
    paddingHorizontal: 14,
    paddingVertical: 7,
  },
  timeframeText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
  },

  /* 2. Title Section */
  titleSection: {
    marginBottom: 16,
  },
  mainTitle: {
    fontSize: 28,
    fontWeight: '900',
    color: '#0F172A',
    letterSpacing: -0.5,
  },
  mainSubtitle: {
    fontSize: 13.5,
    color: '#64748B',
    marginTop: 2,
  },

  /* 3. AI Banner Card */
  aiBannerCard: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#F5EFFF',
    borderRadius: 18,
    padding: 14,
    borderWidth: 1,
    borderColor: '#EDE9FE',
    marginBottom: 16,
  },
  aiBannerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    paddingRight: 8,
  },
  starIconBadge: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#ECE6FF',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  aiBannerTextWrap: {
    flex: 1,
  },
  aiBannerTitle: {
    fontSize: 13.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  aiBannerSub: {
    fontSize: 11.5,
    color: '#64748B',
    marginTop: 1,
  },
  askLivoBtn: {
    backgroundColor: '#ECE6FF',
    borderWidth: 1,
    borderColor: '#DDD6FE',
    borderRadius: 18,
    paddingHorizontal: 12,
    paddingVertical: 7,
  },
  askLivoText: {
    fontSize: 11.5,
    fontWeight: '800',
    color: '#7C3AED',
  },

  /* 4. Two Column Grid */
  twoColumnGrid: {
    flexDirection: 'row',
    marginBottom: 16,
  },
  totalTimeCard: {
    flex: 0.9,
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 14,
    borderWidth: 1,
    borderColor: '#F1F5F9',
    marginRight: 10,
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.03,
    shadowRadius: 6,
    elevation: 2,
  },
  totalTimeLabel: {
    fontSize: 12.5,
    fontWeight: '600',
    color: '#64748B',
  },
  totalTimeValue: {
    fontSize: 22,
    fontWeight: '900',
    color: '#0F172A',
    marginVertical: 4,
  },
  trendRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  trendGreenText: {
    fontSize: 13,
    fontWeight: '800',
    color: '#66C400',
  },
  vsLastWeekText: {
    fontSize: 10.5,
    color: '#94A3B8',
    marginTop: 2,
  },

  donutCard: {
    flex: 1.1,
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 12,
    borderWidth: 1,
    borderColor: '#F1F5F9',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.03,
    shadowRadius: 6,
    elevation: 2,
  },
  donutRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  donutWrap: {
    marginRight: 8,
  },
  donutOuterRing: {
    width: 60,
    height: 60,
    borderRadius: 30,
    borderWidth: 6,
    borderColor: '#66C400',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FFFFFF',
  },
  donutInnerHole: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  donutCenterValue: {
    fontSize: 12,
    fontWeight: '900',
    color: '#0F172A',
    lineHeight: 13,
  },
  donutCenterSub: {
    fontSize: 9,
    color: '#64748B',
    lineHeight: 10,
  },
  legendWrap: {
    flex: 1,
  },
  legendRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 2,
  },
  legendDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
    marginRight: 4,
  },
  legendLabelText: {
    fontSize: 10.5,
    color: '#475569',
    fontWeight: '600',
    flex: 1,
  },
  legendPercentText: {
    fontSize: 10.5,
    fontWeight: '700',
    color: '#0F172A',
  },

  /* 5. White Card Sections */
  whiteCardSection: {
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    padding: 16,
    borderWidth: 1,
    borderColor: '#F1F5F9',
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.03,
    shadowRadius: 6,
    elevation: 2,
  },
  cardHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 14,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
  },
  chartSegmentContainer: {
    flexDirection: 'row',
    backgroundColor: '#F1F5F9',
    borderRadius: 16,
    padding: 2,
  },
  chartSegmentBtn: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 14,
  },
  chartSegmentBtnActive: {
    backgroundColor: '#DCFCE7',
  },
  chartSegmentText: {
    fontSize: 11,
    fontWeight: '600',
    color: '#64748B',
  },
  chartSegmentTextActive: {
    color: '#166534',
    fontWeight: '800',
  },

  /* Stacked Bar Chart Area */
  chartAreaWrapper: {
    position: 'relative',
    height: 210,
    paddingTop: 10,
  },
  gridLinesContainer: {
    position: 'absolute',
    left: 0,
    right: 0,
    top: 10,
    bottom: 40,
    justifyContent: 'space-between',
  },
  gridLineRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  yAxisLabel: {
    width: 26,
    fontSize: 10,
    color: '#94A3B8',
    textAlign: 'right',
    marginRight: 6,
  },
  gridDashLine: {
    flex: 1,
    height: 1,
    borderWidth: 0.5,
    borderColor: '#F1F5F9',
    borderStyle: 'dashed',
  },

  barsRowContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'flex-end',
    height: 160,
    marginLeft: 32,
    marginTop: 6,
  },
  barColumn: {
    alignItems: 'center',
    width: 32,
  },
  stackedBarTrack: {
    width: 14,
    height: 160,
    justifyContent: 'flex-end',
    backgroundColor: 'transparent',
  },
  barSegment: {
    width: '100%',
    marginBottom: 1,
  },
  barDayText: {
    fontSize: 11,
    fontWeight: '700',
    color: '#475569',
    marginTop: 6,
  },
  barDateText: {
    fontSize: 9.5,
    color: '#94A3B8',
    marginTop: 1,
  },

  /* 6. Category Insights List */
  categoryList: {
    marginTop: 10,
  },
  categoryRowItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#F8FAFC',
  },
  categoryIconSquare: {
    width: 30,
    height: 30,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  categoryTitleText: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
    width: 70,
  },
  categoryTimeText: {
    fontSize: 11.5,
    color: '#64748B',
    width: 60,
  },
  categoryPercentText: {
    fontSize: 11.5,
    color: '#94A3B8',
    width: 36,
  },
  categoryProgressTrack: {
    flex: 1,
    height: 6,
    backgroundColor: '#F1F5F9',
    borderRadius: 3,
    overflow: 'hidden',
    marginHorizontal: 6,
  },
  categoryProgressFill: {
    height: '100%',
    borderRadius: 3,
  },

  /* 7. Bottom LIVO Insight Banner */
  livoInsightBanner: {
    backgroundColor: '#F5EFFF',
    borderRadius: 18,
    padding: 16,
    borderWidth: 1,
    borderColor: '#EDE9FE',
  },
  insightHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  insightStarBadge: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: '#ECE6FF',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  insightHeaderTitle: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0F172A',
  },
  insightBodyQuote: {
    fontSize: 12,
    color: '#475569',
    lineHeight: 18,
    fontStyle: 'italic',
    marginBottom: 12,
  },
  getSuggestionsWrap: {
    alignItems: 'flex-end',
  },
  getSuggestionsBtn: {
    backgroundColor: '#ECE6FF',
    borderWidth: 1,
    borderColor: '#DDD6FE',
    borderRadius: 16,
    paddingHorizontal: 14,
    paddingVertical: 7,
  },
  getSuggestionsText: {
    fontSize: 11.5,
    fontWeight: '800',
    color: '#7C3AED',
  },

  /* Modal Styles */
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.4)',
  },
  dropdownModalContent: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 16,
    marginHorizontal: 30,
    marginTop: 'auto',
    marginBottom: 'auto',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 10,
    elevation: 6,
  },
  dropdownModalTitle: {
    fontSize: 15,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 12,
  },
  dropdownItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  dropdownItemText: {
    fontSize: 13.5,
    color: '#475569',
    fontWeight: '600',
  },
  dropdownItemTextSelected: {
    color: '#66C400',
    fontWeight: '800',
  },
});
