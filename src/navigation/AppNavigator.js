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
      onPress={() => {
        try {
          setMenuVisible(true);
        } catch (error) {
          console.error('Menu error:', error);
        }
      }}
      style={{ marginLeft: 20 }}
    >
      <Text style={{ fontSize: 24, color: '#661a72' }}>☰</Text>
    </TouchableOpacity>
  );

  const HomeIcon = ({ navigation }) => (
    <TouchableOpacity
      onPress={() => {
        try {
          if (navigation && navigation.navigate) {
            navigation.navigate('ClassList');
          }
        } catch (error) {
          console.error('Navigation error:', error);
        }
      }}
      style={{ marginRight: 20 }}
    >
      <Text style={{ fontSize: 24, color: '#661a72' }}>🏠</Text>
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
          options={({ navigation }) => ({
            title: 'Sign In',
            headerTitleAlign: 'center',
            headerRight: () => <HomeIcon navigation={navigation} />,
          })}
        />
        <Stack.Screen
          name="Register"
          component={RegisterScreen}
          options={({ navigation }) => ({
            title: 'Create Account',
            headerTitleAlign: 'center',
            headerRight: () => <HomeIcon navigation={navigation} />,
          })}
        />
        <Stack.Screen
          name="Profile"
          component={ProfileScreen}
          options={({ navigation }) => ({
            title: 'My Profile',
            headerTitleAlign: 'center',
            headerLeft: () => <HamburgerIcon navigation={navigation} />,
            headerRight: () => <HomeIcon navigation={navigation} />,
          })}
        />
        <Stack.Screen
          name="ClassList"
          component={ClassListScreen}
          options={({ navigation }) => ({
            title: 'Yoga Classes',
            headerTitleAlign: 'center',
            headerLeft: () => <HamburgerIcon navigation={navigation} />,
            headerRight: null, // No home icon on home screen
          })}
        />
        <Stack.Screen
          name="ClassDetails"
          component={ClassDetailsScreen}
          options={({ navigation }) => ({
            title: 'Class Details',
            headerTitleAlign: 'center',
            headerLeft: () => <HamburgerIcon navigation={navigation} />,
            headerRight: () => <HomeIcon navigation={navigation} />,
          })}
        />
        <Stack.Screen
          name="Cart"
          component={CartScreen}
          options={({ navigation }) => ({
            title: 'Shopping Cart',
            headerTitleAlign: 'center',
            headerLeft: () => <HamburgerIcon navigation={navigation} />,
            headerRight: () => <HomeIcon navigation={navigation} />,
          })}
        />
        <Stack.Screen
          name="MyBookings"
          component={MyBookingsScreen}
          options={({ navigation }) => ({
            title: 'My Bookings',
            headerTitleAlign: 'center',
            headerLeft: () => <HamburgerIcon navigation={navigation} />,
            headerRight: () => <HomeIcon navigation={navigation} />,
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
