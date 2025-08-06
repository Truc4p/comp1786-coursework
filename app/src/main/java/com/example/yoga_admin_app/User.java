package com.example.yoga_admin_app;

/**
 * User model for authentication system
 */
public class User {
    private long id;
    private String username;
    private String email;
    private String passwordHash;
    private String salt;
    private boolean isActive;
    private long createdAt;
    private long updatedAt;
    private long lastLoginAt;
    private int failedLoginAttempts;
    private long lockoutUntil;
    private String sessionToken;
    private long sessionExpiresAt;
    
    // Constructor
    public User() {
        this.isActive = true;
        this.failedLoginAttempts = 0;
        this.lockoutUntil = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
    
    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    
    public long getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(long lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    
    public long getLockoutUntil() { return lockoutUntil; }
    public void setLockoutUntil(long lockoutUntil) { this.lockoutUntil = lockoutUntil; }
    
    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
    
    public long getSessionExpiresAt() { return sessionExpiresAt; }
    public void setSessionExpiresAt(long sessionExpiresAt) { this.sessionExpiresAt = sessionExpiresAt; }
    
    // Utility methods
    public boolean isLocked() {
        return lockoutUntil > System.currentTimeMillis();
    }
    
    public boolean isSessionValid() {
        return sessionToken != null && sessionExpiresAt > System.currentTimeMillis();
    }
    
    public void incrementFailedAttempts() {
        failedLoginAttempts++;
        if (failedLoginAttempts >= 5) {
            // Lock account for 30 minutes after 5 failed login attempts
            lockoutUntil = System.currentTimeMillis() + (30 * 60 * 1000);
        }
        updatedAt = System.currentTimeMillis();
    }
    
    // Resets the failed login attempts and lockout status
    public void resetFailedAttempts() {
        failedLoginAttempts = 0;
        lockoutUntil = 0;
        updatedAt = System.currentTimeMillis();
    }
    
    public void updateLastLogin() {
        lastLoginAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
    }
    
    // Additional getter/setter aliases for DatabaseHelper compatibility
    public int getFailedAttempts() { return failedLoginAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedLoginAttempts = failedAttempts; }
    
    public long getLastLogin() { return lastLoginAt; }
    public void setLastLogin(long lastLogin) { this.lastLoginAt = lastLogin; }
}
