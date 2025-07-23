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
import { useBooking } from '../context/BookingContext';
import { useAuth } from '../context/AuthContext';
import { formatTime, formatDate } from '../utils/helpers';

const MyBookingsScreen = ({ route, navigation }) => {
  const { bookings, getBookingsByEmail, cancelBooking, loading } = useBooking();
  const { user } = useAuth();
  const [filteredBookings, setFilteredBookings] = useState([]);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    if (user && user.email) {
      // Automatically load bookings for the logged-in user
      loadUserBookings();
    }
  }, [bookings, user]);

  const loadUserBookings = () => {
    if (user && user.email) {
      const userBookings = getBookingsByEmail(user.email);
      setFilteredBookings(userBookings);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    if (user && user.email) {
      loadUserBookings();
    }
    setRefreshing(false);
  };

  const handleCancelBooking = (booking) => {
    Alert.alert(
      'Cancel Booking',
      `Are you sure you want to cancel this booking?\n\nBooking ID: ${booking.id}`,
      [
        { text: 'No', style: 'cancel' },
        {
          text: 'Yes, Cancel',
          style: 'destructive',
          onPress: async () => {
            const result = await cancelBooking(booking.id);
            if (result.success) {
              Alert.alert('Success', 'Booking cancelled successfully');
              loadUserBookings(); // Refresh the list
            } else {
              Alert.alert('Error', result.error || 'Failed to cancel booking');
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

  const renderBookingItem = ({ item: booking }) => (
    <View style={styles.bookingCard}>
      <View style={styles.bookingHeader}>
        <View>
          <Text style={styles.bookingId}>Booking #{booking.id.slice(-8)}</Text>
          <Text style={styles.bookingDate}>
            Booked on {formatDate(booking.bookingDate.split('T')[0])}
          </Text>
        </View>
        <View style={[styles.statusBadge, { backgroundColor: getStatusColor(booking.status) }]}>
          <Text style={styles.statusText}>{getStatusText(booking.status)}</Text>
        </View>
      </View>

      <View style={styles.customerInfo}>
        <Text style={styles.customerName}>{booking.customerInfo.name}</Text>
        <Text style={styles.customerEmail}>{booking.customerInfo.email}</Text>
        {booking.customerInfo.phone && (
          <Text style={styles.customerPhone}>{booking.customerInfo.phone}</Text>
        )}
      </View>

      <View style={styles.classesSection}>
        <Text style={styles.sectionTitle}>Classes ({booking.classes.length})</Text>
        {booking.classes.map((classItem, index) => (
          <View key={index} style={styles.classItem}>
            <View style={styles.classInfo}>
              <Text style={styles.className}>{classItem.name}</Text>
              <Text style={styles.classDetails}>
                with {classItem.instructor} • {formatDate(classItem.date)} at {formatTime(classItem.time)}
              </Text>
              <Text style={styles.classDetails}>
                Duration: {classItem.duration} min • Qty: {classItem.quantity}
              </Text>
            </View>
            <Text style={styles.classPrice}>${(classItem.price * classItem.quantity).toFixed(2)}</Text>
          </View>
        ))}
      </View>

      <View style={styles.bookingFooter}>
        <View style={styles.totalContainer}>
          <Text style={styles.totalLabel}>Total Amount:</Text>
          <Text style={styles.totalAmount}>${booking.totalAmount.toFixed(2)}</Text>
        </View>
        
        {booking.status === 'confirmed' && (
          <TouchableOpacity
            style={styles.cancelButton}
            onPress={() => handleCancelBooking(booking)}
          >
            <Text style={styles.cancelButtonText}>Cancel Booking</Text>
          </TouchableOpacity>
        )}
      </View>
    </View>
  );

  const renderEmptyState = () => {
    if (!user) {
      return (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyIcon}>�</Text>
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

      {filteredBookings.length > 0 ? (
        <FlatList
          data={filteredBookings}
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
    backgroundColor: '#fa7575ff',
    paddingVertical: 10,
    paddingHorizontal: 16,
    borderRadius: 8,
    alignSelf: 'flex-start',
  },
  cancelButtonText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: 'bold',
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
    backgroundColor: '#661a72',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
  },
  signInButtonText: {
    color: '#fff',
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
