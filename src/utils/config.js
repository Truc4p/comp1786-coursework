// Configuration file for the yoga customer app
export const CONFIG = {
  // Replace this with your actual cloud service URL
  API_BASE_URL: "https://yogaapp-12d2b-default-rtdb.asia-southeast1.firebasedatabase.app/",
  
  // Alternative local development URL
  // API_BASE_URL: 'http://localhost:3000/api',
  
  // App settings
  APP_NAME: 'Yoga Studio',
  VERSION: '1.0.0',
  
  // UI Configuration
  THEME: {
    PRIMARY_COLOR: '#def4f7',
    SECONDARY_COLOR: '#f5f5f5',
    TEXT_COLOR: '#333',
    LIGHT_TEXT_COLOR: '#666',
    SUCCESS_COLOR: '#4CAF50',
    ERROR_COLOR: '#fa7575ff',
    WARNING_COLOR: '#ff9800',
  },
  
  // Feature flags
  FEATURES: {
    ENABLE_PUSH_NOTIFICATIONS: false,
    ENABLE_PAYMENT_INTEGRATION: false,
    ENABLE_SOCIAL_LOGIN: false,
  },
  
  // API endpoints
  ENDPOINTS: {
    YOGA_CLASSES: '/yoga-classes',
    BOOK_CLASS: '/yoga-classes/{id}/book',
    CUSTOMER_BOOKINGS: '/customers/{id}/bookings',
    INSTRUCTORS: '/instructors',
  },
};
