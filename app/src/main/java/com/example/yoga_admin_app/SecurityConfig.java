package com.example.yoga_admin_app;

/**
 * Security configuration for admin registration
 */
public class SecurityConfig {
    
    // Admin registration key - in production, this should be stored securely
    // or retrieved from a secure configuration service
    private static final String ADMIN_REGISTRATION_KEY = "yogaadmin2025";
    
    // Minimum admin key length
    private static final int MIN_ADMIN_KEY_LENGTH = 8;
    
    /**
     * Validates the admin registration key
     * @param inputKey The key provided by the user
     * @return true if the key is valid, false otherwise
     */
    public static boolean validateAdminKey(String inputKey) {
        if (inputKey == null || inputKey.trim().isEmpty()) {
            return false;
        }
        
        // Check if the key matches the expected admin key
        return ADMIN_REGISTRATION_KEY.equals(inputKey.trim());
    }
    
    /**
     * Get the expected format for admin key (for help text)
     * @return String describing the expected format
     */
    public static String getAdminKeyFormat() {
        return "Contact your system administrator for the admin registration key";
    }
    
    /**
     * Check if admin key meets minimum requirements
     * @param inputKey The key to validate
     * @return true if key meets minimum requirements
     */
    public static boolean meetsMinimumRequirements(String inputKey) {
        return inputKey != null && inputKey.length() >= MIN_ADMIN_KEY_LENGTH;
    }
    
    /**
     * Get the current admin key (for development/testing purposes only)
     * In production, this method should not exist or should be protected
     * @return The current admin key
     */
    public static String getAdminKeyForTesting() {
        return ADMIN_REGISTRATION_KEY;
    }
}
