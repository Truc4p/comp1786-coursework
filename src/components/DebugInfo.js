import React, { useEffect } from 'react';
import { View, Text, StyleSheet } from 'react-native';

const DebugInfo = ({ children, componentName }) => {
  useEffect(() => {
    console.log(`✅ ${componentName} mounted successfully`);
    return () => {
      console.log(`🔄 ${componentName} unmounted`);
    };
  }, [componentName]);

  try {
    return children;
  } catch (error) {
    console.error(`❌ Error in ${componentName}:`, error);
    return (
      <View style={styles.errorContainer}>
        <Text style={styles.errorText}>
          Error in {componentName}: {error.message}
        </Text>
      </View>
    );
  }
};

const styles = StyleSheet.create({
  errorContainer: {
    padding: 20,
    backgroundColor: '#ffebee',
    margin: 10,
    borderRadius: 8,
  },
  errorText: {
    color: '#c62828',
    fontSize: 14,
  },
});

export default DebugInfo;
