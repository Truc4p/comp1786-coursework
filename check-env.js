#!/usr/bin/env node

// Simple script to check environment configuration
console.log('🔍 Checking Yoga App Environment Configuration...\n');

// Check if .env file exists
const fs = require('fs');
const path = require('path');

const envPath = path.join(__dirname, '.env');
const envExamplePath = path.join(__dirname, '.env.example');

console.log('📁 Files:');
console.log(`  .env file: ${fs.existsSync(envPath) ? '✅ Found' : '❌ Missing'}`);
console.log(`  .env.example file: ${fs.existsSync(envExamplePath) ? '✅ Found' : '❌ Missing'}`);

if (!fs.existsSync(envPath)) {
  console.log('\n❌ Missing .env file!');
  console.log('   Run: cp .env.example .env');
  console.log('   Then edit .env with your Firebase URL');
  process.exit(1);
}

// Parse .env file
console.log('\n🔧 Environment Variables:');
const envContent = fs.readFileSync(envPath, 'utf8');
const envLines = envContent.split('\n').filter(line => line.trim() && !line.startsWith('#'));

const envVars = {};
envLines.forEach(line => {
  const [key, value] = line.split('=');
  if (key && value) {
    envVars[key.trim()] = value.trim();
  }
});

// Check required variables
const requiredVars = [
  'EXPO_PUBLIC_API_BASE_URL',
  'EXPO_PUBLIC_APP_NAME',
  'EXPO_PUBLIC_APP_VERSION'
];

let allGood = true;

requiredVars.forEach(varName => {
  if (envVars[varName]) {
    console.log(`  ${varName}: ✅ ${envVars[varName]}`);
  } else {
    console.log(`  ${varName}: ❌ Missing`);
    allGood = false;
  }
});

// Validate Firebase URL
if (envVars['EXPO_PUBLIC_API_BASE_URL']) {
  const url = envVars['EXPO_PUBLIC_API_BASE_URL'];
  if (url.includes('firebasedatabase.app')) {
    console.log(`  Firebase URL format: ✅ Valid`);
  } else {
    console.log(`  Firebase URL format: ⚠️  Doesn't look like a Firebase URL`);
  }
}

console.log('\n🎯 Feature Flags:');
const featureFlags = [
  'EXPO_PUBLIC_ENABLE_PUSH_NOTIFICATIONS',
  'EXPO_PUBLIC_ENABLE_PAYMENT_INTEGRATION', 
  'EXPO_PUBLIC_ENABLE_SOCIAL_LOGIN',
  'EXPO_PUBLIC_ENABLE_AUTHENTICATION',
  'EXPO_PUBLIC_ENABLE_GUEST_MODE'
];

featureFlags.forEach(flag => {
  const value = envVars[flag] || 'not set';
  console.log(`  ${flag.replace('EXPO_PUBLIC_ENABLE_', '')}: ${value}`);
});

if (allGood) {
  console.log('\n✅ Environment configuration looks good!');
  console.log('   You can now run: npm start');
} else {
  console.log('\n❌ Please fix the missing environment variables');
  console.log('   See ENVIRONMENT_SETUP.md for detailed instructions');
  process.exit(1);
}
