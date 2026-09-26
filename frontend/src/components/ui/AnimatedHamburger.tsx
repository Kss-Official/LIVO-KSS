import React, { useEffect, useRef } from 'react';
import { View, StyleSheet, TouchableWithoutFeedback, Animated } from 'react-native';

interface AnimatedHamburgerProps {
  isOpen: boolean;
  onPress: () => void;
  color?: string;
}

export const AnimatedHamburger: React.FC<AnimatedHamburgerProps> = ({ 
  isOpen, 
  onPress,
  color = '#0F172A'
}) => {
  const animation = useRef(new Animated.Value(isOpen ? 1 : 0)).current;

  useEffect(() => {
    Animated.timing(animation, {
      toValue: isOpen ? 1 : 0,
      duration: 300,
      useNativeDriver: true,
    }).start();
  }, [isOpen]);

  const topRot = animation.interpolate({
    inputRange: [0, 1],
    outputRange: ['0deg', '45deg']
  });
  
  const topTransY = animation.interpolate({
    inputRange: [0, 1],
    outputRange: [0, 6]
  });

  const botRot = animation.interpolate({
    inputRange: [0, 1],
    outputRange: ['0deg', '-45deg']
  });

  const botTransY = animation.interpolate({
    inputRange: [0, 1],
    outputRange: [0, -6]
  });

  const midScaleX = animation.interpolate({
    inputRange: [0, 0.5, 1],
    outputRange: [1, 0, 0]
  });

  const midOpacity = animation.interpolate({
    inputRange: [0, 0.5, 1],
    outputRange: [1, 0, 0]
  });

  return (
    <TouchableWithoutFeedback onPress={onPress}>
      <View style={styles.container}>
        <Animated.View 
          style={[
            styles.line, 
            { backgroundColor: color },
            { transform: [{ translateY: topTransY }, { rotate: topRot }] }
          ]} 
        />
        <Animated.View 
          style={[
            styles.line, 
            { backgroundColor: color },
            { transform: [{ scaleX: midScaleX }], opacity: midOpacity }
          ]} 
        />
        <Animated.View 
          style={[
            styles.line, 
            { backgroundColor: color },
            { transform: [{ translateY: botTransY }, { rotate: botRot }] }
          ]} 
        />
      </View>
    </TouchableWithoutFeedback>
  );
};

const styles = StyleSheet.create({
  container: {
    width: 20,
    height: 16,
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 1,
  },
  line: {
    width: 20,
    height: 2,
    borderRadius: 1,
  }
});
