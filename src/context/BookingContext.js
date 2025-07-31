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
    console.log('🔍 Looking for bookings for userId:', userId);
    console.log('📊 Total bookings in context:', bookings.length);
    
    const userBookings = bookings.filter(booking => {
      console.log('🔍 Checking booking:', booking.id, 'Customer info:', booking.customerInfo);
      
      // Safety check: Skip bookings with corrupted customerInfo
      if (!booking.customerInfo) {
        console.log('⚠️ Skipping booking with undefined customerInfo:', booking.id);
        return false;
      }
      
      // Check multiple possible userId fields
      const matchesUserId = booking.customerInfo.userId === userId;
      const matchesEmail = booking.customerInfo.email === userId;
      const matchesId = booking.customerInfo.id === userId;
      
      console.log('Match checks - userId:', matchesUserId, 'email:', matchesEmail, 'id:', matchesId);
      
      return matchesUserId || matchesEmail || matchesId;
    });
    
    console.log('✅ Found', userBookings.length, 'bookings for user');
    return userBookings;
  };

  const getBookingById = (bookingId) => {
    return bookings.find(booking => booking.id === bookingId);
  };

  const cancelBooking = async (bookingId) => {
    setLoading(true);
    try {
      console.log('🔄 Starting booking cancellation process for ID:', bookingId);
      
      // Find the booking to get current data
      const currentBooking = bookings.find(b => b.id === bookingId);
      if (!currentBooking) {
        throw new Error('Booking not found in local state');
      }
      
      console.log('📋 Current booking data:', currentBooking);
      
      // First update in Firebase
      console.log('📤 Updating booking status in Firebase...');
      const firebaseResult = await ApiService.updateBookingStatus(bookingId, 'cancelled');
      
      console.log('📥 Firebase result:', firebaseResult);
      
      if (firebaseResult && firebaseResult.success === false) {
        throw new Error(firebaseResult.error || 'Failed to update Firebase');
      }
      
      console.log('✅ Firebase update successful');
      
      // Then update local state - preserve all original booking data
      const updatedBookings = bookings.map(booking =>
        booking.id === bookingId
          ? { 
              ...booking, // Preserve all original data including customerInfo
              status: 'cancelled', 
              cancelledDate: new Date().toISOString() 
            }
          : booking
      );
      
      console.log('📋 Updated booking in local state:', 
        updatedBookings.find(b => b.id === bookingId)
      );
      
      setBookings(updatedBookings);
      await saveBookingsToStorage(updatedBookings);
      
      console.log('✅ Local state and storage updated');
      
      setLoading(false);
      return { success: true };
    } catch (error) {
      console.error('❌ Error cancelling booking:', error);
      setLoading(false);
      return { success: false, error: error.message };
    }
  };

  const refreshBookingsFromFirebase = async () => {
    try {
      setLoading(true);
      console.log('🔄 Refreshing bookings from Firebase...');
      
      // Add small delay to ensure Firebase has propagated any recent updates
      await new Promise(resolve => setTimeout(resolve, 500));
      
      const firebaseBookings = await ApiService.getAllBookings();
      
      if (firebaseBookings && Array.isArray(firebaseBookings)) {
        // Clean up duplicates and invalid bookings
        const cleanBookings = firebaseBookings.filter((booking, index, self) => {
          // Remove bookings without customerInfo
          if (!booking.customerInfo) {
            console.log('⚠️ Removing booking with no customerInfo:', booking.id);
            return false;
          }
          
          // Remove duplicates (keep first occurrence)
          const firstIndex = self.findIndex(b => b.id === booking.id);
          if (firstIndex !== index) {
            console.log('⚠️ Removing duplicate booking:', booking.id);
            return false;
          }
          
          return true;
        });
        
        console.log('🧹 Cleaned bookings:', cleanBookings.length, 'from', firebaseBookings.length, 'raw bookings');
        setBookings(cleanBookings);
        await saveBookingsToStorage(cleanBookings);
        console.log('✅ Bookings refreshed from Firebase');
      }
    } catch (error) {
      console.error('❌ Error refreshing bookings from Firebase:', error);
    } finally {
      setLoading(false);
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
    refreshBookingsFromFirebase,
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
