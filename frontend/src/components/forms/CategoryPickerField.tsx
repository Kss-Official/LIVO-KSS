import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Feather } from '@expo/vector-icons';
import { SelectionModal, SelectionOption } from './SelectionModal';
import { getCategoryConfig, DEFAULT_CATEGORY_OPTIONS } from '../../utils/categoryGoalUtils';

interface CategoryPickerFieldProps {
  value: string;
  onChange: (category: string) => void;
  label?: string;
  options?: SelectionOption[];
  optional?: boolean;
}

export const CategoryPickerField: React.FC<CategoryPickerFieldProps> = ({
  value,
  onChange,
  label = 'Category',
  options = DEFAULT_CATEGORY_OPTIONS,
  optional = false,
}) => {
  const [showModal, setShowModal] = useState(false);
  const catConfig = getCategoryConfig(value);

  return (
    <View style={styles.container}>
      <Text style={optional ? styles.fieldLabelLight : styles.fieldLabel}>
        {label} {optional && <Text style={styles.optionalText}>(optional)</Text>}
      </Text>

      <TouchableOpacity
        style={styles.dropdownBox}
        onPress={() => setShowModal(true)}
        activeOpacity={0.7}
      >
        <View style={[styles.miniIconBadge, { backgroundColor: catConfig.bg }]}>
          <Feather name={catConfig.icon} size={12} color={catConfig.color} />
        </View>
        <Text style={styles.dropdownText} numberOfLines={1}>
          {value || 'Select Category'}
        </Text>
        <Feather name="chevron-down" size={14} color="#94A3B8" style={{ marginLeft: 'auto' }} />
      </TouchableOpacity>

      <SelectionModal
        visible={showModal}
        onClose={() => setShowModal(false)}
        title={`Select ${label}`}
        options={options}
        selectedValue={value}
        onSelect={(val) => {
          onChange(val);
          setShowModal(false);
        }}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  fieldLabel: {
    fontSize: 13,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 8,
  },
  fieldLabelLight: {
    fontSize: 13,
    fontWeight: '700',
    color: '#334155',
    marginBottom: 8,
  },
  optionalText: {
    fontSize: 11,
    fontWeight: '400',
    color: '#94A3B8',
  },
  dropdownBox: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 12,
    paddingVertical: 12,
    minHeight: 46,
  },
  miniIconBadge: {
    width: 22,
    height: 22,
    borderRadius: 6,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  dropdownText: {
    fontSize: 13,
    color: '#0F172A',
    fontWeight: '500',
    flex: 1,
  },
});
