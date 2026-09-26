import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, SafeAreaView, Modal, TextInput } from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import * as DocumentPicker from 'expo-document-picker';
import { useNavigation } from '@react-navigation/native';

export const ScanAndAddScreen: React.FC<any> = ({ onBack }) => {
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

  const [textModalVisible, setTextModalVisible] = useState(false);
  const [manualText, setManualText] = useState('');

  const navigateToReview = (contentUri: string, type: 'image' | 'document' | 'text') => {
    navigation.navigate('ReviewAndAdd', { contentUri, type });
  };

  const handleCamera = async () => {
    const permissionResult = await ImagePicker.requestCameraPermissionsAsync();
    if (!permissionResult.granted) {
      alert("You've refused to allow this app to access your camera!");
      return;
    }
    const result = await ImagePicker.launchCameraAsync({
      allowsEditing: true,
      quality: 1,
    });
    if (!result.canceled && result.assets && result.assets.length > 0) {
      navigateToReview(result.assets[0].uri, 'image');
    }
  };

  const handleGallery = async () => {
    const permissionResult = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permissionResult.granted) {
      alert("You've refused to allow this app to access your photos!");
      return;
    }
    const result = await ImagePicker.launchImageLibraryAsync({
      allowsEditing: true,
      quality: 1,
    });
    if (!result.canceled && result.assets && result.assets.length > 0) {
      navigateToReview(result.assets[0].uri, 'image');
    }
  };

  const handleDocument = async () => {
    try {
      const result = await DocumentPicker.getDocumentAsync({});
      if (!result.canceled && result.assets && result.assets.length > 0) {
        navigateToReview(result.assets[0].uri, 'document');
      }
    } catch (err) {
      console.log('Error picking document', err);
    }
  };

  const handleTextSubmit = () => {
    if (manualText.trim().length > 0) {
      setTextModalVisible(false);
      navigateToReview(manualText, 'text');
    }
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.header}>
        <TouchableOpacity onPress={handleBack} style={styles.backBtn}>
          <Feather name="chevron-left" size={24} color="#0F172A" />
        </TouchableOpacity>
        <Text style={styles.title}>Scan & Add</Text>
        <View style={{ width: 40 }} />
      </View>

      <View style={styles.content}>
        <Text style={styles.subtitle}>Capture and convert your notes, documents or cards into tasks, events or other items using AI.</Text>

        <TouchableOpacity style={styles.optionCard} onPress={handleCamera}>
          <View style={[styles.iconContainer, { backgroundColor: '#22C55E' }]}>
            <Feather name="camera" size={24} color="#FFFFFF" />
          </View>
          <View style={styles.textContainer}>
            <Text style={styles.optionTitle}>Scan with Camera</Text>
            <Text style={styles.optionDesc}>Take a photo of notes, documents, whiteboard or card</Text>
          </View>
          <Feather name="chevron-right" size={20} color="#94A3B8" />
        </TouchableOpacity>

        <TouchableOpacity style={styles.optionCard} onPress={handleGallery}>
          <View style={[styles.iconContainer, { backgroundColor: '#A855F7' }]}>
            <Feather name="image" size={24} color="#FFFFFF" />
          </View>
          <View style={styles.textContainer}>
            <Text style={styles.optionTitle}>Choose from Gallery</Text>
            <Text style={styles.optionDesc}>Select an existing photo from your device</Text>
          </View>
          <Feather name="chevron-right" size={20} color="#94A3B8" />
        </TouchableOpacity>

        <TouchableOpacity style={styles.optionCard} onPress={handleDocument}>
          <View style={[styles.iconContainer, { backgroundColor: '#3B82F6' }]}>
            <Feather name="file-text" size={24} color="#FFFFFF" />
          </View>
          <View style={styles.textContainer}>
            <Text style={styles.optionTitle}>Import Document</Text>
            <Text style={styles.optionDesc}>PDF, image or text file</Text>
          </View>
          <Feather name="chevron-right" size={20} color="#94A3B8" />
        </TouchableOpacity>

        <Text style={styles.separatorText}>Or create manually</Text>

        <TouchableOpacity style={styles.optionCard} onPress={() => setTextModalVisible(true)}>
          <View style={[styles.iconContainer, { backgroundColor: '#F1F5F9' }]}>
            <Feather name="edit-3" size={24} color="#0F172A" />
          </View>
          <View style={styles.textContainer}>
            <Text style={styles.optionTitle}>Type or Paste Text</Text>
            <Text style={styles.optionDesc}>Add text to create tasks, events and more</Text>
          </View>
          <Feather name="chevron-right" size={20} color="#94A3B8" />
        </TouchableOpacity>

        <View style={styles.infoBanner}>
          <Ionicons name="bulb-outline" size={20} color="#3B82F6" style={styles.infoIcon} />
          <Text style={styles.infoText}>LIVO AI will analyze the content and help you create tasks, events, goals or add to the right section.</Text>
        </View>
      </View>

      {/* Manual Text Modal */}
      <Modal visible={textModalVisible} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <Text style={styles.modalTitle}>Paste Text</Text>
              <TouchableOpacity onPress={() => setTextModalVisible(false)}>
                <Feather name="x" size={24} color="#64748B" />
              </TouchableOpacity>
            </View>
            <TextInput
              style={styles.textInput}
              multiline
              placeholder="E.g., Meeting with John at 3pm tomorrow, Buy groceries..."
              value={manualText}
              onChangeText={setManualText}
              autoFocus
            />
            <TouchableOpacity style={styles.submitBtn} onPress={handleTextSubmit}>
              <Text style={styles.submitBtnText}>Process</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: '#F8FAFC' },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', padding: 16 },
  backBtn: { width: 40, height: 40, justifyContent: 'center', alignItems: 'center' },
  title: { fontSize: 18, fontWeight: '700', color: '#0F172A' },
  content: { padding: 16, flex: 1 },
  subtitle: { fontSize: 15, color: '#64748B', marginBottom: 30, lineHeight: 22, textAlign: 'center' },
  optionCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    padding: 16,
    borderRadius: 16,
    marginBottom: 18,
    borderWidth: 1,
    borderColor: '#F1F5F9',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 3,
    elevation: 1,
  },
  iconContainer: {
    width: 48,
    height: 48,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 16,
  },
  textContainer: { flex: 1 },
  optionTitle: { fontSize: 16, fontWeight: '600', color: '#0F172A', marginBottom: 4 },
  optionDesc: { fontSize: 13, color: '#64748B' },
  separatorText: { fontSize: 13, color: '#94A3B8', marginTop: 8, marginBottom: 12, marginLeft: 4 },
  infoBanner: { flexDirection: 'row', backgroundColor: '#EFF6FF', padding: 16, borderRadius: 12, marginTop: 'auto', marginBottom: 26 },
  infoIcon: { marginRight: 8, marginTop: 2 },
  infoText: { flex: 1, fontSize: 13, color: '#1E3A8A', lineHeight: 20 },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'flex-end' },
  modalContent: { backgroundColor: '#FFF', borderTopLeftRadius: 24, borderTopRightRadius: 24, padding: 20, height: '60%' },
  modalHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, marginTop: 12 },
  modalTitle: { fontSize: 18, fontWeight: '700', color: '#0F172A' },
  textInput: { flex: 1, backgroundColor: '#F8FAFC', borderRadius: 12, padding: 16, fontSize: 16, color: '#0F172A', textAlignVertical: 'top' },
  submitBtn: { backgroundColor: '#66C400', padding: 16, borderRadius: 12, alignItems: 'center', marginTop: 16 },
  submitBtnText: { color: '#FFF', fontSize: 16, fontWeight: '600' },
});
