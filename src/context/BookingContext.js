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

  const submitBooking = async (cartItems, customerInfo) => {
    setLoading(true);
    try {
      // Create booking object
      const booking = {
        id: `booking_${Date.now()}`,
        customerInfo,
        classes: cartItems,
        totalAmount: cartItems.reduce((total, item) => total + (item.price * item.quantity), 0),
        bookingDate: new Date().toISOString(),
        status: 'confirmed',
      };

      // Submit to Firebase
      const result = await ApiService.createBooking(booking);
      
      if (result.success) {
        // Add to local bookings
        const updatedBookings = [...bookings, { ...booking, firebaseId: result.id }];
        setBookings(updatedBookings);
        await saveBookingsToStorage(updatedBookings);
        
        setLoading(false);
        return { success: true, booking };
      } else {
        throw new Error(result.error || 'Failed to submit booking');
      }
    } catch (error) {
      console.error('Error submitting booking:', error);
      setLoading(false);
      return { success: false, error: error.message };
    }
  };

  const getBookingsByEmail = (email) => {
    return bookings.filter(booking => 
      booking.customerInfo.email.toLowerCase() === email.toLowerCase()
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
    getBookingsByEmail,
    getBookingById,
    cancelBooking,
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
