import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import ApiService from '../services/api';
import { testPasswordHashing } from '../utils/passwordMigration';

const PasswordMigrationDebug = () => {
  const [migrating, setMigrating] = useState(false);

  const handleMigration = async () => {
    Alert.alert(
      'Migrate Passwords',
      'This will hash all existing plain text passwords. This should only be done once. Continue?',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Migrate',
          onPress: async () => {
            setMigrating(true);
            try {
              const result = await ApiService.migratePasswords();
              if (result.success) {
                Alert.alert(
                  'Migration Successful',
                  `${result.migratedCount} users migrated successfully.`
                );
              } else {
                Alert.alert('Migration Failed', result.message);
              }
            } catch (error) {
              Alert.alert('Migration Error', error.message);
            } finally {
              setMigrating(false);
            }
          }
        }
      ]
    );
  };

  const handleTest = () => {
    testPasswordHashing();
    Alert.alert('Test Complete', 'Check the console for test results');
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Password Security Debug</Text>
      
      <TouchableOpacity 
        style={styles.button} 
        onPress={handleTest}
        disabled={migrating}
      >
        <Text style={styles.buttonText}>Test Password Hashing</Text>
      </TouchableOpacity>

      <TouchableOpacity 
        style={[styles.button, styles.migrateButton]} 
        onPress={handleMigration}
        disabled={migrating}
      >
        <Text style={styles.buttonText}>
          {migrating ? 'Migrating...' : 'Migrate Existing Passwords'}
        </Text>
      </TouchableOpacity>

      <Text style={styles.warning}>
        ⚠️ Password migration should only be run once!
      </Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    padding: 20,
    backgroundColor: '#f0f0f0',
    margin: 10,
    borderRadius: 8,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 15,
    textAlign: 'center',
  },
  button: {
    backgroundColor: '#007AFF',
    padding: 15,
    borderRadius: 8,
    marginBottom: 10,
    alignItems: 'center',
  },
  migrateButton: {
    backgroundColor: '#FF9500',
  },
  buttonText: {
    color: 'white',
    fontWeight: 'bold',
  },
  warning: {
    fontSize: 12,
    color: '#FF3B30',
    textAlign: 'center',
    marginTop: 10,
    fontStyle: 'italic',
  },
});

export default PasswordMigrationDebug;
