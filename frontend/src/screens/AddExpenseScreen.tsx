import React, { useState } from 'react';
import { DatePickerField, TimePickerField, SelectionModal, AttachmentPicker, SelectionOption, GoalPickerField } from '../components/forms';
import { CreationSuccessModal } from '../components/ui/CreationSuccessModal';

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
import { Feather, Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';

import { useEvents } from '../hooks/useEvents';

interface AddExpenseScreenProps {
  onBack?: () => void;
  onSubmit?: (data: any) => void;
}

export const AddExpenseScreen: React.FC<AddExpenseScreenProps> = ({ onBack, onSubmit }) => {
  const navigation = useNavigation<any>();

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  const { addEvent } = useEvents();
  const [amount, setAmount] = useState('');
  const [currency, setCurrency] = useState('INR');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [selectedTime, setSelectedTime] = useState<Date>(new Date());
  const [duration, setDuration] = useState('30 Min');
  const [location, setLocation] = useState('');
  const [meetingType, setMeetingType] = useState<'in_person' | 'online' | 'phone'>('online');
  const [selectedCategory, setSelectedCategory] = useState('Food & Dining');
  const [paymentMethod, setPaymentMethod] = useState('Cash');
  const [linkedGoal, setLinkedGoal] = useState('Build a strong portfolio');
  const [reminder, setReminder] = useState('No reminder');
  const [repeat, setRepeat] = useState('Does not repeat');
  const [attachments, setAttachments] = useState<any[]>([]);

  // Modals
  const [showDurationModal, setShowDurationModal] = useState(false);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [showCategoryModal, setShowCategoryModal] = useState(false);
  const [showGoalModal, setShowGoalModal] = useState(false);
  const [showReminderModal, setShowReminderModal] = useState(false);
  const [showRepeatModal, setShowRepeatModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [createdItem, setCreatedItem] = useState<any>(null);

  const categories = [
    { name: 'Food & Dining', icon: 'utensils', lib: 'feather' },
    { name: 'Transport', icon: 'car-outline', lib: 'ionicons' },
    { name: 'Shopping', icon: 'bag-handle-outline', lib: 'ionicons' },
    { name: 'Bills & Utilities', icon: 'file-text', lib: 'feather' },
    { name: 'Health', icon: 'heart-outline', lib: 'ionicons' },
    { name: 'Travel', icon: 'airplane-outline', lib: 'ionicons' },
    { name: 'Education', icon: 'school-outline', lib: 'ionicons' },
    { name: 'Entertainment', icon: 'game-controller-outline', lib: 'ionicons' },
    { name: 'Groceries', icon: 'cart-outline', lib: 'ionicons' },
    { name: 'Personal Care', icon: 'sparkles-outline', lib: 'ionicons' },
    { name: 'Work', icon: 'briefcase-outline', lib: 'ionicons' },
    { name: 'Others', icon: 'ellipsis-horizontal-outline', lib: 'ionicons' },
  ];

  const durationOptions: SelectionOption[] = [
    { label: '15 Min', value: '15 Min' },
    { label: '30 Min', value: '30 Min' },
    { label: '45 Min', value: '45 Min' },
    { label: '1 Hour', value: '1 Hour' },
    { label: '1.5 Hours', value: '1.5 Hours' },
    { label: '2 Hours', value: '2 Hours' },
    { label: 'Custom', value: 'Custom' },
  ];

  const paymentOptions: SelectionOption[] = [
    { label: 'Cash', value: 'Cash' },
    { label: 'Credit Card', value: 'Credit Card' },
    { label: 'Debit Card', value: 'Debit Card' },
    { label: 'UPI / NetBanking', value: 'UPI / NetBanking' },
  ];

  const meetingTypeOptions: Array<{
    label: string;
    value: 'in_person' | 'online' | 'phone';
    icon: string;
  }> = [
      { label: 'In person', value: 'in_person', icon: 'user' },
      { label: 'Online', value: 'online', icon: 'video' },
      { label: 'Phone', value: 'phone', icon: 'phone' },
    ];

  const handleSave = async () => {
    const data = {
      amount,
      currency,
      title,
      description,
      date: selectedDate,
      time: selectedTime,
      duration,
      location,
      meetingType,
      category: selectedCategory,
      paymentMethod,
      linkedGoal,
      reminder,
      repeat,
      attachments,
    };

    await addEvent({
      title: title.trim() || `Expense: ${currency}${amount}`,
      description: description || `Payment: ${paymentMethod}`,
      date: selectedDate.toISOString(),
      startTime: selectedTime.toISOString(),
      category: 'Expense',
    });

    if (onSubmit) {
      onSubmit(data);
    }
    
    setCreatedItem({ 
      title: title.trim() || `Expense: ${currency}${amount}`, 
      date: selectedDate, 
      time: selectedTime, 
      category: selectedCategory 
    });
    setShowSuccessModal(true);
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
          {/* Header Bar */}
          <View style={styles.headerRow}>
            <TouchableOpacity style={styles.backButton} onPress={handleBack} activeOpacity={0.7}>
              <Feather name="arrow-left" size={22} color="#0F172A" />
            </TouchableOpacity>

            <View style={styles.headerTitleWrap}>
              <Text style={styles.headerTitle}>Add Expense</Text>
              <Text style={styles.headerSubtitle}>Track your spending. Stay in control.</Text>
            </View>

            <TouchableOpacity style={styles.avatarCircle} onPress={() => navigation.navigate('Profile')}>
              <Text style={styles.avatarText}>R</Text>
            </TouchableOpacity>
          </View>

          {/* Form Content */}
          <View style={styles.formCard}>
            {/* Amount & Currency */}
            <Text style={styles.label}>Amount <Text style={styles.requiredStar}>*</Text></Text>
            <View style={styles.amountInputRow}>
              <TouchableOpacity style={styles.currencyBadge} activeOpacity={0.7}>
                <Text style={styles.currencyText}>{currency === 'INR' ? '₹' : '$'}</Text>
                <Feather name="chevron-down" size={14} color="#64748B" />
              </TouchableOpacity>

              <TextInput
                style={styles.amountInput}
                placeholder="0.00"
                placeholderTextColor="#CBD5E1"
                keyboardType="decimal-pad"
                value={amount}
                onChangeText={setAmount}
              />
            </View>

            {/* Expense Title */}
            <Text style={styles.label}>Expense Title <Text style={styles.requiredStar}>*</Text></Text>
            <TextInput
              style={styles.textInput}
              placeholder="e.g. Dinner with team"
              placeholderTextColor="#94A3B8"
              value={title}
              onChangeText={setTitle}
            />

            {/* Description */}
            <Text style={styles.label}>Description <Text style={styles.optionalText}>(optional)</Text></Text>
            <View style={styles.textAreaWrap}>
              <TextInput
                style={styles.textArea}
                placeholder="Add more details..."
                placeholderTextColor="#94A3B8"
                multiline
                numberOfLines={4}
                maxLength={300}
                value={description}
                onChangeText={setDescription}
                textAlignVertical="top"
              />
              <Text style={styles.charCount}>{description.length}/300</Text>
            </View>

            {/* Date & Time Row 1 */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <DatePickerField value={selectedDate} onChange={setSelectedDate} />
              </View>
              <View style={styles.col}>
                <TimePickerField value={selectedTime} onChange={setSelectedTime} />
              </View>
            </View>

            {/* Duration & Repeat Row 2 */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <Text style={styles.label}>Duration</Text>
                <TouchableOpacity
                  style={styles.dropdownSelect}
                  onPress={() => setShowDurationModal(true)}
                  activeOpacity={0.7}
                >
                  <Feather name="clock" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{duration}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>
              <View style={styles.col}>
                <Text style={styles.label}>Repeat</Text>
                <TouchableOpacity
                  style={styles.dropdownSelect}
                  onPress={() => setShowRepeatModal(true)}
                  activeOpacity={0.7}
                >
                  <Feather name="repeat" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText} numberOfLines={1}>{repeat}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>
            </View>

            {/* Location */}
            <Text style={styles.label}>
              Location <Text style={styles.optionalText}>(optional)</Text>
            </Text>
            <View style={styles.inputWithIconBox}>
              <Feather name="map-pin" size={16} color="#64748B" style={{ marginRight: 8 }} />
              <TextInput
                style={[styles.textInput, { flex: 1, borderWidth: 0, paddingHorizontal: 0 }]}
                placeholder="e.g. Store, Merchant, Office"
                placeholderTextColor="#94A3B8"
                value={location}
                onChangeText={setLocation}
              />
            </View>

            {/* Meeting Type / Format */}
            <Text style={styles.label}>Meeting Type / Format</Text>
            <View style={styles.meetingTypeRow}>
              {meetingTypeOptions.map((opt) => {
                const isSelected = meetingType === opt.value;
                return (
                  <TouchableOpacity
                    key={opt.value}
                    style={[styles.meetingTypePill, isSelected && styles.meetingTypePillSelected]}
                    onPress={() => setMeetingType(opt.value)}
                    activeOpacity={0.7}
                  >
                    <Feather
                      name={opt.icon as any}
                      size={14}
                      color={isSelected ? '#16A34A' : '#64748B'}
                      style={{ marginRight: 6 }}
                    />
                    <Text style={[styles.meetingTypeText, isSelected && styles.meetingTypeTextSelected]}>
                      {opt.label}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </View>

            {/* Category Grid */}
            <View style={styles.categoryHeaderRow}>
              <Text style={styles.labelNoMargin}>Category</Text>
              <TouchableOpacity activeOpacity={0.7}>
                <Text style={styles.seeAllLink}>See all →</Text>
              </TouchableOpacity>
            </View>

            <View style={styles.categoryGrid}>
              {categories.map((cat, idx) => {
                const isSelected = selectedCategory === cat.name;
                return (
                  <TouchableOpacity
                    key={idx}
                    style={[styles.categoryCard, isSelected && styles.categoryCardSelected]}
                    onPress={() => setSelectedCategory(cat.name)}
                    activeOpacity={0.8}
                  >
                    {cat.lib === 'feather' ? (
                      <Feather name={cat.icon as any} size={20} color={isSelected ? '#16A34A' : '#475569'} />
                    ) : (
                      <Ionicons name={cat.icon as any} size={20} color={isSelected ? '#16A34A' : '#475569'} />
                    )}
                    <Text style={[styles.categoryName, isSelected && styles.categoryNameSelected]}>
                      {cat.name}
                    </Text>
                  </TouchableOpacity>
                );
              })}
            </View>

            {/* Payment Method & Link to Goal */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <Text style={styles.label}>Payment Method</Text>
                <TouchableOpacity
                  style={styles.dropdownSelect}
                  onPress={() => setShowPaymentModal(true)}
                  activeOpacity={0.7}
                >
                  <Ionicons name="card-outline" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{paymentMethod}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>

              <View style={styles.col}>
                <GoalPickerField
                  value={linkedGoal}
                  onChange={setLinkedGoal}
                  label="Link to Goal"
                />
              </View>
            </View>

            {/* Attach Receipts */}
            <AttachmentPicker attachments={attachments} onChange={setAttachments} />
          </View>

          {/* Action Buttons */}
          <View style={styles.actionRow}>
            <TouchableOpacity style={styles.cancelBtn} onPress={onBack} activeOpacity={0.8}>
              <Text style={styles.cancelBtnText}>Cancel</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.createBtn} onPress={handleSave} activeOpacity={0.88}>
              <Text style={styles.createBtnText}>Save Expense</Text>
            </TouchableOpacity>
          </View>
        </ScrollView>

        <SelectionModal
          visible={showDurationModal}
          onClose={() => setShowDurationModal(false)}
          title="Select Duration"
          options={durationOptions}
          selectedValue={duration}
          onSelect={setDuration}
        />
        <SelectionModal
          visible={showPaymentModal}
          onClose={() => setShowPaymentModal(false)}
          title="Select Payment Method"
          options={paymentOptions}
          selectedValue={paymentMethod}
          onSelect={setPaymentMethod}
        />

        <SelectionModal
          visible={showRepeatModal}
          onClose={() => setShowRepeatModal(false)}
          title="Repeat"
          options={[
            { label: 'Does not repeat', value: 'Does not repeat' },
            { label: 'Daily', value: 'Daily' },
            { label: 'Weekly', value: 'Weekly' },
            { label: 'Monthly', value: 'Monthly' },
          ]}
          selectedValue={repeat}
          onSelect={setRepeat}
        />
        <CreationSuccessModal
          visible={showSuccessModal}
          itemType="Expense"
          itemData={createdItem}
          onClose={() => {
            setShowSuccessModal(false);
          }}
          onViewTask={() => {
            setShowSuccessModal(false);
            navigation.navigate('MainTabs', { screen: 'Plan' });
          }}
        />
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
    paddingHorizontal: 16,
    paddingTop: 35,
    paddingBottom: 32,
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 18,
  },
  backButton: {
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
    fontSize: 20,
    fontWeight: '800',
    color: '#0F172A',
  },
  headerSubtitle: {
    fontSize: 12,
    color: '#64748B',
  },
  avatarCircle: {
    width: 34,
    height: 34,
    borderRadius: 17,
    backgroundColor: '#66C400',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  formCard: {
    backgroundColor: '#FFFFFF',
  },
  label: {
    fontSize: 13,
    fontWeight: '700',
    color: '#1E293B',
    marginBottom: 6,
  },
  labelNoMargin: {
    fontSize: 13,
    fontWeight: '700',
    color: '#1E293B',
  },
  requiredStar: {
    color: '#EF4444',
  },
  optionalText: {
    fontSize: 11,
    color: '#94A3B8',
    fontWeight: '400',
  },
  amountInputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 12,
    paddingVertical: 10,
    marginBottom: 16,
  },
  currencyBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F1F5F9',
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 6,
    marginRight: 10,
  },
  currencyText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#0F172A',
    marginRight: 4,
  },
  amountInput: {
    flex: 1,
    fontSize: 20,
    fontWeight: '700',
    color: '#0F172A',
  },
  textInput: {
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 14,
    color: '#0F172A',
    marginBottom: 16,
  },
  inputWithIconBox: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingVertical: 10,
    marginBottom: 16,
  },
  textAreaWrap: {
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingVertical: 12,
    height: 100,
    marginBottom: 16,
  },
  textArea: {
    flex: 1,
    fontSize: 14,
    color: '#0F172A',
  },
  charCount: {
    fontSize: 11,
    color: '#94A3B8',
    textAlign: 'right',
  },
  twoColRow: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 16,
  },
  col: {
    flex: 1,
  },
  dropdownSelect: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 12,
    paddingVertical: 12,
  },
  fieldIcon: {
    marginRight: 8,
  },
  dropdownText: {
    flex: 1,
    fontSize: 13,
    color: '#0F172A',
    fontWeight: '500',
  },
  meetingTypeRow: {
    flexDirection: 'row',
    gap: 10,
    marginBottom: 16,
  },
  meetingTypePill: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingVertical: 10,
    paddingHorizontal: 6,
  },
  meetingTypePillSelected: {
    borderColor: '#16A34A',
    backgroundColor: '#F0FDF4',
    borderWidth: 1.5,
  },
  meetingTypeText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#475569',
  },
  meetingTypeTextSelected: {
    color: '#1E293B',
    fontWeight: '700',
  },
  categoryHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  seeAllLink: {
    fontSize: 12,
    color: '#16A34A',
    fontWeight: '700',
  },
  categoryGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginBottom: 16,
  },
  categoryCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F8FAFC',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 10,
    paddingVertical: 8,
  },
  categoryCardSelected: {
    backgroundColor: '#F0FDF4',
    borderColor: '#16A34A',
  },
  categoryName: {
    fontSize: 12,
    color: '#475569',
    marginLeft: 6,
    fontWeight: '500',
  },
  categoryNameSelected: {
    color: '#16A34A',
    fontWeight: '700',
  },
  actionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    marginTop: 10,
  },
  cancelBtn: {
    flex: 1,
    backgroundColor: '#F8FAFC',
    borderRadius: 16,
    paddingVertical: 14,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  cancelBtnText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#475569',
  },
  createBtn: {
    flex: 1.3,
    backgroundColor: '#66C400',
    borderRadius: 16,
    paddingVertical: 14,
    alignItems: 'center',
    justifyContent: 'center',
  },
  createBtnText: {
    fontSize: 14,
    fontWeight: '800',
    color: '#FFFFFF',
  },
});


