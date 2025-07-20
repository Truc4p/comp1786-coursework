import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Image,
  SafeAreaView,
} from 'react-native';
import { formatTime, formatDate } from '../utils/helpers';

const ClassDetailsScreen = ({ route, navigation }) => {
  const { yogaClass } = route.params;

  const {
    name,
    instructor,
    date,
    time,
    duration,
    capacity,
    availableSpots,
    price,
    level,
    description,
    image,
  } = yogaClass;

  const isFullyBooked = availableSpots <= 0;

  const handleBook = () => {
    if (isFullyBooked) {
      return;
    }
    navigation.navigate('BookClass', { yogaClass });
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
        <View style={styles.imageContainer}>
          {image ? (
            <Image source={{ uri: image }} style={styles.image} />
          ) : (
            <View style={styles.placeholderImage}>
              <Text style={styles.placeholderText}>🧘</Text>
            </View>
          )}
          <View style={styles.levelBadge}>
            <Text style={styles.levelText}>{level || 'All Levels'}</Text>
          </View>
        </View>

        <View style={styles.content}>
          <View style={styles.header}>
            <Text style={styles.className}>{name}</Text>
            <Text style={styles.price}>${price}</Text>
          </View>

          <Text style={styles.instructor}>with {instructor}</Text>

          <View style={styles.detailsSection}>
            <Text style={styles.sectionTitle}>Class Details</Text>
            
            <View style={styles.detailGrid}>
              <View style={styles.detailItem}>
                <Text style={styles.detailIcon}>📅</Text>
                <View style={styles.detailContent}>
                  <Text style={styles.detailLabel}>Date</Text>
                  <Text style={styles.detailValue}>{formatDate(date)}</Text>
                </View>
              </View>

              <View style={styles.detailItem}>
                <Text style={styles.detailIcon}>⏰</Text>
                <View style={styles.detailContent}>
                  <Text style={styles.detailLabel}>Time</Text>
                  <Text style={styles.detailValue}>{formatTime(time)}</Text>
                </View>
              </View>

              <View style={styles.detailItem}>
                <Text style={styles.detailIcon}>⏱️</Text>
                <View style={styles.detailContent}>
                  <Text style={styles.detailLabel}>Duration</Text>
                  <Text style={styles.detailValue}>{duration} minutes</Text>
                </View>
              </View>

              <View style={styles.detailItem}>
                <Text style={styles.detailIcon}>👥</Text>
                <View style={styles.detailContent}>
                  <Text style={styles.detailLabel}>Available Spots</Text>
                  <Text style={[
                    styles.detailValue,
                    isFullyBooked && styles.fullyBookedText
                  ]}>
                    {availableSpots} of {capacity}
                  </Text>
                </View>
              </View>
            </View>
          </View>

          {description && (
            <View style={styles.descriptionSection}>
              <Text style={styles.sectionTitle}>About This Class</Text>
              <Text style={styles.description}>{description}</Text>
            </View>
          )}

          <View style={styles.instructorSection}>
            <Text style={styles.sectionTitle}>Instructor</Text>
            <View style={styles.instructorInfo}>
              <View style={styles.instructorAvatar}>
                <Text style={styles.instructorInitial}>
                  {instructor.charAt(0).toUpperCase()}
                </Text>
              </View>
              <View style={styles.instructorDetails}>
                <Text style={styles.instructorName}>{instructor}</Text>
                <Text style={styles.instructorTitle}>Certified Yoga Instructor</Text>
              </View>
            </View>
          </View>
        </View>
      </ScrollView>

      <View style={styles.footer}>
        <View style={styles.priceContainer}>
          <Text style={styles.footerPrice}>${price}</Text>
          <Text style={styles.priceLabel}>per class</Text>
        </View>
        <TouchableOpacity
          style={[
            styles.bookButton,
            isFullyBooked && styles.bookButtonDisabled
          ]}
          onPress={handleBook}
          disabled={isFullyBooked}
        >
          <Text style={[
            styles.bookButtonText,
            isFullyBooked && styles.bookButtonTextDisabled
          ]}>
            {isFullyBooked ? 'Fully Booked' : 'Book This Class'}
          </Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  scrollView: {
    flex: 1,
  },
  imageContainer: {
    position: 'relative',
    height: 250,
  },
  image: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  placeholderImage: {
    width: '100%',
    height: '100%',
    backgroundColor: '#f0f0f0',
    justifyContent: 'center',
    alignItems: 'center',
  },
  placeholderText: {
    fontSize: 80,
  },
  levelBadge: {
    position: 'absolute',
    top: 20,
    right: 20,
    backgroundColor: 'rgba(222, 244, 247, 0.9)',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 15,
  },
  levelText: {
    color: '#661a72',
    fontSize: 14,
    fontWeight: '600',
  },
  content: {
    padding: 20,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 8,
  },
  className: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    flex: 1,
    marginRight: 15,
  },
  price: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#661a72',
  },
  instructor: {
    fontSize: 16,
    color: '#666',
    marginBottom: 25,
    fontStyle: 'italic',
  },
  detailsSection: {
    marginBottom: 25,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#333',
    marginBottom: 15,
  },
  detailGrid: {
    gap: 15,
  },
  detailItem: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  detailIcon: {
    fontSize: 20,
    marginRight: 15,
    width: 25,
  },
  detailContent: {
    flex: 1,
  },
  detailLabel: {
    fontSize: 14,
    color: '#666',
    marginBottom: 2,
  },
  detailValue: {
    fontSize: 16,
    color: '#333',
    fontWeight: '500',
  },
  fullyBookedText: {
    color: '#ff4444',
  },
  descriptionSection: {
    marginBottom: 25,
  },
  description: {
    fontSize: 16,
    color: '#666',
    lineHeight: 24,
  },
  instructorSection: {
    marginBottom: 20,
  },
  instructorInfo: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  instructorAvatar: {
    width: 50,
    height: 50,
    borderRadius: 25,
    backgroundColor: '#def4f7',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 15,
  },
  instructorInitial: {
    color: '#661a72',
    fontSize: 20,
    fontWeight: 'bold',
  },
  instructorDetails: {
    flex: 1,
  },
  instructorName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 2,
  },
  instructorTitle: {
    fontSize: 14,
    color: '#666',
  },
  footer: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 15,
    borderTopWidth: 1,
    borderTopColor: '#e0e0e0',
    backgroundColor: '#fff',
  },
  priceContainer: {
    flex: 1,
  },
  footerPrice: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#661a72',
  },
  priceLabel: {
    fontSize: 12,
    color: '#666',
  },
  bookButton: {
    backgroundColor: '#def4f7',
    paddingVertical: 15,
    paddingHorizontal: 30,
    borderRadius: 10,
    minWidth: 150,
    alignItems: 'center',
  },
  bookButtonDisabled: {
    backgroundColor: '#ccc',
  },
  bookButtonText: {
    color: '#661a72',
    fontSize: 16,
    fontWeight: '600',
  },
  bookButtonTextDisabled: {
    color: '#666',
  },
});

export default ClassDetailsScreen;
