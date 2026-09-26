import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, SafeAreaView, ScrollView, TextInput, ActivityIndicator, Image } from 'react-native';
import { Feather } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { useTasks } from '../hooks/useTasks';
import { useEvents } from '../hooks/useEvents';
import { useHabits } from '../hooks/useHabits';

// Dummy service to simulate LIVO AI processing
const analyzeContent = async (uri: string, type: 'image' | 'document' | 'text'): Promise<any[]> => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve([
        { id: '1', title: 'Buy groceries', type: 'Task', date: 'Today', time: '5:00 PM' },
        { id: '2', title: 'Team Meeting', type: 'Event', date: 'Tomorrow', time: '10:00 AM', duration: 60 },
        { id: '3', title: 'Read 10 pages', type: 'Habit', date: 'Today', time: '8:00 AM' }
      ]);
    }, 2000);
  });
};

export const ReviewAndAddScreen: React.FC<any> = ({ route, navigation: navProp, onBack }) => {
  const navigation = useNavigation<any>() || navProp;

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };
  const { contentUri, type } = route.params || {};
  const [loading, setLoading] = useState(true);
  const [parsedItems, setParsedItems] = useState<any[]>([]);

  const { addTask, refreshTasks } = useTasks();
  const { addEvent, refreshEvents } = useEvents();
  const { addHabit, refreshHabits } = useHabits();

  useEffect(() => {
    const process = async () => {
      try {
        const items = await analyzeContent(contentUri, type);
        setParsedItems(items);
      } catch (err) {
        console.log(err);
      } finally {
        setLoading(false);
      }
    };
    if (contentUri) process();
  }, [contentUri, type]);

  const handleEdit = (id: string, field: string, value: string) => {
    setParsedItems((prev) =>
      prev.map((item) => (item.id === id ? { ...item, [field]: value } : item))
    );
  };

  const handleRemove = (id: string) => {
    setParsedItems((prev) => prev.filter((item) => item.id !== id));
  };

  const handleAddAll = async () => {
    for (const item of parsedItems) {
      if (item.type === 'Task') {
        await addTask({
          id: Date.now().toString() + Math.random(),
          title: item.title,
          date: item.date,
          time: item.time,
          completed: false,
        } as any);
      } else if (item.type === 'Event') {
        await addEvent({
          id: Date.now().toString() + Math.random(),
          title: item.title,
          date: item.date,
          startTime: item.time,
          durationMinutes: item.duration || 60,
        } as any);
      } else if (item.type === 'Habit') {
        await addHabit({
          id: Date.now().toString() + Math.random(),
          title: item.title,
          frequency: 'Daily',
          time: item.time,
          completedDates: [],
        } as any);
      }
    }
    await refreshTasks();
    await refreshEvents();
    await refreshHabits();
    
    // Navigate back to Planning screen
    navigation.navigate('MainTabs', { screen: 'Plan' });
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.header}>
        <TouchableOpacity onPress={handleBack} style={styles.backBtn}>
          <Feather name="chevron-left" size={24} color="#0F172A" />
        </TouchableOpacity>
        <Text style={styles.title}>Review & Add</Text>
        <View style={{ width: 40 }} />
      </View>

      {loading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#66C400" />
          <Text style={styles.loadingText}>LIVO AI is parsing your input...</Text>
        </View>
      ) : (
        <View style={styles.content}>
          <View style={styles.summaryBox}>
            <Feather name="check-circle" size={20} color="#66C400" />
            <Text style={styles.summaryText}>Found {parsedItems.length} items to add.</Text>
          </View>

          <ScrollView style={styles.list} showsVerticalScrollIndicator={false}>
            {parsedItems.map((item) => (
              <View key={item.id} style={styles.itemCard}>
                <View style={styles.itemHeader}>
                  <Text style={styles.itemType}>{item.type}</Text>
                  <TouchableOpacity onPress={() => handleRemove(item.id)}>
                    <Feather name="trash-2" size={18} color="#EF4444" />
                  </TouchableOpacity>
                </View>
                <TextInput
                  style={styles.input}
                  value={item.title}
                  onChangeText={(val) => handleEdit(item.id, 'title', val)}
                  placeholder="Title"
                />
                <View style={styles.row}>
                  <TextInput
                    style={[styles.input, { flex: 1, marginRight: 8 }]}
                    value={item.date}
                    onChangeText={(val) => handleEdit(item.id, 'date', val)}
                    placeholder="Date"
                  />
                  <TextInput
                    style={[styles.input, { flex: 1 }]}
                    value={item.time}
                    onChangeText={(val) => handleEdit(item.id, 'time', val)}
                    placeholder="Time"
                  />
                </View>
              </View>
            ))}
          </ScrollView>

          <View style={styles.footer}>
            <TouchableOpacity style={styles.addBtn} onPress={handleAddAll}>
              <Text style={styles.addBtnText}>Add to LIVO</Text>
            </TouchableOpacity>
          </View>
        </View>
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: '#F8FAFC' },
  header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', padding: 16 },
  backBtn: { width: 40, height: 40, justifyContent: 'center', alignItems: 'center' },
  title: { fontSize: 18, fontWeight: '700', color: '#0F172A' },
  loadingContainer: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  loadingText: { marginTop: 16, fontSize: 16, color: '#64748B' },
  content: { flex: 1, padding: 16 },
  summaryBox: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#F0FDF4', padding: 12, borderRadius: 12, marginBottom: 16 },
  summaryText: { marginLeft: 8, fontSize: 15, color: '#16A34A', fontWeight: '500' },
  list: { flex: 1 },
  itemCard: { backgroundColor: '#FFF', padding: 16, borderRadius: 16, marginBottom: 12, borderWidth: 1, borderColor: '#E2E8F0' },
  itemHeader: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 12 },
  itemType: { fontSize: 13, fontWeight: '600', color: '#64748B', backgroundColor: '#F1F5F9', paddingHorizontal: 8, paddingVertical: 4, borderRadius: 8 },
  input: { backgroundColor: '#F8FAFC', borderRadius: 8, padding: 12, marginBottom: 8, color: '#0F172A', fontSize: 15 },
  row: { flexDirection: 'row' },
  footer: { paddingTop: 16 },
  addBtn: { backgroundColor: '#66C400', padding: 16, borderRadius: 16, alignItems: 'center' },
  addBtnText: { color: '#FFF', fontSize: 16, fontWeight: '600' },
});
