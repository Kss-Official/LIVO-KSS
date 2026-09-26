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
  Dimensions,
  Alert,
  ActivityIndicator,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { useNavigation, useRoute } from '@react-navigation/native';
import { eventService } from '../services/eventService';

export interface EventConflictDetails {
  id?: string;
  title: string;
  time: string;
  location?: string;
  category?: string;
  description?: string;
  date?: string;
}

interface ScheduleConflictScreenProps {
  onBack?: () => void;
  existingEvent?: EventConflictDetails;
  newEvent?: EventConflictDetails;
  suggestedTime?: string;
  suggestionReason?: string;
  onAcceptSuggestion?: () => Promise<void> | void;
  onChooseAnotherTime?: () => void;
  onKeepBoth?: () => void;
}

const { width } = Dimensions.get('window');

export const ScheduleConflictScreen: React.FC<ScheduleConflictScreenProps> = ({
  onBack,
  existingEvent: propExisting,
  newEvent: propNew,
  suggestedTime: propSuggestedTime,
  suggestionReason: propReason,
  onAcceptSuggestion,
  onChooseAnotherTime,
  onKeepBoth,
}) => {
  const navigation = useNavigation<any>();
  const route = useRoute<any>();

  const existingEvent: EventConflictDetails =
    route?.params?.existingEvent ||
    propExisting || {
      title: 'Client Meeting',
      time: '2:00 PM – 3:00 PM',
      location: 'Google Meet',
    };

  const newEvent: EventConflictDetails =
    route?.params?.newEvent ||
    propNew || {
      title: 'Design Review',
      time: '2:00 PM – 3:00 PM',
      location: 'Office',
    };

  const suggestedTime =
    route?.params?.suggestedTime || propSuggestedTime || '3:30 PM – 4:30 PM';
  const suggestionReason =
    route?.params?.suggestionReason ||
    propReason ||
    'This gives you enough time between both events.';

  const [isProcessing, setIsProcessing] = useState(false);

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  const handleAccept = async () => {
    setIsProcessing(true);
    try {
      if (newEvent) {
        // Save or update event with the accepted time
        if (newEvent.id) {
          await eventService.updateEvent({
            id: newEvent.id,
            title: newEvent.title,
            description: newEvent.description,
            startTime: suggestedTime,
            location: newEvent.location,
            category: newEvent.category,
            createdAt: new Date().toISOString(),
          });
        } else {
          await eventService.saveEvent({
            title: newEvent.title,
            description: newEvent.description,
            startTime: suggestedTime,
            location: newEvent.location,
            category: newEvent.category,
            date: newEvent.date || new Date().toISOString(),
          });
        }
      }

      if (onAcceptSuggestion) {
        await onAcceptSuggestion();
      }

      setIsProcessing(false);
      Alert.alert(
        'Schedule Updated 🎉',
        `"${newEvent.title}" has been scheduled for ${suggestedTime}.`,
        [
          {
            text: 'View in Plan',
            onPress: () => navigation.navigate('MainTabs', { screen: 'Plan' }),
          },
        ]
      );
    } catch (e) {
      setIsProcessing(false);
      Alert.alert('Error', 'Could not update schedule. Please try again.');
    }
  };

  const handleChooseAnotherTime = () => {
    if (onChooseAnotherTime) {
      onChooseAnotherTime();
    } else {
      navigation.navigate('AddEvent', { eventData: newEvent });
    }
  };

  const handleKeepBoth = async () => {
    setIsProcessing(true);
    try {
      if (newEvent && !newEvent.id) {
        // Save new event at original conflicting time
        await eventService.saveEvent({
          title: newEvent.title,
          description: newEvent.description,
          startTime: newEvent.time,
          location: newEvent.location,
          category: newEvent.category,
          date: newEvent.date || new Date().toISOString(),
        });
      }

      if (onKeepBoth) {
        await onKeepBoth();
      }

      setIsProcessing(false);
      Alert.alert(
        'Overlap Confirmed',
        `Both "${existingEvent.title}" and "${newEvent.title}" will remain on your schedule at 2:00 PM.`,
        [
          {
            text: 'OK',
            onPress: () => navigation.navigate('MainTabs', { screen: 'Plan' }),
          },
        ]
      );
    } catch (e) {
      setIsProcessing(false);
      Alert.alert('Error', 'Could not save event overlap.');
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
            source={require('../../assets/schedule_conflict.png')}
            style={styles.illustrationImage}
            resizeMode="contain"
          />
        </View>

        {/* 3. Title & Subtitle */}
        <View style={styles.textContainer}>
          <Text style={styles.title}>Schedule conflict</Text>
          <Text style={styles.subtitle}>You already have an event at this time.</Text>
        </View>

        {/* 4. Conflict Cards Container */}
        <View style={styles.cardsContainer}>
          {/* Card 1: Existing Event */}
          <View style={styles.existingCard}>
            <View style={styles.existingIconBadge}>
              <Feather name="calendar" size={18} color="#EF4444" />
            </View>
            <View style={styles.eventTextWrapper}>
              <Text style={styles.existingTag}>Existing Event</Text>
              <Text style={styles.eventTitle}>{existingEvent.title}</Text>
              <Text style={styles.eventTime}>{existingEvent.time}</Text>
              {existingEvent.location && (
                <View style={styles.locationRow}>
                  <Ionicons name="location-outline" size={13} color="#64748B" style={{ marginRight: 3 }} />
                  <Text style={styles.eventLocation}>{existingEvent.location}</Text>
                </View>
              )}
            </View>
          </View>

          {/* Card 2: New Event */}
          <View style={styles.newCard}>
            <View style={styles.newIconBadge}>
              <Feather name="calendar" size={18} color="#3B82F6" />
            </View>
            <View style={styles.eventTextWrapper}>
              <Text style={styles.newTag}>New Event</Text>
              <Text style={styles.eventTitle}>{newEvent.title}</Text>
              <Text style={styles.eventTime}>{newEvent.time}</Text>
              {newEvent.location && (
                <View style={styles.locationRow}>
                  <Ionicons name="location-outline" size={13} color="#64748B" style={{ marginRight: 3 }} />
                  <Text style={styles.eventLocation}>{newEvent.location}</Text>
                </View>
              )}
            </View>
          </View>

          {/* Card 3: LIVO Suggests */}
          <View style={styles.suggestsCard}>
            <View style={styles.suggestsIconBadge}>
              <Ionicons name="sparkles" size={18} color="#16A34A" />
            </View>
            <View style={styles.eventTextWrapper}>
              <Text style={styles.suggestsTag}>LIVO suggests</Text>
              <Text style={styles.suggestsHeadline}>
                Move this event to {suggestedTime}.
              </Text>
              <Text style={styles.suggestsSubtext}>{suggestionReason}</Text>
            </View>
          </View>
        </View>

        {/* 5. Buttons Section */}
        <View style={styles.buttonsContainer}>
          {/* Button 1: Accept Suggestion */}
          <TouchableOpacity
            style={[styles.acceptButton, isProcessing && { opacity: 0.8 }]}
            onPress={handleAccept}
            activeOpacity={0.85}
            disabled={isProcessing}
          >
            {isProcessing ? (
              <ActivityIndicator size="small" color="#FFFFFF" style={{ marginRight: 8 }} />
            ) : (
              <MaterialCommunityIcons
                name="calendar-check-outline"
                size={19}
                color="#FFFFFF"
                style={styles.btnIcon}
              />
            )}
            <Text style={styles.acceptButtonText}>Accept Suggestion</Text>
          </TouchableOpacity>

          {/* Button 2: Choose Another Time */}
          <TouchableOpacity
            style={styles.secondaryButton}
            onPress={handleChooseAnotherTime}
            activeOpacity={0.75}
            disabled={isProcessing}
          >
            <Feather name="clock" size={18} color="#0F172A" style={styles.btnIcon} />
            <Text style={styles.secondaryButtonText}>Choose Another Time</Text>
          </TouchableOpacity>

          {/* Button 3: Keep Both */}
          <TouchableOpacity
            style={styles.keepBothButton}
            onPress={handleKeepBoth}
            activeOpacity={0.75}
            disabled={isProcessing}
          >
            <View style={styles.keepBothTopRow}>
              <Feather name="copy" size={16} color="#0F172A" style={styles.btnIcon} />
              <Text style={styles.secondaryButtonText}>Keep Both</Text>
            </View>
            <Text style={styles.keepBothSubtext}>I'll manage the overlap</Text>
          </TouchableOpacity>
        </View>

        {/* 6. Tip from LIVO */}
        <TouchableOpacity
          style={styles.tipCard}
          onPress={() => {
            Alert.alert(
              'LIVO Balance Tip 💡',
              'Adding 10-15 minute buffers between meetings prevents fatigue and gives you time to review notes.'
            );
          }}
          activeOpacity={0.8}
        >
          <View style={styles.tipIconBadge}>
            <Ionicons name="bulb-outline" size={20} color="#22C55E" />
          </View>
          <View style={styles.tipTextWrapper}>
            <Text style={styles.tipTitle}>Tip from LIVO</Text>
            <Text style={styles.tipDescription}>
              Avoid back-to-back meetings to keep your day balanced.
            </Text>
          </View>
          <Feather name="chevron-right" size={18} color="#94A3B8" />
        </TouchableOpacity>
      </ScrollView>
    </SafeAreaView>
  );
};

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
    height: Math.min(width * 0.58, 220),
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: 6,
  },
  illustrationImage: {
    width: '100%',
    height: '100%',
  },

  /* Text */
  textContainer: {
    alignItems: 'center',
    marginBottom: 18,
    paddingHorizontal: 12,
  },
  title: {
    fontSize: 25,
    fontWeight: '800',
    color: '#0F172A',
    textAlign: 'center',
    marginBottom: 6,
    letterSpacing: -0.3,
  },
  subtitle: {
    fontSize: 14,
    color: '#64748B',
    textAlign: 'center',
  },

  /* Cards */
  cardsContainer: {
    width: '100%',
    gap: 12,
    marginBottom: 20,
  },
  existingCard: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    backgroundColor: '#FFF5F5',
    borderRadius: 18,
    borderWidth: 1,
    borderColor: '#FEE2E2',
    padding: 14,
  },
  existingIconBadge: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#FEE2E2',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
    marginTop: 2,
  },
  existingTag: {
    fontSize: 11,
    fontWeight: '700',
    color: '#EF4444',
    marginBottom: 2,
  },
  newCard: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    backgroundColor: '#F0F7FF',
    borderRadius: 18,
    borderWidth: 1,
    borderColor: '#DBEAFE',
    padding: 14,
  },
  newIconBadge: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#DBEAFE',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
    marginTop: 2,
  },
  newTag: {
    fontSize: 11,
    fontWeight: '700',
    color: '#3B82F6',
    marginBottom: 2,
  },
  suggestsCard: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    backgroundColor: '#EDF8EE',
    borderRadius: 18,
    borderWidth: 1,
    borderColor: '#D4F4DC',
    padding: 14,
  },
  suggestsIconBadge: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#DCFCE7',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
    marginTop: 2,
  },
  suggestsTag: {
    fontSize: 11,
    fontWeight: '800',
    color: '#16A34A',
    marginBottom: 2,
  },
  suggestsHeadline: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 2,
  },
  suggestsSubtext: {
    fontSize: 12,
    color: '#64748B',
    lineHeight: 17,
  },
  eventTextWrapper: {
    flex: 1,
  },
  eventTitle: {
    fontSize: 15,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 2,
  },
  eventTime: {
    fontSize: 12.5,
    color: '#64748B',
    fontWeight: '500',
  },
  locationRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 3,
  },
  eventLocation: {
    fontSize: 12,
    color: '#64748B',
  },

  /* Buttons */
  buttonsContainer: {
    width: '100%',
    gap: 10,
    marginBottom: 18,
  },
  acceptButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#28A745',
    height: 52,
    borderRadius: 26,
    width: '100%',
    shadowColor: '#28A745',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.32,
    shadowRadius: 10,
    elevation: 5,
  },
  acceptButtonText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  secondaryButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#EEF2F6',
    height: 52,
    borderRadius: 26,
    width: '100%',
  },
  secondaryButtonText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
  },
  keepBothButton: {
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#EEF2F6',
    paddingVertical: 9,
    borderRadius: 26,
    width: '100%',
  },
  keepBothTopRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  keepBothSubtext: {
    fontSize: 11,
    color: '#64748B',
    marginTop: 1,
  },
  btnIcon: {
    marginRight: 8,
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
