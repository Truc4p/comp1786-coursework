import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  Alert,
  SafeAreaView,
  TextInput,
  RefreshControl,
} from 'react-native';
import { useBooking } from '../context/BookingContext';
import { formatTime, formatDate } from '../utils/helpers';

const MyBookingsScreen = ({ route, navigation }) => {
  const { bookings, getBookingsByEmail, cancelBooking, loading } = useBooking();
  const [searchEmail, setSearchEmail] = useState(route.params?.userEmail || '');
  const [filteredBookings, setFilteredBookings] = useState([]);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    if (searchEmail) {
      handleSearch();
    }
  }, [bookings, searchEmail]);

  const handleSearch = () => {
    if (!searchEmail.trim()) {
      Alert.alert('Error', 'Please enter your email address');
      return;
    }

    const userBookings = getBookingsByEmail(searchEmail.trim());
    setFilteredBookings(userBookings);
  };

  const onRefresh = async () => {
    setRefreshing(true);
    if (searchEmail) {
      handleSearch();
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
              handleSearch(); // Refresh the list
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
        return '#4CAF50';
      case 'cancelled':
        return '#f44336';
      case 'completed':
        return '#2196F3';
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

  const renderEmptyState = () => (
    <View style={styles.emptyContainer}>
      <Text style={styles.emptyIcon}>📅</Text>
      <Text style={styles.emptyTitle}>No bookings found</Text>
      <Text style={styles.emptySubtitle}>
        {searchEmail 
          ? `No bookings found for ${searchEmail}` 
          : 'Enter your email to view your bookings'
        }
      </Text>
    </View>
  );

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.searchContainer}>
        <Text style={styles.searchLabel}>Enter your email to view bookings:</Text>
        <View style={styles.searchRow}>
          <TextInput
            style={styles.searchInput}
            value={searchEmail}
            onChangeText={setSearchEmail}
            placeholder="your.email@example.com"
            keyboardType="email-address"
            autoCapitalize="none"
          />
          <TouchableOpacity style={styles.searchButton} onPress={handleSearch}>
            <Text style={styles.searchButtonText}>Search</Text>
          </TouchableOpacity>
        </View>
      </View>

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
  searchContainer: {
    backgroundColor: '#fff',
    padding: 20,
  },
  searchLabel: {
    fontSize: 16,
    fontWeight: '500',
    color: '#333',
    marginBottom: 12,
  },
  searchRow: {
    flexDirection: 'row',
    gap: 12,
  },
  searchInput: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#f9f9f9',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    backgroundColor: '#f9f9f9',
  },
  searchButton: {
    backgroundColor: '#def4f7',
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 8,
    justifyContent: 'center',
  },
  searchButtonText: {
    color: '#333',
    fontSize: 16,
    fontWeight: 'bold',
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
    borderTopWidth: 1,
    borderTopColor: '#f0f0f0',
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
  },
});

export default MyBookingsScreen;
