import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';

export type PriorityLevel = 'High' | 'Medium' | 'Low';

interface PrioritySelectorProps {
  selectedPriority: PriorityLevel;
  onSelect: (priority: PriorityLevel) => void;
}

export const PrioritySelector: React.FC<PrioritySelectorProps> = ({ selectedPriority, onSelect }) => {
  const priorityOptions: { label: PriorityLevel; color: string; bgColor: string }[] = [
    { label: 'High', color: '#DC2626', bgColor: '#FEE2E2' },
    { label: 'Medium', color: '#F59E0B', bgColor: '#FEF3C7' },
    { label: 'Low', color: '#64748B', bgColor: '#F1F5F9' },
  ];

  return (
    <View style={styles.priorityRow}>
      {priorityOptions.map((opt) => {
        const isSelected = selectedPriority === opt.label;
        return (
          <TouchableOpacity
            key={opt.label}
            style={[
              styles.priorityPill,
              {
                backgroundColor: isSelected ? opt.bgColor : '#F8FAFC',
                borderColor: isSelected ? opt.color : '#E2E8F0',
              },
            ]}
            onPress={() => onSelect(opt.label)}
          >
            <View style={[styles.priorityDot, { backgroundColor: opt.color }]} />
            <Text
              style={[
                styles.priorityText,
                { color: isSelected ? opt.color : '#64748B' },
              ]}
            >
              {opt.label}
            </Text>
          </TouchableOpacity>
        );
      })}
    </View>
  );
};

const styles = StyleSheet.create({
  priorityRow: {
    flexDirection: 'row',
    marginBottom: 16,
  },
  priorityPill: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 9,
    borderWidth: 1,
    marginRight: 10,
  },
  priorityDot: {
    width: 10,
    height: 10,
    borderRadius: 3,
    marginRight: 6,
  },
  priorityText: {
    fontSize: 13,
    fontWeight: '700',
  },
});
