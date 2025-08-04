package com.example.yoga_admin_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.SecureRandom;
import android.util.Base64;

/**
 * User Registration Activity
 * Features:
 * - Secure user registration with validation
 * - Password confirmation and strength checking
 * - Email format validation
 * - Username uniqueness checking
 */
public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    
    private EditText editUsername;
    private EditText editEmail;
    private EditText editAdminKey;
    private EditText editPassword;
    private EditText editConfirmPassword;
    private Button btnRegister;
    private ProgressBar progressBar;
    private TextView txtBackToLogin;
    
    private DatabaseHelper databaseHelper;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);
        
        initializeViews();
        setupListeners();
    }
    
    private void initializeViews() {
        editUsername = findViewById(R.id.edit_username);
        editEmail = findViewById(R.id.edit_email);
        editAdminKey = findViewById(R.id.edit_admin_key);
        editPassword = findViewById(R.id.edit_password);
        editConfirmPassword = findViewById(R.id.edit_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress_bar);
        txtBackToLogin = findViewById(R.id.txt_back_to_login);
        
        // Set initial state
        progressBar.setVisibility(View.GONE);
    }
    
    private void setupListeners() {
        btnRegister.setOnClickListener(v -> performRegistration());
        
        txtBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    private void performRegistration() {
        final String username = editUsername.getText().toString().trim();
        final String email = editEmail.getText().toString().trim();
        final String adminKey = editAdminKey.getText().toString().trim();
        final String password = editPassword.getText().toString();
        final String confirmPassword = editConfirmPassword.getText().toString();
        
        // Validate inputs (including admin key)
        if (!validateInputs(username, email, adminKey, password, confirmPassword)) {
            return;
        }
        
        // Show loading state
        setRegistrationInProgress(true);
        
        // Perform registration in background thread
        new Thread(() -> {
            try {
                // Check if username or email already exists
                User existingUser = databaseHelper.getUserByUsernameOrEmail(username);
                if (existingUser != null) {
                    final User finalExistingUser = existingUser;
                    runOnUiThread(() -> {
                        setRegistrationInProgress(false);
                        if (finalExistingUser.getUsername().equals(username)) {
                            editUsername.setError("Username already exists");
                            editUsername.requestFocus();
                        } else {
                            editEmail.setError("Email already registered");
                            editEmail.requestFocus();
                        }
                    });
                    return;
                }
                
                // Check email separately
                User emailUser = databaseHelper.getUserByUsernameOrEmail(email);
                if (emailUser != null) {
                    runOnUiThread(() -> {
                        setRegistrationInProgress(false);
                        editEmail.setError("Email already registered");
                        editEmail.requestFocus();
                    });
                    return;
                }
                
                // Create new user
                User newUser = createNewUser(username, email, password);
                long userId = databaseHelper.createUser(newUser);
                
                runOnUiThread(() -> {
                    setRegistrationInProgress(false);
                    if (userId > 0) {
                        handleRegistrationSuccess();
                    } else {
                        showError("Registration failed. Please try again.");
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Registration error", e);
                runOnUiThread(() -> {
                    setRegistrationInProgress(false);
                    showError("Registration failed due to system error");
                });
            }
        }).start();
    }
    
    private boolean validateInputs(String username, String email, String adminKey, String password, String confirmPassword) {
        boolean isValid = true;
        EditText firstErrorField = null;
        StringBuilder errorSummary = new StringBuilder();
        
        // Clear all previous errors first
        editUsername.setError(null);
        editEmail.setError(null);
        editAdminKey.setError(null);
        editPassword.setError(null);
        editConfirmPassword.setError(null);
        
        // Username validation
        if (username.isEmpty()) {
            editUsername.setError("Username is required");
            errorSummary.append("• Username is required\n");
            if (firstErrorField == null) firstErrorField = editUsername;
            isValid = false;
        } else if (username.length() < 3) {
            editUsername.setError("Username must be at least 3 characters");
            errorSummary.append("• Username must be at least 3 characters\n");
            if (firstErrorField == null) firstErrorField = editUsername;
            isValid = false;
        } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
            editUsername.setError("Username can only contain letters, numbers, and underscores");
            errorSummary.append("• Username can only contain letters, numbers, and underscores\n");
            if (firstErrorField == null) firstErrorField = editUsername;
            isValid = false;
        }
        
        // Email validation
        if (email.isEmpty()) {
            editEmail.setError("Email is required");
            errorSummary.append("• Email is required\n");
            if (firstErrorField == null) firstErrorField = editEmail;
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Please enter a valid email address");
            errorSummary.append("• Please enter a valid email address\n");
            if (firstErrorField == null) firstErrorField = editEmail;
            isValid = false;
        }
        
        // Admin Key validation
        if (adminKey.isEmpty()) {
            editAdminKey.setError("Admin registration key is required");
            errorSummary.append("• Admin registration key is required\n");
            if (firstErrorField == null) firstErrorField = editAdminKey;
            isValid = false;
        } else if (!SecurityConfig.validateAdminKey(adminKey)) {
            editAdminKey.setError("Invalid admin registration key");
            errorSummary.append("• Invalid admin registration key\n");
            if (firstErrorField == null) firstErrorField = editAdminKey;
            isValid = false;
        }
        
        // Password validation
        if (password.isEmpty()) {
            editPassword.setError("Password is required");
            errorSummary.append("• Password is required\n");
            if (firstErrorField == null) firstErrorField = editPassword;
            isValid = false;
        } else if (password.length() < 6) {
            editPassword.setError("Password must be at least 6 characters");
            errorSummary.append("• Password must be at least 6 characters\n");
            if (firstErrorField == null) firstErrorField = editPassword;
            isValid = false;
        }
        
        // Password confirmation validation
        if (confirmPassword.isEmpty()) {
            editConfirmPassword.setError("Please confirm your password");
            errorSummary.append("• Please confirm your password\n");
            if (firstErrorField == null) firstErrorField = editConfirmPassword;
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("Passwords do not match");
            errorSummary.append("• Passwords do not match\n");
            if (firstErrorField == null) firstErrorField = editConfirmPassword;
            isValid = false;
        }
        
        // Focus on the first field with an error and show summary if there are errors
        if (firstErrorField != null) {
            firstErrorField.requestFocus();
            
            // Show a summary of all errors in a toast
            if (errorSummary.length() > 0) {
                String summaryMessage = "Please fix the following issues:\n" + errorSummary.toString().trim();
                Toast.makeText(this, summaryMessage, Toast.LENGTH_LONG).show();
            }
        }
        
        return isValid;
    }
    
    private User createNewUser(String username, String email, String password) {
        try {
            // Generate salt and hash password
            String salt = generateSalt();
            String passwordHash = hashPassword(password, salt);
            
            // Create user object
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(passwordHash);
            user.setSalt(salt);
            user.setActive(true);
            user.setCreatedAt(System.currentTimeMillis());
            
            return user;
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating user", e);
            return null;
        }
    }
    
    private void handleRegistrationSuccess() {
        Toast.makeText(this, "Registration successful! Please login with your credentials.", 
                Toast.LENGTH_LONG).show();
        
        // Navigate to login activity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("registered_username", editUsername.getText().toString().trim());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
    
    private void setRegistrationInProgress(boolean inProgress) {
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!inProgress);
        editUsername.setEnabled(!inProgress);
        editEmail.setEnabled(!inProgress);
        editAdminKey.setEnabled(!inProgress);
        editPassword.setEnabled(!inProgress);
        editConfirmPassword.setEnabled(!inProgress);
        
        btnRegister.setText(inProgress ? "Creating Account..." : "Register");
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void togglePasswordVisibility(EditText editText, ImageView toggleIcon, 
                                        boolean currentlyVisible, VisibilityCallback callback) {
        if (currentlyVisible) {
            // Hide password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            callback.onVisibilityChanged(false);
        } else {
            // Show password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            callback.onVisibilityChanged(true);
        }
        
        // Move cursor to end of text
        editText.setSelection(editText.getText().length());
    }
    
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.DEFAULT);
    }
    
    private String hashPassword(String password, String salt) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt.getBytes());
        byte[] hashedPassword = md.digest(password.getBytes());
        return Base64.encodeToString(hashedPassword, Base64.DEFAULT);
    }
    
    @Override
    public void onBackPressed() {
        // Navigate back to login
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
    
    // Interface for password visibility callback
    private interface VisibilityCallback {
        void onVisibilityChanged(boolean visible);
    }
}
