package com.example.yoga_admin_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Authentication Manager - Handles secure local authentication with session management
 * Features:
 * - [L1] Secure Local Authentication with password hashing and salting
 * - [L1] Proper Session Logout with token invalidation
 * - [L1] Session Timeout with configurable expiration
 * - Account lockout after failed attempts
 * - Secure session token generation
 */
public class AuthenticationManager {
    private static final String TAG = "AuthManager";
    private static final String PREFS_NAME = "yoga_auth";
    private static final String KEY_CURRENT_USER_ID = "current_user_id";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_SESSION_EXPIRES = "session_expires";
    
    // Session timeout: 30 minutes of inactivity
    private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000;
    
    // Maximum failed login attempts before lockout
    private static final int MAX_FAILED_ATTEMPTS = 5;
    
    // Account lockout duration: 30 minutes
    private static final long LOCKOUT_DURATION_MS = 30 * 60 * 1000;
    
    private Context context;
    private DatabaseHelper databaseHelper;
    private SharedPreferences prefs;
    private static AuthenticationManager instance;
    
    private AuthenticationManager(Context context) {
        this.context = context.getApplicationContext();
        this.databaseHelper = new DatabaseHelper(this.context);
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized AuthenticationManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthenticationManager(context);
        }
        return instance;
    }
    
    /**
     * [L1] Secure Local Authentication
     * Authenticate user with username/email and password
     */
    public AuthResult authenticate(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || password == null || 
            usernameOrEmail.trim().isEmpty() || password.trim().isEmpty()) {
            return new AuthResult(false, "Username and password are required", null);
        }
        
        try {
            // Get user by username or email
            User user = databaseHelper.getUserByUsernameOrEmail(usernameOrEmail.trim());
            
            if (user == null) {
                Log.w(TAG, "Authentication failed: User not found");
                return new AuthResult(false, "Invalid credentials", null);
            }
            
            // Check if account is locked
            if (user.isLocked()) {
                long remainingLockTime = user.getLockoutUntil() - System.currentTimeMillis();
                int remainingMinutes = (int) (remainingLockTime / (60 * 1000));
                Log.w(TAG, "Authentication failed: Account locked");
                return new AuthResult(false, "Account locked. Try again in " + remainingMinutes + " minutes", null);
            }
            
            // Check if account is active
            if (!user.isActive()) {
                Log.w(TAG, "Authentication failed: Account inactive");
                return new AuthResult(false, "Account is inactive", null);
            }
            
            // Verify password
            if (!verifyPassword(password, user.getPasswordHash(), user.getSalt())) {
                // Increment failed attempts
                user.incrementFailedAttempts();
                databaseHelper.updateUser(user);
                
                Log.w(TAG, "Authentication failed: Invalid password");
                return new AuthResult(false, "Invalid credentials", null);
            }
            
            // Authentication successful
            user.resetFailedAttempts();
            user.updateLastLogin();
            
            // Generate new session token
            String sessionToken = generateSessionToken();
            long sessionExpires = System.currentTimeMillis() + SESSION_TIMEOUT_MS;
            
            user.setSessionToken(sessionToken);
            user.setSessionExpiresAt(sessionExpires);
            
            // Update user in database
            databaseHelper.updateUser(user);
            
            // Store session in SharedPreferences
            storeSession(user.getId(), sessionToken, sessionExpires);
            
            Log.i(TAG, "Authentication successful for user: " + user.getUsername());
            return new AuthResult(true, "Authentication successful", user);
            
        } catch (Exception e) {
            Log.e(TAG, "Authentication error", e);
            return new AuthResult(false, "Authentication failed due to system error", null);
        }
    }
    
    /**
     * [L1] Proper Session Logout
     * Invalidate current session and clear all session data
     */
    public boolean logout() {
        try {
            // Get current user
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                // Invalidate session token in database
                currentUser.setSessionToken(null);
                currentUser.setSessionExpiresAt(0);
                databaseHelper.updateUser(currentUser);
            }
            
            // Clear session from SharedPreferences
            clearSession();
            
            Log.i(TAG, "User logged out successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Logout error", e);
            return false;
        }
    }
    
    /**
     * [L1] Session Timeout
     * Check if current session is valid and not expired
     */
    public boolean isSessionValid() {
        try {
            long userId = prefs.getLong(KEY_CURRENT_USER_ID, -1);
            String sessionToken = prefs.getString(KEY_SESSION_TOKEN, null);
            long sessionExpires = prefs.getLong(KEY_SESSION_EXPIRES, 0);
            
            if (userId == -1 || sessionToken == null || sessionExpires == 0) {
                return false;
            }
            
            // Check if session has expired
            if (System.currentTimeMillis() > sessionExpires) {
                Log.i(TAG, "Session expired");
                clearSession();
                return false;
            }
            
            // Verify session token in database
            User user = databaseHelper.getUserById(userId);
            if (user == null || !sessionToken.equals(user.getSessionToken()) || 
                !user.isSessionValid()) {
                Log.w(TAG, "Invalid session token");
                clearSession();
                return false;
            }
            
            // Extend session on activity (sliding expiration)
            extendSession();
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Session validation error", e);
            clearSession();
            return false;
        }
    }
    
    /**
     * Get currently authenticated user
     */
    public User getCurrentUser() {
        try {
            long userId = prefs.getLong(KEY_CURRENT_USER_ID, -1);
            String sessionToken = prefs.getString(KEY_SESSION_TOKEN, null);
            long sessionExpires = prefs.getLong(KEY_SESSION_EXPIRES, 0);
            
            // Basic session check without calling isSessionValid()
            if (userId == -1 || sessionToken == null || sessionExpires == 0) {
                return null;
            }
            
            // Check if session has expired
            if (System.currentTimeMillis() > sessionExpires) {
                clearSession();
                return null;
            }
            
            return databaseHelper.getUserById(userId);
        } catch (Exception e) {
            Log.e(TAG, "Error getting current user", e);
            return null;
        }
    }

    /**
     * Force logout all sessions (useful for security)
     */
    public void logoutAllSessions() {
        try {
            databaseHelper.invalidateAllSessions();
            clearSession();
            Log.i(TAG, "All sessions invalidated");
        } catch (Exception e) {
            Log.e(TAG, "Error invalidating all sessions", e);
        }
    }
    
    // Private helper methods
    
    private boolean verifyPassword(String password, String hash, String salt) {
        try {
            String computedHash = hashPassword(password, salt);
            return computedHash.equals(hash);
        } catch (Exception e) {
            Log.e(TAG, "Password verification error", e);
            return false;
        }
    }
    
    private String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt.getBytes());
        byte[] hashedPassword = md.digest(password.getBytes());
        return Base64.encodeToString(hashedPassword, Base64.DEFAULT);
    }
    
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.DEFAULT);
    }
    
    private String generateSessionToken() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }
    
    private void storeSession(long userId, String sessionToken, long sessionExpires) {
        prefs.edit()
                .putLong(KEY_CURRENT_USER_ID, userId)
                .putString(KEY_SESSION_TOKEN, sessionToken)
                .putLong(KEY_SESSION_EXPIRES, sessionExpires)
                .apply();
    }
    
    private void clearSession() {
        prefs.edit()
                .remove(KEY_CURRENT_USER_ID)
                .remove(KEY_SESSION_TOKEN)
                .remove(KEY_SESSION_EXPIRES)
                .apply();
    }
    
    private void extendSession() {
        try {
            long newExpiration = System.currentTimeMillis() + SESSION_TIMEOUT_MS;
            prefs.edit()
                    .putLong(KEY_SESSION_EXPIRES, newExpiration)
                    .apply();
            
            // Update database
            long userId = prefs.getLong(KEY_CURRENT_USER_ID, -1);
            if (userId != -1) {
                User user = databaseHelper.getUserById(userId);
                if (user != null) {
                    user.setSessionExpiresAt(newExpiration);
                    databaseHelper.updateUser(user);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extending session", e);
        }
    }
    
    /**
     * Authentication result wrapper
     */
    public static class AuthResult {
        private boolean success;
        private String message;
        private User user;
        
        public AuthResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
    }
}
