import React, { createContext, useContext, useState, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import ApiService from '../services/api';

const AuthContext = createContext({});

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAuthState();
  }, []);

  const checkAuthState = async () => {
    try {
      const userData = await AsyncStorage.getItem('user');
      const token = await AsyncStorage.getItem('authToken');
      
      if (userData && token) {
        setUser(JSON.parse(userData));
      }
    } catch (error) {
      console.error('Error checking auth state:', error);
    } finally {
      setLoading(false);
    }
  };

  const login = async (email, password) => {
    try {
    //   console.log('AuthContext: Attempting login for:', email);
      const response = await ApiService.login(email, password);
      
      if (response.success) {
        const userData = response.user;
        const token = response.token;
        
        // console.log('AuthContext: Login successful, saving user data');
        await AsyncStorage.setItem('user', JSON.stringify(userData));
        await AsyncStorage.setItem('authToken', token);
        
        setUser(userData);
        return userData;
      } else {
        // console.log('AuthContext: Login failed:', response.message);
        throw new Error(response.message || 'Login failed');
      }
    } catch (error) {
      console.error('AuthContext: Login error:', error);
      throw error;
    }
  };

  const register = async (userData) => {
    try {
      // console.log('AuthContext: Attempting registration for:', userData.email);
      const response = await ApiService.register(userData);
      
      if (response.success) {
        const newUser = response.user;
        const token = response.token;
        
        // console.log('AuthContext: Registration successful, saving user data');
        await AsyncStorage.setItem('user', JSON.stringify(newUser));
        await AsyncStorage.setItem('authToken', token);
        
        setUser(newUser);
        return newUser;
      } else {
        // console.log('AuthContext: Registration failed:', response.message);
        throw new Error(response.message || 'Registration failed');
      }
    } catch (error) {
      console.error('AuthContext: Registration error:', error);
      throw error;
    }
  };

  const updateProfile = async (profileData) => {
    try {
      const response = await ApiService.updateProfile(user.id, profileData);
      
      if (response.success) {
        const updatedUser = { ...user, ...profileData };
        await AsyncStorage.setItem('user', JSON.stringify(updatedUser));
        setUser(updatedUser);
        return updatedUser;
      } else {
        throw new Error(response.message || 'Profile update failed');
      }
    } catch (error) {
      console.error('Profile update error:', error);
      throw error;
    }
  };

  const logout = async () => {
    try {
      // Clear all user-related data from AsyncStorage
      await AsyncStorage.removeItem('user');
      await AsyncStorage.removeItem('authToken');
      
      // Clear user state immediately
      setUser(null);
      
      console.log('User logged out successfully');
    } catch (error) {
      console.error('Logout error:', error);
      // Even if there's an error clearing storage, clear the user state
      setUser(null);
    }
  };

  const isAuthenticated = () => {
    return !!user;
  };

  const value = {
    user,
    loading,
    login,
    register,
    updateProfile,
    logout,
    isAuthenticated,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
