import React, { useState, useEffect, useRef } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  SafeAreaView,
  StatusBar,
  Animated,
  Easing,
} from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { Colors } from '../theme/colors';

interface VoiceRecordingScreenProps {
  onBack?: () => void;
  onFinishRecording?: (text: string) => void;
}

export const VoiceRecordingScreen: React.FC<VoiceRecordingScreenProps> = ({
  onBack,
  onFinishRecording,
}) => {
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
  const [isRecording, setIsRecording] = useState(true);
  const [seconds, setSeconds] = useState(0);
  const [transcribedText, setTranscribedText] = useState(
    '"Add a task to finish my UI design tomorrow at 5 PM"'
  );

  // Pulsing animation for mic outer rings
  const pulseAnim1 = useRef(new Animated.Value(1)).current;
  const pulseAnim2 = useRef(new Animated.Value(1)).current;
  // Sound wave bar heights
  const waveAnim1 = useRef(new Animated.Value(10)).current;
  const waveAnim2 = useRef(new Animated.Value(20)).current;
  const waveAnim3 = useRef(new Animated.Value(30)).current;
  const waveAnim4 = useRef(new Animated.Value(15)).current;

  // Timer logic
  useEffect(() => {
    let interval: NodeJS.Timeout | null = null;
    if (isRecording) {
      interval = setInterval(() => {
        setSeconds((prev) => prev + 1);
      }, 1000);
    } else if (!isRecording && seconds !== 0) {
      if (interval) clearInterval(interval);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [isRecording, seconds]);

  // Pulse & Sound Wave Animations while recording
  useEffect(() => {
    if (isRecording) {
      // Ring pulse
      const createPulse = (anim: Animated.Value, delay: number) => {
        return Animated.loop(
          Animated.sequence([
            Animated.delay(delay),
            Animated.timing(anim, {
              toValue: 1.35,
              duration: 1200,
              easing: Easing.out(Easing.ease),
              useNativeDriver: true,
            }),
            Animated.timing(anim, {
              toValue: 1,
              duration: 1200,
              easing: Easing.in(Easing.ease),
              useNativeDriver: true,
            }),
          ])
        );
      };

      // Waves animation
      const animateWave = (anim: Animated.Value, min: number, max: number, speed: number) => {
        return Animated.loop(
          Animated.sequence([
            Animated.timing(anim, {
              toValue: max,
              duration: speed,
              useNativeDriver: false,
            }),
            Animated.timing(anim, {
              toValue: min,
              duration: speed,
              useNativeDriver: false,
            }),
          ])
        );
      };

      const pulseLoop1 = createPulse(pulseAnim1, 0);
      const pulseLoop2 = createPulse(pulseAnim2, 600);
      const waveLoop1 = animateWave(waveAnim1, 8, 32, 400);
      const waveLoop2 = animateWave(waveAnim2, 12, 44, 300);
      const waveLoop3 = animateWave(waveAnim3, 10, 38, 500);
      const waveLoop4 = animateWave(waveAnim4, 6, 28, 350);

      pulseLoop1.start();
      pulseLoop2.start();
      waveLoop1.start();
      waveLoop2.start();
      waveLoop3.start();
      waveLoop4.start();

      return () => {
        pulseLoop1.stop();
        pulseLoop2.stop();
        waveLoop1.stop();
        waveLoop2.stop();
        waveLoop3.stop();
        waveLoop4.stop();
      };
    } else {
      pulseAnim1.setValue(1);
      pulseAnim2.setValue(1);
      waveAnim1.setValue(10);
      waveAnim2.setValue(16);
      waveAnim3.setValue(22);
      waveAnim4.setValue(12);
    }
  }, [isRecording]);

  const toggleRecording = () => {
    if (isRecording) {
      setIsRecording(false);
    } else {
      setSeconds(0);
      setIsRecording(true);
    }
  };

  const handleFinish = () => {
    setIsRecording(false);
    if (onFinishRecording) {
      onFinishRecording(transcribedText);
    } else {
      handleBack();
    }
  };

  const formatTimer = (totalSeconds: number) => {
    const mins = Math.floor(totalSeconds / 60);
    const secs = totalSeconds % 60;
    const formattedMins = mins < 10 ? `0${mins}` : `${mins}`;
    const formattedSecs = secs < 10 ? `0${secs}` : `${secs}`;
    return `${formattedMins}:${formattedSecs}`;
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />

      {/* 1. Header */}
      <View style={styles.header}>
        <TouchableOpacity style={styles.backBtn} onPress={handleBack} activeOpacity={0.7}>
          <Feather name="arrow-left" size={24} color="#0F172A" />
        </TouchableOpacity>

        <View style={styles.aiPill}>
          <Ionicons name="sparkles" size={14} color="#66C400" style={{ marginRight: 5 }} />
          <Text style={styles.aiPillText}>Powered by LIVO AI</Text>
        </View>
      </View>

      {/* 2. Main Title */}
      <View style={styles.titleSection}>
        <Text style={styles.mainTitle}>Speak it.</Text>
        <Text style={styles.highlightTitle}>LIVO will handle the rest.</Text>
        <Text style={styles.subTitle}>
          Tell me what you want to do, and I'll create it for you.
        </Text>
      </View>

      {/* 3. Center Microphone & Visualizer */}
      <View style={styles.visualizerContainer}>
        {/* Left Waves */}
        <View style={styles.waveGroup}>
          <Animated.View style={[styles.waveBar, { height: waveAnim1 }]} />
          <Animated.View style={[styles.waveBar, { height: waveAnim2 }]} />
          <Animated.View style={[styles.waveBar, { height: waveAnim3 }]} />
          <Animated.View style={[styles.waveBar, { height: waveAnim4 }]} />
        </View>

        {/* Center Mic Circle with Concentric Animated Pulse Rings */}
        <View style={styles.micWrapper}>
          {isRecording && (
            <>
              <Animated.View
                style={[
                  styles.pulseRingOuter,
                  { transform: [{ scale: pulseAnim1 }] },
                ]}
              />
              <Animated.View
                style={[
                  styles.pulseRingInner,
                  { transform: [{ scale: pulseAnim2 }] },
                ]}
              />
            </>
          )}

          <TouchableOpacity
            style={styles.micCircle}
            onPress={toggleRecording}
            activeOpacity={0.85}
          >
            <Ionicons name="mic" size={44} color="#FFFFFF" />
          </TouchableOpacity>
        </View>

        {/* Right Waves */}
        <View style={styles.waveGroup}>
          <Animated.View style={[styles.waveBar, { height: waveAnim4 }]} />
          <Animated.View style={[styles.waveBar, { height: waveAnim3 }]} />
          <Animated.View style={[styles.waveBar, { height: waveAnim2 }]} />
          <Animated.View style={[styles.waveBar, { height: waveAnim1 }]} />
        </View>
      </View>

      {/* Status Label */}
      <Text style={styles.statusText}>
        {isRecording ? 'Listening...' : 'Tap to start recording'}
      </Text>

      {/* 4. Dynamic Transcription Quote Pill */}
      <View style={styles.quotePill}>
        <View style={styles.quoteIconBox}>
          <Ionicons name="bar-chart-outline" size={16} color="#66C400" />
        </View>
        <Text style={styles.quoteText}>{transcribedText}</Text>
      </View>

      {/* 5. Bottom Action Controls */}
      <View style={styles.bottomControlsRow}>
        {/* Left: Cancel */}
        <View style={styles.controlCol}>
          <TouchableOpacity style={styles.cancelCircle} onPress={handleBack} activeOpacity={0.7}>
            <Feather name="x" size={22} color="#0F172A" />
          </TouchableOpacity>
          <Text style={styles.controlLabel}>Cancel</Text>
        </View>

        {/* Center: Record/Stop Button */}
        <View style={styles.controlCol}>
          <TouchableOpacity
            style={[styles.recordStopOuter, isRecording ? styles.stopOuter : styles.recordOuter]}
            onPress={toggleRecording}
            activeOpacity={0.8}
          >
            <View style={[styles.recordStopInner, isRecording ? styles.stopInner : styles.recordInner]}>
              {isRecording ? (
                <View style={styles.stopSquare} />
              ) : (
                <Ionicons name="mic" size={24} color="#FFFFFF" />
              )}
            </View>
          </TouchableOpacity>
          <Text style={styles.controlLabel}>
            {isRecording ? 'Tap to stop' : 'Tap to record'}
          </Text>
        </View>

        {/* Right: Timer */}
        <View style={styles.controlCol}>
          <Text style={styles.timerDigits}>{formatTimer(seconds)}</Text>
          <Text style={styles.timerSublabel}>
            {isRecording ? 'Recording...' : 'Stopped'}
          </Text>
        </View>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    justifyContent: 'space-between',
    paddingBottom: 34,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingTop: 12,
  },
  backBtn: {
    padding: 6,
  },
  aiPill: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F1F9E8',
    paddingHorizontal: 14,
    paddingVertical: 7,
    borderRadius: 20,
  },
  aiPillText: {
    fontSize: 13,
    fontWeight: '700',
    color: '#66C400',
  },
  titleSection: {
    alignItems: 'center',
    paddingHorizontal: 28,
    marginTop: 10,
  },
  mainTitle: {
    fontSize: 28,
    fontWeight: '900',
    color: '#0F172A',
    marginBottom: 4,
  },
  highlightTitle: {
    fontSize: 24,
    fontWeight: '900',
    color: '#66C400',
    marginBottom: 10,
    textAlign: 'center',
  },
  subTitle: {
    fontSize: 14,
    color: '#64748B',
    textAlign: 'center',
    lineHeight: 20,
  },
  visualizerContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: 20,
  },
  waveGroup: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    marginHorizontal: 16,
  },
  waveBar: {
    width: 4,
    backgroundColor: '#86EFAC',
    borderRadius: 2,
  },
  micWrapper: {
    width: 170,
    height: 170,
    alignItems: 'center',
    justifyContent: 'center',
  },
  pulseRingOuter: {
    position: 'absolute',
    width: 160,
    height: 160,
    borderRadius: 80,
    backgroundColor: 'rgba(102, 196, 0, 0.12)',
  },
  pulseRingInner: {
    position: 'absolute',
    width: 135,
    height: 135,
    borderRadius: 67.5,
    backgroundColor: 'rgba(102, 196, 0, 0.22)',
  },
  micCircle: {
    width: 110,
    height: 110,
    borderRadius: 55,
    backgroundColor: '#66C400',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#66C400',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.4,
    shadowRadius: 12,
    elevation: 8,
  },
  statusText: {
    textAlign: 'center',
    fontSize: 18,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 16,
  },
  quotePill: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#F7FEE7',
    marginHorizontal: 24,
    paddingHorizontal: 16,
    paddingVertical: 14,
    borderRadius: 18,
    borderWidth: 1,
    borderColor: '#ECFDF5',
  },
  quoteIconBox: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#DCFCE7',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  quoteText: {
    flex: 1,
    fontSize: 14,
    color: '#1E293B',
    fontWeight: '600',
    lineHeight: 20,
  },
  bottomControlsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    paddingHorizontal: 20,
    marginTop: 20,
  },
  controlCol: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 90,
  },
  cancelCircle: {
    width: 54,
    height: 54,
    borderRadius: 27,
    backgroundColor: '#F1F5F9',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 6,
  },
  controlLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: '#64748B',
  },
  recordStopOuter: {
    width: 72,
    height: 72,
    borderRadius: 36,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 6,
  },
  stopOuter: {
    backgroundColor: '#FEE2E2',
  },
  recordOuter: {
    backgroundColor: '#DCFCE7',
  },
  recordStopInner: {
    width: 56,
    height: 56,
    borderRadius: 28,
    alignItems: 'center',
    justifyContent: 'center',
  },
  stopInner: {
    backgroundColor: '#EF4444',
  },
  recordInner: {
    backgroundColor: '#66C400',
  },
  stopSquare: {
    width: 20,
    height: 20,
    borderRadius: 4,
    backgroundColor: '#FFFFFF',
  },
  timerDigits: {
    fontSize: 18,
    fontWeight: '800',
    color: '#0F172A',
    marginBottom: 4,
  },
  timerSublabel: {
    fontSize: 11,
    color: '#64748B',
    fontWeight: '600',
  },
});
