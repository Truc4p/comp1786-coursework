import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Modal,
  SafeAreaView,
  Animated,
} from 'react-native';
import { useAuth } from '../context/AuthContext';
import { useBooking } from '../context/BookingContext';

const HamburgerMenu = ({ visible, onClose, navigation }) => {
  const { user, logout } = useAuth();
  const { clearAllBookings } = useBooking();
  const [slideAnim] = useState(new Animated.Value(-300));

  React.useEffect(() => {
    if (visible) {
      Animated.timing(slideAnim, {
        toValue: 0,
        duration: 300,
        useNativeDriver: true,
      }).start();
    } else {
      Animated.timing(slideAnim, {
        toValue: -300,
        duration: 300,
        useNativeDriver: true,
      }).start();
    }
  }, [visible, slideAnim]);

  const handleMenuItemPress = (screen, params) => {
    onClose();
    setTimeout(() => {
      navigation.navigate(screen, params);
    }, 300);
  };

  const handleLogout = () => {
    onClose();
    setTimeout(async () => {
      // Clear all user data for security
      await clearAllBookings();
      await logout();
      // Navigate to home screen immediately after logout to clear any sensitive data
      if (navigation) {
        navigation.navigate('ClassList');
      }
    }, 300);
  };

  return (
    <Modal
      visible={visible}
      transparent={true}
      animationType="none"
      onRequestClose={onClose}
    >
      <View style={styles.overlay}>
        <Animated.View
          style={[
            styles.menuContainer,
            {
              transform: [{ translateX: slideAnim }],
            },
          ]}
        >
          <SafeAreaView style={styles.menu}>
            <View style={styles.menuHeader}>
              <Text style={styles.menuTitle}>Menu</Text>
              <TouchableOpacity onPress={onClose} style={styles.closeButton}>
                <Text style={styles.closeButtonText}>✕</Text>
              </TouchableOpacity>
            </View>

            {user && (
              <View style={styles.userInfo}>
                <View style={styles.userAvatar}>
                </View>
                <View style={styles.userDetails}>
                  <Text style={styles.userName}>
                    {user.firstName} {user.lastName}
                  </Text>
                  <Text style={styles.userEmail}>{user.email}</Text>
                </View>
              </View>
            )}

            <View style={styles.menuItems}>
              {user ? (
                <>
                  <TouchableOpacity
                    style={styles.menuItem}
                    onPress={() => handleMenuItemPress('Profile')}
                  >
                    <Text style={styles.menuItemText}>My Profile</Text>
                    <Text style={styles.menuItemArrow}>›</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={styles.menuItem}
                    onPress={() => handleMenuItemPress('MyBookings')}
                  >
                    <Text style={styles.menuItemText}>My Bookings</Text>
                    <Text style={styles.menuItemArrow}>›</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={styles.menuItem}
                    onPress={() => handleMenuItemPress('Cart')}
                  >
                    <Text style={styles.menuItemText}>Shopping Cart</Text>
                    <Text style={styles.menuItemArrow}>›</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={[styles.menuItem, styles.logoutItem]}
                    onPress={handleLogout}
                  >
                    <Text style={[styles.menuItemText, styles.logoutText]}>Sign Out</Text>
                    <Text style={styles.menuItemArrow}>›</Text>
                  </TouchableOpacity>
                </>
              ) : (
                <>
                  <TouchableOpacity
                    style={styles.menuItem}
                    onPress={() => handleMenuItemPress('Login')}
                  >
                    <Text style={styles.menuItemText}>Sign In</Text>
                    <Text style={styles.menuItemArrow}>›</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={styles.menuItem}
                    onPress={() => handleMenuItemPress('Register')}
                  >
                    <Text style={styles.menuItemText}>Create Account</Text>
                    <Text style={styles.menuItemArrow}>›</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={styles.menuItem}
                    onPress={() => handleMenuItemPress('Cart')}
                  >
                    <Text style={styles.menuItemText}>Shopping Cart</Text>
                    <Text style={styles.menuItemArrow}>›</Text>
                  </TouchableOpacity>
                </>
              )}
            </View>
          </SafeAreaView>
        </Animated.View>
        <TouchableOpacity style={styles.overlayTouchable} onPress={onClose} />
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  overlayTouchable: {
    position: 'absolute',
    top: 0,
    bottom: 0,
    left: 280,
    right: 0,
  },
  menuContainer: {
    width: 280,
    backgroundColor: '#fff',
    height: '100%',
    shadowColor: '#000',
    shadowOffset: {
      width: 2,
      height: 0,
    },
    shadowOpacity: 0.25,
    shadowRadius: 5,
    elevation: 5,
    position: 'absolute',
    left: 0,
    top: 0,
    bottom: 0,
  },
  menu: {
    flex: 1,
  },
  menuHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 16,
    backgroundColor: '#def4f7',
  },
  menuTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#661a72',
  },
  closeButton: {
    width: 30,
    height: 30,
    borderRadius: 15,
    backgroundColor: 'rgba(255, 255, 255, 0.3)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  closeButtonText: {
    fontSize: 16,
    color: '#661a72',
    fontWeight: 'bold',
  },
  userInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 20,
    backgroundColor: '#F8F8F8',
  },
  userAvatar: {
    width: 50,
    height: 50,
    borderRadius: 25,
    backgroundColor: '#def4f7',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 15,
  },
  userDetails: {
    flex: 1,
  },
  userName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  userEmail: {
    fontSize: 14,
    color: '#666',
    marginTop: 2,
  },
  menuItems: {
    flex: 1,
    paddingTop: 10,
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 16,
  },
  menuItemText: {
    flex: 1,
    fontSize: 16,
    color: '#333',
  },
  menuItemArrow: {
    fontSize: 18,
    color: '#666',
  },
  logoutItem: {
    marginTop: 10,
  },
  logoutText: {
    color: '#E53E3E',
  },
});

export default HamburgerMenu;
