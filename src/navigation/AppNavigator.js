import React, { useState, useEffect } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import AsyncStorage from '@react-native-async-storage/async-storage';
import DemoInfoScreen from '../screens/DemoInfoScreen';
import ClassListScreen from '../screens/ClassListScreen';
import ClassDetailsScreen from '../screens/ClassDetailsScreen';
import BookClassScreen from '../screens/BookClassScreen';
import CartScreen from '../screens/CartScreen';
import MyBookingsScreen from '../screens/MyBookingsScreen';
import LoadingSpinner from '../components/LoadingSpinner';

const Stack = createStackNavigator();

const AppNavigator = () => {
  const [isFirstLaunch, setIsFirstLaunch] = useState(null);

  useEffect(() => {
    const checkFirstLaunch = async () => {
      try {
        const hasLaunched = await AsyncStorage.getItem('hasLaunchedBefore');
        if (hasLaunched === null) {
          // First time launching the app
          setIsFirstLaunch(true);
          await AsyncStorage.setItem('hasLaunchedBefore', 'true');
        } else {
          // App has been launched before
          setIsFirstLaunch(false);
        }
      } catch (error) {
        console.error('Error checking first launch:', error);
        // Default to showing DemoInfo on error
        setIsFirstLaunch(true);
      }
    };

    checkFirstLaunch();
  }, []);

  // Show loading spinner while checking first launch status
  if (isFirstLaunch === null) {
    return <LoadingSpinner message="Loading..." />;
  }

  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName={isFirstLaunch ? "DemoInfo" : "ClassList"}
        screenOptions={{
          headerStyle: {
            backgroundColor: '#def4f7',
            height: 90,
            elevation: 0, // Remove shadow on Android
          },
          headerTintColor: '#661a72',
          headerTitleStyle: {
            fontWeight: 'bold',
            fontSize: 20,
          },
        }}
      >
        <Stack.Screen
          name="DemoInfo"
          component={DemoInfoScreen}
          options={{
            title: 'Yoga Studio App',
            headerTitleAlign: 'center',
          }}
        />
        <Stack.Screen
          name="ClassList"
          component={ClassListScreen}
          options={{
            title: 'Yoga Classes',
            headerTitleAlign: 'center',
          }}
        />
        <Stack.Screen
          name="ClassDetails"
          component={ClassDetailsScreen}
          options={{
            title: 'Class Details',
            headerTitleAlign: 'center',
          }}
        />
        <Stack.Screen
          name="BookClass"
          component={BookClassScreen}
          options={{
            title: 'Book Class',
            headerTitleAlign: 'center',
          }}
        />
        <Stack.Screen
          name="Cart"
          component={CartScreen}
          options={{
            title: 'Shopping Cart',
            headerTitleAlign: 'center',
          }}
        />
        <Stack.Screen
          name="MyBookings"
          component={MyBookingsScreen}
          options={{
            title: 'My Bookings',
            headerTitleAlign: 'center',
          }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default AppNavigator;
