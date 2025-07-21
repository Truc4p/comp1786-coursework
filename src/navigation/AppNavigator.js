import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import DemoInfoScreen from '../screens/DemoInfoScreen';
import ClassListScreen from '../screens/ClassListScreen';
import ClassDetailsScreen from '../screens/ClassDetailsScreen';
import BookClassScreen from '../screens/BookClassScreen';
import CartScreen from '../screens/CartScreen';
import MyBookingsScreen from '../screens/MyBookingsScreen';

const Stack = createStackNavigator();

const AppNavigator = () => {
  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName="DemoInfo"
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
