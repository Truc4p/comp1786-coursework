// Simple password hashing utility for React Native/Expo
// Using basic hashing since crypto-js can have compatibility issues with Expo

/**
 * Simple hash function (not cryptographically secure, but better than plain text)
 * For production, consider using a backend service for password hashing
 */
const simpleHash = (str) => {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash; // Convert to 32-bit integer
  }
  return Math.abs(hash).toString(36);
};

/**
 * Generate a random salt
 */
const generateSalt = () => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let salt = '';
  for (let i = 0; i < 16; i++) {
    salt += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return salt;
};

/**
 * Hash a password using a simple hash with salt
 * @param {string} password - The plain text password to hash
 * @param {string} salt - Optional salt, if not provided, a random salt will be generated
 * @returns {object} - Object containing the hashed password and salt
 */
export const hashPassword = (password, salt = null) => {
  try {
    // Generate a random salt if not provided
    if (!salt) {
      salt = generateSalt();
    }
    
    // Hash the password with the salt (multiple rounds for better security)
    let hashedPassword = password + salt;
    for (let i = 0; i < 1000; i++) {
      hashedPassword = simpleHash(hashedPassword + salt + i);
    }
    
    return {
      hashedPassword,
      salt
    };
  } catch (error) {
    console.error('Error hashing password:', error);
    throw new Error('Failed to hash password');
  }
};

/**
 * Verify a password against a hashed password
 * @param {string} password - The plain text password to verify
 * @param {string} hashedPassword - The stored hashed password
 * @param {string} salt - The salt used when hashing the original password
 * @returns {boolean} - True if password matches, false otherwise
 */
export const verifyPassword = (password, hashedPassword, salt) => {
  try {
    const { hashedPassword: newHash } = hashPassword(password, salt);
    return newHash === hashedPassword;
  } catch (error) {
    console.error('Error verifying password:', error);
    return false;
  }
};

/**
 * Generate a secure random password
 * @param {number} length - Length of the password to generate
 * @returns {string} - Generated password
 */
export const generateSecurePassword = (length = 12) => {
  const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*";
  let password = "";
  
  for (let i = 0; i < length; i++) {
    const randomIndex = Math.floor(Math.random() * charset.length);
    password += charset[randomIndex];
  }
  
  return password;
};