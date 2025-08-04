# Admin Registration Security Feature

## Overview
The admin registration system now requires a valid admin key to prevent unauthorized admin account creation. This adds an extra layer of security to ensure only authorized personnel can create admin accounts.

## How It Works

### For Users Trying to Register
1. **Open the app** and go to registration
2. **Fill in all required fields:**
   - Username (minimum 3 characters, letters/numbers/underscores only)
   - Email address (valid email format required)
   - **Admin Registration Key** (required - contact system administrator)
   - Password (minimum 6 characters)
   - Confirm Password (must match password)

3. **Enter the correct admin key** to proceed with registration
4. **If the admin key is incorrect**, registration will be blocked with an error message

### Admin Key Details
- **Current Admin Key:** `yogaadmin2025`
- **Key is case-sensitive** and must be entered exactly as provided
- **Key is required** for all admin registrations
- **Key is hidden** (appears as dots/asterisks when typing)

## Security Features

### Input Validation
- ✅ **Username validation**: 3+ characters, alphanumeric + underscores only
- ✅ **Email validation**: Proper email format required
- ✅ **Admin key validation**: Must match the configured admin key exactly
- ✅ **Password validation**: Minimum 6 characters required
- ✅ **Password confirmation**: Must match the original password
- ✅ **Duplicate prevention**: Username and email must be unique

### Error Handling
- **Invalid admin key**: Shows error message and provides guidance
- **Missing admin key**: Prevents registration with clear error message
- **Wrong format**: Validates all inputs before attempting registration

## For System Administrators

### Changing the Admin Key
To change the admin registration key:

1. **Open `SecurityConfig.java`**
2. **Locate the line:**
   ```java
   private static final String ADMIN_REGISTRATION_KEY = "yogaadmin2025";
   ```
3. **Change the key value** to your desired key
4. **Rebuild and redeploy** the application

### Best Practices for Admin Keys
- ✅ **Use strong keys**: Mix of letters, numbers, and special characters
- ✅ **Make them memorable**: But not easily guessable
- ✅ **Change periodically**: Update the key regularly for security
- ✅ **Distribute securely**: Only share with authorized personnel
- ✅ **Keep confidential**: Don't store in easily accessible locations

### Production Considerations
For production deployment, consider:

1. **Environment Variables**: Store the admin key in environment variables
2. **External Configuration**: Use secure configuration services
3. **Key Rotation**: Implement automatic key rotation
4. **Audit Logging**: Log admin registration attempts
5. **Multi-factor Authentication**: Add additional security layers

## User Experience

### Success Flow
1. User enters all valid information including correct admin key
2. System validates all inputs
3. Account is created successfully
4. User is redirected to login screen with pre-filled username
5. User can login with their new credentials

### Error Flow
1. User enters invalid or missing admin key
2. System shows clear error message
3. Registration is blocked until valid key is provided
4. User must contact administrator for the correct key

## Error Messages

### Admin Key Errors
- **Empty key**: "Admin registration key is required"
- **Invalid key**: "Invalid admin registration key"
- **Help message**: "Contact your system administrator for the correct admin key"

### Other Validation Errors
- Username, email, and password validation errors as before
- All validation happens client-side for immediate feedback

## Testing

### To Test the Admin Key Feature:

1. **Open the app and go to registration**
2. **Try registering without an admin key** - should show error
3. **Try with wrong admin key** (e.g., "wrongkey") - should show error
4. **Try with correct admin key**: `yogaadmin2025` - should succeed

### Valid Admin Key for Testing
```
yogaadmin2025
```
**Note:** This key is case-sensitive and must be entered exactly as shown.

### Test Scenarios

#### ❌ Invalid Scenarios (Should Fail)
- Empty admin key field
- Wrong admin key: "wrongkey"
- Wrong capitalization: "yogaadmin2025"
- Extra spaces: " yogaadmin2025 "
- Incomplete key: "YogaAdmin"

#### ✅ Valid Scenario (Should Succeed)
- Correct admin key: "yogaadmin2025"
- All other fields filled correctly
- Username: 3+ characters, alphanumeric
- Valid email format
- Password: 6+ characters
- Matching password confirmation

## Benefits

1. **Prevents Unauthorized Access**: Only users with the admin key can create accounts
2. **Controlled User Management**: Administrators control who can register
3. **Security Layer**: Adds protection against automated registration attacks
4. **Audit Trail**: Can track who has access to registration capabilities
5. **Professional Setup**: Ensures only authorized team members join the system

The admin registration key feature successfully secures the user registration process while maintaining a user-friendly interface for authorized personnel.
