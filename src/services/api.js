// API service for connecting to the yoga class cloud service
import { CONFIG } from '../utils/config';

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
    //   console.log('Making API request to:', url);
      const response = await fetch(url, config);
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const data = await response.json();
    //   console.log('Firebase response data:', data);
      
      // Handle Firebase null response
      if (data === null || data === undefined) {
        console.log('Firebase returned null data');
        return [];
      }
      
      // If data is an object (Firebase format), convert to array
      if (typeof data === 'object' && !Array.isArray(data)) {
        const arrayData = Object.keys(data).map(key => ({ 
          id: key, 
          ...data[key] 
        }));
        // console.log('Converted Firebase object to array:', arrayData);
        return arrayData;
      }
      
    //   console.log('Returning data as-is:', data);
      return data;
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

    // Map admin app data structure to customer format
  mapAdminDataToCustomerFormat(adminClasses) {
    return adminClasses.map(adminClass => {
      // Get the first instance date if available, otherwise use a default
      let classDate = new Date().toISOString().split('T')[0]; // Default to today
      let instructor = 'Instructor'; // Default instructor
      
      // If there are instances, use the first one's date and instructor
      if (adminClass.instances && adminClass.instances.length > 0) {
        const firstInstance = adminClass.instances[0];
        if (firstInstance.date) {
          classDate = firstInstance.date; // Keep the DD/MM/YYYY format from admin
        }
        if (firstInstance.instructor) {
          instructor = firstInstance.instructor;
        }
      }
      
      return {
        id: adminClass.id,
        name: adminClass.classType || 'Yoga Class',
        instructor: instructor,
        date: classDate, // Use the actual date from instance
        time: adminClass.time || '09:00',
        duration: adminClass.duration || 60,
        capacity: adminClass.capacity || 10,
        availableSpots: Math.max(0, (adminClass.capacity || 10) - 2), // Assume some bookings
        price: adminClass.price || 25,
        level: adminClass.difficulty || 'All Levels',
        dayOfWeek: adminClass.dayOfWeek || 'Monday',
        description: adminClass.description || 'A wonderful yoga class experience.',
        image: null,
        locationAddress: adminClass.locationAddress,
        latitude: adminClass.latitude,
        longitude: adminClass.longitude,
        instances: adminClass.instances || []
      };
    });
  }

  // Get all yoga classes
  async getYogaClasses() {
    try {
      // First try the nested structure from your admin app
      const response = await this.request('/sync/yogaClasses');
      if (response && response.length > 0) {
        // console.log('Found admin app data:', response);
        // Map the admin app data structure to customer app format
        const mappedData = this.mapAdminDataToCustomerFormat(response);
        // console.log('Mapped data for customer app:', mappedData);
        return mappedData;
      }
    } catch (error) {
      console.log('Trying sync/yogaClasses path failed, trying direct yogaClasses path');
    }
    
    // Fallback to direct yogaClasses path
    return this.request('/yogaClasses');
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
    try {
      const response = await this.request('/bookings', {
        method: 'POST',
        body: JSON.stringify(bookingData),
      });
      
      // Firebase POST returns an object with the new key
      if (response && response.name) {
        return { success: true, id: response.name };
      }
      
      return { success: true, id: `booking_${Date.now()}` };
    } catch (error) {
      console.error('Error creating booking:', error);
      return { success: false, error: error.message };
    }
  }

  // Update booking status
  async updateBookingStatus(bookingId, status) {
    try {
      const updateData = {
        status,
        updatedDate: new Date().toISOString()
      };
      
      return this.request(`/bookings/${bookingId}`, {
        method: 'PATCH',
        body: JSON.stringify(updateData),
      });
    } catch (error) {
      console.error('Error updating booking status:', error);
      return { success: false, error: error.message };
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
}

export default new ApiService();
