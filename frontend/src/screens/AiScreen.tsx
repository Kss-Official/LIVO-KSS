import React, { useState } from 'react';
import { View, Text, StyleSheet, TextInput, TouchableOpacity, ScrollView } from 'react-native';
import { Colors } from '../theme/colors';
import { AiChatMessage } from '../types';

export const AiScreen: React.FC = () => {
  const [messages, setMessages] = useState<AiChatMessage[]>([
    {
      id: 'm1',
      sender: 'LIVO_AI',
      text: "Hello Alex! I've analyzed your upcoming schedule and priorities for today. You have 3 tasks, 2 active goals, and 1 schedule overlap at 10:00 AM. What would you like help with?",
      timestamp: '10:00 AM',
    },
  ]);
  const [inputText, setInputText] = useState('');

  const handleSendMessage = () => {
    if (!inputText.trim()) return;

    const userMsg: AiChatMessage = {
      id: Date.now().toString(),
      sender: 'USER',
      text: inputText,
      timestamp: 'Just now',
    };

    const aiReply: AiChatMessage = {
      id: (Date.now() + 1).toString(),
      sender: 'LIVO_AI',
      text: `Analyzing context for "${inputText}"... I recommend rescheduling Task A to 2:00 PM to eliminate your morning overlap.`,
      timestamp: 'Just now',
      suggestedAction: {
        type: 'MOVE_TASK',
        description: 'Move Task "Finish LIVO AI Engine setup" to 2:00 PM',
        payload: { taskId: '1', newTime: '14:00' },
      },
    };

    setMessages((prev) => [...prev, userMsg, aiReply]);
    setInputText('');
  };

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>LIVO AI Engine</Text>
        <Text style={styles.headerSub}>Context-Aware Intelligence & Decision Support</Text>
      </View>

      {/* Chat Messages */}
      <ScrollView style={styles.chatArea} contentContainerStyle={styles.chatContent}>
        {messages.map((msg) => (
          <View
            key={msg.id}
            style={[
              styles.messageBubble,
              msg.sender === 'USER' ? styles.userBubble : styles.aiBubble,
            ]}
          >
            <Text style={styles.senderLabel}>
              {msg.sender === 'USER' ? 'You' : '✨ LIVO AI'}
            </Text>
            <Text style={styles.messageText}>{msg.text}</Text>

            {msg.suggestedAction && (
              <View style={styles.actionCard}>
                <Text style={styles.actionTitle}>Proposed Action (Backend Validated):</Text>
                <Text style={styles.actionDesc}>{msg.suggestedAction.description}</Text>
                <TouchableOpacity style={styles.confirmActionButton}>
                  <Text style={styles.confirmActionText}>Confirm Action</Text>
                </TouchableOpacity>
              </View>
            )}
          </View>
        ))}
      </ScrollView>

      {/* Input Row */}
      <View style={styles.inputContainer}>
        <TextInput
          style={styles.textInput}
          placeholder="Ask LIVO AI (e.g. 'What should I focus on today?')"
          placeholderTextColor={Colors.textSecondary}
          value={inputText}
          onChangeText={setInputText}
        />
        <TouchableOpacity style={styles.sendButton} onPress={handleSendMessage}>
          <Text style={styles.sendText}>Send</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  header: {
    padding: 20,
    paddingTop: 50,
    borderBottomWidth: 1,
    borderBottomColor: Colors.border,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: 'bold',
    color: Colors.textPrimary,
  },
  headerSub: {
    fontSize: 12,
    color: Colors.aiPurple,
    marginTop: 2,
  },
  chatArea: {
    flex: 1,
  },
  chatContent: {
    padding: 16,
  },
  messageBubble: {
    borderRadius: 14,
    padding: 14,
    marginBottom: 14,
    maxWidth: '85%',
  },
  userBubble: {
    backgroundColor: Colors.primaryDark,
    alignSelf: 'flex-end',
  },
  aiBubble: {
    backgroundColor: Colors.cardBackground,
    borderColor: Colors.aiPurple,
    borderWidth: 1,
    alignSelf: 'flex-start',
  },
  senderLabel: {
    fontSize: 11,
    fontWeight: 'bold',
    color: Colors.textSecondary,
    marginBottom: 4,
  },
  messageText: {
    color: Colors.textPrimary,
    fontSize: 14,
    lineHeight: 20,
  },
  actionCard: {
    marginTop: 10,
    backgroundColor: 'rgba(139, 92, 246, 0.15)',
    borderRadius: 10,
    padding: 10,
    borderColor: Colors.aiPurple,
    borderWidth: 1,
  },
  actionTitle: {
    fontSize: 11,
    fontWeight: 'bold',
    color: Colors.aiPurple,
    marginBottom: 2,
  },
  actionDesc: {
    fontSize: 12,
    color: Colors.textPrimary,
    marginBottom: 8,
  },
  confirmActionButton: {
    backgroundColor: Colors.aiPurple,
    paddingVertical: 6,
    paddingHorizontal: 10,
    borderRadius: 6,
    alignSelf: 'flex-start',
  },
  confirmActionText: {
    color: Colors.white,
    fontSize: 12,
    fontWeight: 'bold',
  },
  inputContainer: {
    flexDirection: 'row',
    padding: 14,
    borderTopWidth: 1,
    borderTopColor: Colors.border,
    backgroundColor: Colors.cardBackground,
  },
  textInput: {
    flex: 1,
    backgroundColor: Colors.background,
    borderRadius: 10,
    paddingHorizontal: 14,
    color: Colors.textPrimary,
    marginRight: 10,
    fontSize: 14,
  },
  sendButton: {
    backgroundColor: Colors.primary,
    borderRadius: 10,
    paddingHorizontal: 16,
    justifyContent: 'center',
  },
  sendText: {
    color: Colors.white,
    fontWeight: 'bold',
    fontSize: 14,
  },
});
