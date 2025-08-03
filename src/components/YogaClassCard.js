import React, { memo, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Image,
} from 'react-native';
import { formatTime, formatDate } from '../utils/helpers';
import { getYogaClassImage } from '../utils/imageMapping';
import { useCart } from '../context/CartContext';

const YogaClassCard = memo(({ yogaClass, onPress, onAddToCart }) => {
  const {
    name,
    date,
    time,
    duration,
    capacity,
    availableSpots,
    price,
    level,
  } = yogaClass;

  const isFullyBooked = availableSpots <= 0;
  const imageUrl = getYogaClassImage(yogaClass);
  const { addToCart } = useCart();

  // Memoize callbacks to prevent unnecessary re-renders
  const handlePress = useCallback(() => {
    onPress(yogaClass);
  }, [onPress, yogaClass]);

  const handleAddToCart = useCallback(() => {
    addToCart(yogaClass);
  }, [addToCart, yogaClass]);

  // Function to get level badge colors
  const getLevelBadgeStyle = (level) => {
    const levelLower = level?.toLowerCase() || '';
    
    if (levelLower.includes('beginner')) {
      return { backgroundColor: 'rgba(76, 175, 79, 0.9)' }; // Green for beginner
    } else if (levelLower.includes('intermediate')) {
      return { backgroundColor: 'rgba(251, 172, 53, 0.9)' }; // Orange for intermediate  
    } else if (levelLower.includes('advanced')) {
      return { backgroundColor: 'rgba(250, 117, 117, 0.9)' }; // Red for advanced
    } else {
      return { backgroundColor: 'rgba(222, 244, 247, 0.9)' }; // Default blue
    }
  };

  return (
    <TouchableOpacity style={styles.card} onPress={handlePress}>
      <View style={styles.imageContainer}>
        <Image
          source={imageUrl}
          style={styles.image}
          defaultSource={require('../../assets/icon.png')}
        />
        <View style={[styles.levelBadge, getLevelBadgeStyle(level)]}>
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

        <View style={styles.details}>
          <View style={styles.detailRow}>
            <Text style={styles.detailLabel}>Day:</Text>
            <Text style={styles.detailValue}>{date}</Text>
          </View>
          <View style={styles.detailRow}>
            <Text style={styles.detailLabel}>Time:</Text>
            <Text style={styles.detailValue}>{formatTime(time)}</Text>
          </View>
          <View style={styles.detailRow}>
            <Text style={styles.detailLabel}>Duration:</Text>
            <Text style={styles.detailValue}>{duration} min</Text>
          </View>
          <View style={styles.detailRow}>
            <Text style={styles.detailLabel}>Available:</Text>
            <Text style={[
              styles.detailValue,
              isFullyBooked && styles.fullyBookedText
            ]}>
              {availableSpots}/{capacity} spots
            </Text>
          </View>
        </View>

        {/* Buttons */}
        {isFullyBooked ? (
          <View style={styles.buttonContainer}>
            <TouchableOpacity
              style={[styles.addToCartButton, styles.buttonDisabled, styles.fullWidthButton]}
              disabled={true}
            >
              <Text style={[styles.addToCartButtonText, styles.buttonTextDisabled]}>
                Fully Booked
              </Text>
            </TouchableOpacity>
          </View>
        ) : (
          <View style={styles.buttonContainer}>
            <TouchableOpacity
              style={[styles.addToCartButton, styles.fullWidthButton]}
              onPress={handleAddToCart}
            >
              <Text style={[styles.addToCartButtonText]}>
                Add to Cart
              </Text>
            </TouchableOpacity>
          </View>
        )}
      </View>
    </TouchableOpacity>
  );
});

// Add display name for debugging
YogaClassCard.displayName = 'YogaClassCard';

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
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
  },
  levelText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: '600',
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
    color: '#4caf4f',
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
    color: '#fa7575ff',
  },
  buttonContainer: {
    flexDirection: 'row',
    gap: 8,
  },
  addToCartButton: {
    flex: 1,
    backgroundColor: '#def4f7',
    paddingVertical: 12,
    borderRadius: 8,
    alignItems: 'center',
  },
  buttonDisabled: {
    backgroundColor: '#f0f0f0',
  },
  fullWidthButton: {
    flex: 0,
    width: '100%',
    borderWidth: 0,
  },
  addToCartButtonText: {
    color: '#661a72',
    fontSize: 14,
    fontWeight: '600',
  },
  buttonTextDisabled: {
    color: '#666',
  },
});

export default YogaClassCard;
