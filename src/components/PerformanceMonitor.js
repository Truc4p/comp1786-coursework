import React, { useEffect, useRef } from 'react';
import { View, Text, StyleSheet } from 'react-native';

/**
 * Performance monitoring component to track render times
 * Only shows in development mode
 */
const PerformanceMonitor = ({ componentName, dataLength = 0 }) => {
  const renderStartTime = useRef(Date.now());
  const renderCount = useRef(0);
  const lastRenderTime = useRef(0);

  useEffect(() => {
    renderCount.current += 1;
    const renderTime = Date.now() - renderStartTime.current;
    lastRenderTime.current = renderTime;
    
    // Only log in development
    if (__DEV__) {
      console.log(`🔍 ${componentName} rendered in ${renderTime}ms (render #${renderCount.current}, ${dataLength} items)`);
      
      // Warn about slow renders
      if (renderTime > 100) {
        console.warn(`⚠️ Slow render detected in ${componentName}: ${renderTime}ms`);
      }
    }
  });

  // Reset timer for next render
  renderStartTime.current = Date.now();

  // Only show in development
  if (!__DEV__) {
    return null;
  }

  return (
    <View style={styles.container}>
      <Text style={styles.text}>
        {componentName}: {lastRenderTime.current}ms (#{renderCount.current})
      </Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    top: 0,
    right: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    padding: 4,
    borderRadius: 4,
    zIndex: 1000,
  },
  text: {
    color: 'white',
    fontSize: 10,
    fontFamily: 'monospace',
  },
});

export default PerformanceMonitor;
