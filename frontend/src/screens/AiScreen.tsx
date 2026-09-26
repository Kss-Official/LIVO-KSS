import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  ScrollView,
  SafeAreaView,
  StatusBar,
  Platform,
} from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { useAi } from '../hooks/useAi';

interface AiScreenProps {
  onBack?: () => void;
}

export const AiScreen: React.FC<AiScreenProps> = ({ onBack }) => {
  const navigation = useNavigation<any>();
  const { messages: historyMessages, sendMessage: sendAiMessage } = useAi();

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };
  
  // Combine local mock messages and hook history
  const defaultMessages = [
    {
      id: 'm1',
      sender: 'LIVO_AI' as const,
      text: "Hello Ritish! I've analyzed your upcoming schedule and priorities for today. You have 4 tasks, 3 events, and 1 schedule overlap at 10:00 AM. What would you like help with?",
      timestamp: '10:00 AM',
    },
    {
      id: 'm2',
      sender: 'LIVO_AI' as const,
      text: "Recommendation: Moving 'Finish LIVO AI Engine setup' to 2:00 PM will keep your afternoon free for deep work.",
      timestamp: '10:01 AM',
      suggestedAction: {
        type: 'MOVE_TASK',
        description: 'Move Task "Finish LIVO AI Engine setup" to 2:00 PM',
        payload: { taskId: '1', newTime: '14:00' },
      },
    },
  ];

  const messages = [...defaultMessages, ...historyMessages];

  const [inputText, setInputText] = useState('');

  const handleSendMessage = async (textToSend?: string) => {
    const query = textToSend || inputText;
    if (!query.trim()) return;

    setInputText('');
    await sendAiMessage(query);
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      <View style={styles.container}>
        {/* 1. Top Header Row */}
        <View style={styles.headerRow}>
          <View style={{ flexDirection: 'row', alignItems: 'center' }}>
            <TouchableOpacity
              onPress={handleBack}
              style={{ marginRight: 10, padding: 4 }}
              hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
            >
              <Feather name="arrow-left" size={22} color="#0F172A" />
            </TouchableOpacity>
            <View>
              <View style={styles.logoRow}>
                <Text style={styles.logoText}>LIVO</Text>
                <View style={styles.logoDot} />
              </View>
              <Text style={styles.logoSubtitle}>A BETTER YOU</Text>
            </View>
          </View>

          <View style={styles.headerRightActions}>
            <TouchableOpacity style={styles.iconBtn} onPress={() => navigation.navigate('Search')}>
              <Feather name="search" size={20} color="#0F172A" />
            </TouchableOpacity>

            <TouchableOpacity style={styles.iconBtn} onPress={() => navigation.navigate('Notifications')}>
              <Feather name="bell" size={20} color="#0F172A" />
              <View style={styles.notificationBadge} />
            </TouchableOpacity>

            <TouchableOpacity style={styles.avatarCircle} onPress={() => navigation.navigate('Profile')}>
              <Text style={styles.avatarText}>R</Text>
            </TouchableOpacity>
          </View>
        </View>

        {/* 2. Banner Title */}
        <View style={styles.aiTitleBanner}>
          <View style={styles.aiTitleLeft}>
            <View style={styles.sparkleIconCircle}>
              <Ionicons name="sparkles" size={16} color="#7C3AED" />
            </View>
            <View>
              <Text style={styles.aiTitleText}>LIVO AI Engine</Text>
              <Text style={styles.aiSubText}>Context-Aware Intelligence & Decision Support</Text>
            </View>
          </View>
        </View>

        {/* 3. Chat Messages Scroll Area */}
        <ScrollView style={styles.chatArea} contentContainerStyle={styles.chatContent}>
          {messages.map((msg) => (
            <View
              key={msg.id}
              style={[
                styles.messageBubble,
                msg.sender === 'USER' ? styles.userBubble : styles.aiBubble,
              ]}
            >
              <View style={styles.bubbleHeaderRow}>
                <Text
                  style={[
                    styles.senderLabel,
                    msg.sender === 'USER' ? styles.userSenderLabel : styles.aiSenderLabel,
                  ]}
                >
                  {msg.sender === 'USER' ? 'You' : '✦ LIVO AI'}
                </Text>
                <Text style={styles.timestampText}>{msg.timestamp}</Text>
              </View>

              <Text style={[styles.messageText, msg.sender === 'USER' && styles.userMessageText]}>
                {msg.text}
              </Text>

              {msg.suggestedAction && (
                <View style={styles.actionCard}>
                  <Text style={styles.actionTitle}>Proposed Action (Backend Validated):</Text>
                  <Text style={styles.actionDesc}>{msg.suggestedAction.description}</Text>
                  <TouchableOpacity style={styles.confirmActionButton}>
                    <Feather name="check" size={13} color="#FFFFFF" style={{ marginRight: 4 }} />
                    <Text style={styles.confirmActionText}>Confirm Action</Text>
                  </TouchableOpacity>
                </View>
              )}
            </View>
          ))}
        </ScrollView>

        {/* 4. Quick Suggestion Chips */}
        <View style={styles.quickChipsWrapper}>
          <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.chipsScroll}>
            <TouchableOpacity
              style={styles.chipBtn}
              onPress={() => handleSendMessage('Fix schedule overlap at 10:00 AM')}
            >
              <Text style={styles.chipText}>Fix schedule overlap</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.chipBtn}
              onPress={() => handleSendMessage('Optimize my afternoon tasks')}
            >
              <Text style={styles.chipText}>Optimize afternoon</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.chipBtn}
              onPress={() => handleSendMessage('Suggest focus habits for today')}
            >
              <Text style={styles.chipText}>Suggest focus habits</Text>
            </TouchableOpacity>
          </ScrollView>
        </View>

        {/* 5. Bottom Input Row */}
        <View style={styles.inputContainer}>
          <TextInput
            style={styles.textInput}
            placeholder="Ask LIVO AI (e.g. 'What should I focus on?')"
            placeholderTextColor="#94A3B8"
            value={inputText}
            onChangeText={setInputText}
          />
          <TouchableOpacity style={styles.sendButton} onPress={() => handleSendMessage()}>
            <Feather name="send" size={16} color="#FFFFFF" />
          </TouchableOpacity>
        </View>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  container: {
    flex: 1,
    backgroundColor: '#F8FAFC',
    paddingTop: Platform.OS === 'android' ? (StatusBar.currentHeight || 24) + 14 : 20,
  },

  /* 1. Header */
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    marginBottom: 12,
  },
  logoRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  logoText: {
    fontSize: 24,
    fontWeight: '900',
    color: '#0F172A',
    letterSpacing: 0.5,
  },
  logoDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#66C400',
    marginLeft: 2,
    marginTop: 6,
  },
  logoSubtitle: {
    fontSize: 9,
    fontWeight: '700',
    color: '#94A3B8',
    letterSpacing: 1.2,
    marginTop: -2,
  },
  headerRightActions: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  iconBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#F1F5F9',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 8,
    position: 'relative',
  },
  notificationBadge: {
    position: 'absolute',
    top: 8,
    right: 8,
    width: 7,
    height: 7,
    borderRadius: 3.5,
    backgroundColor: '#EF4444',
  },
  avatarCircle: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: '#E2F7C5',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 10,
  },
  avatarText: {
    fontSize: 15,
    fontWeight: '700',
    color: '#2D6A00',
  },

  /* 2. Banner Title */
  aiTitleBanner: {
    backgroundColor: '#F5EFFF',
    marginHorizontal: 16,
    borderRadius: 16,
    padding: 12,
    borderWidth: 1,
    borderColor: '#EDE9FE',
    marginBottom: 10,
  },
  aiTitleLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  sparkleIconCircle: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#E9D5FF',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  aiTitleText: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
  },
  aiSubText: {
    fontSize: 11.5,
    color: '#7C3AED',
    marginTop: 1,
  },

  /* 3. Chat Area */
  chatArea: {
    flex: 1,
  },
  chatContent: {
    paddingHorizontal: 16,
    paddingTop: 8,
    paddingBottom: 16,
  },
  messageBubble: {
    borderRadius: 16,
    padding: 14,
    marginBottom: 12,
    maxWidth: '88%',
    borderWidth: 1,
  },
  userBubble: {
    backgroundColor: '#0F172A',
    borderColor: '#1E293B',
    alignSelf: 'flex-end',
  },
  aiBubble: {
    backgroundColor: '#FFFFFF',
    borderColor: '#E2E8F0',
    alignSelf: 'flex-start',
  },
  bubbleHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  senderLabel: {
    fontSize: 11,
    fontWeight: '800',
  },
  userSenderLabel: {
    color: '#94A3B8',
  },
  aiSenderLabel: {
    color: '#7C3AED',
  },
  timestampText: {
    fontSize: 10,
    color: '#94A3B8',
    marginLeft: 8,
  },
  messageText: {
    color: '#0F172A',
    fontSize: 13.5,
    lineHeight: 19,
  },
  userMessageText: {
    color: '#FFFFFF',
  },

  actionCard: {
    marginTop: 10,
    backgroundColor: '#F5EFFF',
    borderRadius: 12,
    padding: 10,
    borderWidth: 1,
    borderColor: '#DDD6FE',
  },
  actionTitle: {
    fontSize: 11,
    fontWeight: '800',
    color: '#7C3AED',
    marginBottom: 2,
  },
  actionDesc: {
    fontSize: 12,
    color: '#0F172A',
    marginBottom: 8,
  },
  confirmActionButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#66C400',
    paddingVertical: 7,
    paddingHorizontal: 12,
    borderRadius: 10,
    alignSelf: 'flex-start',
  },
  confirmActionText: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: '700',
  },

  /* 4. Quick Suggestion Chips */
  quickChipsWrapper: {
    paddingHorizontal: 12,
    marginBottom: 8,
  },
  chipsScroll: {
    flexDirection: 'row',
  },
  chipBtn: {
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 16,
    marginHorizontal: 4,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  chipText: {
    fontSize: 11.5,
    fontWeight: '600',
    color: '#64748B',
  },

  /* 5. Input Container */
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderTopWidth: 1,
    borderTopColor: '#E2E8F0',
    backgroundColor: '#FFFFFF',
  },
  textInput: {
    flex: 1,
    backgroundColor: '#F8FAFC',
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 10,
    color: '#0F172A',
    marginRight: 10,
    fontSize: 13.5,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  sendButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#66C400',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
