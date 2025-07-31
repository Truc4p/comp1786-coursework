import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  SafeAreaView,
  Alert,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import LoadingSpinner from '../components/LoadingSpinner';
import { useAuth } from '../context/AuthContext';
import ApiService from '../services/api';

const ProfileScreen = ({ navigation }) => {
  const { user, updateProfile, logout } = useAuth();
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(false);
  const [validating, setValidating] = useState(false);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
  });

  useEffect(() => {
    if (user) {
      setFormData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        email: user.email || '',
        phone: user.phone || '',
      });
    }
  }, [user]);

  const handleInputChange = (field, value) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const validateForm = async () => {
    const { firstName, lastName, email, phone } = formData;

    if (!firstName.trim() || !lastName.trim() || !email.trim() || !phone.trim()) {
      Alert.alert('Error', 'Please fill in all fields');
      return false;
    }

    if (!isValidEmail(email)) {
      Alert.alert('Error', 'Please enter a valid email address');
      return false;
    }

    if (!isValidPhone(phone)) {
      Alert.alert('Error', 'Please enter a valid phone number');
      return false;
    }

    // Check if email or phone changed from original values
    const emailChanged = user.email !== email;
    const phoneChanged = user.phone !== phone;

    if (emailChanged || phoneChanged) {
      try {
        // Check if email already exists (excluding current user)
        if (emailChanged) {
          const emailExists = await ApiService.checkEmailExists(email, user.id || user.firebaseKey);
          if (emailExists) {
            Alert.alert('Error', 'This email address is already registered by another user');
            return false;
          }
        }

        // Check if phone already exists (excluding current user)
        if (phoneChanged) {
          const phoneExists = await ApiService.checkPhoneExists(phone, user.id || user.firebaseKey);
          if (phoneExists) {
            Alert.alert('Error', 'This phone number is already registered by another user');
            return false;
          }
        }
      } catch (error) {
        console.error('Validation error:', error);
        Alert.alert('Error', 'Unable to validate email/phone. Please try again.');
        return false;
      }
    }

    return true;
  };

  const isValidEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const isValidPhone = (phone) => {
    // Remove all spaces, dashes, parentheses, and other formatting
    const cleanPhone = phone.replace(/[\s\-\(\)\+]/g, '');
    
    // Check if it's between 7 and 15 digits (international standard)
    const phoneRegex = /^\d{7,15}$/;
    return phoneRegex.test(cleanPhone);
  };

  const handleSaveProfile = async () => {
    // Show loading for validation
    setValidating(true);
    
    const isValid = await validateForm();
    setValidating(false);
    
    if (!isValid) {
      return;
    }

    setLoading(true);
    try {
      await updateProfile(formData);
      setEditing(false);
      Alert.alert('Success', 'Profile updated successfully!');
    } catch (error) {
      console.error('Profile update error:', error);
      Alert.alert('Error', 'Failed to update profile. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    Alert.alert(
      'Sign Out',
      'Are you sure you want to sign out?',
      [
        { text: 'Cancel', style: 'cancel' },
        { 
          text: 'Sign Out', 
          style: 'destructive', 
          onPress: async () => {
            await logout();
            // Navigate to home immediately after logout
            navigation.navigate('ClassList');
          }
        },
      ]
    );
  };

  const cancelEditing = () => {
    setEditing(false);
    // Reset form data to original user data
    if (user) {
      setFormData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        email: user.email || '',
        phone: user.phone || '',
      });
    }
  };

  if (loading) {
    return <LoadingSpinner message="Updating profile..." />;
  }

  if (validating) {
    return <LoadingSpinner message="Validating email and phone..." />;
  }

  if (!user) {
    return (
      <SafeAreaView style={styles.container}>
        <View style={styles.notLoggedInContainer}>
          <Text style={styles.notLoggedInIcon}>🧘‍♀️</Text>
          <Text style={styles.notLoggedInTitle}>Not Signed In</Text>
          <Text style={styles.notLoggedInSubtitle}>
            Please sign in to view and edit your profile
          </Text>
          <TouchableOpacity
            style={styles.signInButton}
            onPress={() => navigation.navigate('Login')}
          >
            <Text style={styles.signInButtonText}>Sign In</Text>
          </TouchableOpacity>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.keyboardContainer}
      >
        <ScrollView contentContainerStyle={styles.scrollContainer}>
          <View style={styles.header}>
            <View style={styles.avatarContainer}></View>
            <Text style={styles.nameText}>
              {formData.firstName} {formData.lastName}
            </Text>
            <Text style={styles.emailText}>{formData.email}</Text>
          </View>

          <View style={styles.form}>
            <View style={styles.row}>
              <View style={[styles.inputContainer, styles.halfWidth]}>
                <Text style={styles.label}>First Name</Text>
                <TextInput
                  style={[styles.input, !editing && styles.inputDisabled]}
                  value={formData.firstName}
                  onChangeText={(value) => handleInputChange('firstName', value)}
                  placeholder="First name"
                  autoCapitalize="words"
                  editable={editing}
                />
              </View>
              <View style={[styles.inputContainer, styles.halfWidth]}>
                <Text style={styles.label}>Last Name</Text>
                <TextInput
                  style={[styles.input, !editing && styles.inputDisabled]}
                  value={formData.lastName}
                  onChangeText={(value) => handleInputChange('lastName', value)}
                  placeholder="Last name"
                  autoCapitalize="words"
                  editable={editing}
                />
              </View>
            </View>

            <View style={styles.inputContainer}>
              <Text style={styles.label}>Email</Text>
              <TextInput
                style={[styles.input, !editing && styles.inputDisabled]}
                value={formData.email}
                onChangeText={(value) => handleInputChange('email', value)}
                placeholder="Enter your email"
                keyboardType="email-address"
                autoCapitalize="none"
                autoCorrect={false}
                editable={editing}
              />
            </View>

            <View style={styles.inputContainer}>
              <Text style={styles.label}>Phone Number</Text>
              <TextInput
                style={[styles.input, !editing && styles.inputDisabled]}
                value={formData.phone}
                onChangeText={(value) => handleInputChange('phone', value)}
                placeholder="Enter your phone number"
                keyboardType="phone-pad"
                editable={editing}
              />
            </View>

            {editing ? (
              <View style={styles.buttonRow}>
                <TouchableOpacity
                  style={[styles.button, styles.cancelButton]}
                  onPress={cancelEditing}
                >
                  <Text style={styles.cancelButtonText}>Cancel</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.button, styles.saveButton, (loading || validating) && styles.saveButtonDisabled]}
                  onPress={handleSaveProfile}
                  disabled={loading || validating}
                >
                  <Text style={styles.saveButtonText}>
                    {validating ? 'Validating...' : loading ? 'Saving...' : 'Save Changes'}
                  </Text>
                </TouchableOpacity>
              </View>
            ) : (
              <TouchableOpacity
                style={styles.editButton}
                onPress={() => setEditing(true)}
              >
                <Text style={styles.editButtonText}>Edit Profile</Text>
              </TouchableOpacity>
            )}
          </View>

          <View style={styles.actions}>
            <TouchableOpacity
              style={styles.actionButton}
              onPress={() => navigation.navigate('MyBookings')}
            >
              <Text style={styles.actionButtonIcon}>📅</Text>
              <Text style={styles.actionButtonText}>My Bookings</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.actionButton}
              onPress={() => navigation.navigate('Cart')}
            >
              <Text style={styles.actionButtonIcon}>🛒</Text>
              <Text style={styles.actionButtonText}>Shopping Cart</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[styles.actionButton, styles.logoutButton]}
              onPress={handleLogout}
            >
              <Text style={styles.actionButtonIcon}>👋</Text>
              <Text style={[styles.actionButtonText, styles.logoutText]}>Sign Out</Text>
            </TouchableOpacity>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F3E5F5',
  },
  keyboardContainer: {
    flex: 1,
  },
  scrollContainer: {
    flexGrow: 1,
    paddingHorizontal: 20,
    paddingVertical: 20,
  },
  header: {
    alignItems: 'center',
    marginBottom: 30,
  },
  avatarContainer: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: '#def4f7',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 16,
  },
  nameText: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 4,
  },
  emailText: {
    fontSize: 16,
    color: '#666',
  },
  form: {
    marginBottom: 30,
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  inputContainer: {
    marginBottom: 16,
  },
  halfWidth: {
    width: '48%',
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
    marginBottom: 6,
  },
  input: {
    backgroundColor: '#fff',
    borderRadius: 10,
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 14,
  },
  inputDisabled: {
    backgroundColor: '#fff',
    color: '#666',
  },
  buttonRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 20,
  },
  button: {
    flex: 1,
    paddingVertical: 14,
    borderRadius: 10,
    alignItems: 'center',
  },
  cancelButton: {
    backgroundColor: '#def4f7',
    marginRight: 10,
  },
  cancelButtonText: {
    color: '#661a72',
    fontSize: 16,
    fontWeight: '600',
  },
  saveButton: {
    backgroundColor: '#def4f7',
    marginLeft: 10,
  },
  saveButtonDisabled: {
    backgroundColor: '#e0e0e0',
    opacity: 0.6,
  },
  saveButtonText: {
    color: '#661a72',
    fontSize: 16,
    fontWeight: '600',
  },
  editButton: {
    backgroundColor: '#def4f7',
    paddingVertical: 14,
    borderRadius: 10,
    alignItems: 'center',
    marginTop: 20,
  },
  editButtonText: {
    color: '#661a72',
    fontSize: 16,
    fontWeight: '600',
  },
  actions: {
    gap: 12,
  },
  actionButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    paddingHorizontal: 20,
    paddingVertical: 16,
    borderRadius: 12,
  },
  actionButtonIcon: {
    fontSize: 20,
    marginRight: 16,
  },
  actionButtonText: {
    fontSize: 16,
    fontWeight: '500',
    color: '#333',
  },
  logoutButton: {
    backgroundColor: '#FFF5F5',
  },
  logoutText: {
    color: '#E53E3E',
  },
  notLoggedInContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 40,
  },
  notLoggedInIcon: {
    fontSize: 80,
    marginBottom: 24,
  },
  notLoggedInTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 12,
  },
  notLoggedInSubtitle: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
    marginBottom: 30,
    lineHeight: 24,
  },
  signInButton: {
    backgroundColor: '#def4f7',
    paddingHorizontal: 32,
    paddingVertical: 16,
    borderRadius: 12,
  },
  signInButtonText: {
    color: '#661a72',
    fontSize: 16,
    fontWeight: '600',
  },
});

export default ProfileScreen;
