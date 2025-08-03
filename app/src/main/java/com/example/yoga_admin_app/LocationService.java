package com.example.yoga_admin_app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationService {
    private static final String TAG = "LocationService";
    private static final int LOCATION_UPDATE_MIN_TIME = 1000; // 1 second
    private static final int LOCATION_UPDATE_MIN_DISTANCE = 1; // 1 meter
    private static final int LOCATION_TIMEOUT = 10000; // 10 seconds timeout
    
    private Context context;
    private LocationManager locationManager;
    private LocationCallback locationCallback;
    private LocationListener locationListener;
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;
    
    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude, String address);
        void onLocationError(String error);
        void onPermissionRequired();
    }
    
    public LocationService(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.timeoutHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Enhanced location permission checking with granular control
     */
    public boolean hasLocationPermissions() {
        boolean hasFineLocation = ActivityCompat.checkSelfPermission(context, 
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarseLocation = ActivityCompat.checkSelfPermission(context, 
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        
        // Return true if we have at least one location permission
        return hasFineLocation || hasCoarseLocation;
    }
    
    /**
     * Check if we have high-accuracy location permission
     */
    public boolean hasHighAccuracyPermission() {
        return ActivityCompat.checkSelfPermission(context, 
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Enhanced location retrieval with timeout and fallback
     */
    public void getCurrentLocation(LocationCallback callback) {
        this.locationCallback = callback;
        
        // Check if location permissions are granted
        if (!hasLocationPermissions()) {
            callback.onPermissionRequired();
            return;
        }
        
        // Check if location services are enabled
        if (!isLocationEnabled()) {
            callback.onLocationError("Location services are disabled. Please enable GPS in settings.");
            return;
        }

        // Set up timeout to prevent indefinite waiting
        setupLocationTimeout();

        try {
            // Try to get last known location first for faster response
            Location lastKnownLocation = getLastKnownLocation();
            if (lastKnownLocation != null && isLocationRecentAndAccurate(lastKnownLocation)) {
                Log.d(TAG, "Using cached location");
                String address = getAddressFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                callback.onLocationReceived(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), address);
                cancelLocationTimeout();
                return;
            }

            // Create enhanced location listener
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d(TAG, "New location received");
                    
                    // Cancel timeout and stop listening for updates
                    cancelLocationTimeout();
                    stopLocationUpdates();
                    
                    // Get human-readable address
                    String address = getAddressFromLocation(location.getLatitude(), location.getLongitude());
                    
                    // Return the location
                    callback.onLocationReceived(location.getLatitude(), location.getLongitude(), address);
                }
                
                @Override
                public void onProviderEnabled(String provider) {
                    Log.d(TAG, "Provider enabled: " + provider);
                }
                
                @Override
                public void onProviderDisabled(String provider) {
                    Log.d(TAG, "Provider disabled: " + provider);
                    // Don't immediately fail, other providers might still work
                }
                
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.d(TAG, "Provider status changed: " + provider + ", status: " + status);
                }
            };
            
            // Request location updates from the best available provider
            requestLocationFromBestProvider();
            
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when requesting location", e);
            cancelLocationTimeout();
            callback.onLocationError("Location permission denied. Please grant location permission in app settings.");
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error when requesting location", e);
            cancelLocationTimeout();
            callback.onLocationError("Failed to get location: " + e.getMessage());
        }
    }

    /**
     * Get last known location from all providers
     */
    private Location getLastKnownLocation() {
        Location bestLocation = null;
        
        try {
            // Try GPS provider first (most accurate)
            if (hasHighAccuracyPermission() && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (gpsLocation != null) {
                    bestLocation = gpsLocation;
                }
            }
            
            // Try network provider if GPS not available or less accurate
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (networkLocation != null && (bestLocation == null || 
                    networkLocation.getAccuracy() < bestLocation.getAccuracy())) {
                    bestLocation = networkLocation;
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when getting last known location", e);
        }
        
        return bestLocation;
    }

    /**
     * Check if location is recent and accurate enough
     */
    private boolean isLocationRecentAndAccurate(Location location) {
        long maxAge = 2 * 60 * 1000; // 2 minutes
        float maxAccuracy = 100; // 100 meters
        
        long locationAge = System.currentTimeMillis() - location.getTime();
        return locationAge < maxAge && location.getAccuracy() < maxAccuracy;
    }

    /**
     * Request location from the best available provider
     */
    private void requestLocationFromBestProvider() throws SecurityException {
        boolean requestSent = false;
        
        // Try GPS first if we have fine location permission
        if (hasHighAccuracyPermission() && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_UPDATE_MIN_TIME,
                LOCATION_UPDATE_MIN_DISTANCE,
                locationListener
            );
            requestSent = true;
            Log.d(TAG, "Requesting location updates from GPS provider");
        }
        
        // Also request from network provider for faster initial fix
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                LOCATION_UPDATE_MIN_TIME,
                LOCATION_UPDATE_MIN_DISTANCE,
                locationListener
            );
            requestSent = true;
            Log.d(TAG, "Requesting location updates from Network provider");
        }
        
        if (!requestSent) {
            throw new RuntimeException("No location providers available");
        }
    }

    /**
     * Set up timeout for location requests
     */
    private void setupLocationTimeout() {
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "Location request timed out");
                stopLocationUpdates();
                if (locationCallback != null) {
                    locationCallback.onLocationError("Location request timed out. Please try again or check your GPS settings.");
                }
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, LOCATION_TIMEOUT);
    }

    /**
     * Cancel location timeout
     */
    private void cancelLocationTimeout() {
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
    }

    /**
     * Stop location updates and cleanup resources
     */
    public void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
                locationListener = null;
                Log.d(TAG, "Location updates stopped");
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception when stopping location updates", e);
            }
        }
        
        // Cancel any pending timeout
        cancelLocationTimeout();
    }
    
    /**
     * Check if location services are enabled
     */
    public boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) 
            || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    
    /**
     * Convert coordinates to human-readable address
     */
    private String getAddressFromLocation(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressString = new StringBuilder();
                
                // Build a readable address
                if (address.getFeatureName() != null) {
                    addressString.append(address.getFeatureName()).append(", ");
                }
                if (address.getThoroughfare() != null) {
                    addressString.append(address.getThoroughfare()).append(", ");
                }
                if (address.getSubLocality() != null) {
                    addressString.append(address.getSubLocality()).append(", ");
                }
                if (address.getLocality() != null) {
                    addressString.append(address.getLocality()).append(", ");
                }
                if (address.getAdminArea() != null) {
                    addressString.append(address.getAdminArea()).append(", ");
                }
                if (address.getCountryName() != null) {
                    addressString.append(address.getCountryName());
                }
                
                // Remove trailing comma and space
                String result = addressString.toString().trim();
                if (result.endsWith(",")) {
                    result = result.substring(0, result.length() - 1);
                }
                
                return result.isEmpty() ? "Unknown location" : result;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting address from location", e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in geocoding", e);
        }
        
        return "Lat: " + String.format("%.6f", latitude) + ", Lng: " + String.format("%.6f", longitude);
    }
    
    /**
     * Get distance between two locations in meters
     */
    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(lon1);
        
        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);
        
        return location1.distanceTo(location2);
    }
    
    /**
     * Format location for display
     */
    public static String formatLocation(double latitude, double longitude, String address) {
        if (address != null && !address.trim().isEmpty() && !address.equals("Unknown location")) {
            return address;
        } else {
            return String.format("%.6f, %.6f", latitude, longitude);
        }
    }
}
