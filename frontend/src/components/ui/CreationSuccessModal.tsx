import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  Modal,
  TouchableOpacity,
  TouchableWithoutFeedback,
} from 'react-native';
import { Feather } from '@expo/vector-icons';

interface CreationSuccessModalProps {
  visible: boolean;
  onClose: () => void;
  onViewTask: () => void;
  itemType?: string; // 'Task', 'Event', 'Goal', 'Habit', etc.
  itemData: {
    title: string;
    date?: Date | string;
    time?: Date | string;
    priority?: string;
    category?: string;
  } | null;
}

export const CreationSuccessModal: React.FC<CreationSuccessModalProps> = ({
  visible,
  onClose,
  onViewTask,
  itemType = 'Task',
  itemData,
}) => {
  if (!itemData) return null;

  const formatDate = (dateVal: any) => {
    if (!dateVal) return '';
    const d = new Date(dateVal);
    if (isNaN(d.getTime())) return String(dateVal);
    return d.toLocaleDateString('en-US', {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    });
  };

  const formatTime = (timeVal: any) => {
    if (!timeVal) return '';
    const d = new Date(timeVal);
    if (isNaN(d.getTime())) return String(timeVal);
    return d.toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true,
    });
  };

  const renderPriorityBadge = (p?: string) => {
    if (!p) return null;
    const prio = p.toLowerCase();
    if (prio === 'high' || prio === 'urgent') {
      return (
        <View style={styles.highPrioBadge}>
          <Feather name="flag" size={10} color="#EF4444" style={{ marginRight: 4 }} />
          <Text style={styles.highPrioText}>{p}</Text>
        </View>
      );
    }
    return (
      <View style={styles.lowPrioBadge}>
        <Feather name="flag" size={10} color="#64748B" style={{ marginRight: 4 }} />
        <Text style={styles.lowPrioText}>{p}</Text>
      </View>
    );
  };

  const renderCategoryBadge = (c?: string) => {
    if (!c) return null;
    return (
      <View style={styles.categoryBadge}>
        <Feather name="briefcase" size={10} color="#16A34A" style={{ marginRight: 4 }} />
        <Text style={styles.categoryBadgeText}>{c}</Text>
      </View>
    );
  };

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <TouchableWithoutFeedback onPress={onClose}>
        <View style={styles.overlay} />
      </TouchableWithoutFeedback>
      <View style={styles.modalContainer}>
        <TouchableOpacity style={styles.closeBtn} onPress={onClose}>
          <Feather name="x" size={24} color="#0F172A" />
        </TouchableOpacity>

        {/* Success Icon */}
        <View style={styles.iconContainer}>
          <View style={[styles.ray, styles.ray1]} />
          <View style={[styles.ray, styles.ray2]} />
          <View style={[styles.ray, styles.ray3]} />
          <View style={[styles.ray, styles.ray4]} />
          <View style={[styles.ray, styles.ray5]} />
          
          <View style={styles.iconCircle}>
            <Feather name="check" size={40} color="#66C400" />
          </View>
        </View>

        <Text style={styles.title}>{itemType} Created!</Text>
        <Text style={styles.subtitle}>Your {itemType.toLowerCase()} has been added to your day.</Text>

        {/* Task Card */}
        <View style={styles.taskCard}>
          <View style={styles.taskCardLeft}>
            <View style={styles.taskCardCheck}>
              <Feather name="check" size={18} color="#66C400" />
            </View>
            <View style={styles.taskCardContent}>
              <Text style={styles.taskCardTitle}>{itemData.title}</Text>
              <Text style={styles.taskCardDateTime}>
                {formatDate(itemData.date)} • {formatTime(itemData.time || itemData.date)}
              </Text>
              <View style={styles.taskCardBadges}>
                {renderPriorityBadge(itemData.priority)}
              </View>
            </View>
          </View>
          <View style={styles.taskCardRight}>
            <View style={{ alignItems: 'flex-end' }}>
              {renderCategoryBadge(itemData.category)}
            </View>
            <Feather name="chevron-right" size={16} color="#94A3B8" style={{ marginLeft: 8 }} />
          </View>
        </View>

        {/* Action Button */}
        <TouchableOpacity style={styles.actionBtn} onPress={onViewTask}>
          <Text style={styles.actionBtnText}>View {itemType}</Text>
          <Feather name="arrow-right" size={18} color="#0F172A" />
        </TouchableOpacity>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  modalContainer: {
    position: 'absolute',
    top: '20%',
    left: '5%',
    right: '5%',
    backgroundColor: '#FFFFFF',
    borderRadius: 24,
    padding: 24,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.1,
    shadowRadius: 20,
    elevation: 10,
  },
  closeBtn: {
    position: 'absolute',
    top: 16,
    right: 16,
    padding: 8,
  },
  iconContainer: {
    marginTop: 32,
    marginBottom: 24,
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
  },
  ray: {
    position: 'absolute',
    backgroundColor: '#84CC16',
    width: 2.5,
    height: 10,
    borderRadius: 2,
  },
  ray1: { top: -20, left: '30%', transform: [{ rotate: '-45deg' }] },
  ray2: { top: -25, left: '50%', transform: [{ translateX: -1.25 }] },
  ray3: { top: -20, right: '30%', transform: [{ rotate: '45deg' }] },
  ray4: { top: '30%', right: -25, transform: [{ rotate: '75deg' }] },
  ray5: { top: '30%', left: -25, transform: [{ rotate: '-75deg' }] },
  iconCircle: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: '#F3FCD4',
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    fontSize: 22,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: '#64748B',
    marginBottom: 32,
  },
  taskCard: {
    width: '100%',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.04,
    shadowRadius: 16,
    elevation: 3,
    marginBottom: 32,
  },
  taskCardLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  taskCardCheck: {
    width: 44,
    height: 44,
    borderRadius: 12,
    borderWidth: 1.5,
    borderColor: '#84CC16',
    backgroundColor: '#F7FDF0',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  taskCardContent: {
    flex: 1,
  },
  taskCardTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
    marginBottom: 6,
  },
  taskCardDateTime: {
    fontSize: 11.5,
    color: '#64748B',
    marginBottom: 8,
  },
  taskCardBadges: {
    flexDirection: 'row',
  },
  highPrioBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FEF2F2',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  highPrioText: {
    fontSize: 10,
    fontWeight: '600',
    color: '#EF4444',
  },
  lowPrioBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F1F5F9',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  lowPrioText: {
    fontSize: 10,
    fontWeight: '600',
    color: '#64748B',
  },
  taskCardRight: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  categoryBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F0FDF4',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  categoryBadgeText: {
    fontSize: 10,
    fontWeight: '600',
    color: '#16A34A',
  },
  actionBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#84CC16',
    width: '100%',
    paddingVertical: 14,
    borderRadius: 14,
  },
  actionBtnText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#0F172A',
    marginRight: 6,
  },
});
