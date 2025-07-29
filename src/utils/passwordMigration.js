import { hashPassword, verifyPassword } from './password';

/**
 * Migration utility to hash existing plain text passwords in the database
 * This should be run once to migrate existing users to use hashed passwords
 */
export const migrateUserPasswords = async (ApiService) => {
  try {
    console.log('Starting password migration...');
    
    // Get all users from the database
    const response = await ApiService.request('/users');
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
    
    console.log(`Found ${users.length} users to potentially migrate`);
    
    let migratedCount = 0;
    
    for (const user of users) {
      // Check if user already has a salt (indicating hashed password)
      if (!user.salt) {
        console.log(`Migrating user: ${user.email}`);
        
        // Hash the existing plain text password
        const { hashedPassword, salt } = hashPassword(user.password);
        
        // Update the user in the database
        const updateData = {
          password: hashedPassword,
          salt: salt,
          updatedAt: new Date().toISOString(),
        };
        
        // Use the Firebase key if available, otherwise use the user ID
        const userId = user.firebaseKey || user.id;
        
        const updateResponse = await ApiService.request(`/users/${userId}`, {
          method: 'PATCH',
          body: JSON.stringify(updateData),
        });
        
        if (updateResponse !== null) {
          migratedCount++;
          console.log(`Successfully migrated user: ${user.email}`);
        } else {
          console.error(`Failed to migrate user: ${user.email}`);
        }
      } else {
        console.log(`User ${user.email} already has hashed password, skipping`);
      }
    }
    
    console.log(`Migration completed. ${migratedCount} users migrated.`);
    return {
      success: true,
      message: `Successfully migrated ${migratedCount} users`,
      migratedCount
    };
    
  } catch (error) {
    console.error('Migration failed:', error);
    return {
      success: false,
      message: error.message || 'Migration failed',
      migratedCount: 0
    };
  }
};

/**
 * Test function to verify password hashing functionality
 */
export const testPasswordHashing = () => {
  console.log('Testing password hashing functionality...');
  
  const testPassword = 'testPassword123';
  
  // Test 1: Hash a password
  const { hashedPassword, salt } = hashPassword(testPassword);
  console.log('Original password:', testPassword);
  console.log('Hashed password:', hashedPassword);
  console.log('Salt:', salt);
  
  // Test 2: Verify the password
  const isValid = verifyPassword(testPassword, hashedPassword, salt);
  console.log('Password verification result:', isValid);
  
  // Test 3: Try with wrong password
  const isInvalid = verifyPassword('wrongPassword', hashedPassword, salt);
  console.log('Wrong password verification result:', isInvalid);
  
  // Test 4: Test that same password generates different hashes with different salts
  const { hashedPassword: hash2 } = hashPassword(testPassword);
  console.log('Second hash of same password:', hash2);
  console.log('Hashes are different:', hashedPassword !== hash2);
  
  console.log('Password hashing test completed');
};
