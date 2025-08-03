# Customer Yoga App

A React Native hybrid mobile application for customers to view and book yoga classes. This app connects to a cloud service to fetch available yoga classes and allows customers to search and filter classes by day of the week and time of day.

## Features

### Core Functionality
- **View Yoga Classes**: Browse all available yoga classes with detailed information
- **Search & Filter**: Filter classes by day of the week and/or time of day
- **Class Details**: View comprehensive information about each class including instructor, schedule, pricing, and availability
- **Booking System**: Book yoga classes with customer information form
- **Cloud Integration**: Connects to your existing cloud service/API

### User Interface
- Modern, intuitive design with smooth navigation
- Pull-to-refresh functionality
- Loading states and error handling
- Responsive layout for various screen sizes
- Professional color scheme with purple theme

## Technical Stack

- **Framework**: React Native with Expo
- **Navigation**: React Navigation v6
- **State Management**: React Hooks (useState, useEffect)
- **API Integration**: Fetch API with async/await
- **Styling**: React Native StyleSheet

## Installation & Setup

### Prerequisites
- Node.js (v14 or higher)
- npm or yarn
- Expo CLI
- iOS Simulator (for iOS development) or Android Emulator (for Android development)

### Step 1: Install Dependencies
```bash
cd customer-yoga-app
npm install
```

### Step 2: Environment Configuration
1. Copy the environment template:
```bash
cp .env.example .env
```

2. Update `.env` with your Firebase configuration:
```bash
EXPO_PUBLIC_API_BASE_URL=https://your-firebase-project-default-rtdb.region.firebasedatabase.app/
```

3. Verify your configuration:
```bash
npm run check-env
```

4. For detailed environment setup instructions, see [ENVIRONMENT_SETUP.md](./ENVIRONMENT_SETUP.md)

> **Note**: The app will work with default fallback values, but you should configure your own Firebase URL for production use.

### Step 3: API Endpoints Structure
Your cloud service should provide the following endpoints:

#### GET /yoga-classes
Returns list of all yoga classes
```json
{
  "data": [
    {
      "id": 1,
      "name": "Hatha Yoga Fundamentals",
      "instructor": "Sarah Johnson",
      "date": "2025-01-22",
      "time": "09:00",
      "duration": 60,
      "capacity": 15,
      "availableSpots": 8,
      "price": 25,
      "level": "Beginner",
      "dayOfWeek": "Monday",
      "description": "A gentle introduction to basic yoga postures and breathing techniques.",
      "image": "optional_image_url"
    }
  ]
}
```

#### GET /yoga-classes?dayOfWeek=Monday&timeOfDay=Morning
Returns filtered yoga classes by day and/or time

#### POST /yoga-classes/{id}/book
Books a yoga class
Request body:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "555-0123",
  "emergencyContact": "Jane Doe 555-0124",
  "medicalConditions": "None"
}
```

### Step 4: Run the Application
```bash
# Start the development server
npm start

# Run on iOS simulator
npm run ios

# Run on Android emulator
npm run android

# Run in web browser
npm run web
```

## Project Structure

```
src/
├── components/           # Reusable UI components
│   ├── YogaClassCard.js     # Individual class card display
│   ├── SearchFilter.js      # Search and filter modal
│   └── LoadingSpinner.js    # Loading indicator
├── screens/             # Main application screens
│   ├── ClassListScreen.js   # Main list of classes with search
│   ├── ClassDetailsScreen.js # Detailed view of a single class
│   └── BookClassScreen.js    # Booking form and confirmation
├── services/            # API and external services
│   └── api.js              # API service for cloud integration
├── utils/               # Utility functions and configuration
│   ├── helpers.js          # Date/time formatting and filtering
│   └── config.js           # App configuration and API URLs
└── navigation/          # Navigation setup
    └── AppNavigator.js     # Stack navigator configuration
```

## Key Components

### ClassListScreen
- Main screen displaying all yoga classes
- Search and filter functionality
- Pull-to-refresh capability
- Navigation to class details and booking

### SearchFilter
- Modal component for filtering classes
- Filter by day of the week (Monday-Sunday)
- Filter by time of day (Morning, Afternoon, Evening)
- Apply and reset filter options

### YogaClassCard
- Displays individual class information
- Shows availability status
- Quick book button
- Professional card design

### ClassDetailsScreen
- Comprehensive class information
- Instructor details
- Booking button with availability check

### BookClassScreen
- Customer information form
- Form validation
- Booking confirmation
- Integration with cloud service

## Configuration

### API Configuration
Edit `src/utils/config.js` to configure:
- API base URL
- App theme colors
- Feature flags
- Endpoint paths

### Demo Mode
The app includes demo data for development and testing when the API is not available. Remove or modify the mock data in `ClassListScreen.js` once your API is configured.

## Search and Filter Features

### Day of Week Filter
- All (default)
- Monday through Sunday
- Filters classes based on `dayOfWeek` property

### Time of Day Filter
- All (default)
- Morning (before 12:00)
- Afternoon (12:00-17:00)
- Evening (after 17:00)
- Automatically categorizes based on class time

### Combined Filtering
- Apply both day and time filters simultaneously
- Real-time filtering without API calls
- Clear filter indication in UI

## Integration with Admin App

This customer app is designed to work with your existing admin yoga app. Ensure your cloud service provides:

1. **Consistent Data Structure**: Classes created in the admin app should be accessible through the customer API
2. **Real-time Availability**: Available spots should be updated when bookings are made
3. **Booking Management**: Customer bookings should be visible in the admin system

## Customization

### Styling
- Main theme color: `#def4f7` (purple)
- Modify colors in `src/utils/config.js`
- Component styles are in individual StyleSheet objects

### Features
- Enable/disable features in `src/utils/config.js`
- Add new filter options in `SearchFilter.js`
- Extend booking form in `BookClassScreen.js`

## Troubleshooting

### Common Issues

1. **API Connection Issues**
   - Verify API URL in config.js
   - Check network connectivity
   - Ensure API endpoints match expected structure

2. **Navigation Issues**
   - Ensure all navigation dependencies are installed
   - Check stack navigator configuration

3. **Styling Issues**
   - Verify React Native StyleSheet syntax
   - Check for proper import statements

### Development Tips

1. **Testing with Demo Data**
   - Use the included mock data for initial testing
   - Gradually replace with real API calls

2. **Debugging API Calls**
   - Check console logs for API errors
   - Use network inspection tools
   - Verify request/response formats

3. **Performance Optimization**
   - Implement pagination for large class lists
   - Add image optimization for class photos
   - Consider caching frequently accessed data

## Future Enhancements

Potential features to add:
- User authentication and profiles
- Booking history
- Push notifications for class reminders
- Payment integration
- Social features and reviews
- Waitlist functionality
- Calendar integration

## Support

For issues and questions:
1. Check the troubleshooting section
2. Verify API configuration
3. Review React Native and Expo documentation
4. Check component documentation in code comments
