module.exports = {
  root: true,
  extends: [
    'eslint:recommended',
    'plugin:react/recommended',
    'plugin:react-native/all'
  ],
  plugins: [
    'react',
    'react-native'
  ],
  parser: '@babel/eslint-parser',
  parserOptions: {
    requireConfigFile: false,
    ecmaFeatures: {
      jsx: true,
    },
    ecmaVersion: 2020,
    sourceType: 'module',
  },
  env: {
    'react-native/react-native': true,
    es6: true,
    node: true,
  },
  settings: {
    react: {
      version: 'detect',
    },
  },
  rules: {
    // Unused variable detection
    'no-unused-vars': ['error', { 
      vars: 'all', 
      args: 'after-used', 
      ignoreRestSiblings: false,
      varsIgnorePattern: '^_',
      argsIgnorePattern: '^_'
    }],
    
    // Unused imports detection
    'no-unused-expressions': 'error',
    
    // React specific unused detection
    'react/jsx-uses-react': 'error',
    'react/jsx-uses-vars': 'error',
    'react/no-unused-state': 'error',
    
    // React Native specific
    'react-native/no-unused-styles': 'error',
    'react-native/no-color-literals': 'warn',
    'react-native/split-platform-components': 'error',
  },
  ignorePatterns: [
    'node_modules/',
    'ios/',
    'android/',
    '*.config.js'
  ]
};
