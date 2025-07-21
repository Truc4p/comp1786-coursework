import React from 'react';
import { StatusBar } from 'expo-status-bar';
import { StyleSheet, View } from 'react-native';
import AppNavigator from './src/navigation/AppNavigator';
import { CartProvider } from './src/context/CartContext';
import { BookingProvider } from './src/context/BookingContext';

export default function App() {
  return (
    <CartProvider>
      <BookingProvider>
        <View style={styles.container}>
          <StatusBar style="light" />
          <AppNavigator />
        </View>
      </BookingProvider>
    </CartProvider>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
});
