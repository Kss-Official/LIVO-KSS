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

export interface SignUpScreenProps {
  onBack?: () => void;
  onSignInSubmit?: (data: { email: string; password: string }) => void;
  onCreateAccount?: (data: { name: string; email: string; phone?: string; password: string }) => void;
  onSignIn?: () => void;
  onSignUp?: () => void;
  onGoogleSignUp?: () => void;
  onAppleSignUp?: () => void;
  onMicrosoftSignUp?: () => void;
}

export const SignUpScreen: React.FC<SignUpScreenProps> = ({
  onBack,
  onSignInSubmit,
  onCreateAccount,
  onSignIn,
  onSignUp,
  onGoogleSignUp,
  onAppleSignUp,
  onMicrosoftSignUp,
}) => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [countryCode, setCountryCode] = useState('+91');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [nameFocused, setNameFocused] = useState(false);
  const [emailFocused, setEmailFocused] = useState(false);
  const [phoneFocused, setPhoneFocused] = useState(false);
  const [passwordFocused, setPasswordFocused] = useState(false);
  const [confirmPasswordFocused, setConfirmPasswordFocused] = useState(false);

  const [nameError, setNameError] = useState('');
  const [emailError, setEmailError] = useState('');
  const [phoneError, setPhoneError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [confirmPasswordError, setConfirmPasswordError] = useState('');
  const [termsError, setTermsError] = useState('');

  const [agreeTerms, setAgreeTerms] = useState(true);
  const [agreeUpdates, setAgreeUpdates] = useState(true);

  const handleSignUp = () => {
    let hasError = false;

    // Name validation
    const trimmedName = name.trim();
    const nameRegex = /^[A-Za-z\s]+$/;
    if (!trimmedName || !nameRegex.test(trimmedName)) {
      setNameError('Please enter a valid name.');
      hasError = true;
    } else {
      setNameError('');
    }

    // Email validation
    const trimmedEmail = email.trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!trimmedEmail || !emailRegex.test(trimmedEmail)) {
      setEmailError('Please enter a valid email address.');
      hasError = true;
    } else {
      setEmailError('');
    }

    // Phone validation
    const trimmedPhone = phone.trim();
    const cleanPhone = trimmedPhone.replace(/[\s-]/g, '');
    const phoneRegex = /^[0-9]{7,15}$/;
    if (!trimmedPhone || !phoneRegex.test(cleanPhone)) {
      setPhoneError('Please enter a valid phone number.');
      hasError = true;
    } else {
      setPhoneError('');
    }

    // Password validation
    if (!password || password.length < 8) {
      setPasswordError('Password must be at least 8 characters.');
      hasError = true;
    } else {
      setPasswordError('');
    }

    // Confirm Password validation
    if (!confirmPassword) {
      setConfirmPasswordError('Please confirm your password.');
      hasError = true;
    } else if (confirmPassword !== password) {
      setConfirmPasswordError('Passwords do not match.');
      hasError = true;
    } else {
      setConfirmPasswordError('');
    }

    // Terms validation
    if (!agreeTerms) {
      setTermsError('Please agree to the Terms of Service.');
      hasError = true;
    } else {
      setTermsError('');
    }

    if (hasError) return;

    if (onCreateAccount) {
      onCreateAccount({ name: trimmedName, email: trimmedEmail, phone: `${countryCode} ${trimmedPhone}`, password });
    } else if (onSignInSubmit) {
      onSignInSubmit({ email: trimmedEmail, password });
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

          {/* LIVO Logo & Slogan */}
          <View style={styles.logoContainer}>
            <Image
              source={require('../../assets/livo_logo.png')}
              style={styles.logoImage}
              resizeMode="contain"
            />
            <Text style={styles.logoSlogan}>A BETTER YOU</Text>
          </View>

          {/* Title & Subtitle */}
          <Text style={styles.title}>Create your account</Text>
          <Text style={styles.subtitle}>
            Start your journey towards a more organized, focused and fulfilling life.
          </Text>

          {/* Form Fields */}
          <View style={styles.formContainer}>
            {/* Full Name */}
            <View style={[
              styles.inputWrapper,
              nameFocused && styles.inputWrapperFocused,
              !!nameError && styles.inputWrapperError,
            ]}>
              <Feather name="user" size={18} color={nameError ? '#EF4444' : nameFocused ? '#95E612' : '#94A3B8'} style={styles.inputIcon} />
              <TextInput
                style={styles.input}
                placeholder="Full name"
                placeholderTextColor="#94A3B8"
                value={name}
                onChangeText={(text) => {
                  setName(text);
                  if (nameError) setNameError('');
                }}
                onFocus={() => setNameFocused(true)}
                onBlur={() => setNameFocused(false)}
                autoCapitalize="words"
                returnKeyType="next"
              />
            </View>
            {!!nameError && <Text style={styles.errorText}>{nameError}</Text>}

            {/* Email Address */}
            <View style={[
              styles.inputWrapper,
              emailFocused && styles.inputWrapperFocused,
              !!emailError && styles.inputWrapperError,
            ]}>
              <Feather name="mail" size={18} color={emailError ? '#EF4444' : emailFocused ? '#95E612' : '#94A3B8'} style={styles.inputIcon} />
              <TextInput
                style={styles.input}
                placeholder="Email address"
                placeholderTextColor="#94A3B8"
                value={email}
                onChangeText={(text) => {
                  setEmail(text);
                  if (emailError) setEmailError('');
                }}
                onFocus={() => setEmailFocused(true)}
                onBlur={() => setEmailFocused(false)}
                keyboardType="email-address"
                autoCapitalize="none"
                returnKeyType="next"
              />
            </View>
            {!!emailError && <Text style={styles.errorText}>{emailError}</Text>}

            {/* Phone Number */}
            <View style={[
              styles.inputWrapper,
              phoneFocused && styles.inputWrapperFocused,
              !!phoneError && styles.inputWrapperError,
            ]}>
              <Feather name="phone" size={18} color={phoneError ? '#EF4444' : phoneFocused ? '#95E612' : '#94A3B8'} style={styles.inputIcon} />
              <TouchableOpacity style={styles.countrySelector} activeOpacity={0.7}>
                <Text style={styles.countryCodeText}>{countryCode}</Text>
                <Feather name="chevron-down" size={14} color="#64748B" style={styles.chevronIcon} />
              </TouchableOpacity>
              <View style={styles.phoneDivider} />
              <TextInput
                style={styles.input}
                placeholder="Phone number"
                placeholderTextColor="#94A3B8"
                value={phone}
                onChangeText={(text) => {
                  setPhone(text);
                  if (phoneError) setPhoneError('');
                }}
                onFocus={() => setPhoneFocused(true)}
                onBlur={() => setPhoneFocused(false)}
                keyboardType="phone-pad"
                returnKeyType="next"
              />
            </View>
            {!!phoneError && <Text style={styles.errorText}>{phoneError}</Text>}

            {/* Create Password */}
            <View style={[
              styles.inputWrapper,
              passwordFocused && styles.inputWrapperFocused,
              !!passwordError && styles.inputWrapperError,
              { marginBottom: 4 }
            ]}>
              <MaterialCommunityIcons
                name="lock-outline"
                size={19}
                color={passwordError ? '#EF4444' : passwordFocused ? '#95E612' : '#94A3B8'}
                style={styles.inputIcon}
              />
              <TextInput
                style={styles.input}
                placeholder="Create a password"
                placeholderTextColor="#94A3B8"
                value={password}
                onChangeText={(text) => {
                  setPassword(text);
                  if (passwordError) setPasswordError('');
                }}
                onFocus={() => setPasswordFocused(true)}
                onBlur={() => setPasswordFocused(false)}
                secureTextEntry={!showPassword}
                autoCapitalize="none"
                returnKeyType="next"
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
            {!!passwordError && <Text style={styles.errorText}>{passwordError}</Text>}
            <Text style={styles.passwordHint}>At least 8 characters with a number and a symbol</Text>

            {/* Confirm Password */}
            <View style={[
              styles.inputWrapper,
              confirmPasswordFocused && styles.inputWrapperFocused,
              !!confirmPasswordError && styles.inputWrapperError,
              { marginTop: 10 }
            ]}>
              <MaterialCommunityIcons
                name="lock-outline"
                size={19}
                color={confirmPasswordError ? '#EF4444' : confirmPasswordFocused ? '#95E612' : '#94A3B8'}
                style={styles.inputIcon}
              />
              <TextInput
                style={styles.input}
                placeholder="Confirm password"
                placeholderTextColor="#94A3B8"
                value={confirmPassword}
                onChangeText={(text) => {
                  setConfirmPassword(text);
                  if (confirmPasswordError) setConfirmPasswordError('');
                }}
                onFocus={() => setConfirmPasswordFocused(true)}
                onBlur={() => setConfirmPasswordFocused(false)}
                secureTextEntry={!showConfirmPassword}
                autoCapitalize="none"
                returnKeyType="done"
              />
              <TouchableOpacity
                onPress={() => setShowConfirmPassword(!showConfirmPassword)}
                style={styles.eyeButton}
                activeOpacity={0.7}
              >
                <Ionicons
                  name={showConfirmPassword ? 'eye-outline' : 'eye-off-outline'}
                  size={20}
                  color="#94A3B8"
                />
              </TouchableOpacity>
            </View>
            {!!confirmPasswordError && <Text style={styles.errorText}>{confirmPasswordError}</Text>}
          </View>

          {/* Checkboxes */}
          <View style={styles.checkboxesContainer}>
            <TouchableOpacity
              style={styles.checkboxRow}
              onPress={() => {
                setAgreeTerms(!agreeTerms);
                if (termsError) setTermsError('');
              }}
              activeOpacity={0.8}
            >
              <View style={[
                styles.checkbox,
                agreeTerms && styles.checkboxChecked,
                !!termsError && styles.checkboxError,
              ]}>
                {agreeTerms && <Ionicons name="checkmark" size={14} color="#FFFFFF" />}
              </View>
              <Text style={styles.checkboxText}>
                I agree to the <Text style={styles.greenLinkText}>Terms of Service</Text> and{' '}
                <Text style={styles.greenLinkText}>Privacy Policy</Text>
              </Text>
            </TouchableOpacity>
            {!!termsError && <Text style={[styles.errorText, { marginLeft: 28, marginTop: -6, marginBottom: 8 }]}>{termsError}</Text>}

            <TouchableOpacity
              style={styles.checkboxRow}
              onPress={() => setAgreeUpdates(!agreeUpdates)}
              activeOpacity={0.8}
            >
              <View style={[styles.checkbox, agreeUpdates && styles.checkboxChecked]}>
                {agreeUpdates && <Ionicons name="checkmark" size={14} color="#FFFFFF" />}
              </View>
              <Text style={styles.checkboxText}>
                I would like to receive product updates and helpful tips from LIVO
              </Text>
            </TouchableOpacity>
          </View>

          {/* Create Account Button */}
          <TouchableOpacity
            style={styles.createButton}
            onPress={handleSignUp}
            activeOpacity={0.88}
          >
            <Text style={styles.createButtonText}>Create Account</Text>
            <Feather name="arrow-right" size={20} color="#0F172A" style={styles.arrowIcon} />
          </TouchableOpacity>

          {/* Divider */}
          <View style={styles.dividerRow}>
            <View style={styles.dividerLine} />
            <Text style={styles.dividerText}>Or sign up with</Text>
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

          {/* Footer: Sign In / Log In Link */}
          <View style={styles.footerRow}>
            <Text style={styles.footerText}>Already have an account? </Text>
            <TouchableOpacity onPress={onSignIn} activeOpacity={0.7}>
              <Text style={styles.signInLink}>Sign in</Text>
            </TouchableOpacity>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  flex: { flex: 1 },
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  scrollContent: {
    paddingHorizontal: 24,
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
    marginTop: 35,
    marginBottom: 5,
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
  logoContainer: {
    marginBottom: 20,
  },
  logoImage: {
    width: 130,
    height: 44,
  },
  logoSlogan: {
    fontSize: 10,
    fontWeight: '700',
    color: '#64748B',
    letterSpacing: 1.5,
    marginTop: 2,
    marginLeft: 4,
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
    marginBottom: 24,
    fontWeight: '400',
  },

  /* Form */
  formContainer: {
    marginBottom: 8,
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
  inputWrapperError: {
    borderColor: '#EF4444',
    backgroundColor: '#FEF2F2',
  },
  errorText: {
    fontSize: 12,
    color: '#EF4444',
    fontWeight: '600',
    marginTop: -8,
    marginBottom: 12,
    marginLeft: 4,
  },
  checkboxError: {
    borderColor: '#EF4444',
    backgroundColor: '#FEF2F2',
  },
  inputIcon: {
    marginRight: 12,
  },
  countrySelector: {
    flexDirection: 'row',
    alignItems: 'center',
    marginRight: 10,
  },
  countryCodeText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#1A202C',
    marginRight: 4,
  },
  chevronIcon: {
    marginTop: 1,
  },
  phoneDivider: {
    width: 1,
    height: 24,
    backgroundColor: '#CBD5E1',
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
  passwordHint: {
    fontSize: 12,
    color: '#64748B',
    marginLeft: 4,
    marginBottom: 4,
    fontWeight: '400',
  },

  /* Checkboxes */
  checkboxesContainer: {
    marginBottom: 20,
    marginTop: 6,
    gap: 12,
  },
  checkboxRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 10,
  },
  checkbox: {
    width: 22,
    height: 22,
    borderRadius: 6,
    borderWidth: 2,
    borderColor: '#CBD5E1',
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 1,
    backgroundColor: '#FFFFFF',
  },
  checkboxChecked: {
    backgroundColor: '#84CC16',
    borderColor: '#84CC16',
  },
  checkboxText: {
    flex: 1,
    fontSize: 13,
    color: '#475569',
    lineHeight: 18,
    fontWeight: '400',
  },
  greenLinkText: {
    color: '#65A30D',
    fontWeight: '700',
  },

  /* Create Account Button */
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
    color: '#65A30D',
  },
});
