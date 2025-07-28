import React, { createContext, useContext, useState, useEffect } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import ApiService from '../services/api';

const BookingContext = createContext();

export const BookingProvider = ({ children }) => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);

  // Load bookings from AsyncStorage on app start
  useEffect(() => {
    loadBookingsFromStorage();
  }, []);

  const loadBookingsFromStorage = async () => {
    try {
      const savedBookings = await AsyncStorage.getItem('yogaBookings');
      if (savedBookings) {
        const bookingData = JSON.parse(savedBookings);
        setBookings(bookingData);
      }
    } catch (error) {
      console.error('Error loading bookings from storage:', error);
    }
  };

  const saveBookingsToStorage = async (newBookings) => {
    try {
      await AsyncStorage.setItem('yogaBookings', JSON.stringify(newBookings));
    } catch (error) {
      console.error('Error saving bookings to storage:', error);
    }
  };

  const clearAllBookings = async () => {
    try {
      await AsyncStorage.removeItem('yogaBookings');
      setBookings([]);
      console.log('All bookings cleared for security');
    } catch (error) {
      console.error('Error clearing bookings:', error);
      // Still clear the state even if storage clearing fails
      setBookings([]);
    }
  };

  const submitBooking = async (cartItems, customerInfo) => {
    console.log('🔄 Starting booking submission...');
    setLoading(true);
    
    try {
      // Create booking object with enhanced customerInfo
      const enhancedCustomerInfo = {
        ...customerInfo,
        // Ensure we have a userId for future lookups 
        userId: customerInfo.userId,
      };
      
      const booking = {
        id: `booking_${Date.now()}`,
        customerInfo: enhancedCustomerInfo,
        classes: cartItems,
        totalAmount: cartItems.reduce((total, item) => total + (item.price * item.quantity), 0),
        bookingDate: new Date().toISOString(),
        status: 'confirmed',
      };

      console.log('📦 Booking object created:', JSON.stringify(booking, null, 2));

      // Submit to Firebase
      console.log('🔗 Calling ApiService.createBooking...');
      const result = await ApiService.createBooking(booking);
      console.log('📨 ApiService.createBooking result:', result);
      
      if (result && result.success) {
        // Add to local bookings
        const updatedBookings = [...bookings, { ...booking, firebaseId: result.id }];
        setBookings(updatedBookings);
        await saveBookingsToStorage(updatedBookings);
        
        console.log('✅ Booking submitted successfully');
        setLoading(false);
        return { success: true, booking };
      } else {
        console.log('❌ Booking failed with result:', result);
        setLoading(false);
        throw new Error(result?.error || 'Failed to submit booking');
      }
    } catch (error) {
      console.error('💥 Error submitting booking:', error);
      setLoading(false);
      return { success: false, error: error.message };
    }
  };

  const getBookingsByUserId = (userId) => {
    return bookings.filter(booking => 
      booking.customerInfo.userId === userId
    );
  };

  const getBookingById = (bookingId) => {
    return bookings.find(booking => booking.id === bookingId);
  };

  const cancelBooking = async (bookingId) => {
    setLoading(true);
    try {
      // Update booking status
      const updatedBookings = bookings.map(booking =>
        booking.id === bookingId
          ? { ...booking, status: 'cancelled', cancelledDate: new Date().toISOString() }
          : booking
      );
      
      setBookings(updatedBookings);
      await saveBookingsToStorage(updatedBookings);
      
      // Optionally update in Firebase
      await ApiService.updateBookingStatus(bookingId, 'cancelled');
      
      setLoading(false);
      return { success: true };
    } catch (error) {
      console.error('Error cancelling booking:', error);
      setLoading(false);
      return { success: false, error: error.message };
    }
  };

  const value = {
    bookings,
    loading,
    submitBooking,
    getBookingsByUserId,
    getBookingById,
    cancelBooking,
    clearAllBookings,
  };

  return (
    <BookingContext.Provider value={value}>
      {children}
    </BookingContext.Provider>
  );
};

export const useBooking = () => {
  const context = useContext(BookingContext);
  if (!context) {
    throw new Error('useBooking must be used within a BookingProvider');
  }
  return context;
};

export default BookingContext;
