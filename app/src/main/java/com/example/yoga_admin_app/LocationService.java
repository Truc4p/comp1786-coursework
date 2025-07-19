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
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationService {
    private static final String TAG = "LocationService";
    private static final int LOCATION_UPDATE_MIN_TIME = 1000; // 1 second
    private static final int LOCATION_UPDATE_MIN_DISTANCE = 1; // 1 meter
    
    private Context context;
    private LocationManager locationManager;
    private LocationCallback locationCallback;
    private LocationListener locationListener;
    
    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude, String address);
        void onLocationError(String error);
        void onPermissionRequired();
    }
    
    public LocationService(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }
    
    /**
     * Get current location automatically
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
        
        try {
            // Create location listener
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d(TAG, "Location received: " + location.getLatitude() + ", " + location.getLongitude());
                    
                    // Stop listening for updates
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
                    callback.onLocationError("Location provider disabled: " + provider);
                }
                
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.d(TAG, "Provider status changed: " + provider + ", status: " + status);
                }
            };
            
            // Try to get location from GPS first, then network
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_MIN_TIME,
                    LOCATION_UPDATE_MIN_DISTANCE,
                    locationListener
                );
                
                // Also try to get last known location immediately
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    long timeDifference = System.currentTimeMillis() - lastKnownLocation.getTime();
                    if (timeDifference < 5 * 60 * 1000) { // Use if less than 5 minutes old
                        stopLocationUpdates();
                        String address = getAddressFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        callback.onLocationReceived(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), address);
                        return;
                    }
                }
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_MIN_TIME,
                    LOCATION_UPDATE_MIN_DISTANCE,
                    locationListener
                );
                
                // Try to get last known network location
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (lastKnownLocation != null) {
                    long timeDifference = System.currentTimeMillis() - lastKnownLocation.getTime();
                    if (timeDifference < 10 * 60 * 1000) { // Use if less than 10 minutes old
                        stopLocationUpdates();
                        String address = getAddressFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        callback.onLocationReceived(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), address);
                        return;
                    }
                }
            } else {
                callback.onLocationError("No location providers available");
            }
            
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when requesting location", e);
            callback.onLocationError("Location permission denied");
        } catch (Exception e) {
            Log.e(TAG, "Error getting location", e);
            callback.onLocationError("Error getting location: " + e.getMessage());
        }
    }
    
    /**
     * Stop location updates
     */
    public void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception when stopping location updates", e);
            }
        }
    }
    
    /**
     * Check if location permissions are granted
     */
    public boolean hasLocationPermissions() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
