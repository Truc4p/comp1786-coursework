import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import DemoInfoScreen from '../screens/DemoInfoScreen';
import ClassListScreen from '../screens/ClassListScreen';
import ClassDetailsScreen from '../screens/ClassDetailsScreen';
import BookClassScreen from '../screens/BookClassScreen';

const Stack = createStackNavigator();

const AppNavigator = () => {
  return (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName="DemoInfo"
        screenOptions={{
          headerStyle: {
            backgroundColor: '#8B4CF7',
          },
          headerTintColor: '#fff',
          headerTitleStyle: {
            fontWeight: 'bold',
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
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default AppNavigator;
