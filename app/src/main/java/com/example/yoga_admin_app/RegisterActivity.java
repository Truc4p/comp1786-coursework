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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.SecureRandom;
import android.util.Base64;

/**
 * User Registration Activity
 * Features:
 * - Secure user registration with validation
 * - Password confirmation and strength checking
 * - Role selection (admin, manager, instructor)
 * - Email format validation
 * - Username uniqueness checking
 */
public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    
    private EditText editUsername;
    private EditText editEmail;
    private EditText editPassword;
    private EditText editConfirmPassword;
    private ImageView imgTogglePassword;
    private ImageView imgToggleConfirmPassword;
    private Spinner spinnerRole;
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
        setupRoleSpinner();
    }
    
    private void initializeViews() {
        editUsername = findViewById(R.id.edit_username);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        editConfirmPassword = findViewById(R.id.edit_confirm_password);
        imgTogglePassword = findViewById(R.id.img_toggle_password);
        imgToggleConfirmPassword = findViewById(R.id.img_toggle_confirm_password);
        spinnerRole = findViewById(R.id.spinner_role);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress_bar);
        txtBackToLogin = findViewById(R.id.txt_back_to_login);
        
        // Set initial state
        progressBar.setVisibility(View.GONE);
    }
    
    private void setupListeners() {
        btnRegister.setOnClickListener(v -> performRegistration());
        
        imgTogglePassword.setOnClickListener(v -> togglePasswordVisibility(
                editPassword, imgTogglePassword, isPasswordVisible, 
                visible -> isPasswordVisible = visible));
        
        imgToggleConfirmPassword.setOnClickListener(v -> togglePasswordVisibility(
                editConfirmPassword, imgToggleConfirmPassword, isConfirmPasswordVisible,
                visible -> isConfirmPasswordVisible = visible));
        
        txtBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    private void setupRoleSpinner() {
        String[] roles = {"admin", "manager", "instructor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);
        
        // Set default to "admin"
        spinnerRole.setSelection(0);
    }
    
    private void performRegistration() {
        final String username = editUsername.getText().toString().trim();
        final String email = editEmail.getText().toString().trim();
        final String password = editPassword.getText().toString();
        final String confirmPassword = editConfirmPassword.getText().toString();
        final String role = spinnerRole.getSelectedItem().toString();
        
        // Validate inputs
        if (!validateInputs(username, email, password, confirmPassword)) {
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
                User newUser = createNewUser(username, email, password, role);
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
    
    private boolean validateInputs(String username, String email, String password, String confirmPassword) {
        // Username validation
        if (username.isEmpty()) {
            editUsername.setError("Username is required");
            editUsername.requestFocus();
            return false;
        }
        
        if (username.length() < 3) {
            editUsername.setError("Username must be at least 3 characters");
            editUsername.requestFocus();
            return false;
        }
        
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            editUsername.setError("Username can only contain letters, numbers, and underscores");
            editUsername.requestFocus();
            return false;
        }
        
        // Email validation
        if (email.isEmpty()) {
            editEmail.setError("Email is required");
            editEmail.requestFocus();
            return false;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Please enter a valid email address");
            editEmail.requestFocus();
            return false;
        }
        
        // Password validation
        if (password.isEmpty()) {
            editPassword.setError("Password is required");
            editPassword.requestFocus();
            return false;
        }
        
        if (password.length() < 6) {
            editPassword.setError("Password must be at least 6 characters");
            editPassword.requestFocus();
            return false;
        }
        
        // Password confirmation validation
        if (confirmPassword.isEmpty()) {
            editConfirmPassword.setError("Please confirm your password");
            editConfirmPassword.requestFocus();
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("Passwords do not match");
            editConfirmPassword.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private User createNewUser(String username, String email, String password, String role) {
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
            user.setRole(role);
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
        editPassword.setEnabled(!inProgress);
        editConfirmPassword.setEnabled(!inProgress);
        spinnerRole.setEnabled(!inProgress);
        
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
            toggleIcon.setImageResource(R.drawable.ic_visibility_off);
            callback.onVisibilityChanged(false);
        } else {
            // Show password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(R.drawable.ic_visibility);
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
    }
    
    // Interface for password visibility callback
    private interface VisibilityCallback {
        void onVisibilityChanged(boolean visible);
    }
}
