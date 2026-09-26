import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Modal, TouchableWithoutFeedback, ScrollView } from 'react-native';
import { Feather } from '@expo/vector-icons';
import { getCategoryConfig, getGoalConfig } from '../../utils/categoryGoalUtils';

export interface SelectionOption {
  label: string;
  value: string;
  icon?: keyof typeof Feather.glyphMap;
  iconColor?: string;
  iconBgColor?: string;
}

interface SelectionModalProps {
  visible: boolean;
  onClose: () => void;
  title: string;
  options: SelectionOption[];
  selectedValue: string;
  onSelect: (value: string) => void;
}

export const SelectionModal: React.FC<SelectionModalProps> = ({
  visible,
  onClose,
  title,
  options,
  selectedValue,
  onSelect,
}) => {
  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <TouchableWithoutFeedback onPress={onClose}>
        <View style={styles.modalOverlay} />
      </TouchableWithoutFeedback>
      <View style={styles.dropdownModalContent}>
        <View style={styles.headerRow}>
          <Text style={styles.dropdownModalTitle}>{title}</Text>
          <TouchableOpacity onPress={onClose} hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}>
            <Feather name="x" size={20} color="#64748B" />
          </TouchableOpacity>
        </View>
        <ScrollView style={styles.scrollArea} showsVerticalScrollIndicator={false}>
          {options.map((opt) => {
            const isSelected = selectedValue === opt.value;
            
            // Auto resolve icon if not explicitly provided
            let iconName = opt.icon;
            let iconColor = opt.iconColor;
            let iconBgColor = opt.iconBgColor;

            if (!iconName) {
              const lowerTitle = (title || '').toLowerCase();
              if (lowerTitle.includes('cat')) {
                const conf = getCategoryConfig(opt.value || opt.label);
                iconName = conf.icon;
                iconColor = conf.color;
                iconBgColor = conf.bg;
              } else if (lowerTitle.includes('goal')) {
                const conf = getGoalConfig(opt.value || opt.label);
                iconName = conf.icon;
                iconColor = conf.color;
                iconBgColor = conf.bg;
              }
            }

            return (
              <TouchableOpacity
                key={opt.value}
                style={[styles.dropdownItem, isSelected && styles.dropdownItemSelected]}
                onPress={() => {
                  onSelect(opt.value);
                  onClose();
                }}
              >
                <View style={styles.itemLeft}>
                  {iconName && (
                    <View style={[styles.iconWrap, { backgroundColor: iconBgColor || '#F1F5F9' }]}>
                      <Feather name={iconName} size={14} color={iconColor || '#64748B'} />
                    </View>
                  )}
                  <Text style={[styles.dropdownItemText, isSelected && styles.dropdownItemTextSelected]}>
                    {opt.label}
                  </Text>
                </View>
                {isSelected && <Feather name="check" size={16} color="#66C400" />}
              </TouchableOpacity>
            );
          })}
        </ScrollView>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.4)',
  },
  dropdownModalContent: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 24,
    paddingBottom: 40,
    maxHeight: '70%',
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  dropdownModalTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#0F172A',
  },
  scrollArea: {
    maxHeight: 400,
  },
  dropdownItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: '#F8FAFC',
  },
  dropdownItemSelected: {
    backgroundColor: '#F8FAF5',
    borderRadius: 12,
    paddingHorizontal: 12,
    marginHorizontal: -2,
    borderBottomWidth: 0,
  },
  itemLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  iconWrap: {
    width: 28,
    height: 28,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  dropdownItemText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#334155',
  },
  dropdownItemTextSelected: {
    color: '#2D6A00',
    fontWeight: '700',
  },
});
