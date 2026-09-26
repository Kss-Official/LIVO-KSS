import React, { useState, useEffect } from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { NavigationContainer } from '@react-navigation/native';
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator, Platform } from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { authService } from '../services/authService';

import { WelcomeScreen } from '../screens/WelcomeScreen';
import { SignUpScreen } from '../screens/SignUpScreen';
import { SignInScreen } from '../screens/SignInScreen';
import { SelectCategoriesScreen } from '../screens/SelectCategoriesScreen';
import { PersonalizeScreen } from '../screens/PersonalizeScreen';
import { AllSetScreen } from '../screens/AllSetScreen';
import { HomeScreen } from '../screens/HomeScreen';
import { PlanningScreen } from '../screens/PlanningScreen';
import { AiScreen } from '../screens/AiScreen';
import { TasksScreen } from '../screens/TasksScreen';
import { AddHubScreen } from '../screens/AddHubScreen';
import { InsightsScreen } from '../screens/InsightsScreen';
import { ProfileScreen } from '../screens/ProfileScreen';
import { AddTaskScreen } from '../screens/AddTaskScreen';
import { AddEventScreen } from '../screens/AddEventScreen';
import { AddGoalScreen } from '../screens/AddGoalScreen';
import { AddHabitScreen } from '../screens/AddHabitScreen';
import { AddExpenseScreen } from '../screens/AddExpenseScreen';
import { AddTripScreen } from '../screens/AddTripScreen';
import { AddLearningScreen } from '../screens/AddLearningScreen';
import { AddHealthScreen } from '../screens/AddHealthScreen';
import { AddOptainsScreen } from '../screens/AddOptainsScreen';
import { ScanAndAddScreen } from '../screens/ScanAndAddScreen';
import { ReviewAndAddScreen } from '../screens/ReviewAndAddScreen';
import { CalendarScreen } from '../screens/CalendarScreen';
import { AiUnavailableScreen } from '../screens/AiUnavailableScreen';
import { OfflineScreen } from '../screens/OfflineScreen';
import { GeneralErrorScreen } from '../screens/GeneralErrorScreen';
import { UpdatingLivoScreen } from '../screens/UpdatingLivoScreen';
import { ScheduleConflictScreen } from '../screens/ScheduleConflictScreen';
import { OverloadedDayScreen } from '../screens/OverloadedDayScreen';
import { TaskOverdueScreen } from '../screens/TaskOverdueScreen';
import { FreeDayScreen } from '../screens/FreeDayScreen';
import { ScheduleScreen } from '../screens/ScheduleScreen';
import { ProgressScreen } from '../screens/ProgressScreen';
import { SearchScreen } from '../screens/SearchScreen';
import { NotificationsScreen } from '../screens/NotificationsScreen';
import { ChatWithLivoScreen } from '../screens/ChatWithLivoScreen';
import { TimeDetailsScreen } from '../screens/TimeDetailsScreen';
import { VoiceRecordingScreen } from '../screens/VoiceRecordingScreen';

import { OnboardingFirstTaskScreen } from '../screens/onboarding/OnboardingFirstTaskScreen';
import { OnboardingFirstGoalScreen } from '../screens/onboarding/OnboardingFirstGoalScreen';
import { OnboardingFirstEventScreen } from '../screens/onboarding/OnboardingFirstEventScreen';
import { OnboardingFirstHabitScreen } from '../screens/onboarding/OnboardingFirstHabitScreen';

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

const MainTabNavigator: React.FC = () => {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarStyle: {
          backgroundColor: '#FFFFFF',
          borderTopColor: '#E2E8F0',
          height: Platform.OS === 'ios' ? 84 : 74,
          paddingBottom: Platform.OS === 'ios' ? 24 : 14,
          paddingTop: 8,
          elevation: 10,
          shadowColor: '#000',
          shadowOffset: { width: 0, height: -2 },
          shadowOpacity: 0.05,
          shadowRadius: 4,
        },
        tabBarActiveTintColor: '#66C400',
        tabBarInactiveTintColor: '#64748B',
        tabBarLabelStyle: {
          fontSize: 11,
          fontWeight: '600',
          marginTop: -2,
        },
        tabBarIcon: ({ focused, color }) => {
          if (route.name === 'Home') {
            return <Ionicons name={focused ? 'home' : 'home-outline'} size={22} color={color} />;
          }
          if (route.name === 'Plan') {
            return <Ionicons name={focused ? 'calendar' : 'calendar-outline'} size={22} color={color} />;
          }
          if (route.name === 'Insights') {
            return <Ionicons name={focused ? 'stats-chart' : 'stats-chart-outline'} size={22} color={color} />;
          }
          if (route.name === 'Profile') {
            return <Ionicons name={focused ? 'person' : 'person-outline'} size={22} color={color} />;
          }
          return null;
        },
      })}
    >
      <Tab.Screen name="Home" component={HomeScreen} />
      <Tab.Screen name="Plan" component={PlanningScreen} />
      <Tab.Screen
        name="Add"
        component={AddHubScreen}
        options={{
          tabBarLabel: 'Add',
          tabBarButton: (props) => (
            <TouchableOpacity
              {...props}
              style={{
                top: -18,
                justifyContent: 'center',
                alignItems: 'center',
                flex: 1,
              }}
            >
              <View
                style={{
                  width: 46,
                  height: 46,
                  borderRadius: 23,
                  backgroundColor: '#66C400',
                  alignItems: 'center',
                  justifyContent: 'center',
                  shadowColor: '#66C400',
                  shadowOffset: { width: 0, height: 4 },
                  shadowOpacity: 0.35,
                  shadowRadius: 6,
                  elevation: 5,
                }}
              >
                <Feather name="plus" size={24} color="#FFFFFF" />
              </View>
              <Text
                style={{
                  fontSize: 11,
                  fontWeight: '600',
                  color: '#64748B',
                  marginTop: 2,
                }}
              >
                Add
              </Text>
            </TouchableOpacity>
          ),
        }}
      />
      <Tab.Screen name="Insights" component={InsightsScreen} />
      <Tab.Screen name="Profile" component={ProfileScreen} />
    </Tab.Navigator>
  );
};

export const RootNavigator: React.FC = () => {
  const [initialRoute, setInitialRoute] = useState<string | null>(null);

  useEffect(() => {
    const checkOnboardingStatus = async () => {
      try {
        const value = await AsyncStorage.getItem('@livo_onboarding_completed');
        if (value === 'true') {
          setInitialRoute('MainTabs');
        } else {
          setInitialRoute('Welcome');
        }
      } catch (e) {
        setInitialRoute('Welcome');
      }
    };
    checkOnboardingStatus();
  }, []);

  if (!initialRoute) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#EFF8E2' }}>
        <ActivityIndicator size="large" color="#80D611" />
      </View>
    );
  }

  return (
    <NavigationContainer>
      <Stack.Navigator screenOptions={{ headerShown: false }} initialRouteName={initialRoute}>
        <Stack.Screen name="Welcome">
          {(props) => (
            <WelcomeScreen
              onGetStarted={() => props.navigation.navigate('SignUp')}
              onLogin={() => props.navigation.navigate('SignIn')}
            />
          )}
        </Stack.Screen>

        {/* First-Time Onboarding Flow Screens */}
        <Stack.Screen name="OnboardingFirstTask">
          {(props) => (
            <OnboardingFirstTaskScreen
              onNext={() => props.navigation.goBack()}
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>

        <Stack.Screen name="OnboardingFirstGoal">
          {(props) => (
            <OnboardingFirstGoalScreen
              onNext={() => props.navigation.goBack()}
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>

        <Stack.Screen name="OnboardingFirstEvent">
          {(props) => (
            <OnboardingFirstEventScreen
              onNext={() => props.navigation.goBack()}
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>

        <Stack.Screen name="OnboardingFirstHabit">
          {(props) => (
            <OnboardingFirstHabitScreen
              onFinish={() => props.navigation.goBack()}
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>

        <Stack.Screen name="SignUp">
          {(props) => (
            <SignUpScreen
              onBack={() => props.navigation.goBack()}
              onCreateAccount={async (data) => {
                if (data?.email) {
                  await authService.syncUser({
                    email: data.email,
                    fullName: data.name || data.email.split('@')[0],
                  });
                }
                props.navigation.navigate('SelectCategories');
              }}
              onSignIn={() => props.navigation.navigate('SignIn')}
              onGoogleSignUp={async () => {
                await authService.syncUser({ email: 'google.user@livo.app', fullName: 'Google User' });
                props.navigation.navigate('MainTabs');
              }}
              onAppleSignUp={async () => {
                await authService.syncUser({ email: 'apple.user@livo.app', fullName: 'Apple User' });
                props.navigation.navigate('MainTabs');
              }}
              onMicrosoftSignUp={async () => {
                await authService.syncUser({ email: 'microsoft.user@livo.app', fullName: 'Microsoft User' });
                props.navigation.navigate('MainTabs');
              }}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="SignIn">
          {(props) => {
            const handleSuccessLogin = async (data?: { email?: string; password?: string }) => {
              try {
                if (data?.email) {
                  await authService.syncUser({
                    email: data.email,
                    fullName: data.email.split('@')[0],
                  });
                }
                await AsyncStorage.setItem('@livo_onboarding_completed', 'true');
              } catch (e) {}
              props.navigation.reset({ index: 0, routes: [{ name: 'MainTabs' }] });
            };
            return (
              <SignInScreen
                onBack={() => props.navigation.goBack()}
                onSignInSubmit={handleSuccessLogin}
                onSignIn={handleSuccessLogin}
                onSignUp={() => props.navigation.navigate('SignUp')}
                onGoogleSignUp={() => handleSuccessLogin({ email: 'google.user@livo.app' })}
                onAppleSignUp={() => handleSuccessLogin({ email: 'apple.user@livo.app' })}
                onMicrosoftSignUp={() => handleSuccessLogin({ email: 'microsoft.user@livo.app' })}
              />
            );
          }}
        </Stack.Screen>
        <Stack.Screen name="SelectCategories">
          {(props) => (
            <SelectCategoriesScreen
              onBack={() => props.navigation.goBack()}
              onSkip={() => props.navigation.navigate('Personalize')}
              onContinue={() => props.navigation.navigate('Personalize')}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="Personalize">
          {(props) => (
            <PersonalizeScreen
              onBack={() => props.navigation.goBack()}
              onComplete={() => props.navigation.navigate('AllSet')}
              onSkip={() => props.navigation.navigate('AllSet')}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="AllSet">
          {(props) => (
            <AllSetScreen
              onGoHome={async () => {
                try {
                  await AsyncStorage.setItem('@livo_onboarding_completed', 'true');
                } catch (e) {}
                props.navigation.reset({ index: 0, routes: [{ name: 'MainTabs' }] });
              }}
              onCreateGoal={() => props.navigation.navigate('OnboardingFirstGoal')}
              onAddTask={() => props.navigation.navigate('OnboardingFirstTask')}
              onPlanSchedule={() => props.navigation.navigate('OnboardingFirstEvent')}
              onBuildHabit={() => props.navigation.navigate('OnboardingFirstHabit')}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="MainTabs" component={MainTabNavigator} />
        <Stack.Screen name="AddTask">
          {(props: any) => (
            <AddTaskScreen
              {...props}
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="AddEvent">
          {(props: any) => (
            <AddEventScreen
              {...props}
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="AddGoal">
          {(props: any) => (
            <AddGoalScreen
              {...props}
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="AddHabit">
          {(props: any) => (
            <AddHabitScreen
              {...props}
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="AddExpense">
          {(props: any) => (
            <AddExpenseScreen
              {...props}
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="AddTrip">
          {(props: any) => (
            <AddTripScreen
              {...props}
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="AddLearning">
          {(props: any) => (
            <AddLearningScreen
              {...props}
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="AddHealth">
          {(props: any) => (
            <AddHealthScreen
              {...props}
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="AddOptains">
          {(props: any) => (
            <AddOptainsScreen
              {...props}
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="ScanAndAdd">
          {(props) => <ScanAndAddScreen navigation={props.navigation} onBack={() => props.navigation.goBack()} />}
        </Stack.Screen>
        <Stack.Screen name="ReviewAndAdd">
          {(props) => <ReviewAndAddScreen route={props.route} navigation={props.navigation} onBack={() => props.navigation.goBack()} />}
        </Stack.Screen>
        <Stack.Screen name="Calendar">
          {(props) => (
            <CalendarScreen
              onBack={() => props.navigation.goBack()}
              onNavigateTab={(tab) => {
                props.navigation.navigate('MainTabs', { screen: tab });
              }}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="AiUnavailable">
          {(props) => (
            <AiUnavailableScreen
              onBack={() => props.navigation.goBack()}
              onContinueWithoutAi={() => props.navigation.navigate('MainTabs')}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="Offline">
          {(props) => (
            <OfflineScreen
              onBack={() => props.navigation.goBack()}
              onContinueOffline={() => props.navigation.navigate('MainTabs')}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="GeneralError">
          {(props) => (
            <GeneralErrorScreen
              onBack={() => props.navigation.goBack()}
              onGoHome={() => props.navigation.navigate('MainTabs')}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="SomethingWentWrong">
          {(props) => (
            <GeneralErrorScreen
              onBack={() => props.navigation.goBack()}
              onGoHome={() => props.navigation.navigate('MainTabs')}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="UpdatingLivo">
          {(props) => (
            <UpdatingLivoScreen
              onBack={() => props.navigation.goBack()}
              onComplete={() => props.navigation.navigate('MainTabs')}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="Loading">
          {(props) => (
            <UpdatingLivoScreen
              onBack={() => props.navigation.goBack()}
              onComplete={() => props.navigation.navigate('MainTabs')}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="ScheduleConflict">
          {(props) => (
            <ScheduleConflictScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="OverloadedDay">
          {(props) => (
            <OverloadedDayScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="TaskOverdue">
          {(props) => (
            <TaskOverdueScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="MissedDeadline">
          {(props) => (
            <TaskOverdueScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="FreeDay">
          {(props) => (
            <FreeDayScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="EmptyDay">
          {(props) => (
            <FreeDayScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="Tasks">
          {(props) => (
            <TasksScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="Schedule">
          {(props) => (
            <ScheduleScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="Progress">
          {(props) => (
            <ProgressScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="Search">
          {(props) => (
            <SearchScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="Notifications">
          {(props) => (
            <NotificationsScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="ChatWithLivo">
          {(props) => (
            <ChatWithLivoScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="Ai">
          {(props) => (
            <AiScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="TimeDetails">
          {(props) => (
            <TimeDetailsScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="VoiceRecording">
          {(props) => (
            <VoiceRecordingScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="AddHub">
          {(props) => (
            <AddHubScreen
              onBack={() => props.navigation.goBack()}
            />
          )}
        </Stack.Screen>
      </Stack.Navigator>
    </NavigationContainer>
  );
};
