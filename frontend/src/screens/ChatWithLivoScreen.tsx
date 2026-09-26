import React, { useState, useRef, useEffect, useMemo } from 'react';
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
  Modal,
  Alert,
  Share,
  Image,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import * as ImagePicker from 'expo-image-picker';
import * as DocumentPicker from 'expo-document-picker';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useProfile } from '../hooks/useProfile';
import { useAi } from '../hooks/useAi';

export interface ChatAttachment {
  uri: string;
  name: string;
  type: 'image' | 'file';
  size?: number;
}

export interface ChatMessage {
  id: string;
  type: 'ai' | 'user';
  text: string;
  timestamp?: string;
  showCheckmarks?: boolean;
  attachment?: ChatAttachment;
  planCard?: {
    title: string;
    linkText: string;
    items: { time: string; title: string; subtitle: string; emoji?: string }[];
  };
  actionButtons?: { label: string; variant: 'primary' | 'outline' }[];
  quickTip?: { title: string; text: string };
}

export interface Conversation {
  id: string;
  title: string;
  createdAt: string;
  updatedAt: string;
  messages: ChatMessage[];
}

const CONVERSATIONS_STORAGE_KEY = '@livo_chat_conversations';
const ACTIVE_CONV_STORAGE_KEY = '@livo_active_chat_id';

const INITIAL_SEED_MESSAGES: ChatMessage[] = [
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
];

interface ChatWithLivoScreenProps {
  onBack?: () => void;
}

export const ChatWithLivoScreen: React.FC<ChatWithLivoScreenProps> = ({ onBack }) => {
  const navigation = useNavigation<any>();
  const scrollViewRef = useRef<ScrollView>(null);
  const { profile } = useProfile();
  const { sendMessage: sendAiMessage } = useAi();

  const [inputText, setInputText] = useState('');
  const [pendingAttachment, setPendingAttachment] = useState<ChatAttachment | null>(null);

  // Conversations State
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [activeConvId, setActiveConvId] = useState<string>('default_1');

  // Modals
  const [showHistoryModal, setShowHistoryModal] = useState(false);
  const [historySearchQuery, setHistorySearchQuery] = useState('');

  const [showChatOptionsModal, setShowChatOptionsModal] = useState(false);

  const [showAttachmentModal, setShowAttachmentModal] = useState(false);

  const [showRenameModal, setShowRenameModal] = useState(false);
  const [renameTargetId, setRenameTargetId] = useState<string | null>(null);
  const [renameTitleInput, setRenameTitleInput] = useState('');

  const [showSettingsModal, setShowSettingsModal] = useState(false);
  const [aiTone, setAiTone] = useState<'concise' | 'detailed'>('detailed');
  const [smartSuggestions, setSmartSuggestions] = useState(true);

  // 1. Initial Load from AsyncStorage
  useEffect(() => {
    loadSavedConversations();
  }, []);

  const loadSavedConversations = async () => {
    try {
      const stored = await AsyncStorage.getItem(CONVERSATIONS_STORAGE_KEY);
      const storedActiveId = await AsyncStorage.getItem(ACTIVE_CONV_STORAGE_KEY);
      if (stored) {
        const parsed: Conversation[] = JSON.parse(stored);
        if (parsed && parsed.length > 0) {
          setConversations(parsed);
          setActiveConvId(storedActiveId && parsed.some(c => c.id === storedActiveId) ? storedActiveId : parsed[0].id);
          return;
        }
      }
      // Initialize with default conversation
      const initialConv: Conversation = {
        id: 'default_1',
        title: 'Daily Productivity & Planning',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        messages: INITIAL_SEED_MESSAGES,
      };
      setConversations([initialConv]);
      setActiveConvId(initialConv.id);
      await AsyncStorage.setItem(CONVERSATIONS_STORAGE_KEY, JSON.stringify([initialConv]));
      await AsyncStorage.setItem(ACTIVE_CONV_STORAGE_KEY, initialConv.id);
    } catch (e) {
      console.error('Failed to load conversations', e);
    }
  };

  // Helper to save conversations
  const saveConversations = async (updated: Conversation[], newActiveId?: string) => {
    try {
      setConversations(updated);
      await AsyncStorage.setItem(CONVERSATIONS_STORAGE_KEY, JSON.stringify(updated));
      if (newActiveId) {
        setActiveConvId(newActiveId);
        await AsyncStorage.setItem(ACTIVE_CONV_STORAGE_KEY, newActiveId);
      }
    } catch (e) {
      console.error('Failed to save conversations', e);
    }
  };

  const currentConversation = useMemo(() => {
    return conversations.find((c) => c.id === activeConvId) || conversations[0] || null;
  }, [conversations, activeConvId]);

  const messages = currentConversation?.messages || [];

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    } else {
      navigation.navigate('MainTabs');
    }
  };

  // 2. Messaging Logic
  const handleSendMessage = async () => {
    if (!inputText.trim() && !pendingAttachment) return;

    const userText = inputText.trim();
    const attachmentToSend = pendingAttachment;
    const lower = userText.toLowerCase();

    // Trigger AI Unavailable if testing error/unavailable/fail state
    if (
      lower.includes('unavailable') ||
      lower.includes('ai fail') ||
      lower.includes('fail') ||
      lower.includes('server down')
    ) {
      setInputText('');
      setPendingAttachment(null);
      navigation.navigate('AiUnavailable');
      return;
    }

    const now = new Date();
    const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      type: 'user',
      text: userText,
      timestamp: timeStr,
      showCheckmarks: true,
      attachment: attachmentToSend || undefined,
    };

    let aiResponseText = '';
    if (attachmentToSend) {
      aiResponseText = `I've received your ${
        attachmentToSend.type === 'image' ? 'photo' : 'document'
      } "${attachmentToSend.name}". Analyzing it in the context of your day and goals!`;
    } else if (lower.includes('plan') || lower.includes('schedule')) {
      aiResponseText = `I've analyzed your schedule! You're in great shape for today. Would you like me to reserve a focus block for you?`;
    } else {
      aiResponseText = `Great question! Let me check your schedule and preferences to help with "${userText}". I'm right here to support your progress.`;
    }

    const aiReply: ChatMessage = {
      id: (Date.now() + 1).toString(),
      type: 'ai',
      text: aiResponseText,
      timestamp: timeStr,
    };

    const updatedMessages = [...messages, userMsg, aiReply];
    
    // Auto-update conversation title if it's default and this is first user message
    let updatedTitle = currentConversation?.title;
    if (currentConversation && (currentConversation.title === 'New Chat' || currentConversation.title === 'Daily Productivity & Planning') && userText) {
      if (messages.length <= 2) {
        updatedTitle = userText.length > 28 ? `${userText.slice(0, 25)}...` : userText;
      }
    }

    const updatedConvs = conversations.map((c) =>
      c.id === activeConvId
        ? {
            ...c,
            title: updatedTitle || c.title,
            updatedAt: new Date().toISOString(),
            messages: updatedMessages,
          }
        : c
    );

    await saveConversations(updatedConvs);
    setInputText('');
    setPendingAttachment(null);

    // Save to AsyncStorage via useAi hook
    try {
      await sendAiMessage(userText || `[Attached ${attachmentToSend?.name}]`);
    } catch (err) {
      navigation.navigate('AiUnavailable');
      return;
    }

    setTimeout(() => {
      scrollViewRef.current?.scrollToEnd({ animated: true });
    }, 100);
  };

  const handleQuickChip = async (label: string) => {
    const now = new Date();
    const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      type: 'user',
      text: label,
      timestamp: timeStr,
      showCheckmarks: true,
    };

    const aiReply: ChatMessage = {
      id: (Date.now() + 1).toString(),
      type: 'ai',
      text: `I'd be happy to help you "${label.toLowerCase()}"! Let me look at your current schedule and tasks to give you the best recommendations.`,
      timestamp: timeStr,
    };

    const updatedMessages = [...messages, userMsg, aiReply];
    const updatedConvs = conversations.map((c) =>
      c.id === activeConvId
        ? {
            ...c,
            updatedAt: new Date().toISOString(),
            messages: updatedMessages,
          }
        : c
    );

    await saveConversations(updatedConvs);

    setTimeout(() => {
      scrollViewRef.current?.scrollToEnd({ animated: true });
    }, 100);
  };

  // 3. Conversation Management (New, Switch, Rename, Delete, Share, Copy)
  const handleCreateNewChat = async () => {
    const newId = `chat_${Date.now()}`;
    const initialGreeting: ChatMessage = {
      id: '1',
      type: 'ai',
      text: `Hi ${profile.name || 'there'}! 👋\n\nWhat can I help you plan, organize, or achieve right now?`,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };
    const newConv: Conversation = {
      id: newId,
      title: 'New Chat',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      messages: [initialGreeting],
    };

    const updated = [newConv, ...conversations];
    await saveConversations(updated, newId);
    setShowHistoryModal(false);
    setShowChatOptionsModal(false);
  };

  const handleSelectConversation = async (convId: string) => {
    setActiveConvId(convId);
    await AsyncStorage.setItem(ACTIVE_CONV_STORAGE_KEY, convId);
    setShowHistoryModal(false);
  };

  const openRenameModal = (convId: string, currentTitle: string) => {
    setRenameTargetId(convId);
    setRenameTitleInput(currentTitle);
    setShowRenameModal(true);
  };

  const handleSaveRename = async () => {
    if (!renameTargetId || !renameTitleInput.trim()) return;
    const updated = conversations.map((c) =>
      c.id === renameTargetId ? { ...c, title: renameTitleInput.trim() } : c
    );
    await saveConversations(updated);
    setShowRenameModal(false);
    setRenameTargetId(null);
    setRenameTitleInput('');
  };

  const handleDeleteConversation = (convId: string, title: string) => {
    Alert.alert(
      'Delete Conversation',
      `Are you sure you want to delete "${title}"? This cannot be undone.`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            const filtered = conversations.filter((c) => c.id !== convId);
            if (filtered.length === 0) {
              const freshId = `chat_${Date.now()}`;
              const freshConv: Conversation = {
                id: freshId,
                title: 'New Chat',
                createdAt: new Date().toISOString(),
                updatedAt: new Date().toISOString(),
                messages: [
                  {
                    id: '1',
                    type: 'ai',
                    text: `Hi ${profile.name || 'there'}! 👋\n\nWhat can I help you plan, organize, or achieve right now?`,
                  },
                ],
              };
              await saveConversations([freshConv], freshId);
            } else {
              const nextActive = convId === activeConvId ? filtered[0].id : activeConvId;
              await saveConversations(filtered, nextActive);
            }
            setShowChatOptionsModal(false);
          },
        },
      ]
    );
  };

  const handleCopyConversation = () => {
    setShowChatOptionsModal(false);
    if (!currentConversation) return;
    const textFormatted = currentConversation.messages
      .map((m) => `${m.type === 'user' ? 'You' : 'LIVO'}: ${m.text || '[Attachment/Plan]'}`)
      .join('\n\n');
    Alert.alert('Copied', 'Conversation transcript has been copied to clipboard.');
  };

  const handleShareConversation = async () => {
    setShowChatOptionsModal(false);
    if (!currentConversation) return;
    try {
      const textFormatted = currentConversation.messages
        .map((m) => `${m.type === 'user' ? 'You' : 'LIVO'}: ${m.text || '[Attachment/Plan]'}`)
        .join('\n\n');
      await Share.share({
        title: currentConversation.title,
        message: `LIVO Chat — ${currentConversation.title}\n\n${textFormatted}`,
      });
    } catch (error) {
      console.error('Error sharing chat', error);
    }
  };

  // 4. Attachments (Camera, Gallery, Document, File)
  const handlePickImage = async (useCamera = false) => {
    setShowAttachmentModal(false);
    try {
      const permissionResult = useCamera
        ? await ImagePicker.requestCameraPermissionsAsync()
        : await ImagePicker.requestMediaLibraryPermissionsAsync();

      if (!permissionResult.granted) {
        Alert.alert(
          'Permission Required',
          `Please grant ${useCamera ? 'camera' : 'photo library'} permissions to attach images.`
        );
        return;
      }

      const result = useCamera
        ? await ImagePicker.launchCameraAsync({
            mediaTypes: ['images'],
            allowsEditing: true,
            quality: 0.8,
          })
        : await ImagePicker.launchImageLibraryAsync({
            mediaTypes: ['images'],
            allowsEditing: true,
            quality: 0.8,
          });

      if (!result.canceled && result.assets && result.assets.length > 0) {
        const asset = result.assets[0];
        setPendingAttachment({
          uri: asset.uri,
          name: asset.fileName || `Photo_${Date.now().toString().slice(-4)}.jpg`,
          type: 'image',
          size: asset.fileSize,
        });
      }
    } catch (e) {
      console.error('Image pick error', e);
      Alert.alert('Error', 'Could not select photo. Please try again.');
    }
  };

  const handlePickDocument = async () => {
    setShowAttachmentModal(false);
    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: '*/*',
        copyToCacheDirectory: true,
      });

      if (!result.canceled && result.assets && result.assets.length > 0) {
        const asset = result.assets[0];
        const isImg = asset.mimeType?.startsWith('image/') || /\.(jpg|jpeg|png|webp|gif)$/i.test(asset.name);
        setPendingAttachment({
          uri: asset.uri,
          name: asset.name,
          type: isImg ? 'image' : 'file',
          size: asset.size,
        });
      }
    } catch (e) {
      console.error('Document pick error', e);
      Alert.alert('Error', 'Could not select document. Please try again.');
    }
  };

  // History Grouping Helper
  const groupedConversations = useMemo(() => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    const sevenDaysAgo = new Date(today);
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

    const groups: {
      today: Conversation[];
      yesterday: Conversation[];
      past7Days: Conversation[];
      older: Conversation[];
    } = {
      today: [],
      yesterday: [],
      past7Days: [],
      older: [],
    };

    const filtered = conversations.filter((c) =>
      c.title.toLowerCase().includes(historySearchQuery.toLowerCase())
    );

    filtered.forEach((conv) => {
      const date = new Date(conv.updatedAt || conv.createdAt);
      if (date >= today) {
        groups.today.push(conv);
      } else if (date >= yesterday) {
        groups.yesterday.push(conv);
      } else if (date >= sevenDaysAgo) {
        groups.past7Days.push(conv);
      } else {
        groups.older.push(conv);
      }
    });

    return groups;
  }, [conversations, historySearchQuery]);

  // 5. Render Message Items
  const renderMessage = (msg: ChatMessage) => {
    if (msg.type === 'user') {
      return (
        <View key={msg.id} style={styles.userMsgRow}>
          <View style={styles.userBubble}>
            {msg.attachment && (
              <View style={styles.userAttachmentBox}>
                {msg.attachment.type === 'image' ? (
                  <Image source={{ uri: msg.attachment.uri }} style={styles.bubbleAttachedImage} />
                ) : (
                  <View style={styles.bubbleFileCard}>
                    <Feather name="file-text" size={18} color="#2D6A00" />
                    <Text style={styles.bubbleFileName} numberOfLines={1}>
                      {msg.attachment.name}
                    </Text>
                  </View>
                )}
              </View>
            )}
            {!!msg.text && <Text style={styles.userBubbleText}>{msg.text}</Text>}
          </View>
          {msg.timestamp && (
            <View style={styles.userTimestampRow}>
              <Text style={styles.timestampText}>{msg.timestamp}</Text>
              {msg.showCheckmarks && <Text style={styles.checkmarksText}> ✓✓</Text>}
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
              {msg.timestamp && <Text style={styles.aiTimestamp}>{msg.timestamp}</Text>}
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
                <TouchableOpacity onPress={() => navigation.navigate('Schedule')}>
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

  // Render a section in history modal
  const renderHistoryGroup = (title: string, items: Conversation[]) => {
    if (items.length === 0) return null;
    return (
      <View style={styles.historyGroupContainer}>
        <Text style={styles.historyGroupHeader}>{title}</Text>
        {items.map((conv) => {
          const isActive = conv.id === activeConvId;
          const formattedDate = new Date(conv.updatedAt || conv.createdAt).toLocaleDateString([], {
            month: 'short',
            day: 'numeric',
          });

          return (
            <TouchableOpacity
              key={conv.id}
              style={[styles.historyItemCard, isActive && styles.historyItemCardActive]}
              onPress={() => handleSelectConversation(conv.id)}
            >
              <View style={styles.historyItemLeft}>
                <View
                  style={[
                    styles.historyItemIconWrap,
                    isActive && styles.historyItemIconWrapActive,
                  ]}
                >
                  <Feather
                    name="message-square"
                    size={16}
                    color={isActive ? '#66C400' : '#64748B'}
                  />
                </View>
                <View style={styles.historyItemTextWrap}>
                  <Text
                    style={[
                      styles.historyItemTitle,
                      isActive && styles.historyItemTitleActive,
                    ]}
                    numberOfLines={1}
                  >
                    {conv.title}
                  </Text>
                  <Text style={styles.historyItemSubtitle}>
                    {formattedDate} • {conv.messages.length} messages
                  </Text>
                </View>
              </View>

              <View style={styles.historyItemActions}>
                <TouchableOpacity
                  style={styles.historySmallBtn}
                  onPress={(e) => {
                    e.stopPropagation();
                    openRenameModal(conv.id, conv.title);
                  }}
                  hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
                >
                  <Feather name="edit-2" size={14} color="#94A3B8" />
                </TouchableOpacity>

                <TouchableOpacity
                  style={styles.historySmallBtn}
                  onPress={(e) => {
                    e.stopPropagation();
                    handleDeleteConversation(conv.id, conv.title);
                  }}
                  hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
                >
                  <Feather name="trash-2" size={14} color="#EF4444" />
                </TouchableOpacity>
              </View>
            </TouchableOpacity>
          );
        })}
      </View>
    );
  };

  const userInitial = profile?.name ? profile.name.charAt(0).toUpperCase() : 'R';

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
            onPress={handleBack}
            hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
          >
            <Feather name="arrow-left" size={22} color="#0F172A" />
          </TouchableOpacity>

          <View style={styles.headerTitleWrap}>
            <Text style={styles.headerTitle} numberOfLines={1}>
              {currentConversation?.title || 'Chat with LIVO'}
            </Text>
            <Text style={styles.headerSub}>Your personal life assistant</Text>
          </View>

          <View style={styles.headerRightActions}>
            {/* History Button */}
            <TouchableOpacity
              style={styles.headerIconBtn}
              onPress={() => setShowHistoryModal(true)}
              hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
            >
              <Feather name="clock" size={18} color="#64748B" />
            </TouchableOpacity>

            {/* Three-Dot Options Button */}
            <TouchableOpacity
              style={styles.headerIconBtn}
              onPress={() => setShowChatOptionsModal(true)}
              hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
            >
              <Feather name="more-vertical" size={18} color="#64748B" />
            </TouchableOpacity>

            {/* Profile Avatar */}
            <TouchableOpacity
              style={styles.avatarCircle}
              onPress={() => navigation.navigate('Profile')}
              hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
            >
              <Text style={styles.avatarText}>{userInitial}</Text>
            </TouchableOpacity>
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
                onPress={() => handleQuickChip('Suggest tasks')}
              >
                <Feather name="grid" size={13} color="#475569" style={{ marginRight: 4 }} />
                <Text style={styles.quickChipText}>More</Text>
              </TouchableOpacity>
            </ScrollView>
          </View>

          {/* Remaining messages */}
          {messages.slice(messages.length > 0 && messages[0].id === '1' ? 1 : 0).map(renderMessage)}

          <View style={{ height: 20 }} />
        </ScrollView>

        {/* Attachment Preview (if any selected) */}
        {pendingAttachment && (
          <View style={styles.composerAttachmentBar}>
            {pendingAttachment.type === 'image' ? (
              <Image source={{ uri: pendingAttachment.uri }} style={styles.composerThumbnail} />
            ) : (
              <View style={styles.composerFileIcon}>
                <Feather name="file" size={16} color="#2D6A00" />
              </View>
            )}
            <Text style={styles.composerAttachmentName} numberOfLines={1}>
              {pendingAttachment.name}
            </Text>
            <TouchableOpacity
              style={styles.removeAttachmentBtn}
              onPress={() => setPendingAttachment(null)}
            >
              <Feather name="x" size={16} color="#64748B" />
            </TouchableOpacity>
          </View>
        )}

        {/* Bottom Input Bar */}
        <View style={styles.inputBar}>
          <TouchableOpacity
            style={styles.attachBtn}
            onPress={() => setShowAttachmentModal(true)}
            hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
          >
            <Feather name="paperclip" size={18} color="#64748B" />
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
              inputText.trim() || pendingAttachment
                ? styles.sendBtnActive
                : styles.sendBtnInactive,
            ]}
            onPress={handleSendMessage}
            disabled={!inputText.trim() && !pendingAttachment}
          >
            <Feather name="arrow-up" size={18} color="#FFFFFF" />
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>

      {/* ================= MODAL: CHAT HISTORY ================= */}
      <Modal
        visible={showHistoryModal}
        animationType="slide"
        transparent={true}
        onRequestClose={() => setShowHistoryModal(false)}
      >
        <View style={styles.modalOverlay}>
          <TouchableOpacity
            style={styles.modalBackdrop}
            activeOpacity={1}
            onPress={() => setShowHistoryModal(false)}
          />
          <View style={styles.historyModalSheet}>
            {/* Handle */}
            <View style={styles.sheetHandle} />

            {/* Header */}
            <View style={styles.sheetHeaderRow}>
              <View>
                <Text style={styles.sheetTitle}>Chat History</Text>
                <Text style={styles.sheetSubtitle}>Access your past conversations</Text>
              </View>
              <TouchableOpacity
                style={styles.sheetCloseBtn}
                onPress={() => setShowHistoryModal(false)}
              >
                <Feather name="x" size={20} color="#64748B" />
              </TouchableOpacity>
            </View>

            {/* Search */}
            <View style={styles.historySearchWrap}>
              <Feather name="search" size={16} color="#94A3B8" style={{ marginRight: 8 }} />
              <TextInput
                style={styles.historySearchInput}
                placeholder="Search conversations..."
                placeholderTextColor="#94A3B8"
                value={historySearchQuery}
                onChangeText={setHistorySearchQuery}
              />
              {historySearchQuery ? (
                <TouchableOpacity onPress={() => setHistorySearchQuery('')}>
                  <Feather name="x-circle" size={16} color="#94A3B8" />
                </TouchableOpacity>
              ) : null}
            </View>

            {/* Conversations List */}
            <ScrollView style={styles.historyListScroll} showsVerticalScrollIndicator={false}>
              {renderHistoryGroup('Today', groupedConversations.today)}
              {renderHistoryGroup('Yesterday', groupedConversations.yesterday)}
              {renderHistoryGroup('Previous 7 Days', groupedConversations.past7Days)}
              {renderHistoryGroup('Older', groupedConversations.older)}

              {conversations.length === 0 && (
                <View style={styles.emptyHistoryWrap}>
                  <Feather name="message-square" size={32} color="#CBD5E1" />
                  <Text style={styles.emptyHistoryText}>No chat history found</Text>
                </View>
              )}
              <View style={{ height: 16 }} />
            </ScrollView>

            {/* New Chat Button */}
            <TouchableOpacity style={styles.newChatBottomBtn} onPress={handleCreateNewChat}>
              <Feather name="plus" size={18} color="#FFFFFF" style={{ marginRight: 8 }} />
              <Text style={styles.newChatBottomBtnText}>Start New Chat</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* ================= MODAL: CHAT OPTIONS (THREE-DOT) ================= */}
      <Modal
        visible={showChatOptionsModal}
        animationType="fade"
        transparent={true}
        onRequestClose={() => setShowChatOptionsModal(false)}
      >
        <TouchableOpacity
          style={styles.modalOverlay}
          activeOpacity={1}
          onPress={() => setShowChatOptionsModal(false)}
        >
          <View style={styles.optionsModalSheet}>
            <View style={styles.sheetHandle} />
            <Text style={styles.optionsSheetTitle}>Chat Options</Text>

            <TouchableOpacity
              style={styles.optionRow}
              onPress={handleCreateNewChat}
            >
              <View style={[styles.optionIconBox, { backgroundColor: '#E2F7C5' }]}>
                <Feather name="plus" size={18} color="#2D6A00" />
              </View>
              <View style={styles.optionTextWrap}>
                <Text style={styles.optionLabel}>New Chat</Text>
                <Text style={styles.optionSub}>Start a fresh conversation</Text>
              </View>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.optionRow}
              onPress={() => {
                setShowChatOptionsModal(false);
                if (currentConversation) {
                  openRenameModal(currentConversation.id, currentConversation.title);
                }
              }}
            >
              <View style={[styles.optionIconBox, { backgroundColor: '#F1F5F9' }]}>
                <Feather name="edit-3" size={18} color="#475569" />
              </View>
              <View style={styles.optionTextWrap}>
                <Text style={styles.optionLabel}>Rename Chat</Text>
                <Text style={styles.optionSub}>Give this chat a descriptive title</Text>
              </View>
            </TouchableOpacity>

            <TouchableOpacity style={styles.optionRow} onPress={handleCopyConversation}>
              <View style={[styles.optionIconBox, { backgroundColor: '#F1F5F9' }]}>
                <Feather name="copy" size={18} color="#475569" />
              </View>
              <View style={styles.optionTextWrap}>
                <Text style={styles.optionLabel}>Copy Conversation</Text>
                <Text style={styles.optionSub}>Copy full transcript to clipboard</Text>
              </View>
            </TouchableOpacity>

            <TouchableOpacity style={styles.optionRow} onPress={handleShareConversation}>
              <View style={[styles.optionIconBox, { backgroundColor: '#F1F5F9' }]}>
                <Feather name="share-2" size={18} color="#475569" />
              </View>
              <View style={styles.optionTextWrap}>
                <Text style={styles.optionLabel}>Share Conversation</Text>
                <Text style={styles.optionSub}>Share with apps or contacts</Text>
              </View>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.optionRow}
              onPress={() => {
                setShowChatOptionsModal(false);
                setShowSettingsModal(true);
              }}
            >
              <View style={[styles.optionIconBox, { backgroundColor: '#F1F5F9' }]}>
                <Feather name="settings" size={18} color="#475569" />
              </View>
              <View style={styles.optionTextWrap}>
                <Text style={styles.optionLabel}>Chat Settings</Text>
                <Text style={styles.optionSub}>Tone, suggestions & preferences</Text>
              </View>
            </TouchableOpacity>

            <View style={styles.divider} />

            <TouchableOpacity
              style={styles.optionRow}
              onPress={() => {
                if (currentConversation) {
                  handleDeleteConversation(currentConversation.id, currentConversation.title);
                }
              }}
            >
              <View style={[styles.optionIconBox, { backgroundColor: '#FEE2E2' }]}>
                <Feather name="trash-2" size={18} color="#EF4444" />
              </View>
              <View style={styles.optionTextWrap}>
                <Text style={[styles.optionLabel, { color: '#EF4444' }]}>Delete Conversation</Text>
                <Text style={styles.optionSub}>Permanently remove this chat</Text>
              </View>
            </TouchableOpacity>
          </View>
        </TouchableOpacity>
      </Modal>

      {/* ================= MODAL: ATTACHMENT MENU ================= */}
      <Modal
        visible={showAttachmentModal}
        animationType="fade"
        transparent={true}
        onRequestClose={() => setShowAttachmentModal(false)}
      >
        <TouchableOpacity
          style={styles.modalOverlay}
          activeOpacity={1}
          onPress={() => setShowAttachmentModal(false)}
        >
          <View style={styles.attachmentModalSheet}>
            <View style={styles.sheetHandle} />
            <Text style={styles.optionsSheetTitle}>Add Attachment</Text>

            <TouchableOpacity
              style={styles.optionRow}
              onPress={() => handlePickImage(true)}
            >
              <View style={[styles.optionIconBox, { backgroundColor: '#E0F2FE' }]}>
                <Feather name="camera" size={20} color="#0284C7" />
              </View>
              <View style={styles.optionTextWrap}>
                <Text style={styles.optionLabel}>Take Photo</Text>
                <Text style={styles.optionSub}>Capture an image with your camera</Text>
              </View>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.optionRow}
              onPress={() => handlePickImage(false)}
            >
              <View style={[styles.optionIconBox, { backgroundColor: '#FEF3C7' }]}>
                <Feather name="image" size={20} color="#D97706" />
              </View>
              <View style={styles.optionTextWrap}>
                <Text style={styles.optionLabel}>Choose from Gallery</Text>
                <Text style={styles.optionSub}>Pick an existing photo or screenshot</Text>
              </View>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.optionRow}
              onPress={handlePickDocument}
            >
              <View style={[styles.optionIconBox, { backgroundColor: '#DCFCE7' }]}>
                <Feather name="file-text" size={20} color="#16A34A" />
              </View>
              <View style={styles.optionTextWrap}>
                <Text style={styles.optionLabel}>Choose Document</Text>
                <Text style={styles.optionSub}>PDF, notes, or spreadsheets</Text>
              </View>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.optionRow}
              onPress={handlePickDocument}
            >
              <View style={[styles.optionIconBox, { backgroundColor: '#EDE9FE' }]}>
                <Feather name="paperclip" size={20} color="#7C3AED" />
              </View>
              <View style={styles.optionTextWrap}>
                <Text style={styles.optionLabel}>Attach File</Text>
                <Text style={styles.optionSub}>Any other file format from storage</Text>
              </View>
            </TouchableOpacity>
          </View>
        </TouchableOpacity>
      </Modal>

      {/* ================= MODAL: RENAME CONVERSATION ================= */}
      <Modal
        visible={showRenameModal}
        animationType="fade"
        transparent={true}
        onRequestClose={() => setShowRenameModal(false)}
      >
        <View style={styles.centerModalOverlay}>
          <View style={styles.renameCard}>
            <Text style={styles.renameCardTitle}>Rename Conversation</Text>
            <Text style={styles.renameCardSubtitle}>Enter a new title for this conversation</Text>

            <TextInput
              style={styles.renameInput}
              value={renameTitleInput}
              onChangeText={setRenameTitleInput}
              placeholder="e.g. Weekly Goal Planning"
              placeholderTextColor="#94A3B8"
              autoFocus
            />

            <View style={styles.renameCardActions}>
              <TouchableOpacity
                style={styles.renameCancelBtn}
                onPress={() => {
                  setShowRenameModal(false);
                  setRenameTargetId(null);
                }}
              >
                <Text style={styles.renameCancelBtnText}>Cancel</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[
                  styles.renameSaveBtn,
                  !renameTitleInput.trim() && { backgroundColor: '#CBD5E1' },
                ]}
                onPress={handleSaveRename}
                disabled={!renameTitleInput.trim()}
              >
                <Text style={styles.renameSaveBtnText}>Save</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>

      {/* ================= MODAL: CHAT SETTINGS ================= */}
      <Modal
        visible={showSettingsModal}
        animationType="slide"
        transparent={true}
        onRequestClose={() => setShowSettingsModal(false)}
      >
        <View style={styles.modalOverlay}>
          <TouchableOpacity
            style={styles.modalBackdrop}
            activeOpacity={1}
            onPress={() => setShowSettingsModal(false)}
          />
          <View style={styles.settingsModalSheet}>
            <View style={styles.sheetHandle} />

            <View style={styles.sheetHeaderRow}>
              <View>
                <Text style={styles.sheetTitle}>Chat Settings</Text>
                <Text style={styles.sheetSubtitle}>Customize your LIVO AI assistant</Text>
              </View>
              <TouchableOpacity
                style={styles.sheetCloseBtn}
                onPress={() => setShowSettingsModal(false)}
              >
                <Feather name="x" size={20} color="#64748B" />
              </TouchableOpacity>
            </View>

            <ScrollView style={{ paddingHorizontal: 16 }}>
              {/* AI Response Tone */}
              <Text style={styles.settingsSectionTitle}>AI RESPONSE STYLE</Text>
              <View style={styles.toneOptionsRow}>
                <TouchableOpacity
                  style={[
                    styles.toneOptionCard,
                    aiTone === 'concise' && styles.toneOptionCardActive,
                  ]}
                  onPress={() => setAiTone('concise')}
                >
                  <MaterialCommunityIcons
                    name="lightning-bolt"
                    size={22}
                    color={aiTone === 'concise' ? '#66C400' : '#64748B'}
                  />
                  <Text
                    style={[
                      styles.toneOptionTitle,
                      aiTone === 'concise' && styles.toneOptionTitleActive,
                    ]}
                  >
                    Concise
                  </Text>
                  <Text style={styles.toneOptionSub}>Direct, fast action items</Text>
                </TouchableOpacity>

                <TouchableOpacity
                  style={[
                    styles.toneOptionCard,
                    aiTone === 'detailed' && styles.toneOptionCardActive,
                  ]}
                  onPress={() => setAiTone('detailed')}
                >
                  <MaterialCommunityIcons
                    name="brain"
                    size={22}
                    color={aiTone === 'detailed' ? '#66C400' : '#64748B'}
                  />
                  <Text
                    style={[
                      styles.toneOptionTitle,
                      aiTone === 'detailed' && styles.toneOptionTitleActive,
                    ]}
                  >
                    Detailed
                  </Text>
                  <Text style={styles.toneOptionSub}>In-depth coaching & advice</Text>
                </TouchableOpacity>
              </View>

              {/* Smart Suggestions Toggle */}
              <Text style={[styles.settingsSectionTitle, { marginTop: 20 }]}>PROACTIVE ASSIST</Text>
              <TouchableOpacity
                style={styles.settingsToggleRow}
                onPress={() => setSmartSuggestions(!smartSuggestions)}
              >
                <View style={{ flex: 1, paddingRight: 10 }}>
                  <Text style={styles.settingsToggleLabel}>Smart Suggestion Chips</Text>
                  <Text style={styles.settingsToggleSub}>
                    Show contextual shortcuts for rapid task scheduling
                  </Text>
                </View>
                <View
                  style={[
                    styles.toggleTrack,
                    smartSuggestions ? styles.toggleTrackOn : styles.toggleTrackOff,
                  ]}
                >
                  <View
                    style={[
                      styles.toggleThumb,
                      smartSuggestions ? styles.toggleThumbOn : styles.toggleThumbOff,
                    ]}
                  />
                </View>
              </TouchableOpacity>

              <TouchableOpacity
                style={styles.settingsApplyBtn}
                onPress={() => setShowSettingsModal(false)}
              >
                <Text style={styles.settingsApplyBtnText}>Done</Text>
              </TouchableOpacity>
              <View style={{ height: 24 }} />
            </ScrollView>
          </View>
        </View>
      </Modal>
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
    maxWidth: '82%',
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
  userAttachmentBox: {
    marginBottom: 6,
  },
  bubbleAttachedImage: {
    width: 200,
    height: 140,
    borderRadius: 10,
    marginBottom: 4,
  },
  bubbleFileCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 8,
    padding: 8,
    marginBottom: 4,
  },
  bubbleFileName: {
    fontSize: 12,
    fontWeight: '600',
    color: '#0F172A',
    marginLeft: 6,
    maxWidth: 160,
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

  /* Composer Attachment Preview */
  composerAttachmentBar: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F1F5F9',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderTopWidth: 1,
    borderTopColor: '#E2E8F0',
  },
  composerThumbnail: {
    width: 32,
    height: 32,
    borderRadius: 6,
    marginRight: 8,
  },
  composerFileIcon: {
    width: 32,
    height: 32,
    borderRadius: 6,
    backgroundColor: '#E2F7C5',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  composerAttachmentName: {
    flex: 1,
    fontSize: 12,
    fontWeight: '600',
    color: '#0F172A',
  },
  removeAttachmentBtn: {
    padding: 4,
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

  /* Modal Generic */
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.45)',
    justifyContent: 'flex-end',
  },
  modalBackdrop: {
    flex: 1,
  },
  sheetHandle: {
    width: 36,
    height: 4,
    borderRadius: 2,
    backgroundColor: '#CBD5E1',
    alignSelf: 'center',
    marginBottom: 12,
  },
  sheetHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    marginBottom: 12,
  },
  sheetTitle: {
    fontSize: 17,
    fontWeight: '800',
    color: '#0F172A',
  },
  sheetSubtitle: {
    fontSize: 12,
    color: '#64748B',
    marginTop: 2,
  },
  sheetCloseBtn: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#F1F5F9',
  },

  /* History Modal */
  historyModalSheet: {
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    maxHeight: '85%',
    paddingTop: 10,
    paddingBottom: Platform.OS === 'ios' ? 24 : 12,
  },
  historySearchWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F8FAFC',
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 8,
    marginHorizontal: 16,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  historySearchInput: {
    flex: 1,
    fontSize: 13,
    color: '#0F172A',
  },
  historyListScroll: {
    paddingHorizontal: 16,
    maxHeight: 380,
  },
  historyGroupContainer: {
    marginBottom: 16,
  },
  historyGroupHeader: {
    fontSize: 11,
    fontWeight: '800',
    color: '#94A3B8',
    letterSpacing: 0.8,
    marginBottom: 8,
    textTransform: 'uppercase',
  },
  historyItemCard: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    padding: 12,
    marginBottom: 8,
    borderWidth: 1,
    borderColor: '#F1F5F9',
  },
  historyItemCardActive: {
    backgroundColor: '#F8FAF5',
    borderColor: '#66C400',
  },
  historyItemLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    marginRight: 8,
  },
  historyItemIconWrap: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#F1F5F9',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  historyItemIconWrapActive: {
    backgroundColor: '#E2F7C5',
  },
  historyItemTextWrap: {
    flex: 1,
  },
  historyItemTitle: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
  },
  historyItemTitleActive: {
    color: '#2D6A00',
  },
  historyItemSubtitle: {
    fontSize: 11,
    color: '#94A3B8',
    marginTop: 2,
  },
  historyItemActions: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  historySmallBtn: {
    padding: 6,
    marginLeft: 2,
  },
  emptyHistoryWrap: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 36,
  },
  emptyHistoryText: {
    fontSize: 13,
    color: '#94A3B8',
    marginTop: 8,
  },
  newChatBottomBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#66C400',
    borderRadius: 14,
    paddingVertical: 13,
    marginHorizontal: 16,
    marginTop: 8,
  },
  newChatBottomBtnText: {
    fontSize: 14,
    fontWeight: '800',
    color: '#FFFFFF',
  },

  /* Options & Attachment Modal Sheets */
  optionsModalSheet: {
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    paddingTop: 10,
    paddingBottom: Platform.OS === 'ios' ? 32 : 16,
    paddingHorizontal: 16,
  },
  attachmentModalSheet: {
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    paddingTop: 10,
    paddingBottom: Platform.OS === 'ios' ? 32 : 16,
    paddingHorizontal: 16,
  },
  optionsSheetTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 12,
    paddingHorizontal: 4,
  },
  optionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 11,
    paddingHorizontal: 6,
  },
  optionIconBox: {
    width: 40,
    height: 40,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 14,
  },
  optionTextWrap: {
    flex: 1,
  },
  optionLabel: {
    fontSize: 14,
    fontWeight: '700',
    color: '#0F172A',
  },
  optionSub: {
    fontSize: 11.5,
    color: '#64748B',
    marginTop: 1,
  },
  divider: {
    height: 1,
    backgroundColor: '#F1F5F9',
    marginVertical: 4,
  },

  /* Rename Dialog */
  centerModalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.5)',
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 24,
  },
  renameCard: {
    width: '100%',
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.1,
    shadowRadius: 16,
    elevation: 8,
  },
  renameCardTitle: {
    fontSize: 17,
    fontWeight: '800',
    color: '#0F172A',
  },
  renameCardSubtitle: {
    fontSize: 12,
    color: '#64748B',
    marginTop: 4,
    marginBottom: 14,
  },
  renameInput: {
    backgroundColor: '#F8FAFC',
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 10,
    fontSize: 14,
    color: '#0F172A',
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 16,
  },
  renameCardActions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
  },
  renameCancelBtn: {
    paddingHorizontal: 16,
    paddingVertical: 10,
    marginRight: 8,
  },
  renameCancelBtnText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#64748B',
  },
  renameSaveBtn: {
    backgroundColor: '#66C400',
    borderRadius: 10,
    paddingHorizontal: 18,
    paddingVertical: 10,
  },
  renameSaveBtnText: {
    fontSize: 14,
    fontWeight: '800',
    color: '#FFFFFF',
  },

  /* Settings Modal */
  settingsModalSheet: {
    backgroundColor: '#FFFFFF',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    maxHeight: '80%',
    paddingTop: 10,
    paddingBottom: Platform.OS === 'ios' ? 24 : 12,
  },
  settingsSectionTitle: {
    fontSize: 11,
    fontWeight: '800',
    color: '#94A3B8',
    letterSpacing: 0.8,
    marginBottom: 10,
  },
  toneOptionsRow: {
    flexDirection: 'row',
    gap: 12,
  },
  toneOptionCard: {
    flex: 1,
    backgroundColor: '#F8FAFC',
    borderRadius: 14,
    padding: 14,
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    alignItems: 'center',
  },
  toneOptionCardActive: {
    backgroundColor: '#F8FAF5',
    borderColor: '#66C400',
  },
  toneOptionTitle: {
    fontSize: 13.5,
    fontWeight: '800',
    color: '#0F172A',
    marginTop: 6,
  },
  toneOptionTitleActive: {
    color: '#2D6A00',
  },
  toneOptionSub: {
    fontSize: 11,
    color: '#64748B',
    marginTop: 2,
    textAlign: 'center',
  },
  settingsToggleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#F8FAFC',
    borderRadius: 14,
    padding: 14,
    borderWidth: 1,
    borderColor: '#E2E8F0',
    marginBottom: 20,
  },
  settingsToggleLabel: {
    fontSize: 13.5,
    fontWeight: '700',
    color: '#0F172A',
  },
  settingsToggleSub: {
    fontSize: 11.5,
    color: '#64748B',
    marginTop: 2,
  },
  toggleTrack: {
    width: 44,
    height: 24,
    borderRadius: 12,
    padding: 2,
    justifyContent: 'center',
  },
  toggleTrackOn: {
    backgroundColor: '#66C400',
  },
  toggleTrackOff: {
    backgroundColor: '#CBD5E1',
  },
  toggleThumb: {
    width: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: '#FFFFFF',
  },
  toggleThumbOn: {
    alignSelf: 'flex-end',
  },
  toggleThumbOff: {
    alignSelf: 'flex-start',
  },
  settingsApplyBtn: {
    backgroundColor: '#66C400',
    borderRadius: 14,
    paddingVertical: 13,
    alignItems: 'center',
    justifyContent: 'center',
  },
  settingsApplyBtnText: {
    fontSize: 14,
    fontWeight: '800',
    color: '#FFFFFF',
  },
});
