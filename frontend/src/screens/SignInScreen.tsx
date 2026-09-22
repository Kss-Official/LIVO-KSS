import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  SafeAreaView,
  StatusBar,
  ScrollView,
  Image,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { Feather, Ionicons, MaterialCommunityIcons } from '@expo/vector-icons';

export interface SignInScreenProps {
  onBack?: () => void;
  onSignInSubmit?: (data: { email: string; password: string }) => void;
  onCreateAccount?: (data: { name: string; email: string; password: string }) => void;
  onSignIn?: () => void;
  onSignUp?: () => void;
  onForgotPassword?: () => void;
  onGoogleSignUp?: () => void;
  onAppleSignUp?: () => void;
  onMicrosoftSignUp?: () => void;
}

export const SignInScreen: React.FC<SignInScreenProps> = ({
  onBack,
  onSignInSubmit,
  onCreateAccount,
  onSignIn,
  onSignUp,
  onForgotPassword,
  onGoogleSignUp,
  onAppleSignUp,
  onMicrosoftSignUp,
}) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [emailFocused, setEmailFocused] = useState(false);
  const [passwordFocused, setPasswordFocused] = useState(false);

  const handleSignIn = () => {
    if (onSignInSubmit) {
      onSignInSubmit({ email, password });
    } else if (onCreateAccount) {
      onCreateAccount({ name: '', email, password });
    }
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        {/* Background — heavily diffused soft glow, pure white base */}
        <View style={[styles.bgGlow, { top: -320, right: -320, width: 800, height: 800, borderRadius: 400, backgroundColor: '#C8F07A', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { top: -260, right: -260, width: 680, height: 680, borderRadius: 340, backgroundColor: '#B5E84E', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { top: -200, right: -200, width: 560, height: 560, borderRadius: 280, backgroundColor: '#AEED44', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { top: -150, right: -150, width: 440, height: 440, borderRadius: 220, backgroundColor: '#A4E83A', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { top: -110, right: -110, width: 340, height: 340, borderRadius: 170, backgroundColor: '#99E030', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { top: -80, right: -80, width: 250, height: 250, borderRadius: 125, backgroundColor: '#95E612', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { bottom: -280, left: -280, width: 720, height: 720, borderRadius: 360, backgroundColor: '#D7F59E', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { bottom: -220, left: -220, width: 580, height: 580, borderRadius: 290, backgroundColor: '#C8F07A', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { bottom: -170, left: -170, width: 460, height: 460, borderRadius: 230, backgroundColor: '#BCE84A', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { bottom: -130, left: -130, width: 360, height: 360, borderRadius: 180, backgroundColor: '#B0E040', opacity: 0.04 }]} />
        <View style={[styles.bgGlow, { bottom: -100, left: -100, width: 270, height: 270, borderRadius: 135, backgroundColor: '#A8DC38', opacity: 0.04 }]} />
        <View style={styles.bgArcCircle} />

        <ScrollView
          style={styles.flex}
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          {/* Top Bar: Back + Step Indicator */}
          <View style={styles.topBar}>
            <TouchableOpacity style={styles.backButton} onPress={onBack} activeOpacity={0.7}>
              <Feather name="chevron-left" size={24} color="#1A202C" />
            </TouchableOpacity>

            <View style={styles.stepIndicatorContainer}>
              <View style={styles.stepBarsRow}>
                <View style={styles.stepBarActive} />
                <View style={styles.stepBarInactive} />
                <View style={styles.stepBarInactive} />
              </View>
              <Text style={styles.stepText}>Step 1 of 3</Text>
            </View>
          </View>

          {/* LIVO Logo */}
          <Image
            source={require('../../assets/livo_logo.png')}
            style={styles.logoImage}
            resizeMode="contain"
          />

          {/* Title & Subtitle */}
          <Text style={styles.title}>Welcome back</Text>
          <Text style={styles.subtitle}>
            Sign in to continue building a more organized, focused and fulfilling life.
          </Text>

          {/* Form Fields */}
          <View style={styles.formContainer}>
            {/* Email */}
            <View style={[styles.inputWrapper, emailFocused && styles.inputWrapperFocused]}>
              <Feather name="mail" size={18} color={emailFocused ? '#95E612' : '#94A3B8'} style={styles.inputIcon} />
              <TextInput
                style={styles.input}
                placeholder="Email address"
                placeholderTextColor="#94A3B8"
                value={email}
                onChangeText={setEmail}
                onFocus={() => setEmailFocused(true)}
                onBlur={() => setEmailFocused(false)}
                keyboardType="email-address"
                autoCapitalize="none"
                returnKeyType="next"
              />
            </View>

            {/* Password */}
            <View style={[styles.inputWrapper, passwordFocused && styles.inputWrapperFocused]}>
              <MaterialCommunityIcons
                name="lock-outline"
                size={19}
                color={passwordFocused ? '#95E612' : '#94A3B8'}
                style={styles.inputIcon}
              />
              <TextInput
                style={styles.input}
                placeholder="Password"
                placeholderTextColor="#94A3B8"
                value={password}
                onChangeText={setPassword}
                onFocus={() => setPasswordFocused(true)}
                onBlur={() => setPasswordFocused(false)}
                secureTextEntry={!showPassword}
                autoCapitalize="none"
                returnKeyType="done"
              />
              <TouchableOpacity
                onPress={() => setShowPassword(!showPassword)}
                style={styles.eyeButton}
                activeOpacity={0.7}
              >
                <Ionicons
                  name={showPassword ? 'eye-outline' : 'eye-off-outline'}
                  size={20}
                  color="#94A3B8"
                />
              </TouchableOpacity>
            </View>

            {/* Forgot Password Link (Aligned Right) */}
            <TouchableOpacity
              style={styles.forgotPasswordContainer}
              onPress={onForgotPassword}
              activeOpacity={0.7}
            >
              <Text style={styles.forgotPasswordText}>Forgot Password?</Text>
            </TouchableOpacity>
          </View>

          {/* Sign In Button */}
          <TouchableOpacity
            style={styles.createButton}
            onPress={handleSignIn}
            activeOpacity={0.88}
          >
            <Text style={styles.createButtonText}>Sign In</Text>
            <Feather name="arrow-right" size={20} color="#0F172A" style={styles.arrowIcon} />
          </TouchableOpacity>

          {/* Divider */}
          <View style={styles.dividerRow}>
            <View style={styles.dividerLine} />
            <Text style={styles.dividerText}>Or continue with</Text>
            <View style={styles.dividerLine} />
          </View>

          {/* Social Login Buttons */}
          <View style={styles.socialRow}>
            {/* Google */}
            <TouchableOpacity style={styles.socialButton} onPress={onGoogleSignUp} activeOpacity={0.8}>
              <Image
                source={require('../../assets/google_logo.png')}
                style={styles.socialIconImage}
                resizeMode="contain"
              />
              <Text style={styles.socialLabel}>Google</Text>
            </TouchableOpacity>

            {/* Apple */}
            <TouchableOpacity style={styles.socialButton} onPress={onAppleSignUp} activeOpacity={0.8}>
              <Ionicons name="logo-apple" size={26} color="#1A202C" />
              <Text style={styles.socialLabel}>Apple</Text>
            </TouchableOpacity>

            {/* Microsoft */}
            <TouchableOpacity style={styles.socialButton} onPress={onMicrosoftSignUp} activeOpacity={0.8}>
              <View style={styles.msGrid}>
                <View style={[styles.msSquare, { backgroundColor: '#F25022' }]} />
                <View style={[styles.msSquare, { backgroundColor: '#7FBA00' }]} />
                <View style={[styles.msSquare, { backgroundColor: '#00A4EF' }]} />
                <View style={[styles.msSquare, { backgroundColor: '#FFB900' }]} />
              </View>
              <Text style={styles.socialLabel}>Microsoft</Text>
            </TouchableOpacity>
          </View>

          {/* Footer: Sign Up Link */}
          <View style={styles.footerRow}>
            <Text style={styles.footerText}>Don't have an account?  </Text>
            <TouchableOpacity onPress={onSignUp || onSignIn} activeOpacity={0.7}>
              <Text style={styles.signInLink}>Sign up</Text>
            </TouchableOpacity>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

// Backwards compatibility export
export const SignUpScreen = SignInScreen;

const styles = StyleSheet.create({
  flex: { flex: 1 },
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  scrollContent: {
    paddingHorizontal: 24,
    paddingTop: 16,
    paddingBottom: 40,
  },

  /* Background */
  bgGlow: {
    position: 'absolute',
  },
  bgArcCircle: {
    position: 'absolute',
    top: 30,
    right: -110,
    width: 400,
    height: 400,
    borderRadius: 200,
    borderWidth: 1,
    borderColor: 'rgba(149, 230, 18, 0.18)',
  },

  /* Top Bar */
  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: 45,
    marginBottom: 10,
  },
  backButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: 'rgba(255,255,255,0.8)',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 6,
    elevation: 3,
  },
  stepIndicatorContainer: {
    alignItems: 'flex-end',
  },
  stepBarsRow: {
    flexDirection: 'row',
    gap: 5,
    marginBottom: 4,
  },
  stepBarActive: {
    width: 36,
    height: 5,
    borderRadius: 3,
    backgroundColor: '#95E612',
  },
  stepBarInactive: {
    width: 22,
    height: 5,
    borderRadius: 3,
    backgroundColor: '#CBD5E1',
  },
  stepText: {
    fontSize: 12,
    color: '#64748B',
    fontWeight: '500',
  },

  /* Logo */
  logoImage: {
    width: 130,
    height: 48,
    marginBottom: 20,
  },

  /* Title */
  title: {
    fontSize: 28,
    fontWeight: '800',
    color: '#0F172A',
    letterSpacing: -0.5,
    marginBottom: 8,
    lineHeight: 34,
  },
  subtitle: {
    fontSize: 15,
    color: '#64748B',
    lineHeight: 22,
    marginBottom: 28,
    fontWeight: '400',
  },

  /* Form */
  formContainer: {
    marginBottom: 10,
  },
  inputWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    paddingHorizontal: 16,
    height: 56,
    marginBottom: 14,
    shadowColor: '#94A3B8',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.07,
    shadowRadius: 6,
    elevation: 2,
  },
  inputWrapperFocused: {
    borderColor: '#95E612',
    shadowColor: '#95E612',
    shadowOpacity: 0.2,
    shadowRadius: 8,
    elevation: 4,
  },
  inputIcon: {
    marginRight: 12,
  },
  input: {
    flex: 1,
    fontSize: 15,
    color: '#1A202C',
    fontWeight: '500',
  },
  eyeButton: {
    padding: 4,
  },
  forgotPasswordContainer: {
    alignSelf: 'flex-end',
    marginTop: 2,
    marginBottom: 20,
    paddingVertical: 4,
  },
  forgotPasswordText: {
    fontSize: 13.5,
    fontWeight: '600',
    color: '#64748B',
    textDecorationLine: 'underline',
  },

  /* Sign In Button */
  createButton: {
    height: 56,
    backgroundColor: '#95E612',
    borderRadius: 28,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    marginBottom: 28,
    shadowColor: '#84CC16',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.4,
    shadowRadius: 14,
    elevation: 8,
  },
  createButtonText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#0F172A',
  },
  arrowIcon: {
    marginTop: 1,
  },

  /* Divider */
  dividerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    marginBottom: 22,
  },
  dividerLine: {
    flex: 1,
    height: 1,
    backgroundColor: '#E2E8F0',
  },
  dividerText: {
    fontSize: 13,
    color: '#94A3B8',
    fontWeight: '500',
  },

  /* Social Buttons */
  socialRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 12,
    marginBottom: 24,
  },
  socialButton: {
    flex: 1,
    height: 72,
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
    borderWidth: 1,
    borderColor: '#F1F5F9',
    shadowColor: '#94A3B8',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 8,
    elevation: 3,
  },
  socialIconImage: {
    width: 26,
    height: 26,
  },
  socialLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: '#475569',
  },

  /* Microsoft grid icon */
  msGrid: {
    width: 24,
    height: 24,
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 2,
  },
  msSquare: {
    width: 10,
    height: 10,
    borderRadius: 1,
  },

  /* Footer */
  footerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  footerText: {
    fontSize: 14,
    color: '#64748B',
  },
  signInLink: {
    fontSize: 14,
    fontWeight: '700',
    color: '#78D600',
  },
});
