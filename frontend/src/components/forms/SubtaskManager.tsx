import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, TextInput } from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';

interface SubtaskManagerProps {
  subtasks: string[];
  onChange: (subtasks: string[]) => void;
  onGenerateAI?: () => void;
}

export const SubtaskManager: React.FC<SubtaskManagerProps> = ({ subtasks, onChange, onGenerateAI }) => {
  const [newSubtask, setNewSubtask] = useState('');
  const [showSubtaskInput, setShowSubtaskInput] = useState(false);

  const addSubtask = () => {
    if (newSubtask.trim()) {
      onChange([...subtasks, newSubtask.trim()]);
      setNewSubtask('');
      setShowSubtaskInput(false);
    }
  };

  const removeSubtask = (index: number) => {
    onChange(subtasks.filter((_, i) => i !== index));
  };

  return (
    <View style={styles.subtasksSection}>
      {subtasks.map((st, idx) => (
        <View key={idx} style={styles.subtaskRow}>
          <View style={styles.subtaskCheckbox}>
            <Feather name="circle" size={14} color="#CBD5E1" />
          </View>
          <Text style={styles.subtaskText}>{st}</Text>
          <TouchableOpacity onPress={() => removeSubtask(idx)}>
            <Feather name="x" size={14} color="#94A3B8" />
          </TouchableOpacity>
        </View>
      ))}

      {showSubtaskInput ? (
        <View style={styles.subtaskInputRow}>
          <TextInput
            style={styles.subtaskInput}
            placeholder="Enter subtask..."
            placeholderTextColor="#CBD5E1"
            value={newSubtask}
            onChangeText={setNewSubtask}
            onSubmitEditing={addSubtask}
            autoFocus
          />
          <TouchableOpacity onPress={addSubtask} style={styles.subtaskAddIcon}>
            <Feather name="check" size={16} color="#66C400" />
          </TouchableOpacity>
        </View>
      ) : null}

      <View style={styles.subtaskActionsRow}>
        <TouchableOpacity
          style={styles.subtaskActionBtn}
          onPress={() => setShowSubtaskInput(true)}
        >
          <View style={styles.addSubCircle}>
            <Feather name="plus" size={14} color="#66C400" />
          </View>
          <Text style={styles.subtaskActionText}>Add a subtask</Text>
        </TouchableOpacity>

        {onGenerateAI && (
          <TouchableOpacity style={styles.subtaskActionBtn} onPress={onGenerateAI}>
            <Feather name="plus" size={14} color="#7C3AED" style={{ marginRight: 4 }} />
            <Text style={[styles.subtaskActionText, { color: '#7C3AED' }]}>
              Generate with AI
            </Text>
          </TouchableOpacity>
        )}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  subtasksSection: {
    marginBottom: 16,
  },
  subtaskRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 12,
    paddingVertical: 10,
    marginBottom: 8,
  },
  subtaskCheckbox: {
    marginRight: 8,
  },
  subtaskText: {
    flex: 1,
    fontSize: 13,
    color: '#0F172A',
  },
  subtaskInputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#66C400',
    paddingHorizontal: 12,
    paddingVertical: 8,
    marginBottom: 8,
  },
  subtaskInput: {
    flex: 1,
    fontSize: 13,
    color: '#0F172A',
  },
  subtaskAddIcon: {
    padding: 4,
  },
  subtaskActionsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: 8,
  },
  subtaskActionBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 18,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  addSubCircle: {
    width: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: '#E2F7C5',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 6,
  },
  subtaskActionText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#475569',
  },
});
