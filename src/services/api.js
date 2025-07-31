// API service for connecting to the yoga class cloud service
import { CONFIG } from '../utils/config';
import { hashPassword, verifyPassword } from '../utils/password';

const API_BASE_URL = CONFIG.API_BASE_URL;

class ApiService {
  constructor() {
    this.baseURL = API_BASE_URL.endsWith('/') ? API_BASE_URL.slice(0, -1) : API_BASE_URL;
  }

  async request(endpoint, options = {}) {
    // For Firebase Realtime Database, we need to add .json to endpoints
    const firebaseEndpoint = endpoint.endsWith('.json') ? endpoint : `${endpoint}.json`;
    const url = `${this.baseURL}${firebaseEndpoint}`;
    
    const config = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      ...options,
    };

    try {
      // console.log('Making API request to:', url, 'with method:', options.method || 'GET');
      const response = await fetch(url, config);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
      // console.log('Firebase response data:', data);
      
      // Handle Firebase null response
      if (data === null || data === undefined) {
        // console.log('Firebase returned null data');
        return [];
      }
      
      // For POST requests, Firebase returns {name: "id"}, don't convert to array
      if (options.method === 'POST' && data && typeof data === 'object' && data.name) {
        // console.log('POST response, returning as-is');
        return data;
      }
      
      // If data is an array, filter out null values and ensure proper structure
      if (Array.isArray(data)) {
        const filteredData = data.filter(item => item !== null && item !== undefined);
        // console.log('Filtered Firebase array data:', filteredData);
        return filteredData;
      }
      
      // If data is an object (Firebase format) and NOT a POST response, convert to array
      if (typeof data === 'object' && !Array.isArray(data) && options.method !== 'POST') {
        const arrayData = Object.keys(data).map(key => ({ 
          firebaseKey: key, // Store Firebase key separately to avoid conflicts with internal id
          ...data[key] 
        }));
        // console.log('Converted Firebase object to array:', arrayData);
        return arrayData;
      }
      
      // console.log('Returning data as-is:', data);
      return data;
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

    // Map yoga class data to customer format (simplified - no instances)
  mapAdminDataToCustomerFormat(yogaClasses) {
    return yogaClasses.map(yogaClass => {
      return {
        id: yogaClass.id || Math.random().toString(),
        name: yogaClass.classType || 'Yoga Class',
        date: yogaClass.dayOfWeek || 'Monday', // Show day of week
        time: yogaClass.time || '09:00',
        duration: yogaClass.duration || 60,
        capacity: yogaClass.capacity || 10,
        availableSpots: Math.max(0, (yogaClass.capacity || 10) - Math.floor(Math.random() * 5)),
        price: yogaClass.price || 25,
        level: yogaClass.difficulty || 'All Levels',
        dayOfWeek: yogaClass.dayOfWeek || 'Monday',
        description: yogaClass.description || 'A wonderful yoga class experience.',
        image: null,
        locationAddress: yogaClass.locationAddress,
        latitude: yogaClass.latitude,
        longitude: yogaClass.longitude
      };
    });
  }

  // Get all yoga classes
  async getYogaClasses() {
    try {
      // Fetch only yoga classes (not class instances)
      const yogaClassesResponse = await this.request('/yoga-classes');
      
      if (yogaClassesResponse && yogaClassesResponse.length > 0) {
        console.log('Found Firebase yoga classes:', yogaClassesResponse);
        
        // Remove duplicates based on ID before mapping
        const uniqueYogaClasses = yogaClassesResponse.filter((yogaClass, index, self) => {
          const firstIndex = self.findIndex(c => c.id === yogaClass.id);
          if (firstIndex !== index) {
            console.log('🔄 Removing duplicate yoga class with ID:', yogaClass.id);
          }
          return firstIndex === index;
        });
        
        console.log('📊 Original classes:', yogaClassesResponse.length, 'Unique classes:', uniqueYogaClasses.length);
        
        // Map the Firebase data structure to customer app format
        const mappedData = uniqueYogaClasses.map(yogaClass => {
          return {
            id: yogaClass.id || Math.random().toString(),
            name: yogaClass.classType || 'Yoga Class',
            date: yogaClass.dayOfWeek || 'Monday', // Show day of week instead of date
            time: yogaClass.time || '09:00',
            duration: yogaClass.duration || 60,
            capacity: yogaClass.capacity || 10,
            availableSpots: Math.max(0, (yogaClass.capacity || 10) - Math.floor(Math.random() * 5)), // Random available spots
            price: yogaClass.price || 25,
            level: yogaClass.difficulty || 'All Levels',
            dayOfWeek: yogaClass.dayOfWeek || 'Monday',
            description: yogaClass.description || 'A wonderful yoga class experience.',
            image: null,
            locationAddress: yogaClass.locationAddress,
            latitude: yogaClass.latitude,
            longitude: yogaClass.longitude
          };
        });
        
        // Final deduplication check based on mapped data
        const finalUniqueData = mappedData.filter((item, index, self) => {
          const firstIndex = self.findIndex(c => c.id === item.id);
          if (firstIndex !== index) {
            console.log('🔄 Removing duplicate mapped class with ID:', item.id, item.name);
          }
          return firstIndex === index;
        });
        
        console.log('📊 Mapped classes:', mappedData.length, 'Final unique classes:', finalUniqueData.length);
        console.log('🎯 Final mapped data for customer app:', finalUniqueData);
        return finalUniqueData;
      }
      
      // If no data found, return empty array (don't show demo mode)
      return [];
      
    } catch (error) {
      console.error('Error fetching yoga classes:', error);
      throw error; // Let the calling code handle the error
    }
  }

  // Get yoga classes by day of the week
  async getYogaClassesByDay(dayOfWeek) {
    const classes = await this.getYogaClasses();
    return classes.filter(yogaClass => 
      yogaClass.dayOfWeek?.toLowerCase() === dayOfWeek.toLowerCase()
    );
  }

  // Get yoga classes by time
  async getYogaClassesByTime(timeOfDay) {
    const classes = await this.getYogaClasses();
    return classes.filter(yogaClass => {
      const hour = parseInt(yogaClass.time?.split(':')[0] || '0');
      let classTimeOfDay = '';
      
      if (hour < 12) classTimeOfDay = 'Morning';
      else if (hour < 17) classTimeOfDay = 'Afternoon';
      else classTimeOfDay = 'Evening';
      
      return classTimeOfDay === timeOfDay;
    });
  }

  // Get yoga classes by day and time
  async getYogaClassesByDayAndTime(dayOfWeek, timeOfDay) {
    const classes = await this.getYogaClasses();
    return classes.filter(yogaClass => {
      const matchesDay = yogaClass.dayOfWeek?.toLowerCase() === dayOfWeek.toLowerCase();
      
      const hour = parseInt(yogaClass.time?.split(':')[0] || '0');
      let classTimeOfDay = '';
      
      if (hour < 12) classTimeOfDay = 'Morning';
      else if (hour < 17) classTimeOfDay = 'Afternoon';
      else classTimeOfDay = 'Evening';
      
      const matchesTime = classTimeOfDay === timeOfDay;
      
      return matchesDay && matchesTime;
    });
  }

  // Get specific yoga class details
  async getYogaClassDetails(classId) {
    return this.request(`/yogaClasses/${classId}`);
  }

  // Get class instances for a specific yoga class
  async getClassInstances(classId) {
    try {
      console.log('🔍 Fetching class instances for classId:', classId);
      const classInstancesResponse = await this.request('/class-instances');
      
      console.log('📊 Raw class instances response:', classInstancesResponse);
      console.log('📊 Number of total instances:', classInstancesResponse?.length || 0);
      
      if (classInstancesResponse && classInstancesResponse.length > 0) {
        // Log all instance data for debugging
        console.log('🔍 All instances:', classInstancesResponse.map(instance => ({
          instanceId: instance.id,
          yogaClassId: instance.yogaClassId,
          classId: instance.classId,
          date: instance.date,
          instructor: instance.instructor
        })));
        
        // Filter instances that belong to this specific class
        const classInstances = classInstancesResponse.filter(instance => {
          // Firebase uses 'yogaClassId' field, not 'classId'
          // Convert both to strings for comparison to handle type mismatches
          const instanceYogaClassId = String(instance.yogaClassId || '');
          const instanceClassId = String(instance.classId || '');
          const targetClassId = String(classId || '');
          
          const matches = instanceYogaClassId === targetClassId || instanceClassId === targetClassId;
          console.log(`🔍 Instance ${instance.id}: yogaClassId=${instance.yogaClassId}, classId=${instance.classId}, target=${classId}, matches=${matches}`);
          return matches;
        });
        
        console.log(`✅ Found ${classInstances.length} instances for class ${classId}`);
        console.log('📋 Matching instances:', classInstances);
        
        if (classInstances.length > 0) {
          // Sort instances by date
          const sortedInstances = classInstances.sort((a, b) => {
            const dateA = new Date(a.date.split('/').reverse().join('-')); // Convert DD/MM/YYYY to YYYY-MM-DD
            const dateB = new Date(b.date.split('/').reverse().join('-'));
            return dateA - dateB;
          });
          
          console.log('📅 Sorted instances:', sortedInstances);
          return sortedInstances;
        }
      }
      
      console.log('❌ No instances found or no data returned');
      return [];
    } catch (error) {
      console.error('❌ Error fetching class instances:', error);
      return [];
    }
  }

  // Book a yoga class
  async bookYogaClass(classId, customerInfo) {
    // For Firebase, we'll create a booking entry
    const bookingData = {
      classId,
      customerInfo,
      bookingDate: new Date().toISOString(),
      status: 'confirmed'
    };
    
    return this.request('/bookings', {
      method: 'POST',
      body: JSON.stringify(bookingData),
    });
  }

  // Create a new booking with multiple classes (shopping cart)
  async createBooking(bookingData) {
    // console.log('ApiService.createBooking called with:', bookingData);
    
    try {
      // console.log('Making request to /bookings...');
      const response = await this.request('/bookings', {
        method: 'POST',
        body: JSON.stringify(bookingData),
      });
      
      // console.log('Firebase response:', response);
      
      // Firebase POST returns an object with the new key
      if (response && response.name) {
        // console.log('Booking created successfully with ID:', response.name);
        return { success: true, id: response.name };
      }
      
      // console.log('No response.name, using fallback ID');
      return { success: true, id: `booking_${Date.now()}` };
    } catch (error) {
      console.error('Error creating booking:', error);
      return { success: false, error: error.message };
    }
  }

  // Update booking status
  async updateBookingStatus(bookingId, status) {
    try {
      console.log('🔄 Updating booking status in Firebase:', bookingId, 'to', status);
      
      // First, get all bookings and find the one we need to update
      console.log('📥 Fetching all bookings to find the target booking...');
      const allBookings = await this.request('/bookings');
      
      if (!allBookings || !Array.isArray(allBookings)) {
        throw new Error('Failed to fetch bookings from Firebase');
      }
      
      // Find the booking by our internal ID
      const currentBooking = allBookings.find(booking => booking.id === bookingId);
      
      if (!currentBooking) {
        throw new Error(`Booking with ID ${bookingId} not found in Firebase`);
      }
      
      console.log('📋 Current booking data found:', currentBooking);
      
      // Get the Firebase key for this booking
      const firebaseKey = currentBooking.firebaseKey || currentBooking.id;
      console.log('🔑 Using Firebase key:', firebaseKey);
      
      // Preserve all existing data and only update status and timestamp
      const updateData = {
        ...currentBooking,
        status,
        updatedDate: new Date().toISOString(),
        // Add cancellation timestamp if status is cancelled
        ...(status === 'cancelled' && { cancelledDate: new Date().toISOString() })
      };
      
      // Remove firebaseKey from the data we send (it's just metadata)
      if (updateData.firebaseKey) {
        delete updateData.firebaseKey;
      }
      
      console.log('📤 Sending complete update data to preserve all fields');
      
      const response = await this.request(`/bookings/${firebaseKey}`, {
        method: 'PUT', // Use PUT to replace entire object
        body: JSON.stringify(updateData),
      });
      
      console.log('📥 Firebase update response:', response);
      
      // Check if response indicates success
      if (response !== null) {
        console.log('✅ Firebase booking status update successful with all data preserved');
        return { success: true, response };
      } else {
        console.log('❌ Firebase update failed - null response');
        return { success: false, error: 'Firebase returned null response' };
      }
      
    } catch (error) {
      console.error('❌ Error updating booking status:', error);
      return { success: false, error: error.message };
    }
  }

  // Get all bookings from Firebase
  async getAllBookings() {
    try {
      const response = await this.request('/bookings');
      return response || [];
    } catch (error) {
      console.error('Error fetching all bookings:', error);
      return [];
    }
  }

  // Get customer bookings by email
  async getCustomerBookingsByEmail(email) {
    try {
      const bookings = await this.request('/bookings');
      if (!bookings || bookings.length === 0) {
        return [];
      }
      
      return bookings.filter(booking => 
        booking.customerInfo && 
        booking.customerInfo.email && 
        booking.customerInfo.email.toLowerCase() === email.toLowerCase()
      );
    } catch (error) {
      console.error('Error getting customer bookings:', error);
      return [];
    }
  }

  // Get customer bookings
  async getCustomerBookings(customerId) {
    return this.request(`/bookings`);
  }

  // Authentication methods
  async login(email, password) {
    try {
      // console.log('Attempting login for:', email);
      
      // Check if user exists in Firebase
      const response = await this.request('/users');
      // console.log('Users response:', response);
      
      let users = [];
      
      // Handle Firebase response format
      if (Array.isArray(response)) {
        users = response;
      } else if (response && typeof response === 'object' && response !== null) {
        // Convert Firebase object format to array
        users = Object.keys(response).map(key => ({
          firebaseKey: key,
          ...response[key]
        }));
      }
      
      // console.log('Processed users array:', users);
      
      // Find user by email first
      const user = users.find(u => u.email === email);
      // console.log('Found user by email:', user ? 'Yes' : 'No');
      
      if (user) {
        // Verify password using hash comparison
        const isPasswordValid = verifyPassword(password, user.password, user.salt);
        // console.log('Password verification result:', isPasswordValid);
        
        if (isPasswordValid) {
          // Remove password and salt from response
          const { password: _, salt: __, ...userWithoutPassword } = user;
          // console.log('Login successful for user:', userWithoutPassword.email);
          return {
            success: true,
            user: userWithoutPassword,
            token: `token_${user.id || user.firebaseKey}_${Date.now()}`,
          };
        } else {
          // console.log('Password verification failed');
          return {
            success: false,
            message: 'Invalid email or password',
          };
        }
      } else {
        // console.log('No user found with matching email');
        return {
          success: false,
          message: 'Invalid email or password',
        };
      }
    } catch (error) {
      console.error('Login error:', error);
      return {
        success: false,
        message: error.message || 'Login failed',
      };
    }
  }

  async register(userData) {
    try {
      // console.log('Attempting to register user:', userData.email);
      
      // Check if user already exists
      const response = await this.request('/users');
      // console.log('Existing users response:', response);
      
      let users = [];
      
      // Handle Firebase response format
      if (Array.isArray(response)) {
        users = response;
      } else if (response && typeof response === 'object' && response !== null) {
        // Convert Firebase object format to array
        users = Object.keys(response).map(key => ({
          firebaseKey: key,
          ...response[key]
        }));
      }
      
      // Check for existing user
      const existingUser = users.find(u => u.email === userData.email);
      
      if (existingUser) {
        // console.log('User already exists with email:', userData.email);
        return {
          success: false,
          message: 'An account with this email already exists',
        };
      }
      
      // Create new user
      const { hashedPassword, salt } = hashPassword(userData.password);
      const newUser = {
        ...userData,
        password: hashedPassword,
        salt: salt,
        id: `user_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      
      // console.log('Creating new user:', newUser);
      
      // Save to Firebase - Firebase will auto-generate a key
      const saveResponse = await this.request('/users', {
        method: 'POST',
        body: JSON.stringify(newUser),
      });
      
      // console.log('Firebase save response:', saveResponse);
      
      if (saveResponse) {
        // Remove password and salt from response
        const { password: _, salt: __, ...userWithoutPassword } = newUser;
        // console.log('Registration successful for user:', userWithoutPassword.email);
        return {
          success: true,
          user: userWithoutPassword,
          token: `token_${newUser.id}_${Date.now()}`,
        };
      } else {
        // console.log('Failed to save user to Firebase');
        return {
          success: false,
          message: 'Failed to create account',
        };
      }
    } catch (error) {
      console.error('Registration error:', error);
      return {
        success: false,
        message: error.message || 'Registration failed',
      };
    }
  }

  async updateProfile(userId, profileData) {
    try {
      const updatedData = {
        ...profileData,
        updatedAt: new Date().toISOString(),
      };
      
      // In Firebase, we need to update the specific user
      const response = await this.request(`/users/${userId}`, {
        method: 'PATCH',
        body: JSON.stringify(updatedData),
      });
      
      if (response) {
        return {
          success: true,
          user: { ...profileData, id: userId },
        };
      } else {
        return {
          success: false,
          message: 'Failed to update profile',
        };
      }
    } catch (error) {
      console.error('Profile update error:', error);
      return {
        success: false,
        message: error.message || 'Profile update failed',
      };
    }
  }

  // Check if email already exists (excluding current user)
  async checkEmailExists(email, excludeUserId = null) {
    try {
      const response = await this.request('/users');
      let users = [];
      
      // Handle Firebase response format
      if (Array.isArray(response)) {
        users = response;
      } else if (response && typeof response === 'object' && response !== null) {
        users = Object.keys(response).map(key => ({
          firebaseKey: key,
          ...response[key]
        }));
      }
      
      // Find user with the same email (excluding current user)
      const existingUser = users.find(u => 
        u.email && u.email.toLowerCase() === email.toLowerCase() && 
        (u.id || u.firebaseKey) !== excludeUserId
      );
      
      return existingUser ? true : false;
    } catch (error) {
      console.error('Error checking email exists:', error);
      return false; // Return false on error to not block valid updates
    }
  }

  // Check if phone number already exists (excluding current user)
  async checkPhoneExists(phone, excludeUserId = null) {
    try {
      const response = await this.request('/users');
      let users = [];
      
      // Handle Firebase response format
      if (Array.isArray(response)) {
        users = response;
      } else if (response && typeof response === 'object' && response !== null) {
        users = Object.keys(response).map(key => ({
          firebaseKey: key,
          ...response[key]
        }));
      }
      
      // Clean and normalize phone numbers for comparison
      const cleanPhone = phone.replace(/[\s\-\(\)\+]/g, '');
      
      // Find user with the same phone number (excluding current user)
      const existingUser = users.find(u => {
        if (!u.phone) return false;
        const cleanExistingPhone = u.phone.replace(/[\s\-\(\)\+]/g, '');
        return cleanExistingPhone === cleanPhone && 
               (u.id || u.firebaseKey) !== excludeUserId;
      });
      
      return existingUser ? true : false;
    } catch (error) {
      console.error('Error checking phone exists:', error);
      return false; // Return false on error to not block valid updates
    }
  }
}

export default new ApiService();
