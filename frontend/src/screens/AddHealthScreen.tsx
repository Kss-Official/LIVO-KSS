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

interface AddHealthScreenProps {
  onBack?: () => void;
  onSubmit?: (data: any) => void;
}

export const AddHealthScreen: React.FC<AddHealthScreenProps> = ({ onBack, onSubmit }) => {
  const [healthTitle, setHealthTitle] = useState('');
  const [description, setDescription] = useState('');
  const [healthType, setHealthType] = useState<'workout' | 'nutrition' | 'medical' | 'sleep' | 'checkup' | 'mental'>('workout');
  const [date, setDate] = useState('Mon, 2 Sep 2024');
  const [time, setTime] = useState('7:00 AM');
  const [duration, setDuration] = useState('30 Min');
  const [intensity, setIntensity] = useState('Moderate');
  const [goalConnection, setGoalConnection] = useState('Select a goal');
  const [selectedMetric, setSelectedMetric] = useState('Steps');
  const [notes, setNotes] = useState('');
  const [attachments, setAttachments] = useState('');

  const handleSave = () => {
    const data = {
      healthTitle,
      description,
      healthType,
      date,
      time,
      duration,
      intensity,
      goalConnection,
      selectedMetric,
      notes,
      attachments,
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
              <Text style={styles.headerTitle}>Add Health Entry</Text>
              <Text style={styles.headerSubtitle}>Take care today for a better tomorrow.</Text>
            </View>

            <View style={styles.avatarCircle}>
              <Text style={styles.avatarText}>R</Text>
            </View>
          </View>

          {/* 2. Top AI Banner */}
          <View style={styles.aiBanner}>
            <View style={styles.aiBannerLeft}>
              <View style={styles.aiIconWrap}>
                <MaterialCommunityIcons name="magic-staff" size={18} color="#2D6A00" />
              </View>
              <View style={styles.aiTextWrap}>
                <Text style={styles.aiTitle}>Need help tracking your health?</Text>
                <Text style={styles.aiSubtitle}>
                  Tell LIVO what you want to track, and I'll suggest the best routines and goals for you.
                </Text>
              </View>
            </View>

            <TouchableOpacity style={styles.useAiBtn} activeOpacity={0.8}>
              <MaterialCommunityIcons name="magic-staff" size={14} color="#2D6A00" style={{ marginRight: 4 }} />
              <Text style={styles.useAiBtnText}>Use AI</Text>
            </TouchableOpacity>
          </View>

          {/* 3. Health Title */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>
              Health Title <Text style={styles.requiredStar}>*</Text>
            </Text>
            <View style={styles.inputBox}>
              <TextInput
                style={styles.textInput}
                placeholder="e.g. Morning Run, Doctor Appointment, Medicine"
                placeholderTextColor="#94A3B8"
                value={healthTitle}
                onChangeText={setHealthTitle}
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
                placeholder="Add more details..."
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

          {/* 5. Health Type */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>Health Type</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.typeScroll}>
              <TouchableOpacity
                style={[
                  styles.typeCard,
                  healthType === 'workout' && styles.typeCardActive,
                ]}
                onPress={() => setHealthType('workout')}
              >
                <Ionicons
                  name="barbell-outline"
                  size={18}
                  color={healthType === 'workout' ? '#2D6A00' : '#15803D'}
                />
                <Text
                  style={[
                    styles.typeText,
                    healthType === 'workout' && styles.typeTextActive,
                  ]}
                >
                  Workout
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.typeCard,
                  healthType === 'nutrition' && styles.typeCardActive,
                ]}
                onPress={() => setHealthType('nutrition')}
              >
                <Ionicons
                  name="nutrition-outline"
                  size={18}
                  color={healthType === 'nutrition' ? '#2D6A00' : '#DC2626'}
                />
                <Text
                  style={[
                    styles.typeText,
                    healthType === 'nutrition' && styles.typeTextActive,
                  ]}
                >
                  Nutrition
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.typeCard,
                  healthType === 'medical' && styles.typeCardActive,
                ]}
                onPress={() => setHealthType('medical')}
              >
                <Ionicons
                  name="medical-outline"
                  size={18}
                  color={healthType === 'medical' ? '#2D6A00' : '#2563EB'}
                />
                <Text
                  style={[
                    styles.typeText,
                    healthType === 'medical' && styles.typeTextActive,
                  ]}
                >
                  Medical
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.typeCard,
                  healthType === 'sleep' && styles.typeCardActive,
                ]}
                onPress={() => setHealthType('sleep')}
              >
                <Ionicons
                  name="moon-outline"
                  size={18}
                  color={healthType === 'sleep' ? '#2D6A00' : '#7C3AED'}
                />
                <Text
                  style={[
                    styles.typeText,
                    healthType === 'sleep' && styles.typeTextActive,
                  ]}
                >
                  Sleep
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.typeCard,
                  healthType === 'checkup' && styles.typeCardActive,
                ]}
                onPress={() => setHealthType('checkup')}
              >
                <Ionicons
                  name="medkit-outline"
                  size={18}
                  color={healthType === 'checkup' ? '#2D6A00' : '#E11D48'}
                />
                <Text
                  style={[
                    styles.typeText,
                    healthType === 'checkup' && styles.typeTextActive,
                  ]}
                >
                  Checkup
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.typeCard,
                  healthType === 'mental' && styles.typeCardActive,
                ]}
                onPress={() => setHealthType('mental')}
              >
                <Ionicons
                  name="leaf-outline"
                  size={18}
                  color={healthType === 'mental' ? '#2D6A00' : '#0D9488'}
                />
                <Text
                  style={[
                    styles.typeText,
                    healthType === 'mental' && styles.typeTextActive,
                  ]}
                >
                  Mental Health
                </Text>
              </TouchableOpacity>
            </ScrollView>
          </View>

          {/* 6. Date & Time (2-Column Row) */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <Text style={styles.label}>
                Date <Text style={styles.requiredStar}>*</Text>
              </Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Feather name="calendar" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <Text style={styles.selectText}>{date}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>

            <View style={styles.colHalf}>
              <Text style={styles.label}>
                Time <Text style={styles.optionalText}>(optional)</Text>
              </Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Feather name="clock" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <Text style={styles.selectText}>{time}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>
          </View>

          {/* 7. Duration & Intensity (2-Column Row) */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <Text style={styles.label}>
                Duration <Text style={styles.optionalText}>(optional)</Text>
              </Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Ionicons name="stopwatch-outline" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <Text style={styles.selectText}>{duration}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>

            <View style={styles.colHalf}>
              <Text style={styles.label}>
                Intensity <Text style={styles.optionalText}>(for workout)</Text>
              </Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Feather name="bar-chart-2" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <Text style={styles.selectText}>{intensity}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>
          </View>

          {/* 8. Goal Connection (optional) */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>
              Goal Connection <Text style={styles.optionalText}>(optional)</Text>
            </Text>
            <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
              <Ionicons name="disc-outline" size={16} color="#64748B" style={styles.inputLeftIcon} />
              <Text style={styles.selectTextPlaceholder}>{goalConnection}</Text>
              <Feather name="chevron-down" size={16} color="#64748B" />
            </TouchableOpacity>
          </View>

          {/* 9. Add Metrics (optional) */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>
              Add Metrics <Text style={styles.optionalText}>(optional)</Text>
            </Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.pillsScroll}>
              <TouchableOpacity
                style={[
                  styles.pillItem,
                  selectedMetric === 'Steps' && styles.pillActive,
                ]}
                onPress={() => setSelectedMetric('Steps')}
              >
                <Ionicons
                  name="footsteps-outline"
                  size={14}
                  color={selectedMetric === 'Steps' ? '#2D6A00' : '#475569'}
                  style={{ marginRight: 6 }}
                />
                <Text
                  style={[
                    styles.pillText,
                    selectedMetric === 'Steps' && styles.pillTextActive,
                  ]}
                >
                  Steps
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.pillItem,
                  selectedMetric === 'Distance' && styles.pillActive,
                ]}
                onPress={() => setSelectedMetric('Distance')}
              >
                <Feather
                  name="map-pin"
                  size={14}
                  color={selectedMetric === 'Distance' ? '#2D6A00' : '#475569'}
                  style={{ marginRight: 6 }}
                />
                <Text
                  style={[
                    styles.pillText,
                    selectedMetric === 'Distance' && styles.pillTextActive,
                  ]}
                >
                  Distance
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.pillItem,
                  selectedMetric === 'Calories' && styles.pillActive,
                ]}
                onPress={() => setSelectedMetric('Calories')}
              >
                <Ionicons
                  name="flame-outline"
                  size={14}
                  color={selectedMetric === 'Calories' ? '#2D6A00' : '#475569'}
                  style={{ marginRight: 6 }}
                />
                <Text
                  style={[
                    styles.pillText,
                    selectedMetric === 'Calories' && styles.pillTextActive,
                  ]}
                >
                  Calories
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.pillItem,
                  selectedMetric === 'Weight' && styles.pillActive,
                ]}
                onPress={() => setSelectedMetric('Weight')}
              >
                <MaterialCommunityIcons
                  name="scale-bathroom"
                  size={14}
                  color={selectedMetric === 'Weight' ? '#2D6A00' : '#475569'}
                  style={{ marginRight: 6 }}
                />
                <Text
                  style={[
                    styles.pillText,
                    selectedMetric === 'Weight' && styles.pillTextActive,
                  ]}
                >
                  Weight
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.pillItem,
                  selectedMetric === 'Heart Rate' && styles.pillActive,
                ]}
                onPress={() => setSelectedMetric('Heart Rate')}
              >
                <Ionicons
                  name="heart-outline"
                  size={14}
                  color={selectedMetric === 'Heart Rate' ? '#2D6A00' : '#475569'}
                  style={{ marginRight: 6 }}
                />
                <Text
                  style={[
                    styles.pillText,
                    selectedMetric === 'Heart Rate' && styles.pillTextActive,
                  ]}
                >
                  Heart Rate
                </Text>
              </TouchableOpacity>
            </ScrollView>
          </View>

          {/* 10. Notes (optional) */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>
              Notes <Text style={styles.optionalText}>(optional)</Text>
            </Text>
            <View style={styles.selectBox}>
              <Feather name="edit-3" size={16} color="#64748B" style={styles.inputLeftIcon} />
              <TextInput
                style={[styles.textInput, { flex: 1 }]}
                placeholder="Add any notes about your health activity..."
                placeholderTextColor="#94A3B8"
                value={notes}
                onChangeText={setNotes}
              />
            </View>
          </View>

          {/* 11. Add Attachments (optional) */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>
              Add Attachments <Text style={styles.optionalText}>(optional)</Text>
            </Text>
            <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
              <Feather name="paperclip" size={16} color="#64748B" style={styles.inputLeftIcon} />
              <TextInput
                style={[styles.textInput, { flex: 1 }]}
                placeholder="Attach photo, screenshot or file"
                placeholderTextColor="#94A3B8"
                value={attachments}
                onChangeText={setAttachments}
              />
              <Feather name="chevron-right" size={16} color="#64748B" />
            </TouchableOpacity>
          </View>

          {/* 12. Health Insight Banner */}
          <View style={styles.tipCard}>
            <View style={styles.tipLeftCol}>
              <View style={styles.tipIconWrap}>
                <MaterialCommunityIcons name="magic-staff" size={16} color="#2D6A00" />
              </View>
              <View style={styles.tipTextWrap}>
                <Text style={styles.tipTitle}>Health Insight</Text>
                <Text style={styles.tipSubtitle}>
                  Consistent tracking helps you understand your progress and build a healthier you.
                </Text>
              </View>
            </View>

            <TouchableOpacity style={styles.getSuggestionsBtn} activeOpacity={0.8}>
              <Text style={styles.getSuggestionsText}>View Insights</Text>
            </TouchableOpacity>
          </View>

          {/* 13. Bottom Action Buttons */}
          <View style={styles.actionButtonsRow}>
            <TouchableOpacity style={styles.cancelBtn} onPress={onBack} activeOpacity={0.8}>
              <Text style={styles.cancelBtnText}>Cancel</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.createBtn} onPress={handleSave} activeOpacity={0.8}>
              <Text style={styles.createBtnText}>Save Entry</Text>
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

  /* Type Cards */
  typeScroll: {
    flexDirection: 'row',
  },
  typeCard: {
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderRadius: 16,
    marginRight: 8,
    minWidth: 72,
  },
  typeCardActive: {
    backgroundColor: '#F0FDF4',
    borderColor: '#66C400',
  },
  typeText: {
    fontSize: 11.5,
    fontWeight: '600',
    color: '#475569',
    marginTop: 4,
  },
  typeTextActive: {
    color: '#2D6A00',
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
