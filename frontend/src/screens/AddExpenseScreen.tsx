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

interface AddExpenseScreenProps {
  onBack?: () => void;
  onSubmit?: (data: any) => void;
}

export const AddExpenseScreen: React.FC<AddExpenseScreenProps> = ({ onBack, onSubmit }) => {
  const [amount, setAmount] = useState('');
  const [currency, setCurrency] = useState('INR');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [date, setDate] = useState('Mon, 2 Sep 2024');
  const [time, setTime] = useState('2:00 PM');
  const [selectedCategory, setSelectedCategory] = useState('Food & Dining');
  const [paymentMethod, setPaymentMethod] = useState('Cash');
  const [linkedGoal, setLinkedGoal] = useState('Select a goal');

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

  const handleSave = () => {
    const data = {
      amount,
      currency,
      title,
      description,
      date,
      time,
      category: selectedCategory,
      paymentMethod,
      linkedGoal,
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
          {/* Header Bar */}
          <View style={styles.headerRow}>
            <TouchableOpacity style={styles.backButton} onPress={onBack} activeOpacity={0.7}>
              <Feather name="arrow-left" size={22} color="#0F172A" />
            </TouchableOpacity>

            <View style={styles.headerTitleWrap}>
              <Text style={styles.headerTitle}>Add Expense</Text>
              <Text style={styles.headerSubtitle}>Track your spending. Stay in control.</Text>
            </View>

            <View style={styles.avatarCircle}>
              <Text style={styles.avatarText}>R</Text>
            </View>
          </View>

          {/* AI Banner */}
          <View style={styles.aiBanner}>
            <View style={styles.aiBannerLeft}>
              <View style={styles.aiIconWrap}>
                <Ionicons name="sparkles" size={18} color="#15803D" />
              </View>
              <View style={styles.aiTextWrap}>
                <Text style={styles.aiTitle}>Need help adding this expense?</Text>
                <Text style={styles.aiSubtitle}>
                  Tell LIVO what you spent on, and I'll help you categorize it and suggest better choices.
                </Text>
              </View>
            </View>
            <TouchableOpacity style={styles.useAiBtn} activeOpacity={0.8}>
              <Ionicons name="sparkles-outline" size={14} color="#15803D" style={{ marginRight: 4 }} />
              <Text style={styles.useAiBtnText}>Use AI</Text>
            </TouchableOpacity>
          </View>

          {/* Form Section */}
          <View style={styles.formSection}>
            {/* Amount */}
            <Text style={styles.label}>Amount <Text style={styles.requiredStar}>*</Text></Text>
            <View style={styles.amountInputWrap}>
              <Text style={styles.currencySymbol}>₹</Text>
              <TextInput
                style={styles.amountInput}
                placeholder="e.g. 500"
                placeholderTextColor="#94A3B8"
                keyboardType="numeric"
                value={amount}
                onChangeText={setAmount}
              />
              <TouchableOpacity style={styles.currencySelector} activeOpacity={0.7}>
                <Text style={styles.currencyCodeText}>{currency}</Text>
                <Feather name="chevron-down" size={14} color="#64748B" />
              </TouchableOpacity>
            </View>

            {/* Title */}
            <Text style={styles.label}>Title <Text style={styles.requiredStar}>*</Text></Text>
            <TextInput
              style={styles.textInput}
              placeholder="e.g. Lunch with team"
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

            {/* Date & Time */}
            <View style={styles.twoColRow}>
              <View style={styles.col}>
                <Text style={styles.label}>Date</Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Feather name="calendar" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{date}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>

              <View style={styles.col}>
                <Text style={styles.label}>Time <Text style={styles.optionalText}>(optional)</Text></Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Feather name="clock" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{time}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>
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
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Ionicons name="card-outline" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{paymentMethod}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>

              <View style={styles.col}>
                <Text style={styles.label}>Link to Goal <Text style={styles.optionalText}>(optional)</Text></Text>
                <TouchableOpacity style={styles.dropdownSelect} activeOpacity={0.7}>
                  <Ionicons name="disc-outline" size={16} color="#64748B" style={styles.fieldIcon} />
                  <Text style={styles.dropdownText}>{linkedGoal}</Text>
                  <Feather name="chevron-down" size={16} color="#64748B" />
                </TouchableOpacity>
              </View>
            </View>

            {/* Add Receipt */}
            <Text style={styles.label}>Add Receipt <Text style={styles.optionalText}>(optional)</Text></Text>
            <TouchableOpacity style={styles.attachmentButton} activeOpacity={0.7}>
              <View style={styles.attachmentLeft}>
                <Feather name="paperclip" size={16} color="#64748B" style={{ marginRight: 10 }} />
                <Text style={styles.attachmentText}>Attach photo or file</Text>
              </View>
              <Feather name="chevron-right" size={18} color="#94A3B8" />
            </TouchableOpacity>

            {/* Smart Insight Banner */}
            <View style={styles.smartInsightCard}>
              <View style={styles.smartIconWrap}>
                <Ionicons name="star" size={18} color="#15803D" />
              </View>
              <View style={styles.smartTextWrap}>
                <Text style={styles.smartTitle}>Smart Insight</Text>
                <Text style={styles.smartSubtitle}>
                  You've spent ₹2,450 on dining this week, 20% higher than your usual.
                </Text>
              </View>
              <TouchableOpacity style={styles.viewInsightBtn} activeOpacity={0.8}>
                <Text style={styles.viewInsightBtnText}>View Insights</Text>
              </TouchableOpacity>
            </View>
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
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  flex: { flex: 1 },
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  scrollContent: {
    paddingHorizontal: 20,
    paddingTop: 40,
    paddingBottom: 40,
  },

  /* Header */
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 20,
  },
  backButton: {
    width: 38,
    height: 38,
    borderRadius: 19,
    backgroundColor: '#F8FAFC',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  headerTitleWrap: {
    flex: 1,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: '800',
    color: '#0F172A',
  },
  headerSubtitle: {
    fontSize: 13,
    color: '#64748B',
    marginTop: 2,
  },
  avatarCircle: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#DCFCE7',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#15803D',
  },

  /* AI Banner */
  aiBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#F4FBF4',
    borderWidth: 1,
    borderColor: '#DCFCE7',
    borderRadius: 16,
    padding: 14,
    marginBottom: 24,
  },
  aiBannerLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    paddingRight: 10,
  },
  aiIconWrap: {
    width: 38,
    height: 38,
    borderRadius: 19,
    backgroundColor: '#DCFCE7',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  aiTextWrap: {
    flex: 1,
  },
  aiTitle: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
  },
  aiSubtitle: {
    fontSize: 11.5,
    color: '#64748B',
    marginTop: 2,
    lineHeight: 16,
  },
  useAiBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#CBD5E1',
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  useAiBtnText: {
    fontSize: 12.5,
    fontWeight: '700',
    color: '#0F172A',
  },

  /* Form */
  formSection: {
    marginBottom: 20,
  },
  label: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 8,
    marginTop: 14,
  },
  labelNoMargin: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
  },
  requiredStar: {
    color: '#DC2626',
  },
  optionalText: {
    fontWeight: '400',
    color: '#64748B',
  },
  amountInputWrap: {
    height: 52,
    backgroundColor: '#FFFFFF',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 14,
    paddingHorizontal: 16,
    flexDirection: 'row',
    alignItems: 'center',
  },
  currencySymbol: {
    fontSize: 16,
    fontWeight: '700',
    color: '#0F172A',
    marginRight: 8,
  },
  amountInput: {
    flex: 1,
    fontSize: 15,
    color: '#0F172A',
    fontWeight: '500',
  },
  currencySelector: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingLeft: 10,
    borderLeftWidth: 1,
    borderLeftColor: '#E2E8F0',
    gap: 4,
  },
  currencyCodeText: {
    fontSize: 13.5,
    fontWeight: '600',
    color: '#0F172A',
  },
  textInput: {
    height: 50,
    backgroundColor: '#FFFFFF',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 14,
    paddingHorizontal: 16,
    fontSize: 14.5,
    color: '#0F172A',
  },
  textAreaWrap: {
    backgroundColor: '#FFFFFF',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 14,
    padding: 14,
  },
  textArea: {
    height: 90,
    fontSize: 14,
    color: '#0F172A',
  },
  charCount: {
    alignSelf: 'flex-end',
    fontSize: 11.5,
    color: '#94A3B8',
    marginTop: 4,
  },

  twoColRow: {
    flexDirection: 'row',
    gap: 12,
  },
  col: {
    flex: 1,
  },
  dropdownSelect: {
    height: 50,
    backgroundColor: '#FFFFFF',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 14,
    paddingHorizontal: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  fieldIcon: {
    marginRight: 8,
  },
  dropdownText: {
    flex: 1,
    fontSize: 13,
    fontWeight: '500',
    color: '#0F172A',
  },

  categoryHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: 16,
    marginBottom: 10,
  },
  seeAllLink: {
    fontSize: 13,
    fontWeight: '700',
    color: '#16A34A',
  },
  categoryGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
    marginBottom: 6,
  },
  categoryCard: {
    width: '31%',
    height: 72,
    backgroundColor: '#F8FAFC',
    borderRadius: 14,
    borderWidth: 1.5,
    borderColor: '#F1F5F9',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 6,
    gap: 4,
  },
  categoryCardSelected: {
    backgroundColor: '#F0FDF4',
    borderColor: '#86EFAC',
    borderWidth: 2,
  },
  categoryName: {
    fontSize: 11,
    fontWeight: '600',
    color: '#475569',
    textAlign: 'center',
  },
  categoryNameSelected: {
    color: '#16A34A',
    fontWeight: '700',
  },

  attachmentButton: {
    height: 50,
    backgroundColor: '#FFFFFF',
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderRadius: 14,
    paddingHorizontal: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  attachmentLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  attachmentText: {
    fontSize: 13.5,
    color: '#94A3B8',
  },

  smartInsightCard: {
    marginTop: 16,
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F4FBF4',
    borderWidth: 1,
    borderColor: '#DCFCE7',
    borderRadius: 16,
    padding: 14,
  },
  smartIconWrap: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#DCFCE7',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  smartTextWrap: {
    flex: 1,
    paddingRight: 6,
  },
  smartTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
  },
  smartSubtitle: {
    fontSize: 11,
    color: '#64748B',
    marginTop: 2,
    lineHeight: 15,
  },
  viewInsightBtn: {
    backgroundColor: '#DCFCE7',
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 12,
  },
  viewInsightBtnText: {
    fontSize: 11.5,
    fontWeight: '700',
    color: '#15803D',
  },

  /* Actions */
  actionRow: {
    flexDirection: 'row',
    gap: 12,
    marginTop: 10,
  },
  cancelBtn: {
    flex: 1,
    height: 52,
    backgroundColor: '#F1F5F9',
    borderRadius: 26,
    alignItems: 'center',
    justifyContent: 'center',
  },
  cancelBtnText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
  },
  createBtn: {
    flex: 1,
    height: 52,
    backgroundColor: '#66C400',
    borderRadius: 26,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#66C400',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 4,
  },
  createBtnText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#FFFFFF',
  },
});
