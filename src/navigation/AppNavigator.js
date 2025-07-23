import React, { useState, useRef } from 'react';
import { TouchableOpacity, Text } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator } from '@react-navigation/stack';
import LoginScreen from '../screens/LoginScreen';
import RegisterScreen from '../screens/RegisterScreen';
import ProfileScreen from '../screens/ProfileScreen';
import ClassListScreen from '../screens/ClassListScreen';
import ClassDetailsScreen from '../screens/ClassDetailsScreen';
import CartScreen from '../screens/CartScreen';
import MyBookingsScreen from '../screens/MyBookingsScreen';
import HamburgerMenu from '../components/HamburgerMenu';

const Stack = createStackNavigator();

const AppNavigator = () => {
  const [menuVisible, setMenuVisible] = useState(false);
  const navigationRef = useRef();

  const HamburgerIcon = ({ navigation }) => (
    <TouchableOpacity
      onPress={() => setMenuVisible(true)}
      style={{ marginLeft: 20 }}
    >
      <Text style={{ fontSize: 24, color: '#661a72' }}>☰</Text>
    </TouchableOpacity>
  );

  return (
    <NavigationContainer ref={navigationRef}>
      <Stack.Navigator
        initialRouteName="ClassList"
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
          name="Login"
          component={LoginScreen}
          options={{
            title: 'Sign In',
            headerTitleAlign: 'center',
          }}
        />
        <Stack.Screen
          name="Register"
          component={RegisterScreen}
          options={{
            title: 'Create Account',
            headerTitleAlign: 'center',
          }}
        />
        <Stack.Screen
          name="Profile"
          component={ProfileScreen}
          options={({ navigation }) => ({
            title: 'My Profile',
            headerTitleAlign: 'center',
            headerLeft: () => <HamburgerIcon navigation={navigation} />,
          })}
        />
        <Stack.Screen
          name="ClassList"
          component={ClassListScreen}
          options={({ navigation }) => ({
            title: 'Yoga Classes',
            headerTitleAlign: 'center',
            headerLeft: () => <HamburgerIcon navigation={navigation} />,
          })}
        />
        <Stack.Screen
          name="ClassDetails"
          component={ClassDetailsScreen}
          options={({ navigation }) => ({
            title: 'Class Details',
            headerTitleAlign: 'center',
            headerLeft: () => <HamburgerIcon navigation={navigation} />,
          })}
        />
        <Stack.Screen
          name="Cart"
          component={CartScreen}
          options={({ navigation }) => ({
            title: 'Shopping Cart',
            headerTitleAlign: 'center',
            headerLeft: () => <HamburgerIcon navigation={navigation} />,
          })}
        />
        <Stack.Screen
          name="MyBookings"
          component={MyBookingsScreen}
          options={({ navigation }) => ({
            title: 'My Bookings',
            headerTitleAlign: 'center',
            headerLeft: () => <HamburgerIcon navigation={navigation} />,
          })}
        />
      </Stack.Navigator>
      <HamburgerMenu
        visible={menuVisible}
        onClose={() => setMenuVisible(false)}
        navigation={navigationRef.current}
      />
    </NavigationContainer>
  );
};

export default AppNavigator;
