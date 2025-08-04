package com.example.yoga_admin_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * [L1] Secure Local Authentication Activity
 * Features:
 * - Secure login with username/email and password
 * - Account lockout protection after failed attempts
 * - Session management with automatic timeout
 * - Input validation and sanitization
 * - Password visibility toggle
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    
    private EditText editUsername;
    private EditText editPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView txtForgotPassword;
    private TextView txtRegister;
    
    private AuthenticationManager authManager;
    private boolean isPasswordVisible = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Initialize authentication manager
        authManager = AuthenticationManager.getInstance(this);
        
        // Check if user is already logged in
        if (authManager.isSessionValid()) {
            navigateToMainActivity();
            return;
        }
        
        initializeViews();
        setupListeners();
    }
    
    private void initializeViews() {
        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        txtForgotPassword = findViewById(R.id.txt_forgot_password);
        txtRegister = findViewById(R.id.txt_register);
        
        // Set initial state
        progressBar.setVisibility(View.GONE);
        
        // Check if we have a registered username to pre-fill
        Intent intent = getIntent();
        String registeredUsername = intent.getStringExtra("registered_username");
        if (registeredUsername != null && !registeredUsername.isEmpty()) {
            editUsername.setText(registeredUsername);
            editPassword.requestFocus();
        } else {
            // Pre-fill default admin credentials for demo
            editUsername.setText("admin");
            editPassword.setText("admin123");
        }
    }
    
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
                
        txtForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Please contact system administrator to reset password", 
                    Toast.LENGTH_LONG).show();
        });
        
        txtRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
        
        // Allow login by pressing Enter on password field
        editPassword.setOnEditorActionListener((v, actionId, event) -> {
            performLogin();
            return true;
        });
    }
    
    private void performLogin() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString();
        
        // Input validation
        if (username.isEmpty()) {
            editUsername.setError("Username or email is required");
            editUsername.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            editPassword.setError("Password is required");
            editPassword.requestFocus();
            return;
        }
        
        // Show loading state
        setLoginInProgress(true);
        
        // Perform authentication in background thread to avoid blocking UI
        new Thread(() -> {
            try {
                AuthenticationManager.AuthResult result = authManager.authenticate(username, password);
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    setLoginInProgress(false);
                    handleAuthResult(result);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Login error", e);
                runOnUiThread(() -> {
                    setLoginInProgress(false);
                    showError("Login failed due to system error");
                });
            }
        }).start();
    }
    
    private void handleAuthResult(AuthenticationManager.AuthResult result) {
        if (result.isSuccess()) {
            Log.i(TAG, "Login successful");
            Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
            navigateToMainActivity();
        } else {
            Log.w(TAG, "Login failed: " + result.getMessage());
            showError(result.getMessage());
            
            // Clear password field for security
            editPassword.setText("");
            editPassword.requestFocus();
        }
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void setLoginInProgress(boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!inProgress);
        editUsername.setEnabled(!inProgress);
        editPassword.setEnabled(!inProgress);
        
        btnLogin.setText(inProgress ? "Logging in..." : "Login");
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            isPasswordVisible = false;
        } else {
            // Show password
            editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            isPasswordVisible = true;
        }
        
        // Move cursor to end of text
        editPassword.setSelection(editPassword.getText().length());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Security: Clear password field when activity resumes
        // This prevents unauthorized access if user logged out or session expired
        clearSensitiveData();
        
        // Check session validity when activity resumes
        if (authManager.isSessionValid()) {
            navigateToMainActivity();
        }
    }
    
    @Override
    public void onBackPressed() {
        // Override back button to prevent going back to previous activity
        // without proper authentication
        moveTaskToBack(true);
        super.onBackPressed();
    }
    
    /**
     * Clear sensitive data for security
     * Called when activity resumes to prevent unauthorized access
     */
    private void clearSensitiveData() {
        Intent intent = getIntent();
        boolean logoutOccurred = intent.getBooleanExtra("logout_occurred", false);
        String registeredUsername = intent.getStringExtra("registered_username");
        
        // Always clear password for security
        if (editPassword != null) {
            editPassword.setText("");
        }
        
        // Clear username if user logged out or no registration username provided
        if (logoutOccurred || (registeredUsername == null || registeredUsername.isEmpty())) {
            if (editUsername != null) {
                editUsername.setText("");
            }
        }
        
        // Clear any previous error messages
        if (editUsername != null) {
            editUsername.setError(null);
        }
        if (editPassword != null) {
            editPassword.setError(null);
        }
        
        // Clear the logout flag after processing to prevent repeated clearing
        if (logoutOccurred) {
            intent.removeExtra("logout_occurred");
        }
    }
}
