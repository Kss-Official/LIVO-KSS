import { apiClient, setAuthToken } from './api';
import { storage } from '../storage/asyncStorage';
import { User } from '../types';

const USER_PROFILE_KEY = '@livo_user_profile';

export interface AuthSyncResponse {
  user: {
    id: string;
    firebaseUid: string;
    email: string;
    fullName: string;
    avatarUrl?: string;
    timezone?: string;
    onboardingCompleted: boolean;
  };
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  isNewUser: boolean;
}

export const authService = {
  async syncUser(params: {
    firebaseUid?: string;
    email: string;
    fullName: string;
    avatarUrl?: string;
    timezone?: string;
  }): Promise<AuthSyncResponse | null> {
    try {
      const uid = params.firebaseUid || `usr_${params.email.replace(/[^a-zA-Z0-9]/g, '_')}`;
      const payload = {
        firebaseUid: uid,
        email: params.email,
        fullName: params.fullName || params.email.split('@')[0],
        avatarUrl: params.avatarUrl || 'https://livo.app/avatar.png',
        timezone: params.timezone || Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC',
      };

      const response = await apiClient.post<{ data: AuthSyncResponse }>('/auth/sync', payload);
      const authData = response.data?.data;

      if (authData?.accessToken) {
        await setAuthToken(authData.accessToken);
        if (authData.user) {
          const userObj: User = {
            id: authData.user.id,
            email: authData.user.email,
            fullName: authData.user.fullName,
            avatarUrl: authData.user.avatarUrl,
          };
          await storage.setItem(USER_PROFILE_KEY, userObj);
        }
      }
      return authData;
    } catch (error) {
      console.warn('Backend /auth/sync call failed, using local offline session:', error);
      // Fallback: create mock local session for smooth offline experience
      const localUser: User = {
        id: 'local_user_' + Date.now(),
        email: params.email,
        fullName: params.fullName || params.email.split('@')[0],
        avatarUrl: params.avatarUrl,
      };
      await storage.setItem(USER_PROFILE_KEY, localUser);
      return null;
    }
  },

  async getCurrentUser(): Promise<User | null> {
    try {
      const response = await apiClient.get<{ data: any }>('/auth/me');
      const u = response.data?.data;
      if (u) {
        const userObj: User = {
          id: u.id,
          email: u.email,
          fullName: u.fullName,
          avatarUrl: u.avatarUrl,
        };
        await storage.setItem(USER_PROFILE_KEY, userObj);
        return userObj;
      }
    } catch (e) {
      // offline fallback
    }
    return await storage.getItem<User>(USER_PROFILE_KEY);
  },

  async logout(): Promise<void> {
    try {
      await apiClient.post('/auth/logout', {});
    } catch (e) {
      // ignore network errors on logout
    }
    await setAuthToken(null);
    await storage.removeItem(USER_PROFILE_KEY);
    await storage.removeItem('@livo_onboarding_completed');
  }
};
