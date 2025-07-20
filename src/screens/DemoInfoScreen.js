import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
} from 'react-native';

const DemoInfoScreen = ({ navigation }) => {
  const features = [
    {
      icon: '🧘‍♀️',
      title: 'Browse Classes',
      description: 'View all available yoga classes with detailed information including instructor, schedule, and pricing.',
    },
    {
      icon: '🔍',
      title: 'Search & Filter',
      description: 'Filter classes by day of the week (Monday-Sunday) and time of day (Morning, Afternoon, Evening).',
    },
    {
      icon: '📱',
      title: 'Cloud Integration',
      description: 'Connects to your cloud service to fetch real-time class information and availability.',
    },
    {
      icon: '📅',
      title: 'Easy Booking',
      description: 'Book classes with a simple form that collects customer information and confirms reservations.',
    },
    {
      icon: '🔄',
      title: 'Real-time Updates',
      description: 'Pull-to-refresh functionality keeps class information and availability up to date.',
    },
    {
      icon: '💜',
      title: 'Modern Design',
      description: 'Clean, intuitive interface designed for optimal user experience on mobile devices.',
    },
  ];

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      <View style={styles.header}>
        <Text style={styles.title}>Customer Yoga App</Text>
        <Text style={styles.subtitle}>
          A hybrid React Native app for booking yoga classes
        </Text>
      </View>

      <View style={styles.featuresSection}>
        <Text style={styles.sectionTitle}>Key Features</Text>
        {features.map((feature, index) => (
          <View key={index} style={styles.featureCard}>
            <Text style={styles.featureIcon}>{feature.icon}</Text>
            <View style={styles.featureContent}>
              <Text style={styles.featureTitle}>{feature.title}</Text>
              <Text style={styles.featureDescription}>{feature.description}</Text>
            </View>
          </View>
        ))}
      </View>

      <View style={styles.configSection}>
        <Text style={styles.sectionTitle}>Configuration Required</Text>
        <View style={styles.configCard}>
          <Text style={styles.configText}>
            📡 <Text style={styles.bold}>API Configuration:</Text> Update the API_BASE_URL in src/utils/config.js to connect to your cloud service.
          </Text>
        </View>
        <View style={styles.configCard}>
          <Text style={styles.configText}>
            🔗 <Text style={styles.bold}>Cloud Service:</Text> Ensure your cloud service provides the required endpoints for yoga classes and bookings.
          </Text>
        </View>
      </View>

      <TouchableOpacity
        style={styles.startButton}
        onPress={() => navigation.navigate('ClassList')}
      >
        <Text style={styles.startButtonText}>Explore Yoga Classes</Text>
      </TouchableOpacity>

      <View style={styles.footer}>
        <Text style={styles.footerText}>
          This app is built with React Native and connects to your existing admin yoga app's cloud service.
        </Text>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F3E5F5',
  },
  header: {
    alignItems: 'center',
    paddingVertical: 40,
    paddingHorizontal: 20,
    backgroundColor: '#def4f7',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#661a72',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    color: '#661a72',
    textAlign: 'center',
    opacity: 0.9,
  },
  featuresSection: {
    padding: 20,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 20,
  },
  featureCard: {
    flexDirection: 'row',
    backgroundColor: '#fff',
    padding: 20,
    borderRadius: 15,
    marginBottom: 15,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.1,
    shadowRadius: 3.84,
  },
  featureIcon: {
    fontSize: 30,
    marginRight: 15,
    alignSelf: 'flex-start',
  },
  featureContent: {
    flex: 1,
  },
  featureTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 5,
  },
  featureDescription: {
    fontSize: 14,
    color: '#666',
    lineHeight: 20,
  },
  configSection: {
    paddingHorizontal: 20,
    paddingBottom: 20,
  },
  configCard: {
    backgroundColor: '#fff',
    padding: 20,
    borderRadius: 15,
    marginBottom: 15,
  },
  configText: {
    fontSize: 14,
    color: '#666',
    lineHeight: 20,
  },
  bold: {
    fontWeight: '600',
    color: '#333',
  },
  startButton: {
    backgroundColor: '#def4f7',
    marginHorizontal: 20,
    paddingVertical: 15,
    borderRadius: 10,
    alignItems: 'center',
    marginBottom: 20,
  },
  startButtonText: {
    color: '#661a72',
    fontSize: 16,
    fontWeight: '600',
  },
  footer: {
    paddingHorizontal: 20,
    paddingVertical: 30,
    alignItems: 'center',
  },
  footerText: {
    fontSize: 12,
    color: '#666',
    textAlign: 'center',
    lineHeight: 18,
  },
});

export default DemoInfoScreen;
