import React, { useState, useRef } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  SafeAreaView,
  StatusBar,
  Platform,
  TextInput,
  KeyboardAvoidingView,
} from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';

interface ChatMessage {
  id: string;
  type: 'ai' | 'user';
  text: string;
  timestamp?: string;
  showCheckmarks?: boolean;
  planCard?: {
    title: string;
    linkText: string;
    items: { time: string; title: string; subtitle: string; emoji?: string }[];
  };
  actionButtons?: { label: string; variant: 'primary' | 'outline' }[];
  quickTip?: { title: string; text: string };
}

interface ChatWithLivoScreenProps {
  onBack?: () => void;
}

export const ChatWithLivoScreen: React.FC<ChatWithLivoScreenProps> = ({ onBack }) => {
  const scrollViewRef = useRef<ScrollView>(null);
  const [inputText, setInputText] = useState('');

  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: '1',
      type: 'ai',
      text: "Hi Ritish Dev! 👋\n\nI'm LIVO, here to help you plan, stay focused, and make progress in your life.",
    },
    {
      id: '2',
      type: 'user',
      text: 'Can you help me plan the rest of my day?',
      timestamp: '10:24 AM',
      showCheckmarks: true,
    },
    {
      id: '3',
      type: 'ai',
      text: "Absolutely! Here's a plan for the rest of your day based on your tasks, calendar and energy levels.",
      timestamp: '10:24 AM',
    },
    {
      id: '4',
      type: 'ai',
      text: '',
      planCard: {
        title: 'Your Plan for Today',
        linkText: 'View in Plan →',
        items: [
          { time: '2:00 PM', title: 'Client Meeting', subtitle: 'Discuss project updates' },
          { time: '4:00 PM', title: 'Portfolio Review', subtitle: 'Go through designs' },
          { time: '5:00 PM', title: 'Gym', subtitle: 'Stay consistent', emoji: '💪' },
          { time: '7:00 PM', title: 'Free Time', subtitle: 'Relax or learn something new' },
        ],
      },
    },
    {
      id: '5',
      type: 'ai',
      text: 'You also have 3 unscheduled tasks. Would you like me to find the best time slots for them?',
      timestamp: '10:24 AM',
      actionButtons: [
        { label: 'Yes, schedule them', variant: 'primary' },
        { label: 'Maybe later', variant: 'outline' },
      ],
    },
    {
      id: '6',
      type: 'user',
      text: 'Yes, schedule them',
      timestamp: '10:25 AM',
      showCheckmarks: true,
    },
    {
      id: '7',
      type: 'ai',
      text: "Done! I've scheduled your tasks in available time slots. Your day is now 87% planned. 🎯",
      timestamp: '10:25 AM',
    },
    {
      id: '8',
      type: 'ai',
      text: '',
      quickTip: {
        title: 'Quick tip',
        text: 'You tend to be most productive in the mornings. Consider keeping afternoons for meetings and lighter work.',
      },
    },
  ]);

  const handleSendMessage = () => {
    if (!inputText.trim()) return;

    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      type: 'user',
      text: inputText.trim(),
      timestamp: 'Just now',
      showCheckmarks: true,
    };

    const aiReply: ChatMessage = {
      id: (Date.now() + 1).toString(),
      type: 'ai',
      text: `Great question! Let me analyze your schedule and preferences to help with "${inputText.trim()}". I'll have suggestions for you shortly.`,
      timestamp: 'Just now',
    };

    setMessages((prev) => [...prev, userMsg, aiReply]);
    setInputText('');

    setTimeout(() => {
      scrollViewRef.current?.scrollToEnd({ animated: true });
    }, 100);
  };

  const handleQuickChip = (label: string) => {
    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      type: 'user',
      text: label,
      timestamp: 'Just now',
      showCheckmarks: true,
    };

    const aiReply: ChatMessage = {
      id: (Date.now() + 1).toString(),
      type: 'ai',
      text: `I'd be happy to help you "${label.toLowerCase()}"! Let me look at your current schedule and tasks to give you the best recommendations.`,
      timestamp: 'Just now',
    };

    setMessages((prev) => [...prev, userMsg, aiReply]);

    setTimeout(() => {
      scrollViewRef.current?.scrollToEnd({ animated: true });
    }, 100);
  };

  const renderMessage = (msg: ChatMessage) => {
    if (msg.type === 'user') {
      return (
        <View key={msg.id} style={styles.userMsgRow}>
          <View style={styles.userBubble}>
            <Text style={styles.userBubbleText}>{msg.text}</Text>
          </View>
          {msg.timestamp && (
            <View style={styles.userTimestampRow}>
              <Text style={styles.timestampText}>{msg.timestamp}</Text>
              {msg.showCheckmarks && (
                <Text style={styles.checkmarksText}> ✓✓</Text>
              )}
            </View>
          )}
        </View>
      );
    }

    // AI message
    return (
      <View key={msg.id} style={styles.aiMsgRow}>
        {/* AI Sparkle Icon */}
        <View style={styles.aiIconCircle}>
          <Ionicons name="sparkles" size={12} color="#66C400" />
        </View>

        <View style={styles.aiContentWrap}>
          {/* Regular text bubble */}
          {msg.text ? (
            <View style={styles.aiBubble}>
              <Text style={styles.aiBubbleText}>{msg.text}</Text>
              {msg.timestamp && (
                <Text style={styles.aiTimestamp}>{msg.timestamp}</Text>
              )}
            </View>
          ) : null}

          {/* Plan Card */}
          {msg.planCard && (
            <View style={styles.planCard}>
              <View style={styles.planCardHeader}>
                <View style={styles.planCardTitleWrap}>
                  <Feather name="calendar" size={14} color="#0F172A" style={{ marginRight: 6 }} />
                  <Text style={styles.planCardTitle}>{msg.planCard.title}</Text>
                </View>
                <TouchableOpacity>
                  <Text style={styles.planCardLink}>{msg.planCard.linkText}</Text>
                </TouchableOpacity>
              </View>

              {msg.planCard.items.map((item, idx) => (
                <View key={idx} style={styles.planItem}>
                  <View style={[styles.planDot, { backgroundColor: '#66C400' }]} />
                  <View style={styles.planItemContent}>
                    <View style={styles.planItemTopRow}>
                      <Text style={styles.planItemTime}>{item.time}</Text>
                      <Text style={styles.planItemTitle}>
                        {item.title} {item.emoji || ''}
                      </Text>
                    </View>
                    <Text style={styles.planItemSub}>{item.subtitle}</Text>
                  </View>
                </View>
              ))}
            </View>
          )}

          {/* Action Buttons */}
          {msg.actionButtons && (
            <View style={styles.actionButtonsRow}>
              {msg.actionButtons.map((btn, idx) => (
                <TouchableOpacity
                  key={idx}
                  style={[
                    styles.actionBtn,
                    btn.variant === 'primary'
                      ? styles.actionBtnPrimary
                      : styles.actionBtnOutline,
                  ]}
                  onPress={() => handleQuickChip(btn.label)}
                >
                  <Text
                    style={[
                      styles.actionBtnText,
                      btn.variant === 'primary'
                        ? styles.actionBtnTextPrimary
                        : styles.actionBtnTextOutline,
                    ]}
                  >
                    {btn.label}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
          )}

          {/* Quick Tip Card */}
          {msg.quickTip && (
            <View style={styles.quickTipCard}>
              <View style={styles.quickTipHeader}>
                <View style={styles.quickTipIconCircle}>
                  <Ionicons name="bulb" size={14} color="#3B82F6" />
                </View>
                <Text style={styles.quickTipTitle}>{msg.quickTip.title}</Text>
              </View>
              <Text style={styles.quickTipText}>{msg.quickTip.text}</Text>
            </View>
          )}
        </View>
      </View>
    );
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        keyboardVerticalOffset={Platform.OS === 'ios' ? 0 : 0}
      >
        {/* Header */}
        <View style={styles.headerBar}>
          <TouchableOpacity
            style={styles.backBtn}
            onPress={onBack}
            hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
          >
            <Feather name="arrow-left" size={22} color="#0F172A" />
          </TouchableOpacity>

          <View style={styles.headerTitleWrap}>
            <Text style={styles.headerTitle}>Chat with LIVO</Text>
            <Text style={styles.headerSub}>Your personal life assistant</Text>
          </View>

          <View style={styles.headerRightActions}>
            <TouchableOpacity style={styles.headerIconBtn}>
              <Feather name="clock" size={18} color="#64748B" />
            </TouchableOpacity>
            <TouchableOpacity style={styles.headerIconBtn}>
              <Feather name="more-vertical" size={18} color="#64748B" />
            </TouchableOpacity>
            <View style={styles.avatarCircle}>
              <Text style={styles.avatarText}>R</Text>
            </View>
          </View>
        </View>

        {/* Chat Messages */}
        <ScrollView
          ref={scrollViewRef}
          style={styles.chatScrollArea}
          contentContainerStyle={styles.chatContent}
          showsVerticalScrollIndicator={false}
        >
          {/* Greeting with handwritten badge */}
          {messages.length > 0 && messages[0].id === '1' && (
            <View style={styles.greetingSection}>
              {renderMessage(messages[0])}
              <View style={styles.handwrittenBadge}>
                <Text style={styles.handwrittenLine1}>Small steps</Text>
                <Text style={styles.handwrittenLine2}>bigger tomorrow</Text>
                <View style={styles.handwrittenUnderline} />
              </View>
            </View>
          )}

          {/* Quick Action Chips (shown after greeting) */}
          <View style={styles.quickChipsContainer}>
            <ScrollView
              horizontal
              showsHorizontalScrollIndicator={false}
              contentContainerStyle={styles.quickChipsScroll}
            >
              <TouchableOpacity
                style={styles.quickChip}
                onPress={() => handleQuickChip('Plan my day')}
              >
                <Feather name="sun" size={13} color="#475569" style={{ marginRight: 4 }} />
                <Text style={styles.quickChipText}>Plan my day</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={styles.quickChip}
                onPress={() => handleQuickChip('Break down a goal')}
              >
                <Feather name="target" size={13} color="#475569" style={{ marginRight: 4 }} />
                <Text style={styles.quickChipText}>Break down a goal</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={styles.quickChip}
                onPress={() => handleQuickChip('Give me focus tips')}
              >
                <Ionicons name="bulb-outline" size={13} color="#475569" style={{ marginRight: 4 }} />
                <Text style={styles.quickChipText}>Give me focus tips</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={styles.quickChip}
                onPress={() => handleQuickChip('More suggestions')}
              >
                <Feather name="grid" size={13} color="#475569" style={{ marginRight: 4 }} />
                <Text style={styles.quickChipText}>More</Text>
              </TouchableOpacity>
            </ScrollView>
          </View>

          {/* Remaining messages */}
          {messages.slice(1).map(renderMessage)}

          <View style={{ height: 20 }} />
        </ScrollView>

        {/* Bottom Input Bar */}
        <View style={styles.inputBar}>
          <TouchableOpacity style={styles.attachBtn}>
            <Feather name="paperclip" size={18} color="#94A3B8" />
          </TouchableOpacity>

          <TextInput
            style={styles.textInput}
            placeholder="Ask LIVO anything..."
            placeholderTextColor="#94A3B8"
            value={inputText}
            onChangeText={setInputText}
            onSubmitEditing={handleSendMessage}
            returnKeyType="send"
          />

          <TouchableOpacity
            style={[
              styles.sendBtn,
              inputText.trim() ? styles.sendBtnActive : styles.sendBtnInactive,
            ]}
            onPress={handleSendMessage}
            disabled={!inputText.trim()}
          >
            <Feather name="arrow-up" size={18} color="#FFFFFF" />
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },

  /* Header Bar */
  headerBar: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    paddingVertical: 10,
    paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) + 8 : 10,
    backgroundColor: '#FFFFFF',
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  backBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  headerTitleWrap: {
    flex: 1,
  },
  headerTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
  },
  headerSub: {
    fontSize: 11,
    color: '#64748B',
    marginTop: -1,
  },
  headerRightActions: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  headerIconBtn: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 4,
  },
  avatarCircle: {
    width: 30,
    height: 30,
    borderRadius: 15,
    backgroundColor: '#E2F7C5',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 6,
  },
  avatarText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#2D6A00',
  },

  /* Chat Area */
  chatScrollArea: {
    flex: 1,
    backgroundColor: '#F8FAF5',
  },
  chatContent: {
    paddingHorizontal: 14,
    paddingTop: 16,
    paddingBottom: 10,
  },

  /* Greeting Section */
  greetingSection: {
    position: 'relative',
    marginBottom: 8,
  },
  handwrittenBadge: {
    position: 'absolute',
    top: 4,
    right: 0,
    alignItems: 'center',
    transform: [{ rotate: '-4deg' }],
  },
  handwrittenLine1: {
    fontSize: 13,
    fontFamily: Platform.OS === 'web' ? 'Caveat, cursive' : 'Caveat_700Bold',
    fontWeight: '700',
    color: '#2D6A00',
  },
  handwrittenLine2: {
    fontSize: 14,
    fontFamily: Platform.OS === 'web' ? 'Caveat, cursive' : 'Caveat_700Bold',
    fontWeight: '700',
    color: '#2D6A00',
    marginTop: -4,
  },
  handwrittenUnderline: {
    width: '90%',
    height: 2,
    backgroundColor: '#2D6A00',
    borderRadius: 1,
    marginTop: 0,
  },

  /* Quick Action Chips */
  quickChipsContainer: {
    marginBottom: 14,
  },
  quickChipsScroll: {
    flexDirection: 'row',
  },
  quickChip: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    borderRadius: 18,
    paddingHorizontal: 12,
    paddingVertical: 7,
    marginRight: 8,
  },
  quickChipText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#475569',
  },

  /* AI Message Row */
  aiMsgRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 12,
  },
  aiIconCircle: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: '#E2F7C5',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
    marginTop: 2,
  },
  aiContentWrap: {
    flex: 1,
  },
  aiBubble: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    borderTopLeftRadius: 4,
    padding: 12,
    borderWidth: 1,
    borderColor: '#F1F5F9',
    marginBottom: 4,
  },
  aiBubbleText: {
    fontSize: 13.5,
    color: '#0F172A',
    lineHeight: 20,
  },
  aiTimestamp: {
    fontSize: 10,
    color: '#94A3B8',
    marginTop: 6,
    textAlign: 'right',
  },

  /* User Message Row */
  userMsgRow: {
    alignItems: 'flex-end',
    marginBottom: 12,
  },
  userBubble: {
    backgroundColor: '#E2F7C5',
    borderRadius: 16,
    borderTopRightRadius: 4,
    padding: 12,
    maxWidth: '80%',
    borderWidth: 1,
    borderColor: '#D4EDC0',
  },
  userBubbleText: {
    fontSize: 13.5,
    color: '#0F172A',
    lineHeight: 20,
  },
  userTimestampRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 3,
    paddingRight: 2,
  },
  timestampText: {
    fontSize: 10,
    color: '#94A3B8',
  },
  checkmarksText: {
    fontSize: 10,
    color: '#66C400',
    fontWeight: '700',
  },

  /* Plan Card */
  planCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 6,
  },
  planCardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 14,
  },
  planCardTitleWrap: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  planCardTitle: {
    fontSize: 14,
    fontWeight: '800',
    color: '#0F172A',
  },
  planCardLink: {
    fontSize: 11.5,
    fontWeight: '700',
    color: '#64748B',
  },
  planItem: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 12,
    paddingLeft: 4,
  },
  planDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginTop: 5,
    marginRight: 10,
  },
  planItemContent: {
    flex: 1,
  },
  planItemTopRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  planItemTime: {
    fontSize: 12.5,
    fontWeight: '700',
    color: '#64748B',
    marginRight: 10,
    width: 55,
  },
  planItemTitle: {
    fontSize: 13.5,
    fontWeight: '800',
    color: '#0F172A',
  },
  planItemSub: {
    fontSize: 12,
    color: '#94A3B8',
    marginTop: 1,
    paddingLeft: 65,
  },

  /* Action Buttons */
  actionButtonsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginTop: 4,
    marginBottom: 4,
  },
  actionBtn: {
    borderRadius: 18,
    paddingHorizontal: 14,
    paddingVertical: 8,
    marginRight: 8,
    marginBottom: 4,
  },
  actionBtnPrimary: {
    backgroundColor: '#66C400',
  },
  actionBtnOutline: {
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  actionBtnText: {
    fontSize: 12.5,
    fontWeight: '700',
  },
  actionBtnTextPrimary: {
    color: '#FFFFFF',
  },
  actionBtnTextOutline: {
    color: '#475569',
  },

  /* Quick Tip Card */
  quickTipCard: {
    backgroundColor: '#F8FAFC',
    borderRadius: 14,
    padding: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginTop: 4,
  },
  quickTipHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 6,
  },
  quickTipIconCircle: {
    width: 22,
    height: 22,
    borderRadius: 11,
    backgroundColor: '#DBEAFE',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 6,
  },
  quickTipTitle: {
    fontSize: 13,
    fontWeight: '800',
    color: '#0F172A',
  },
  quickTipText: {
    fontSize: 12,
    color: '#475569',
    lineHeight: 18,
  },

  /* Bottom Input Bar */
  inputBar: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 8,
    backgroundColor: '#FFFFFF',
    borderTopWidth: 1,
    borderTopColor: '#F1F5F9',
  },
  attachBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 6,
  },
  textInput: {
    flex: 1,
    backgroundColor: '#F8FAFC',
    borderRadius: 22,
    paddingHorizontal: 14,
    paddingVertical: 9,
    fontSize: 13.5,
    color: '#0F172A',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginRight: 8,
  },
  sendBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
  },
  sendBtnActive: {
    backgroundColor: '#66C400',
  },
  sendBtnInactive: {
    backgroundColor: '#CBD5E1',
  },
});
