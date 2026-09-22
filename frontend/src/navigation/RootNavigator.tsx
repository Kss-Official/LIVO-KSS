import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { NavigationContainer } from '@react-navigation/native';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Feather, Ionicons } from '@expo/vector-icons';

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
          height: 64,
          paddingBottom: 8,
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
                top: -14,
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
  return (
    <NavigationContainer>
      <Stack.Navigator screenOptions={{ headerShown: false }} initialRouteName="Welcome">
        <Stack.Screen name="Welcome">
          {(props) => (
            <WelcomeScreen
              onGetStarted={() => props.navigation.navigate('SignUp')}
              onLogin={() => props.navigation.navigate('MainTabs')}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="SignUp">
          {(props) => (
            <SignUpScreen
              onBack={() => props.navigation.goBack()}
              onCreateAccount={() => props.navigation.navigate('SelectCategories')}
              onSignIn={() => props.navigation.navigate('SignIn')}
              onGoogleSignUp={() => props.navigation.navigate('MainTabs')}
              onAppleSignUp={() => props.navigation.navigate('MainTabs')}
              onMicrosoftSignUp={() => props.navigation.navigate('MainTabs')}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="SignIn">
          {(props) => (
            <SignInScreen
              onBack={() => props.navigation.goBack()}
              onSignInSubmit={() => props.navigation.navigate('MainTabs')}
              onSignUp={() => props.navigation.navigate('SignUp')}
              onGoogleSignUp={() => props.navigation.navigate('MainTabs')}
              onAppleSignUp={() => props.navigation.navigate('MainTabs')}
              onMicrosoftSignUp={() => props.navigation.navigate('MainTabs')}
            />
          )}
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
              onGoHome={() => props.navigation.navigate('MainTabs')}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="MainTabs" component={MainTabNavigator} />
        <Stack.Screen name="AddTask">
          {(props) => <AddTaskScreen onBack={() => props.navigation.goBack()} />}
        </Stack.Screen>
        <Stack.Screen name="AddEvent">
          {(props) => <AddEventScreen onBack={() => props.navigation.goBack()} />}
        </Stack.Screen>
        <Stack.Screen name="AddGoal">
          {(props) => <AddGoalScreen onBack={() => props.navigation.goBack()} />}
        </Stack.Screen>
        <Stack.Screen name="AddHabit">
          {(props) => <AddHabitScreen onBack={() => props.navigation.goBack()} />}
        </Stack.Screen>
        <Stack.Screen name="AddExpense">
          {(props) => <AddExpenseScreen onBack={() => props.navigation.goBack()} />}
        </Stack.Screen>
        <Stack.Screen name="AddTrip">
          {(props) => <AddTripScreen onBack={() => props.navigation.goBack()} />}
        </Stack.Screen>
        <Stack.Screen name="AddLearning">
          {(props) => <AddLearningScreen onBack={() => props.navigation.goBack()} />}
        </Stack.Screen>
        <Stack.Screen name="AddHealth">
          {(props) => <AddHealthScreen onBack={() => props.navigation.goBack()} />}
        </Stack.Screen>
      </Stack.Navigator>
    </NavigationContainer>
  );
};

