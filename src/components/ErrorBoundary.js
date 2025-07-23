import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Alert } from 'react-native';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null, errorInfo: null };
  }

  static getDerivedStateFromError(error) {
    // Update state so the next render will show the fallback UI.
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    // You can also log the error to an error reporting service
    console.error('ErrorBoundary caught an error:', error, errorInfo);
    this.setState({
      error: error,
      errorInfo: errorInfo
    });
  }

  render() {
    if (this.state.hasError) {
      // You can render any custom fallback UI
      return (
        <View style={styles.container}>
          <Text style={styles.errorIcon}>⚠️</Text>
          <Text style={styles.errorTitle}>Oops! Something went wrong</Text>
          <Text style={styles.errorMessage}>
            The app encountered an unexpected error. Please try restarting the app.
          </Text>
          
          <TouchableOpacity 
            style={styles.retryButton}
            onPress={() => this.setState({ hasError: false, error: null, errorInfo: null })}
          >
            <Text style={styles.retryButtonText}>Try Again</Text>
          </TouchableOpacity>

          <TouchableOpacity 
            style={styles.detailsButton}
            onPress={() => {
              Alert.alert(
                'Error Details',
                `Error: ${this.state.error?.message || 'Unknown error'}\n\nStack: ${this.state.errorInfo?.componentStack || 'No stack trace'}`
              );
            }}
          >
            <Text style={styles.detailsButtonText}>Show Details</Text>
          </TouchableOpacity>
        </View>
      );
    }

    return this.props.children;
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F3E5F5',
    paddingHorizontal: 40,
  },
  errorIcon: {
    fontSize: 80,
    marginBottom: 20,
  },
  errorTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 16,
    textAlign: 'center',
  },
  errorMessage: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
    lineHeight: 24,
    marginBottom: 40,
  },
  retryButton: {
    backgroundColor: '#661a72',
    paddingHorizontal: 32,
    paddingVertical: 16,
    borderRadius: 12,
    marginBottom: 16,
  },
  retryButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  detailsButton: {
    backgroundColor: '#def4f7',
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderRadius: 8,
  },
  detailsButtonText: {
    color: '#661a72',
    fontSize: 14,
    fontWeight: '500',
  },
});

export default ErrorBoundary;
