import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Image,
} from 'react-native';
import { formatTime, formatDate } from '../utils/helpers';

const YogaClassCard = ({ yogaClass, onPress, onBook }) => {
  const {
    id,
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

  return (
    <TouchableOpacity style={styles.card} onPress={() => onPress(yogaClass)}>
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
          <Text style={styles.className} numberOfLines={2}>
            {name}
          </Text>
          <Text style={styles.price}>${price}</Text>
        </View>

        <Text style={styles.instructor}>with {instructor}</Text>

        <View style={styles.details}>
          <View style={styles.detailRow}>
            <Text style={styles.detailLabel}>📅 Date:</Text>
            <Text style={styles.detailValue}>{formatDate(date)}</Text>
          </View>
          <View style={styles.detailRow}>
            <Text style={styles.detailLabel}>⏰ Time:</Text>
            <Text style={styles.detailValue}>{formatTime(time)}</Text>
          </View>
          <View style={styles.detailRow}>
            <Text style={styles.detailLabel}>⏱️ Duration:</Text>
            <Text style={styles.detailValue}>{duration} min</Text>
          </View>
          <View style={styles.detailRow}>
            <Text style={styles.detailLabel}>👥 Available:</Text>
            <Text style={[
              styles.detailValue,
              isFullyBooked && styles.fullyBookedText
            ]}>
              {availableSpots}/{capacity} spots
            </Text>
          </View>
        </View>

        {description && (
          <Text style={styles.description} numberOfLines={2}>
            {description}
          </Text>
        )}

        <TouchableOpacity
          style={[
            styles.bookButton,
            isFullyBooked && styles.bookButtonDisabled
          ]}
          onPress={() => onBook(yogaClass)}
          disabled={isFullyBooked}
        >
          <Text style={[
            styles.bookButtonText,
            isFullyBooked && styles.bookButtonTextDisabled
          ]}>
            {isFullyBooked ? 'Fully Booked' : 'Book Now'}
          </Text>
        </TouchableOpacity>
      </View>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#fff',
    borderRadius: 15,
    marginHorizontal: 20,
    marginVertical: 8,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.1,
    shadowRadius: 3.84,
    elevation: 5,
    overflow: 'hidden',
  },
  imageContainer: {
    position: 'relative',
    height: 150,
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
    fontSize: 40,
  },
  levelBadge: {
    position: 'absolute',
    top: 10,
    right: 10,
    backgroundColor: 'rgba(139, 76, 247, 0.9)',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  levelText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '500',
  },
  content: {
    padding: 16,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 4,
  },
  className: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    flex: 1,
    marginRight: 10,
  },
  price: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#8B4CF7',
  },
  instructor: {
    fontSize: 14,
    color: '#666',
    marginBottom: 12,
    fontStyle: 'italic',
  },
  details: {
    marginBottom: 12,
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 4,
  },
  detailLabel: {
    fontSize: 14,
    color: '#666',
    flex: 1,
  },
  detailValue: {
    fontSize: 14,
    color: '#333',
    fontWeight: '500',
    flex: 1,
    textAlign: 'right',
  },
  fullyBookedText: {
    color: '#ff4444',
  },
  description: {
    fontSize: 14,
    color: '#666',
    lineHeight: 20,
    marginBottom: 12,
  },
  bookButton: {
    backgroundColor: '#8B4CF7',
    paddingVertical: 12,
    borderRadius: 8,
    alignItems: 'center',
  },
  bookButtonDisabled: {
    backgroundColor: '#ccc',
  },
  bookButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
  bookButtonTextDisabled: {
    color: '#666',
  },
});

export default YogaClassCard;
