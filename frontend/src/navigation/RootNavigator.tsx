import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { NavigationContainer } from '@react-navigation/native';
import { Text } from 'react-native';

import { HomeScreen } from '../screens/HomeScreen';
import { PlanningScreen } from '../screens/PlanningScreen';
import { AiScreen } from '../screens/AiScreen';
import { InsightsScreen } from '../screens/InsightsScreen';
import { Colors } from '../theme/colors';

const Tab = createBottomTabNavigator();

export const RootNavigator: React.FC = () => {
  return (
    <NavigationContainer>
      <Tab.Navigator
        screenOptions={({ route }) => ({
          headerShown: false,
          tabBarStyle: {
            backgroundColor: Colors.cardBackground,
            borderTopColor: Colors.border,
            height: 60,
            paddingBottom: 8,
            paddingTop: 8,
          },
          tabBarActiveTintColor: Colors.primary,
          tabBarInactiveTintColor: Colors.textSecondary,
          tabBarIcon: ({ focused }) => {
            let symbol = '🏠';
            if (route.name === 'Planning') symbol = '📅';
            if (route.name === 'LIVO AI') symbol = '✨';
            if (route.name === 'Insights') symbol = '📊';
            return <Text style={{ fontSize: 18 }}>{symbol}</Text>;
          },
        })}
      >
        <Tab.Screen name="Home" component={HomeScreen} />
        <Tab.Screen name="Planning" component={PlanningScreen} />
        <Tab.Screen name="LIVO AI" component={AiScreen} />
        <Tab.Screen name="Insights" component={InsightsScreen} />
      </Tab.Navigator>
    </NavigationContainer>
  );
};
