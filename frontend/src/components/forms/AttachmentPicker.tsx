import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Image, Modal, TouchableWithoutFeedback } from 'react-native';
import { Feather } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import * as DocumentPicker from 'expo-document-picker';

export interface Attachment {
  uri: string;
  name: string;
  type: 'image' | 'file' | 'link';
}

interface AttachmentPickerProps {
  attachments: Attachment[];
  onChange: (attachments: Attachment[]) => void;
}

export const AttachmentPicker: React.FC<AttachmentPickerProps> = ({ attachments, onChange }) => {
  const [showOptions, setShowOptions] = useState(false);

  const handlePickImage = async () => {
    setShowOptions(false);
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      quality: 0.8,
    });
    if (!result.canceled && result.assets && result.assets.length > 0) {
      const asset = result.assets[0];
      onChange([...attachments, { uri: asset.uri, name: asset.fileName || 'Image', type: 'image' }]);
    }
  };

  const handleTakePhoto = async () => {
    setShowOptions(false);
    const result = await ImagePicker.launchCameraAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      quality: 0.8,
    });
    if (!result.canceled && result.assets && result.assets.length > 0) {
      const asset = result.assets[0];
      onChange([...attachments, { uri: asset.uri, name: 'Photo', type: 'image' }]);
    }
  };

  const handlePickDocument = async () => {
    setShowOptions(false);
    const result = await DocumentPicker.getDocumentAsync({});
    if (!result.canceled && result.assets && result.assets.length > 0) {
      const asset = result.assets[0];
      onChange([...attachments, { uri: asset.uri, name: asset.name, type: 'file' }]);
    }
  };

  const removeAttachment = (index: number) => {
    onChange(attachments.filter((_, i) => i !== index));
  };

  return (
    <View style={styles.container}>
      <Text style={styles.fieldLabelLight}>
        Add Attachments <Text style={styles.optionalText}>(optional)</Text>
      </Text>

      {attachments.length > 0 && (
        <View style={styles.attachmentList}>
          {attachments.map((att, idx) => (
            <View key={idx} style={styles.attachmentBadge}>
              <Feather name={att.type === 'image' ? 'image' : 'file-text'} size={14} color="#64748B" style={{ marginRight: 6 }} />
              <Text style={styles.attachmentName} numberOfLines={1}>{att.name}</Text>
              <TouchableOpacity onPress={() => removeAttachment(idx)} style={styles.removeIcon}>
                <Feather name="x" size={14} color="#EF4444" />
              </TouchableOpacity>
            </View>
          ))}
        </View>
      )}

      <TouchableOpacity style={styles.attachmentRow} onPress={() => setShowOptions(true)}>
        <View style={styles.attachLeftRow}>
          <Feather name="paperclip" size={16} color="#64748B" style={{ marginRight: 8 }} />
          <Text style={styles.attachText}>Attach file, image or link</Text>
        </View>
        <Feather name="chevron-right" size={16} color="#94A3B8" />
      </TouchableOpacity>

      <Modal visible={showOptions} transparent animationType="fade" onRequestClose={() => setShowOptions(false)}>
        <TouchableWithoutFeedback onPress={() => setShowOptions(false)}>
          <View style={styles.modalOverlay} />
        </TouchableWithoutFeedback>
        <View style={styles.dropdownModalContent}>
          <Text style={styles.dropdownModalTitle}>Add Attachment</Text>

          <TouchableOpacity style={styles.dropdownItem} onPress={handleTakePhoto}>
            <Feather name="camera" size={18} color="#475569" style={{ marginRight: 12 }} />
            <Text style={styles.dropdownItemText}>Take Photo</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.dropdownItem} onPress={handlePickImage}>
            <Feather name="image" size={18} color="#475569" style={{ marginRight: 12 }} />
            <Text style={styles.dropdownItemText}>Choose Image</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.dropdownItem} onPress={handlePickDocument}>
            <Feather name="file" size={18} color="#475569" style={{ marginRight: 12 }} />
            <Text style={styles.dropdownItemText}>Choose Document</Text>
          </TouchableOpacity>
        </View>
      </Modal>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginBottom: 16,
  },
  fieldLabelLight: {
    fontSize: 13,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 6,
  },
  optionalText: {
    color: '#94A3B8',
    fontWeight: '400',
    fontStyle: 'italic',
  },
  attachmentRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    paddingHorizontal: 14,
    paddingVertical: 13,
  },
  attachLeftRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  attachText: {
    fontSize: 13,
    color: '#94A3B8',
  },
  attachmentList: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginBottom: 10,
  },
  attachmentBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F1F5F9',
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    maxWidth: '48%',
  },
  attachmentName: {
    fontSize: 12,
    color: '#334155',
    flex: 1,
  },
  removeIcon: {
    marginLeft: 6,
  },
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
  },
  dropdownModalTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 16,
  },
  dropdownItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: '#F8FAFC',
  },
  dropdownItemText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#334155',
  },
});
