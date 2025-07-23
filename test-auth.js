// Test script for debugging authentication issues
import ApiService from './src/services/api';

const testRegistrationAndLogin = async () => {
  console.log('=== Testing Registration and Login ===');
  
  // Test data
  const testUser = {
    firstName: 'Test',
    lastName: 'User',
    email: 'test@example.com',
    phone: '+1234567890',
    password: 'password123'
  };
  
  try {
    console.log('\n1. Testing Registration...');
    const registerResponse = await ApiService.register(testUser);
    console.log('Registration response:', registerResponse);
    
    if (registerResponse.success) {
      console.log('\n2. Testing Login with new account...');
      const loginResponse = await ApiService.login(testUser.email, testUser.password);
      console.log('Login response:', loginResponse);
      
      if (loginResponse.success) {
        console.log('\n✅ Registration and Login working correctly!');
      } else {
        console.log('\n❌ Login failed:', loginResponse.message);
      }
    } else {
      console.log('\n❌ Registration failed:', registerResponse.message);
    }
    
    console.log('\n3. Testing existing users fetch...');
    const usersResponse = await ApiService.request('/users');
    console.log('Users in database:', usersResponse);
    
  } catch (error) {
    console.error('Test error:', error);
  }
};

// Export for manual testing
export { testRegistrationAndLogin };
