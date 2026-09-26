import { useState, useEffect, useCallback } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { authService } from '../services/authService';

export interface UserProfile {
  name: string;
  email: string;
  bio: string;
}

const DEFAULT_PROFILE: UserProfile = {
  name: 'LIVO User',
  email: 'user@livo.app',
  bio: 'Designing a better me, everyday',
};

const PROFILE_STORAGE_KEY = '@livo_user_profile';

export const useProfile = () => {
  const [profile, setProfile] = useState<UserProfile>(DEFAULT_PROFILE);
  const [isLoading, setIsLoading] = useState(true);

  const loadProfile = useCallback(async () => {
    try {
      setIsLoading(true);
      // Try backend user first
      const currentUser = await authService.getCurrentUser();
      if (currentUser) {
        const p: UserProfile = {
          name: currentUser.fullName || currentUser.email.split('@')[0],
          email: currentUser.email,
          bio: 'Designing a better me, everyday',
        };
        setProfile(p);
        await AsyncStorage.setItem(PROFILE_STORAGE_KEY, JSON.stringify(p));
        return;
      }

      const stored = await AsyncStorage.getItem(PROFILE_STORAGE_KEY);
      if (stored) {
        const parsed = JSON.parse(stored);
        setProfile({
          name: parsed.name || parsed.fullName || DEFAULT_PROFILE.name,
          email: parsed.email || DEFAULT_PROFILE.email,
          bio: parsed.bio || DEFAULT_PROFILE.bio,
        });
      }
    } catch (e) {
      console.warn('Failed to load profile', e);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadProfile();
  }, [loadProfile]);

  const updateProfile = async (updates: Partial<UserProfile>) => {
    try {
      const newProfile = { ...profile, ...updates };
      setProfile(newProfile);
      await AsyncStorage.setItem(PROFILE_STORAGE_KEY, JSON.stringify(newProfile));
    } catch (e) {
      console.warn('Failed to save profile', e);
    }
  };

  return {
    profile,
    updateProfile,
    refreshProfile: loadProfile,
    isLoading,
  };
};
