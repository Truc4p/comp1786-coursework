package com.example.yoga_admin_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Base authenticated activity that enforces authentication for all subclasses
 * [L1] Server-Side Authentication with automatic session checking
 * [L1] Session Timeout enforcement
 */
public abstract class BaseAuthenticatedActivity extends AppCompatActivity {
    
    protected AuthenticationManager authManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize authentication manager
        authManager = AuthenticationManager.getInstance(this);
        
        // Check authentication before proceeding
        if (!AuthHelper.requireAuthentication(this)) {
            return; // Activity will be finished by requireAuthentication
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Check session validity when activity resumes
        if (!AuthHelper.requireAuthentication(this)) {
            return; // Activity will be finished by requireAuthentication
        }
    }
    
    /**
     * Get current authenticated user
     */
    protected User getCurrentUser() {
        return authManager.getCurrentUser();
    }
    
    /**
     * Check if current user has admin role
     */
    protected boolean isCurrentUserAdmin() {
        return authManager.isAdmin();
    }
    
    /**
     * Require admin role for current operation
     */
    protected boolean requireAdminRole() {
        return AuthHelper.requireAdminRole(this);
    }
}
