import js from '@eslint/js';
import react from 'eslint-plugin-react';
import reactNative from 'eslint-plugin-react-native';

export default [
  js.configs.recommended,
  {
    files: ['**/*.{js,jsx,mjs,cjs}'],
    plugins: {
      react,
      'react-native': reactNative,
    },
    languageOptions: {
      parserOptions: {
        ecmaFeatures: {
          jsx: true,
        },
        ecmaVersion: 2020,
        sourceType: 'module',
      },
      globals: {
        ...globalThis,
        __DEV__: 'readonly',
        fetch: 'readonly',
        console: 'readonly',
        process: 'readonly',
        global: 'readonly',
        require: 'readonly',
        module: 'readonly',
        exports: 'readonly',
        Buffer: 'readonly',
      },
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
    },
  },
  {
    ignores: [
      'node_modules/',
      'ios/',
      'android/',
      '*.config.js',
      '.expo/',
      'build/',
    ],
  },
];
