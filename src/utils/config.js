// Configuration file for the yoga customer app

// Configuration validation function for debugging
export const validateConfig = () => {
  const issues = [];
  
  if (!CONFIG.API_BASE_URL) {
    issues.push('API_BASE_URL is not set');
  } else if (!CONFIG.API_BASE_URL.includes('firebasedatabase.app')) {
    issues.push('API_BASE_URL does not appear to be a Firebase URL');
  }
  
  if (issues.length > 0) {
    console.warn('⚠️ Configuration Issues Found:');
    issues.forEach(issue => console.warn(`  - ${issue}`));
    console.warn('📖 See ENVIRONMENT_SETUP.md for configuration help');
  } else {
    console.log('✅ Configuration validated successfully');
    console.log(`🔗 API Base URL: ${CONFIG.API_BASE_URL}`);
  }
  
  return issues.length === 0;
};

export const CONFIG = {
  // Firebase URL from environment variables with fallback
  API_BASE_URL: process.env.EXPO_PUBLIC_API_BASE_URL || "https://yogaapp-12d2b-default-rtdb.asia-southeast1.firebasedatabase.app/",

  // App settings from environment variables with fallbacks
  APP_NAME: process.env.EXPO_PUBLIC_APP_NAME || 'Yoga Studio',
  VERSION: process.env.EXPO_PUBLIC_APP_VERSION || '1.0.0',
  
  // UI Configuration
  THEME: {
    PRIMARY_COLOR: '#def4f7',
    SECONDARY_COLOR: '#f5f5f5',
    TEXT_COLOR: '#333',
    LIGHT_TEXT_COLOR: '#666',
    SUCCESS_COLOR: '#4caf4f',
    ERROR_COLOR: '#fa7575ff',
    WARNING_COLOR: '#fbac35ff',
  },
  
  // Feature flags from environment variables with fallbacks
  FEATURES: {
    ENABLE_PUSH_NOTIFICATIONS: process.env.EXPO_PUBLIC_ENABLE_PUSH_NOTIFICATIONS === 'true' || false,
    ENABLE_PAYMENT_INTEGRATION: process.env.EXPO_PUBLIC_ENABLE_PAYMENT_INTEGRATION === 'true' || false,
    ENABLE_SOCIAL_LOGIN: process.env.EXPO_PUBLIC_ENABLE_SOCIAL_LOGIN === 'true' || false,
    ENABLE_AUTHENTICATION: process.env.EXPO_PUBLIC_ENABLE_AUTHENTICATION === 'true' || true,
    ENABLE_GUEST_MODE: process.env.EXPO_PUBLIC_ENABLE_GUEST_MODE === 'true' || true,
  },
  
  // API endpoints
  ENDPOINTS: {
    YOGA_CLASSES: '/yoga-classes',
    BOOK_CLASS: '/yoga-classes/{id}/book',
    CUSTOMER_BOOKINGS: '/customers/{id}/bookings',
    INSTRUCTORS: '/instructors',
    USERS: '/users',
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    PROFILE: '/users/{id}',
  },
};
