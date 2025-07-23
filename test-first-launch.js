// Test script to verify first launch functionality
// Run this with: node test-first-launch.js

const { resetFirstLaunchFlag, checkIsFirstLaunch } = require('./src/utils/helpers');

async function testFirstLaunch() {
  console.log('Testing first launch functionality...');
  
  // Reset the flag
  console.log('1. Resetting first launch flag...');
  await resetFirstLaunchFlag();
  
  // Check if it's first launch (should be true)
  console.log('2. Checking if first launch...');
  const isFirst = await checkIsFirstLaunch();
  console.log('Is first launch:', isFirst);
  
  console.log('\nTest completed! Check the console output above.');
  console.log('When you run the app now, it should show DemoInfoScreen first.');
  console.log('After navigating through it once, subsequent launches should show ClassListScreen.');
}

// For React Native environment, we can't run this directly
// But this shows how the functions work
console.log('First launch test functions are available in src/utils/helpers.js');
console.log('Use resetFirstLaunchFlag() to reset for testing');
console.log('Use checkIsFirstLaunch() to check current state');
