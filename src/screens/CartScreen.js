import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  Alert,
  SafeAreaView,
  TextInput,
  ScrollView,
} from 'react-native';
import { useCart } from '../context/CartContext';
import { useBooking } from '../context/BookingContext';
import { useAuth } from '../context/AuthContext';
import { formatTime, formatDate } from '../utils/helpers';

const CartScreen = ({ navigation }) => {
  const { cart, removeFromCart, updateQuantity, clearCart, getCartTotal } = useCart();
  const { submitBooking, loading } = useBooking();
  const { user } = useAuth();
  const [customerInfo, setCustomerInfo] = useState({
    name: '',
    email: '',
    phone: '',
  });
  const [showCheckout, setShowCheckout] = useState(false);
  
  const handleProceedToCheckout = () => {
    // Auto-fill customer info if user is logged in
    if (user) {
      setCustomerInfo({
        name: `${user.firstName} ${user.lastName}`,
        email: user.email,
        phone: user.phone || '',
      });
    }
    setShowCheckout(true);
  };

  const handleQuantityChange = (classId, newQuantity) => {
    if (newQuantity === 0) {
      Alert.alert(
        'Remove Item',
        'Are you sure you want to remove this class from your cart?',
        [
          { text: 'Cancel', style: 'cancel' },
          { text: 'Remove', onPress: () => removeFromCart(classId) },
        ]
      );
    } else {
      updateQuantity(classId, newQuantity);
    }
  };

  const validateForm = () => {
    const { name, email } = customerInfo;
    
    if (!name.trim()) {
      Alert.alert('Error', 'Please enter your full name');
      return false;
    }
    
    if (!email.trim()) {
      Alert.alert('Error', 'Please enter your email address');
      return false;
    }
    
    if (!email.includes('@')) {
      Alert.alert('Error', 'Please enter a valid email address');
      return false;
    }
    
    return true;
  };

  const handleCheckout = async () => {
    console.log('🛒 handleCheckout called');
    
    try {
      if (!validateForm()) {
        console.log('❌ Form validation failed');
        return;
      }
      
      if (cart.items.length === 0) {
        console.log('❌ Cart is empty');
        Alert.alert('Empty Cart', 'Please add some classes to your cart first.');
        return;
      }

      console.log('🚀 Starting booking process...');
      
      const result = await submitBooking(cart.items, customerInfo);
      console.log('📝 Final booking submission result:', result);
      
      if (result && result.success) {
        console.log('🎉 Booking successful, showing confirmation');
        Alert.alert(
          'Booking Confirmed!',
          `Your booking has been confirmed. Booking ID: ${result.booking?.id || 'N/A'}`,
          [
            {
              text: 'View Bookings',
              onPress: () => {
                clearCart();
                navigation.navigate('MyBookings', { 
                  userEmail: customerInfo.email 
                });
              },
            },
            {
              text: 'Continue Shopping',
              onPress: () => {
                clearCart();
                navigation.navigate('ClassList');
              },
            },
          ]
        );
      } else {
        const errorMessage = result?.error || 'Something went wrong. Please try again.';
        console.log('❌ Booking failed:', errorMessage);
        Alert.alert('Booking Failed', errorMessage);
      }
    } catch (error) {
      console.error('💥 Unexpected error in handleCheckout:', error);
      Alert.alert('Error', 'An unexpected error occurred. Please try again.');
    }
  };

  const renderCartItem = ({ item }) => (
    <View style={styles.cartItem}>
      <View style={styles.itemHeader}>
        <Text style={styles.className}>{item.name}</Text>
        <TouchableOpacity
          onPress={() => removeFromCart(item.id)}
          style={styles.removeButton}
        >
          <Text style={styles.removeButtonText}>✕</Text>
        </TouchableOpacity>
      </View>
      
      <Text style={styles.itemDetails}>with {item.instructor}</Text>
      <Text style={styles.itemDetails}>
        {formatDate(item.date)} at {formatTime(item.time)}
      </Text>
      <Text style={styles.itemDetails}>{item.duration} minutes</Text>
      
      <View style={styles.itemFooter}>
        <View style={styles.quantityContainer}>
          <TouchableOpacity
            onPress={() => handleQuantityChange(item.id, item.quantity - 1)}
            style={styles.quantityButton}
          >
            <Text style={styles.quantityButtonText}>−</Text>
          </TouchableOpacity>
          <Text style={styles.quantity}>{item.quantity}</Text>
          <TouchableOpacity
            onPress={() => handleQuantityChange(item.id, item.quantity + 1)}
            style={styles.quantityButton}
          >
            <Text style={styles.quantityButtonText}>+</Text>
          </TouchableOpacity>
        </View>
        <Text style={styles.itemPrice}>${(item.price * item.quantity).toFixed(2)}</Text>
      </View>
    </View>
  );

  const renderCheckoutForm = () => (
    <View style={styles.checkoutForm}>
      <Text style={styles.sectionTitle}>Customer Information</Text>
      
      <View style={styles.inputGroup}>
        <Text style={styles.label}>Full Name *</Text>
        <TextInput
          style={styles.input}
          value={customerInfo.name}
          onChangeText={(value) => setCustomerInfo(prev => ({ ...prev, name: value }))}
          placeholder="Enter your full name"
        />
      </View>

      <View style={styles.inputGroup}>
        <Text style={styles.label}>Email Address *</Text>
        <TextInput
          style={styles.input}
          value={customerInfo.email}
          onChangeText={(value) => setCustomerInfo(prev => ({ ...prev, email: value }))}
          placeholder="Enter your email address"
          keyboardType="email-address"
          autoCapitalize="none"
        />
      </View>

      <View style={styles.inputGroup}>
        <Text style={styles.label}>Phone Number</Text>
        <TextInput
          style={styles.input}
          value={customerInfo.phone}
          onChangeText={(value) => setCustomerInfo(prev => ({ ...prev, phone: value }))}
          placeholder="Enter your phone number"
          keyboardType="phone-pad"
        />
      </View>
      
      {!user && (
        <View style={styles.guestNotice}>
          <Text style={styles.guestNoticeText}>
            💡 Tip: Sign in to auto-fill this information next time!
          </Text>
          <TouchableOpacity
            style={styles.signInLink}
            onPress={() => navigation.navigate('Login')}
          >
            <Text style={styles.signInLinkText}>Sign In</Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );

  if (cart.items.length === 0 && !showCheckout) {
    return (
      <SafeAreaView style={styles.container}>
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyIcon}>🛒</Text>
          <Text style={styles.emptyTitle}>Your cart is empty</Text>
          <Text style={styles.emptySubtitle}>
            Browse our yoga classes and add them to your cart!
          </Text>
          <TouchableOpacity
            style={styles.browseButton}
            onPress={() => navigation.navigate('ClassList')}
          >
            <Text style={styles.browseButtonText}>Browse Classes</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.scrollView}>
        {cart.items.length > 0 && (
          <>
            <Text style={styles.title}>Shopping Cart ({cart.items.length} items)</Text>
            
            <FlatList
              data={cart.items}
              renderItem={renderCartItem}
              keyExtractor={(item) => item.id.toString()}
              scrollEnabled={false}
            />
          </>
        )}
        
        {showCheckout && renderCheckoutForm()}
      </ScrollView>

      {cart.items.length > 0 && (
        <View style={styles.footer}>
          <View style={styles.totalContainer}>
            <Text style={styles.totalLabel}>Total:</Text>
            <Text style={styles.totalAmount}>${getCartTotal().toFixed(2)}</Text>
          </View>
          
          {!showCheckout ? (
            <TouchableOpacity
              style={styles.checkoutButton}
              onPress={handleProceedToCheckout}
            >
              <Text style={styles.checkoutButtonText}>Proceed to Checkout</Text>
            </TouchableOpacity>
          ) : (
            <View style={styles.checkoutActions}>
              <TouchableOpacity
                style={styles.backButton}
                onPress={() => setShowCheckout(false)}
              >
                <Text style={styles.backButtonText}>Back to Cart</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.confirmButton, loading && styles.confirmButtonDisabled]}
                onPress={handleCheckout}
                disabled={loading}
              >
                <Text style={styles.confirmButtonText}>
                  {loading ? 'Processing...' : 'Confirm Booking'}
                </Text>
              </TouchableOpacity>
            </View>
          )}
        </View>
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F3E5F5',
  },
  scrollView: {
    flex: 1,
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
    padding: 20,
    paddingBottom: 10,
  },
  cartItem: {
    backgroundColor: '#fff',
    marginHorizontal: 20,
    marginVertical: 8,
    padding: 16,
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  itemHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 8,
  },
  className: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
    flex: 1,
  },
  removeButton: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: '#fa7575ff',
    justifyContent: 'center',
    alignItems: 'center',
  },
  removeButtonText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: 'bold',
  },
  itemDetails: {
    fontSize: 14,
    color: '#666',
    marginBottom: 4,
  },
  itemFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 12,
  },
  quantityContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  quantityButton: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#def4f7',
    justifyContent: 'center',
    alignItems: 'center',
  },
  quantityButtonText: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  quantity: {
    fontSize: 16,
    fontWeight: 'bold',
    marginHorizontal: 16,
    color: '#333',
  },
  itemPrice: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#4caf4f',
  },
  checkoutForm: {
    backgroundColor: '#fff',
    margin: 20,
    padding: 20,
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 20,
  },
  autoFillNotice: {
    backgroundColor: '#E8F5E8',
    padding: 12,
    borderRadius: 8,
    marginBottom: 16,
  },
  autoFillText: {
    fontSize: 14,
    color: '#2E7D32',
    fontWeight: '500',
  },
  guestNotice: {
    backgroundColor: '#FFF3E0',
    padding: 12,
    borderRadius: 8,
    marginTop: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  guestNoticeText: {
    fontSize: 14,
    color: '#E65100',
    flex: 1,
    marginRight: 12,
  },
  signInLink: {
    backgroundColor: '#661a72',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 6,
  },
  signInLinkText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '600',
  },
  inputGroup: {
    marginBottom: 16,
  },
  label: {
    fontSize: 14,
    fontWeight: '500',
    color: '#333',
    marginBottom: 8,
  },
  input: {
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    backgroundColor: '#f9f9f9',
  },
  footer: {
    backgroundColor: '#fff',
    padding: 20,
  },
  totalContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  totalLabel: {
    fontSize: 18,
    fontWeight: '500',
    color: '#333',
  },
  totalAmount: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#4caf4f',
  },
  checkoutButton: {
    backgroundColor: '#def4f7',
    paddingVertical: 16,
    borderRadius: 10,
    alignItems: 'center',
  },
  checkoutButtonText: {
    color: '#333',
    fontSize: 16,
    fontWeight: 'bold',
  },
  checkoutActions: {
    flexDirection: 'row',
    gap: 12,
  },
  backButton: {
    flex: 1,
    backgroundColor: '#f0f0f0',
    paddingVertical: 16,
    borderRadius: 10,
    alignItems: 'center',
  },
  backButtonText: {
    color: '#333',
    fontSize: 16,
    fontWeight: '500',
  },
  confirmButton: {
    flex: 2,
    backgroundColor: '#def4f7',
    paddingVertical: 16,
    borderRadius: 10,
    alignItems: 'center',
  },
  confirmButtonDisabled: {
    backgroundColor: '#ccc',
  },
  confirmButtonText: {
    color: '#333',
    fontSize: 16,
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
    marginBottom: 30,
  },
  browseButton: {
    backgroundColor: '#def4f7',
    paddingHorizontal: 30,
    paddingVertical: 15,
    borderRadius: 10,
  },
  browseButtonText: {
    color: '#333',
    fontSize: 16,
    fontWeight: 'bold',
  },
});

export default CartScreen;
