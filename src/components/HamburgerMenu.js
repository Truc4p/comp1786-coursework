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

const HamburgerMenu = ({ visible, onClose, navigation }) => {
  const { user, logout } = useAuth();
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
    setTimeout(() => {
      logout();
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
        <TouchableOpacity style={styles.overlayTouchable} onPress={onClose} />
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
                  <Text style={styles.userAvatarText}>
                    {(user.firstName?.charAt(0) + user.lastName?.charAt(0)).toUpperCase()}
                  </Text>
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
                    <Text style={styles.menuItemIcon}>👤</Text>
                    <Text style={styles.menuItemText}>My Profile</Text>
                    <Text style={styles.menuItemArrow}>›</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={styles.menuItem}
                    onPress={() => handleMenuItemPress('MyBookings')}
                  >
                    <Text style={styles.menuItemIcon}>📅</Text>
                    <Text style={styles.menuItemText}>My Bookings</Text>
                    <Text style={styles.menuItemArrow}>›</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={styles.menuItem}
                    onPress={() => handleMenuItemPress('Cart')}
                  >
                    <Text style={styles.menuItemIcon}>🛒</Text>
                    <Text style={styles.menuItemText}>Shopping Cart</Text>
                    <Text style={styles.menuItemArrow}>›</Text>
                  </TouchableOpacity>

                  <View style={styles.divider} />

                  <TouchableOpacity
                    style={[styles.menuItem, styles.logoutItem]}
                    onPress={handleLogout}
                  >
                    <Text style={styles.menuItemIcon}>👋</Text>
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
                    <Text style={styles.menuItemIcon}>🔐</Text>
                    <Text style={styles.menuItemText}>Sign In</Text>
                    <Text style={styles.menuItemArrow}>›</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={styles.menuItem}
                    onPress={() => handleMenuItemPress('Register')}
                  >
                    <Text style={styles.menuItemIcon}>📝</Text>
                    <Text style={styles.menuItemText}>Create Account</Text>
                    <Text style={styles.menuItemArrow}>›</Text>
                  </TouchableOpacity>

                  <TouchableOpacity
                    style={styles.menuItem}
                    onPress={() => handleMenuItemPress('Cart')}
                  >
                    <Text style={styles.menuItemIcon}>🛒</Text>
                    <Text style={styles.menuItemText}>Shopping Cart</Text>
                    <Text style={styles.menuItemArrow}>›</Text>
                  </TouchableOpacity>
                </>
              )}
            </View>
          </SafeAreaView>
        </Animated.View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    flexDirection: 'row',
  },
  overlayTouchable: {
    flex: 1,
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
    borderBottomWidth: 1,
    borderBottomColor: '#E0E0E0',
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
    borderBottomWidth: 1,
    borderBottomColor: '#E0E0E0',
  },
  userAvatar: {
    width: 50,
    height: 50,
    borderRadius: 25,
    backgroundColor: '#661a72',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 15,
  },
  userAvatarText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 'bold',
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
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  menuItemIcon: {
    fontSize: 20,
    marginRight: 15,
    width: 25,
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
  divider: {
    height: 1,
    backgroundColor: '#E0E0E0',
    marginVertical: 10,
  },
  logoutItem: {
    marginTop: 10,
  },
  logoutText: {
    color: '#E53E3E',
  },
});

export default HamburgerMenu;
