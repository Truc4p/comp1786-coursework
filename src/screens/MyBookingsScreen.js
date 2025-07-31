import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  Alert,
  SafeAreaView,
  RefreshControl,
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { useBooking } from '../context/BookingContext';
import { useAuth } from '../context/AuthContext';
import { formatTime, formatDate } from '../utils/helpers';

const MyBookingsScreen = ({ route, navigation }) => {
  const { bookings, getBookingsByUserId, cancelBooking, loading, refreshBookingsFromFirebase } = useBooking();
  const { user } = useAuth();
  const [userBookings, setUserBookings] = useState([]);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    if (user && (user.id || user.email)) {
      // Load bookings with Firebase refresh on initial mount
      loadUserBookings(true);
    } else {
      // Clear bookings immediately when user logs out
      setUserBookings([]);
      // Navigate to login screen if trying to access bookings without being logged in
      if (!user) {
        // Small delay to avoid navigation conflicts
        setTimeout(() => {
          navigation.navigate('ClassList');
        }, 100);
      }
    }
  }, [user]); // Remove 'bookings' from dependencies to prevent infinite loop

  // Refresh bookings when screen comes into focus (force refresh to get latest data)
  useFocusEffect(
    React.useCallback(() => {
      if (user && (user.id || user.email)) {
        loadUserBookings(true); // Force refresh from Firebase when screen comes into focus
      }
    }, [user])
  );

  const loadUserBookings = async (forceRefresh = false) => {
    try {
      // Security check: Only load bookings if user is actually logged in
      if (!user) {
        setUserBookings([]);
        return;
      }
      
      console.log('🔄 loadUserBookings called with forceRefresh:', forceRefresh);
      console.log('👤 Current user:', { id: user.id, email: user.email });
      
      // Only refresh from Firebase when explicitly requested (initial load, pull-to-refresh, or after actions)
      if (forceRefresh) {
        await refreshBookingsFromFirebase();
      }
      
      if (user) {
        // Use userId (which is either user.id or user.email as fallback from CartScreen)
        const userId = user.id || user.email;
        console.log('🔍 Using userId for filtering:', userId);
        
        const userBookingsList = getBookingsByUserId(userId);
        console.log('📋 Raw user bookings:', userBookingsList);
        
        // Sort bookings by booking date in descending order (latest first)
        const sortedBookings = userBookingsList.sort((a, b) => {
          const dateA = new Date(a.bookingDate || new Date());
          const dateB = new Date(b.bookingDate || new Date());
          return dateB - dateA; // Latest bookings first
        });
        
        console.log('📋 Sorted bookings:', sortedBookings.length, 'items');
        setUserBookings(sortedBookings);
      }
    } catch (error) {
      console.error('❌ Error loading user bookings:', error);
      // Don't crash the app, just show empty state
      setUserBookings([]);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    if (user && user.id) {
      await loadUserBookings(true); // Force refresh from Firebase when pull-to-refresh
    }
    setRefreshing(false);
  };

  const handleCancelBooking = (booking) => {
    Alert.alert(
      'Cancel Booking',
      `Are you sure you want to cancel this booking?\n\nBooking ID: ${booking.id}\n\nThis action will notify the admin immediately.`,
      [
        { text: 'No', style: 'cancel' },
        {
          text: 'Yes, Cancel',
          style: 'destructive',
          onPress: async () => {
            console.log('🔄 Cancelling booking:', booking.id);
            console.log('📋 Booking before cancel:', booking);
            const result = await cancelBooking(booking.id);
            console.log('📄 Cancel result:', result);
            
            if (result.success) {
              console.log('✅ Booking cancelled successfully and synced to Firebase');
              Alert.alert(
                'Success', 
                'Booking cancelled successfully!\n\nThe admin has been notified automatically.'
              );
              
              // Don't force Firebase refresh immediately - local state is already updated
              // Just refresh the local user bookings display without hitting Firebase
              await loadUserBookings(false);
            } else {
              console.error('❌ Failed to cancel booking:', result.error);
              Alert.alert(
                'Error', 
                `Failed to cancel booking: ${result.error || 'Unknown error'}\n\nPlease try again or contact support.`
              );
            }
          },
        },
      ]
    );
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'confirmed':
        return '#4caf4f';
      case 'pending':
        return '#ffa726';
      case 'cancelled':
        return '#fa7575ff';
      case 'completed':
        return '#41a4f6ff';
      default:
        return '#999';
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'confirmed':
        return 'Confirmed';
      case 'cancelled':
        return 'Cancelled';
      case 'completed':
        return 'Completed';
      default:
        return status;
    }
  };

  const renderBookingItem = ({ item: booking }) => {
    // Debug log to see booking status
    console.log('📋 Booking status for ID', booking.id?.slice(-8), ':', booking.status);
    
    // Safety checks to prevent crashes
    if (!booking || !booking.id || !booking.customerInfo) {
      console.error('⚠️ Invalid booking data:', booking);
      return null;
    }
    
    return (
    <View style={styles.bookingCard}>
      <View style={styles.bookingHeader}>
        <View>
          <Text style={styles.bookingId}>Booking #{booking.id.slice(-8)}</Text>
          <Text style={styles.bookingDate}>
            Booked on {formatDate(booking.bookingDate?.split('T')[0] || new Date().toISOString().split('T')[0])}
          </Text>
        </View>
        <View style={[styles.statusBadge, { backgroundColor: getStatusColor(booking.status || 'pending') }]}>
          <Text style={styles.statusText}>{getStatusText(booking.status || 'pending')}</Text>
        </View>
      </View>

      <View style={styles.customerInfo}>
        <Text style={styles.customerName}>{booking.customerInfo.name || 'Unknown'}</Text>
        <Text style={styles.customerEmail}>{booking.customerInfo.email || 'No email'}</Text>
        {booking.customerInfo.phone && (
          <Text style={styles.customerPhone}>{booking.customerInfo.phone}</Text>
        )}
      </View>

      <View style={styles.classesSection}>
        <Text style={styles.sectionTitle}>Classes ({booking.classes?.length || 0})</Text>
        {(booking.classes || []).map((classItem, index) => (
          <View key={index} style={styles.classItem}>
            <View style={styles.classInfo}>
              <Text style={styles.className}>{classItem.name || 'Unknown Class'}</Text>
              <Text style={styles.classDetails}>
                with {classItem.instructor || 'TBA'} • {formatDate(classItem.date)} at {formatTime(classItem.time)}
              </Text>
              <Text style={styles.classDetails}>
                Duration: {classItem.duration || 0} min • Qty: {classItem.quantity || 1}
              </Text>
            </View>
            <Text style={styles.classPrice}>${((classItem.price || 0) * (classItem.quantity || 1)).toFixed(2)}</Text>
          </View>
        ))}
      </View>

      <View style={styles.bookingFooter}>
        <View style={styles.totalContainer}>
          <Text style={styles.totalLabel}>Total Amount:</Text>
          <Text style={styles.totalAmount}>${(booking.totalAmount || 0).toFixed(2)}</Text>
        </View>
        
        {/* Show cancel button for confirmed bookings, and temporarily for debugging */}
        {(booking.status === 'confirmed' || booking.status === 'pending') && (
          <TouchableOpacity
            style={styles.cancelButton}
            onPress={() => handleCancelBooking(booking)}
          >
            <Text style={styles.cancelButtonText}>Cancel</Text>
          </TouchableOpacity>
        )}
        
      </View>
    </View>
    );
  };

  const renderEmptyState = () => {
    if (!user) {
      return (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyTitle}>Please Sign In</Text>
          <Text style={styles.emptySubtitle}>
            You need to sign in to view your bookings
          </Text>
          <TouchableOpacity
            style={styles.signInButton}
            onPress={() => navigation.navigate('Login')}
          >
            <Text style={styles.signInButtonText}>Sign In</Text>
          </TouchableOpacity>
        </View>
      );
    }

    return (
      <View style={styles.emptyContainer}>
        <Text style={styles.emptyIcon}>📅</Text>
        <Text style={styles.emptyTitle}>No bookings yet</Text>
        <Text style={styles.emptySubtitle}>
          You haven't made any bookings yet. Start exploring yoga classes!
        </Text>
        <TouchableOpacity
          style={styles.exploreButton}
          onPress={() => navigation.navigate('ClassList')}
        >
          <Text style={styles.exploreButtonText}>Explore Classes</Text>
        </TouchableOpacity>
      </View>
    );
  };

  return (
    <SafeAreaView style={styles.container}>
      {user && (
        <View style={styles.userInfoContainer}>
          <Text style={styles.userInfoText}>
            Bookings for: {user.firstName} {user.lastName} ({user.email})
          </Text>
        </View>
      )}

      {userBookings.length > 0 ? (
        <FlatList
          data={userBookings}
          renderItem={renderBookingItem}
          keyExtractor={(item) => item.id}
          refreshControl={
            <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
          }
          showsVerticalScrollIndicator={false}
        />
      ) : (
        renderEmptyState()
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F3E5F5',
  },
  userInfoContainer: {
    backgroundColor: '#F3E5F5',
    padding: 16,
  },
  userInfoText: {
    fontSize: 16,
    fontWeight: '500',
    color: '#333',
    textAlign: 'center',
  },
  bookingCard: {
    backgroundColor: '#fff',
    marginHorizontal: 20,
    marginVertical: 8,
    borderRadius: 12,
    padding: 16,
  },
  bookingHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 16,
  },
  bookingId: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  bookingDate: {
    fontSize: 14,
    color: '#666',
    marginTop: 4,
  },
  statusBadge: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 15,
  },
  statusText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: 'bold',
  },
  customerInfo: {
    backgroundColor: '#f8f8f8',
    padding: 12,
    borderRadius: 8,
    marginBottom: 16,
  },
  customerName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  customerEmail: {
    fontSize: 14,
    color: '#666',
    marginTop: 2,
  },
  customerPhone: {
    fontSize: 14,
    color: '#666',
    marginTop: 2,
  },
  classesSection: {
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 12,
  },
  classItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  classInfo: {
    flex: 1,
  },
  className: {
    fontSize: 15,
    fontWeight: '600',
    color: '#333',
    marginBottom: 4,
  },
  classDetails: {
    fontSize: 13,
    color: '#666',
    marginBottom: 2,
  },
  classPrice: {
    fontSize: 15,
    fontWeight: 'bold',
    color: '#4caf4f',
    marginLeft: 12,
  },
  bookingFooter: {
    paddingTop: 16,
  },
  totalContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  totalLabel: {
    fontSize: 16,
    fontWeight: '500',
    color: '#333',
  },
  totalAmount: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#4caf4f',
  },
  cancelButton: {
    backgroundColor: '#ffebee',
    borderWidth: 1,
    borderColor: '#fa7575ff',
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 8,
    alignSelf: 'flex-start',
  },
  cancelButtonText: {
    color: '#fa7575ff',
    fontSize: 14,
    fontWeight: '600',
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 40,
  },
  emptyIcon: {
    fontSize: 80,
    marginBottom: 20,
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 10,
    textAlign: 'center',
  },
  emptySubtitle: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
    lineHeight: 24,
    marginBottom: 24,
  },
  signInButton: {
    backgroundColor: '#def4f7',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
  },
  signInButtonText: {
    color: '#661a72',
    fontSize: 16,
    fontWeight: '600',
  },
  exploreButton: {
    backgroundColor: '#def4f7',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
  },
  exploreButtonText: {
    color: '#661a72',
    fontSize: 16,
    fontWeight: '600',
  },
});

export default MyBookingsScreen;
