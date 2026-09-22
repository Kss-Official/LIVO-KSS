import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  SafeAreaView,
  StatusBar,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';

interface AddTripScreenProps {
  onBack?: () => void;
  onSubmit?: (data: any) => void;
}

export const AddTripScreen: React.FC<AddTripScreenProps> = ({ onBack, onSubmit }) => {
  const [tripTitle, setTripTitle] = useState('');
  const [description, setDescription] = useState('');
  const [destination, setDestination] = useState('');
  const [tripType, setTripType] = useState('Leisure');
  const [startDate, setStartDate] = useState('Mon, 2 Sep 2024');
  const [endDate, setEndDate] = useState('Fri, 6 Sep 2024');
  const [travelWith, setTravelWith] = useState<'solo' | 'partner' | 'family' | 'friends' | 'work'>('solo');
  const [budget, setBudget] = useState('');
  const [currency, setCurrency] = useState('INR (₹)');
  const [travelMode, setTravelMode] = useState<'flight' | 'train' | 'bus' | 'car' | 'other'>('flight');
  const [accommodation, setAccommodation] = useState('');
  const [linkedGoal, setLinkedGoal] = useState('Select a goal');
  const [notes, setNotes] = useState('');

  const handleSave = () => {
    const data = {
      tripTitle,
      description,
      destination,
      tripType,
      startDate,
      endDate,
      travelWith,
      budget,
      currency,
      travelMode,
      accommodation,
      linkedGoal,
      notes,
    };
    if (onSubmit) {
      onSubmit(data);
    } else if (onBack) {
      onBack();
    }
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        <ScrollView
          style={styles.flex}
          contentContainerStyle={styles.scrollContent}
          showsVerticalScrollIndicator={false}
          keyboardShouldPersistTaps="handled"
        >
          {/* 1. Header Bar */}
          <View style={styles.headerRow}>
            <TouchableOpacity style={styles.backButton} onPress={onBack} activeOpacity={0.7}>
              <Feather name="arrow-left" size={22} color="#0F172A" />
            </TouchableOpacity>

            <View style={styles.headerTitleWrap}>
              <Text style={styles.headerTitle}>Add Trip</Text>
              <Text style={styles.headerSubtitle}>Plan your journey. Make it memorable.</Text>
            </View>

            <View style={styles.avatarCircle}>
              <Text style={styles.avatarText}>R</Text>
            </View>
          </View>

          {/* 2. Top AI Planning Banner */}
          <View style={styles.aiBanner}>
            <View style={styles.aiBannerLeft}>
              <View style={styles.aiIconWrap}>
                <Ionicons name="sparkles" size={16} color="#2D6A00" />
              </View>
              <View style={styles.aiTextWrap}>
                <Text style={styles.aiTitle}>Need help planning this trip?</Text>
                <Text style={styles.aiSubtitle}>
                  Tell LIVO your destination, dates and budget, and I'll help you create an itinerary.
                </Text>
              </View>
            </View>

            <TouchableOpacity style={styles.useAiBtn} activeOpacity={0.8}>
              <Feather name="plus" size={14} color="#2D6A00" style={{ marginRight: 2 }} />
              <Text style={styles.useAiBtnText}>Use AI</Text>
            </TouchableOpacity>
          </View>

          {/* 3. Trip Title */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>
              Trip Title <Text style={styles.requiredStar}>*</Text>
            </Text>
            <View style={styles.inputBox}>
              <TextInput
                style={styles.textInput}
                placeholder="e.g. Goa Vacation"
                placeholderTextColor="#94A3B8"
                value={tripTitle}
                onChangeText={setTripTitle}
              />
            </View>
          </View>

          {/* 4. Description (optional) */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>
              Description <Text style={styles.optionalText}>(optional)</Text>
            </Text>
            <View style={styles.multilineBox}>
              <TextInput
                style={styles.multilineInput}
                placeholder="Add more details about your trip..."
                placeholderTextColor="#94A3B8"
                multiline
                numberOfLines={3}
                maxLength={300}
                value={description}
                onChangeText={setDescription}
              />
              <Text style={styles.charCount}>{description.length}/300</Text>
            </View>
          </View>

          {/* 5. Destination & Trip Type (2-Column Row) */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <Text style={styles.label}>
                Destination <Text style={styles.requiredStar}>*</Text>
              </Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Feather name="map-pin" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <TextInput
                  style={[styles.textInput, { flex: 1 }]}
                  placeholder="e.g. Goa, India"
                  placeholderTextColor="#94A3B8"
                  value={destination}
                  onChangeText={setDestination}
                />
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>

            <View style={styles.colHalf}>
              <Text style={styles.label}>Trip Type</Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Feather name="briefcase" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <Text style={styles.selectText}>{tripType}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>
          </View>

          {/* 6. Start Date & End Date (2-Column Row) */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <Text style={styles.label}>
                Start Date <Text style={styles.requiredStar}>*</Text>
              </Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Feather name="calendar" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <Text style={styles.selectText}>{startDate}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>

            <View style={styles.colHalf}>
              <Text style={styles.label}>
                End Date <Text style={styles.requiredStar}>*</Text>
              </Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Feather name="calendar" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <Text style={styles.selectText}>{endDate}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>
          </View>

          {/* 7. Travel With */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>Travel With</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.pillsScroll}>
              <TouchableOpacity
                style={[
                  styles.pillItem,
                  travelWith === 'solo' && styles.pillActive,
                ]}
                onPress={() => setTravelWith('solo')}
              >
                <Feather
                  name="user-plus"
                  size={14}
                  color={travelWith === 'solo' ? '#2D6A00' : '#475569'}
                  style={{ marginRight: 6 }}
                />
                <Text
                  style={[
                    styles.pillText,
                    travelWith === 'solo' && styles.pillTextActive,
                  ]}
                >
                  Solo
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.pillItem,
                  travelWith === 'partner' && styles.pillActive,
                ]}
                onPress={() => setTravelWith('partner')}
              >
                <Ionicons
                  name="heart-outline"
                  size={14}
                  color={travelWith === 'partner' ? '#2D6A00' : '#475569'}
                  style={{ marginRight: 6 }}
                />
                <Text
                  style={[
                    styles.pillText,
                    travelWith === 'partner' && styles.pillTextActive,
                  ]}
                >
                  Partner
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.pillItem,
                  travelWith === 'family' && styles.pillActive,
                ]}
                onPress={() => setTravelWith('family')}
              >
                <Feather
                  name="users"
                  size={14}
                  color={travelWith === 'family' ? '#2D6A00' : '#475569'}
                  style={{ marginRight: 6 }}
                />
                <Text
                  style={[
                    styles.pillText,
                    travelWith === 'family' && styles.pillTextActive,
                  ]}
                >
                  Family
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.pillItem,
                  travelWith === 'friends' && styles.pillActive,
                ]}
                onPress={() => setTravelWith('friends')}
              >
                <Ionicons
                  name="people-outline"
                  size={14}
                  color={travelWith === 'friends' ? '#2D6A00' : '#475569'}
                  style={{ marginRight: 6 }}
                />
                <Text
                  style={[
                    styles.pillText,
                    travelWith === 'friends' && styles.pillTextActive,
                  ]}
                >
                  Friends
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.pillItem,
                  travelWith === 'work' && styles.pillActive,
                ]}
                onPress={() => setTravelWith('work')}
              >
                <Feather
                  name="briefcase"
                  size={14}
                  color={travelWith === 'work' ? '#2D6A00' : '#475569'}
                  style={{ marginRight: 6 }}
                />
                <Text
                  style={[
                    styles.pillText,
                    travelWith === 'work' && styles.pillTextActive,
                  ]}
                >
                  Work
                </Text>
              </TouchableOpacity>
            </ScrollView>
          </View>

          {/* 8. Budget & Currency (2-Column Row) */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <Text style={styles.label}>
                Budget <Text style={styles.optionalText}>(optional)</Text>
              </Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Text style={[styles.inputLeftIcon, { fontSize: 14, fontWeight: '700', color: '#64748B' }]}>
                  ₹
                </Text>
                <TextInput
                  style={[styles.textInput, { flex: 1 }]}
                  placeholder="e.g. 25000"
                  placeholderTextColor="#94A3B8"
                  keyboardType="numeric"
                  value={budget}
                  onChangeText={setBudget}
                />
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>

            <View style={styles.colHalf}>
              <Text style={styles.label}>Currency</Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Text style={styles.selectText}>{currency}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>
          </View>

          {/* 9. Travel Mode (optional) */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>
              Travel Mode <Text style={styles.optionalText}>(optional)</Text>
            </Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.pillsScroll}>
              <TouchableOpacity
                style={[
                  styles.pillItem,
                  travelMode === 'flight' && styles.pillActive,
                ]}
                onPress={() => setTravelMode('flight')}
              >
                <Ionicons
                  name="airplane-outline"
                  size={14}
                  color={travelMode === 'flight' ? '#2D6A00' : '#475569'}
                  style={{ marginRight: 6 }}
                />
                <Text
                  style={[
                    styles.pillText,
                    travelMode === 'flight' && styles.pillTextActive,
                  ]}
                >
                  Flight
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.pillItem,
                  travelMode === 'train' && styles.pillActive,
                ]}
                onPress={() => setTravelMode('train')}
              >
                <MaterialCommunityIcons
                  name="train"
                  size={14}
                  color={travelMode === 'train' ? '#2D6A00' : '#475569'}
                  style={{ marginRight: 6 }}
                />
                <Text
                  style={[
                    styles.pillText,
                    travelMode === 'train' && styles.pillTextActive,
                  ]}
                >
                  Train
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.pillItem,
                  travelMode === 'bus' && styles.pillActive,
                ]}
                onPress={() => setTravelMode('bus')}
              >
                <MaterialCommunityIcons
                  name="bus"
                  size={14}
                  color={travelMode === 'bus' ? '#2D6A00' : '#475569'}
                  style={{ marginRight: 6 }}
                />
                <Text
                  style={[
                    styles.pillText,
                    travelMode === 'bus' && styles.pillTextActive,
                  ]}
                >
                  Bus
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.pillItem,
                  travelMode === 'car' && styles.pillActive,
                ]}
                onPress={() => setTravelMode('car')}
              >
                <Ionicons
                  name="car-outline"
                  size={14}
                  color={travelMode === 'car' ? '#2D6A00' : '#475569'}
                  style={{ marginRight: 6 }}
                />
                <Text
                  style={[
                    styles.pillText,
                    travelMode === 'car' && styles.pillTextActive,
                  ]}
                >
                  Car
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.pillItem,
                  travelMode === 'other' && styles.pillActive,
                ]}
                onPress={() => setTravelMode('other')}
              >
                <Feather
                  name="more-horizontal"
                  size={14}
                  color={travelMode === 'other' ? '#2D6A00' : '#475569'}
                  style={{ marginRight: 6 }}
                />
                <Text
                  style={[
                    styles.pillText,
                    travelMode === 'other' && styles.pillTextActive,
                  ]}
                >
                  Other
                </Text>
              </TouchableOpacity>
            </ScrollView>
          </View>

          {/* 10. Accommodation & Link to Goal (2-Column Row) */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <Text style={styles.label}>
                Accommodation <Text style={styles.optionalText}>(optional)</Text>
              </Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Ionicons name="bed-outline" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <TextInput
                  style={[styles.textInput, { flex: 1 }]}
                  placeholder="e.g. Hotel, Airbnb, Ho"
                  placeholderTextColor="#94A3B8"
                  value={accommodation}
                  onChangeText={setAccommodation}
                />
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>

            <View style={styles.colHalf}>
              <Text style={styles.label}>
                Link to Goal <Text style={styles.optionalText}>(optional)</Text>
              </Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Ionicons name="disc-outline" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <Text style={styles.selectTextPlaceholder}>{linkedGoal}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>
          </View>

          {/* 11. Add Notes / Attachments */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>
              Add Notes / Attachments <Text style={styles.optionalText}>(optional)</Text>
            </Text>
            <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
              <Feather name="paperclip" size={16} color="#64748B" style={styles.inputLeftIcon} />
              <TextInput
                style={[styles.textInput, { flex: 1 }]}
                placeholder="Add notes, itinerary, or attachments"
                placeholderTextColor="#94A3B8"
                value={notes}
                onChangeText={setNotes}
              />
              <Feather name="chevron-right" size={16} color="#64748B" />
            </TouchableOpacity>
          </View>

          {/* 12. Bottom Travel Tip Card */}
          <View style={styles.tipCard}>
            <View style={styles.tipLeftCol}>
              <View style={styles.tipIconWrap}>
                <Feather name="plus" size={16} color="#2D6A00" />
              </View>
              <View style={styles.tipTextWrap}>
                <Text style={styles.tipTitle}>Travel Tip</Text>
                <Text style={styles.tipSubtitle}>
                  I can suggest the best time to visit, places to explore, budget tips and even create a day-wise plan.
                </Text>
              </View>
            </View>

            <TouchableOpacity style={styles.getSuggestionsBtn} activeOpacity={0.8}>
              <Text style={styles.getSuggestionsText}>Get Suggestions</Text>
            </TouchableOpacity>
          </View>

          {/* 13. Bottom Action Buttons */}
          <View style={styles.actionButtonsRow}>
            <TouchableOpacity style={styles.cancelBtn} onPress={onBack} activeOpacity={0.8}>
              <Text style={styles.cancelBtnText}>Cancel</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.createBtn} onPress={handleSave} activeOpacity={0.8}>
              <Text style={styles.createBtnText}>Create Trip</Text>
            </TouchableOpacity>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  flex: {
    flex: 1,
  },
  scrollContent: {
    paddingHorizontal: 20,
    paddingTop: Platform.OS === 'android' ? 40 : 8,
    paddingBottom: 40,
  },

  /* Header */
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 20,
  },
  backButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  headerTitleWrap: {
    flex: 1,
    marginHorizontal: 12,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: '800',
    color: '#0F172A',
  },
  headerSubtitle: {
    fontSize: 12,
    color: '#64748B',
    marginTop: 2,
  },
  avatarCircle: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#E2F7C5',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#2D6A00',
  },

  /* AI Banner */
  aiBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#F0FDF4',
    borderWidth: 1,
    borderColor: '#DCFCE7',
    borderRadius: 16,
    padding: 14,
    marginBottom: 20,
  },
  aiBannerLeft: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    flex: 1,
    marginRight: 10,
  },
  aiIconWrap: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#DCFCE7',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
    marginTop: 2,
  },
  aiTextWrap: {
    flex: 1,
  },
  aiTitle: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 2,
  },
  aiSubtitle: {
    fontSize: 11.5,
    color: '#475569',
    lineHeight: 16,
  },
  useAiBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#DCFCE7',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 20,
  },
  useAiBtnText: {
    fontSize: 12.5,
    fontWeight: '700',
    color: '#2D6A00',
  },

  /* Form Section & Labels */
  fieldSection: {
    marginBottom: 16,
  },
  label: {
    fontSize: 13,
    fontWeight: '600',
    color: '#0F172A',
    marginBottom: 6,
  },
  requiredStar: {
    color: '#DC2626',
  },
  optionalText: {
    fontWeight: '400',
    color: '#94A3B8',
    fontSize: 12,
  },
  inputBox: {
    height: 48,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 14,
    justifyContent: 'center',
  },
  textInput: {
    fontSize: 14,
    color: '#0F172A',
    padding: 0,
  },

  /* Multiline Input */
  multilineBox: {
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    backgroundColor: '#FFFFFF',
    padding: 12,
    minHeight: 100,
    justifyContent: 'space-between',
  },
  multilineInput: {
    fontSize: 14,
    color: '#0F172A',
    textAlignVertical: 'top',
    padding: 0,
    minHeight: 65,
  },
  charCount: {
    fontSize: 11,
    color: '#94A3B8',
    alignSelf: 'flex-end',
    marginTop: 4,
  },

  /* Two Column Row */
  twoColRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 16,
  },
  colHalf: {
    width: '48.5%',
  },
  selectBox: {
    height: 48,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  inputLeftIcon: {
    marginRight: 8,
  },
  selectText: {
    fontSize: 13.5,
    color: '#0F172A',
    fontWeight: '500',
    flex: 1,
  },
  selectTextPlaceholder: {
    fontSize: 13.5,
    color: '#94A3B8',
    fontWeight: '400',
    flex: 1,
  },

  /* Option Pills */
  pillsScroll: {
    flexDirection: 'row',
  },
  pillItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderRadius: 24,
    marginRight: 8,
  },
  pillActive: {
    backgroundColor: '#F0FDF4',
    borderColor: '#66C400',
  },
  pillText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#475569',
  },
  pillTextActive: {
    color: '#2D6A00',
  },

  /* Tip Card */
  tipCard: {
    backgroundColor: '#F0FDF4',
    borderWidth: 1,
    borderColor: '#DCFCE7',
    borderRadius: 16,
    padding: 14,
    marginTop: 8,
    marginBottom: 24,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  tipLeftCol: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    flex: 1,
    marginRight: 10,
  },
  tipIconWrap: {
    width: 30,
    height: 30,
    borderRadius: 15,
    backgroundColor: '#DCFCE7',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
    marginTop: 2,
  },
  tipTextWrap: {
    flex: 1,
  },
  tipTitle: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 2,
  },
  tipSubtitle: {
    fontSize: 11.5,
    color: '#475569',
    lineHeight: 16,
  },
  getSuggestionsBtn: {
    backgroundColor: '#DCFCE7',
    paddingHorizontal: 12,
    paddingVertical: 9,
    borderRadius: 12,
  },
  getSuggestionsText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#2D6A00',
  },

  /* Action Buttons */
  actionButtonsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: 4,
  },
  cancelBtn: {
    flex: 1,
    height: 48,
    borderRadius: 24,
    backgroundColor: '#F1F5F9',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  cancelBtnText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#0F172A',
  },
  createBtn: {
    flex: 1,
    height: 48,
    borderRadius: 24,
    backgroundColor: '#66C400',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 8,
  },
  createBtnText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#FFFFFF',
  },
});
