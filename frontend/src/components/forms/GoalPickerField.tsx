import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Feather } from '@expo/vector-icons';
import { SelectionModal, SelectionOption } from './SelectionModal';
import { getGoalConfig, DEFAULT_GOAL_OPTIONS } from '../../utils/categoryGoalUtils';

interface GoalPickerFieldProps {
  value: string;
  onChange: (goal: string) => void;
  label?: string;
  options?: SelectionOption[];
  optional?: boolean;
}

export const GoalPickerField: React.FC<GoalPickerFieldProps> = ({
  value,
  onChange,
  label = 'Add to Goal',
  options = DEFAULT_GOAL_OPTIONS,
  optional = true,
}) => {
  const [showModal, setShowModal] = useState(false);
  const goalConfig = getGoalConfig(value);

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
        <View style={[styles.miniIconBadge, { backgroundColor: goalConfig.bg }]}>
          <Feather name={goalConfig.icon} size={12} color={goalConfig.color} />
        </View>
        <Text style={styles.dropdownText} numberOfLines={1}>
          {value || 'None'}
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
