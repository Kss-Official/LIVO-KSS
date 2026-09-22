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

interface AddLearningScreenProps {
  onBack?: () => void;
  onSubmit?: (data: any) => void;
}

export const AddLearningScreen: React.FC<AddLearningScreenProps> = ({ onBack, onSubmit }) => {
  const [learningTitle, setLearningTitle] = useState('');
  const [description, setDescription] = useState('');
  const [learningType, setLearningType] = useState<'course' | 'book' | 'video' | 'skill' | 'mentorship'>('course');
  const [category, setCategory] = useState('Design & Creative');
  const [difficulty, setDifficulty] = useState('Beginner');
  const [startDate, setStartDate] = useState('Mon, 2 Sep 2024');
  const [targetDate, setTargetDate] = useState('Select date');
  const [studyTime, setStudyTime] = useState('30 Min');
  const [repeat, setRepeat] = useState('Daily');
  const [learningGoal, setLearningGoal] = useState('');
  const [resources, setResources] = useState('');
  const [notes, setNotes] = useState('');

  const handleSave = () => {
    const data = {
      learningTitle,
      description,
      learningType,
      category,
      difficulty,
      startDate,
      targetDate,
      studyTime,
      repeat,
      learningGoal,
      resources,
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
              <Text style={styles.headerTitle}>Add Learning</Text>
              <Text style={styles.headerSubtitle}>Invest in yourself. Learn something new.</Text>
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
                <Text style={styles.aiTitle}>Need help planning your learning?</Text>
                <Text style={styles.aiSubtitle}>
                  Tell LIVO what you want to learn, and I'll suggest resources, a study plan and milestones.
                </Text>
              </View>
            </View>

            <TouchableOpacity style={styles.useAiBtn} activeOpacity={0.8}>
              <Feather name="plus" size={14} color="#2D6A00" style={{ marginRight: 2 }} />
              <Text style={styles.useAiBtnText}>Use AI</Text>
            </TouchableOpacity>
          </View>

          {/* 3. Learning Title */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>
              Learning Title <Text style={styles.requiredStar}>*</Text>
            </Text>
            <View style={styles.inputBox}>
              <TextInput
                style={styles.textInput}
                placeholder="e.g. Learn UI/UX Design"
                placeholderTextColor="#94A3B8"
                value={learningTitle}
                onChangeText={setLearningTitle}
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
                placeholder="What do you want to learn? Why is it important to you?"
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

          {/* 5. Learning Type */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>Learning Type</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.typeScroll}>
              <TouchableOpacity
                style={[
                  styles.typeCard,
                  learningType === 'course' && styles.typeCardActive,
                ]}
                onPress={() => setLearningType('course')}
              >
                <Feather
                  name="book-open"
                  size={18}
                  color={learningType === 'course' ? '#2D6A00' : '#475569'}
                />
                <Text
                  style={[
                    styles.typeText,
                    learningType === 'course' && styles.typeTextActive,
                  ]}
                >
                  Course
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.typeCard,
                  learningType === 'book' && styles.typeCardActive,
                ]}
                onPress={() => setLearningType('book')}
              >
                <Feather
                  name="book"
                  size={18}
                  color={learningType === 'book' ? '#2563EB' : '#475569'}
                />
                <Text
                  style={[
                    styles.typeText,
                    learningType === 'book' && styles.typeTextActive,
                  ]}
                >
                  Book
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.typeCard,
                  learningType === 'video' && styles.typeCardActive,
                ]}
                onPress={() => setLearningType('video')}
              >
                <Feather
                  name="video"
                  size={18}
                  color={learningType === 'video' ? '#DC2626' : '#475569'}
                />
                <Text
                  style={[
                    styles.typeText,
                    learningType === 'video' && styles.typeTextActive,
                  ]}
                >
                  Video Series
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.typeCard,
                  learningType === 'skill' && styles.typeCardActive,
                ]}
                onPress={() => setLearningType('skill')}
              >
                <Ionicons
                  name="school-outline"
                  size={18}
                  color={learningType === 'skill' ? '#7C3AED' : '#475569'}
                />
                <Text
                  style={[
                    styles.typeText,
                    learningType === 'skill' && styles.typeTextActive,
                  ]}
                >
                  Skill Practice
                </Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={[
                  styles.typeCard,
                  learningType === 'mentorship' && styles.typeCardActive,
                ]}
                onPress={() => setLearningType('mentorship')}
              >
                <Feather
                  name="user-check"
                  size={18}
                  color={learningType === 'mentorship' ? '#0D9488' : '#475569'}
                />
                <Text
                  style={[
                    styles.typeText,
                    learningType === 'mentorship' && styles.typeTextActive,
                  ]}
                >
                  Mentorship
                </Text>
              </TouchableOpacity>
            </ScrollView>
          </View>

          {/* 6. Category & Difficulty Level (2-Column Row) */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <Text style={styles.label}>Category</Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Feather name="grid" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <Text style={styles.selectText}>{category}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>

            <View style={styles.colHalf}>
              <Text style={styles.label}>Difficulty Level</Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Feather name="bar-chart-2" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <Text style={styles.selectText}>{difficulty}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>
          </View>

          {/* 7. Start Date & Target Completion Date (2-Column Row) */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <Text style={styles.label}>Start Date</Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Feather name="calendar" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <Text style={styles.selectText}>{startDate}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>

            <View style={styles.colHalf}>
              <Text style={styles.label}>
                Target Completion Date <Text style={styles.optionalText}>(optional)</Text>
              </Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Feather name="calendar" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <Text style={styles.selectTextPlaceholder}>{targetDate}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>
          </View>

          {/* 8. Daily / Weekly Study Time & Repeat (2-Column Row) */}
          <View style={styles.twoColRow}>
            <View style={styles.colHalf}>
              <Text style={styles.label}>Daily / Weekly Study Time</Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Feather name="clock" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <Text style={styles.selectText}>{studyTime}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>

            <View style={styles.colHalf}>
              <Text style={styles.label}>Repeat</Text>
              <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
                <Feather name="repeat" size={16} color="#64748B" style={styles.inputLeftIcon} />
                <Text style={styles.selectText}>{repeat}</Text>
                <Feather name="chevron-down" size={16} color="#64748B" />
              </TouchableOpacity>
            </View>
          </View>

          {/* 9. Learning Goal (optional) */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>
              Learning Goal <Text style={styles.optionalText}>(optional)</Text>
            </Text>
            <View style={styles.selectBox}>
              <Ionicons name="disc-outline" size={16} color="#64748B" style={styles.inputLeftIcon} />
              <TextInput
                style={[styles.textInput, { flex: 1 }]}
                placeholder="e.g. Complete the full course and build 3 projects"
                placeholderTextColor="#94A3B8"
                value={learningGoal}
                onChangeText={setLearningGoal}
              />
            </View>
          </View>

          {/* 10. Add Resources (optional) */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>
              Add Resources <Text style={styles.optionalText}>(optional)</Text>
            </Text>
            <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
              <Feather name="paperclip" size={16} color="#64748B" style={styles.inputLeftIcon} />
              <TextInput
                style={[styles.textInput, { flex: 1 }]}
                placeholder="Add a course link, video, book or notes"
                placeholderTextColor="#94A3B8"
                value={resources}
                onChangeText={setResources}
              />
              <Feather name="chevron-right" size={16} color="#64748B" />
            </TouchableOpacity>
          </View>

          {/* 11. Add Notes (optional) */}
          <View style={styles.fieldSection}>
            <Text style={styles.label}>
              Add Notes <Text style={styles.optionalText}>(optional)</Text>
            </Text>
            <TouchableOpacity style={styles.selectBox} activeOpacity={0.8}>
              <Feather name="edit-3" size={16} color="#64748B" style={styles.inputLeftIcon} />
              <TextInput
                style={[styles.textInput, { flex: 1 }]}
                placeholder="Any additional notes..."
                placeholderTextColor="#94A3B8"
                value={notes}
                onChangeText={setNotes}
              />
              <Feather name="chevron-right" size={16} color="#64748B" />
            </TouchableOpacity>
          </View>

          {/* 12. Learning Insight Banner */}
          <View style={styles.tipCard}>
            <View style={styles.tipLeftCol}>
              <View style={styles.tipIconWrap}>
                <Feather name="plus" size={16} color="#2D6A00" />
              </View>
              <View style={styles.tipTextWrap}>
                <Text style={styles.tipTitle}>Learning Insight</Text>
                <Text style={styles.tipSubtitle}>
                  Consistent learning, even for 30 minutes a day, can make a big difference over time.
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
              <Text style={styles.createBtnText}>Create Learning</Text>
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
