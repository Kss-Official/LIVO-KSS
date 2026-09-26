import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Platform } from 'react-native';
import DateTimePicker from '@react-native-community/datetimepicker';
import { Feather } from '@expo/vector-icons';

interface TimePickerFieldProps {
  value: Date;
  onChange: (date: Date) => void;
  label?: string;
}

export const TimePickerField: React.FC<TimePickerFieldProps> = ({ value, onChange, label = 'Time' }) => {
  const [show, setShow] = useState(false);

  const handleChange = (event: any, selectedDate?: Date) => {
    if (Platform.OS === 'android') setShow(false);
    if (selectedDate) onChange(selectedDate);
  };

  const displayTime = value.toLocaleTimeString('en-US', {
    hour: 'numeric',
    minute: '2-digit',
    hour12: true,
  });

  return (
    <View style={styles.container}>
      <Text style={styles.fieldLabel}>{label}</Text>
      <TouchableOpacity style={styles.dropdownBox} onPress={() => setShow(true)}>
        <Feather name="clock" size={15} color="#64748B" style={{ marginRight: 6 }} />
        <Text style={styles.dropdownText}>{displayTime}</Text>
        <Feather name="chevron-down" size={14} color="#94A3B8" style={{ marginLeft: 'auto' }} />
      </TouchableOpacity>

      {show && (
        <DateTimePicker
          value={value}
          mode="time"
          display={Platform.OS === 'ios' ? 'spinner' : 'default'}
          onValueChange={handleChange}
          onDismiss={() => setShow(false)}
        />
      )}
      {Platform.OS === 'ios' && show && (
        <TouchableOpacity style={styles.doneBtn} onPress={() => setShow(false)}>
          <Text style={styles.doneText}>Done</Text>
        </TouchableOpacity>
      )}
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
    marginBottom: 6,
  },
  dropdownBox: {
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 12,
    paddingVertical: 11,
    flexDirection: 'row',
    alignItems: 'center',
  },
  dropdownText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#0F172A',
    flex: 1,
  },
  doneBtn: {
    padding: 8,
    alignItems: 'flex-end',
  },
  doneText: {
    color: '#66C400',
    fontWeight: 'bold',
  },
});
