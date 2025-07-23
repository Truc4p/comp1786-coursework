// Utility functions for date and time formatting
import AsyncStorage from '@react-native-async-storage/async-storage';

export const formatTime = (time) => {
  if (!time) return '';
  
  // Handle different time formats
  if (typeof time === 'string') {
    // Handle admin app format like "11:08 AM"
    if (time.includes('AM') || time.includes('PM')) {
      return time;
    }
    
    // Handle 24-hour format like "09:00"
    const date = new Date(`2000-01-01T${time}`);
    if (!isNaN(date.getTime())) {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
  }
  
  return time;
};

export const formatDate = (date) => {
  if (!date) return '';
  
  // Handle admin app date format like "25/07/2025" - keep the original format
  if (typeof date === 'string' && date.includes('/')) {
    const parts = date.split('/');
    if (parts.length === 3) {
      // Keep the original DD/MM/YYYY format from admin app
      return date; // Return as-is: "25/07/2025"
    }
  }
  
  // Handle other date formats and convert to DD/MM/YYYY
  const dateObj = new Date(date);
  if (!isNaN(dateObj.getTime())) {
    // Convert to DD/MM/YYYY format
    const day = dateObj.getDate().toString().padStart(2, '0');
    const month = (dateObj.getMonth() + 1).toString().padStart(2, '0');
    const year = dateObj.getFullYear();
    return `${day}/${month}/${year}`;
  }
  
  return date;
};

export const getDayOfWeek = (date) => {
  const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  const dateObj = new Date(date);
  return days[dateObj.getDay()];
};

export const getTimeOfDay = (time) => {
  if (!time) return '';
  
  // Handle admin app format like "11:08 AM"
  if (time.includes('AM') || time.includes('PM')) {
    if (time.includes('AM')) {
      const hour = parseInt(time.split(':')[0]);
      return hour < 12 ? 'Morning' : 'Morning';
    } else {
      return 'Afternoon'; // PM times
    }
  }
  
  // Handle 24-hour format
  const hour = parseInt(time.split(':')[0]);
  
  if (hour < 12) return 'Morning';
  if (hour < 17) return 'Afternoon';
  return 'Evening';
};

export const filterClassesBySearchCriteria = (classes, searchCriteria) => {
  const { dayOfWeek, timeOfDay } = searchCriteria;
  
  return classes.filter(yogaClass => {
    let matchesDay = true;
    let matchesTime = true;
    
    if (dayOfWeek && dayOfWeek !== 'All') {
      matchesDay = yogaClass.dayOfWeek?.toLowerCase() === dayOfWeek.toLowerCase();
    }
    
    if (timeOfDay && timeOfDay !== 'All') {
      const classTimeOfDay = getTimeOfDay(yogaClass.time);
      matchesTime = classTimeOfDay === timeOfDay;
    }
    
    return matchesDay && matchesTime;
  });
};

// App launch utilities
export const resetFirstLaunchFlag = async () => {
  try {
    await AsyncStorage.removeItem('hasLaunchedBefore');
    console.log('First launch flag reset - app will show demo on next launch');
    return true;
  } catch (error) {
    console.error('Error resetting first launch flag:', error);
    return false;
  }
};

export const checkIsFirstLaunch = async () => {
  try {
    const hasLaunched = await AsyncStorage.getItem('hasLaunchedBefore');
    return hasLaunched === null;
  } catch (error) {
    console.error('Error checking first launch:', error);
    return true; // Default to true on error
  }
};
