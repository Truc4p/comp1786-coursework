package com.example.yoga_admin_app;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * [L1] Server-Side Authentication Helper
 * Provides centralized authentication checking across all activities
 * Ensures consistent security enforcement throughout the application
 */
public class AuthHelper {
    private static final String TAG = "AuthHelper";
    
    /**
     * Check authentication for any activity
     * Redirects to login if session is invalid
     * @param activity The activity to check authentication for
     * @return true if authenticated, false if redirected to login
     */
    public static boolean requireAuthentication(Activity activity) {
        AuthenticationManager authManager = AuthenticationManager.getInstance(activity);
        
        if (!authManager.isSessionValid()) {
            Log.w(TAG, "Authentication required - redirecting to login");
            redirectToLogin(activity);
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if user has admin role
     * @param activity The activity context
     * @return true if user is admin, false otherwise
     */
    public static boolean requireAdminRole(Activity activity) {
        AuthenticationManager authManager = AuthenticationManager.getInstance(activity);
        
        if (!authManager.isAdmin()) {
            Log.w(TAG, "Admin role required");
            Toast.makeText(activity, "Admin access required", Toast.LENGTH_LONG).show();
            return false;
        }
        
        return true;
    }
    
    /**
     * Get current authenticated user
     * @param activity The activity context
     * @return Current user or null if not authenticated
     */
    public static User getCurrentUser(Activity activity) {
        AuthenticationManager authManager = AuthenticationManager.getInstance(activity);
        return authManager.getCurrentUser();
    }
    
    /**
     * Redirect to login activity
     * @param activity The activity to redirect from
     */
    public static void redirectToLogin(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
    
    /**
     * Perform logout with proper cleanup
     * @param activity The activity context
     */
    public static void performLogout(Activity activity) {
        AuthenticationManager authManager = AuthenticationManager.getInstance(activity);
        boolean success = authManager.logout();
        
        if (success) {
            Log.i(TAG, "User logged out successfully");
            Toast.makeText(activity, "Logged out successfully", Toast.LENGTH_SHORT).show();
        } else {
            Log.e(TAG, "Logout failed");
            Toast.makeText(activity, "Logout failed", Toast.LENGTH_SHORT).show();
        }
        
        redirectToLogin(activity);
    }
}
