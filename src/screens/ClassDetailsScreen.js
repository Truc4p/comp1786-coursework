import{ useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Image,
  SafeAreaView,
  Alert,
} from 'react-native';
import { formatTime } from '../utils/helpers';
import { getYogaClassImage } from '../utils/imageMapping';
import { useCart } from '../context/CartContext';
import ApiService from '../services/api';

const ClassDetailsScreen = ({ route, navigation }) => {
  const { yogaClass } = route.params;
  const { addToCart } = useCart();
  const [classInstances, setClassInstances] = useState([]);
  const [loadingInstances, setLoadingInstances] = useState(true);

  // Fetch class instances on component mount
  useEffect(() => {
    const fetchClassInstances = async () => {
      try {
        setLoadingInstances(true);
        const instances = await ApiService.getClassInstances(yogaClass.id);
        setClassInstances(instances);
      } catch (error) {
        setClassInstances([]);
      } finally {
        setLoadingInstances(false);
      }
    };

    fetchClassInstances();
  }, [yogaClass.id]);

  const {
    name,
    date,
    time,
    duration,
    capacity,
    availableSpots,
    price,
    level,
    description,
  } = yogaClass;

  const isFullyBooked = availableSpots <= 0;
  const imageUrl = getYogaClassImage(yogaClass);

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

  const handleAddToCart = () => {
    if (isFullyBooked) {
      return;
    }
    addToCart(yogaClass);
    Alert.alert(
      'Added to Cart',
      `${name} has been added to your cart!`,
      [
        { text: 'Continue Shopping', style: 'cancel' },
        { text: 'View Cart', onPress: () => navigation.navigate('Cart') },
      ]
    );
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
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
            <Text style={styles.className}>{name}</Text>
            <View style={styles.priceContainer}>
              <Text style={styles.price}>${price}</Text>
              <Text style={styles.priceSubtext}>per session</Text>
            </View>
          </View>

          <View style={styles.detailsSection}>
            <Text style={styles.sectionTitle}>Class Details</Text>
            
            <View style={styles.detailsCard}>
              <View style={styles.detailRow}>
                <View style={styles.detailItem}>
                  <Text style={styles.detailLabel}>Day</Text>
                  <Text style={styles.detailValue}>{date}</Text>
                </View>
                <View style={styles.detailItem}>
                  <Text style={styles.detailLabel}>Time</Text>
                  <Text style={styles.detailValue}>{formatTime(time)}</Text>
                </View>
              </View>
              
              <View style={styles.detailDivider} />
              
              <View style={styles.detailRow}>
                <View style={styles.detailItem}>
                  <Text style={styles.detailLabel}>Duration</Text>
                  <Text style={styles.detailValue}>{duration} minutes</Text>
                </View>
                <View style={styles.detailItem}>
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
              <View style={styles.descriptionCard}>
                <Text style={styles.description}>{description}</Text>
              </View>
            </View>
          )}

          {/* Class Instances Section */}
          {!loadingInstances && classInstances.length > 0 && (
            <View style={styles.instancesSection}>
              <Text style={styles.sectionTitle}>Upcoming Sessions</Text>
              <View style={styles.instancesCard}>
                {classInstances.map((instance, index) => (
                  <View key={instance.id || index} style={styles.instanceItem}>
                    <View style={styles.instanceLeft}>
                      <Text style={styles.instanceDate}>{instance.date}</Text>
                      <Text style={styles.instanceInstructor}>with {instance.instructor}</Text>
                    </View>
                    <View style={styles.instanceRight}>
                      <Text style={styles.instanceTime}>{formatTime(instance.time || time)}</Text>
                      <Text style={styles.instanceDuration}>{instance.duration || duration} min</Text>
                    </View>
                  </View>
                ))}
              </View>
            </View>
          )}
        </View>
      </ScrollView>

      <View style={styles.footer}>
        <View style={styles.priceContainer}>
          <Text style={styles.footerPrice}>${price}</Text>
          <Text style={styles.priceLabel}>per class</Text>
        </View>
        <TouchableOpacity
          style={[
            styles.addToCartButton,
            isFullyBooked && styles.addToCartButtonDisabled
          ]}
          onPress={handleAddToCart}
          disabled={isFullyBooked}
        >
          <Text style={[
            styles.addToCartButtonText,
            isFullyBooked && styles.addToCartButtonTextDisabled
          ]}>
            {isFullyBooked ? 'Fully Booked' : 'Add to Cart'}
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
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 15,
  },
  levelText: {
    color: '#fff',
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
  priceContainer: {
    alignItems: 'flex-end',
  },
  price: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#4caf4f',
  },
  priceSubtext: {
    fontSize: 12,
    color: '#666',
    marginTop: 2,
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
  detailsCard: {
    backgroundColor: '#f8f9fa',
    borderRadius: 12,
    padding: 20,
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  detailItem: {
    flex: 1,
    alignItems: 'center',
  },
  detailDivider: {
    height: 1,
    backgroundColor: '#e9ecef',
    marginVertical: 15,
  },
  detailLabel: {
    fontSize: 14,
    color: '#666',
    marginBottom: 4,
    textAlign: 'center',
  },
  detailValue: {
    fontSize: 16,
    color: '#333',
    fontWeight: '600',
    textAlign: 'center',
  },
  fullyBookedText: {
    color: '#fa7575ff',
  },
  descriptionSection: {
    marginBottom: 25,
  },
  descriptionCard: {
    backgroundColor: '#f8f9fa',
    borderRadius: 12,
    padding: 20,
  },
  description: {
    fontSize: 16,
    color: '#666',
    lineHeight: 24,
  },
  instructorSection: {
    marginBottom: 20,
  },
  instructorCard: {
    backgroundColor: '#f8f9fa',
    borderRadius: 12,
    padding: 20,
  },
  instructorInfo: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  instructorAvatar: {
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: '#def4f7',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 15,
  },
  instructorInitial: {
    color: '#661a72',
    fontSize: 24,
    fontWeight: 'bold',
  },
  instructorDetails: {
    flex: 1,
  },
  instructorName: {
    fontSize: 18,
    fontWeight: '600',
    color: '#333',
    marginBottom: 4,
  },
  instructorTitle: {
    fontSize: 14,
    color: '#666',
    marginBottom: 2,
  },
  instructorExperience: {
    fontSize: 12,
    color: '#999',
  },
  footer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingVertical: 15,
    backgroundColor: '#fff',
  },
  priceContainer: {
    flex: 0,
  },
  footerPrice: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#4caf4f',
  },
  priceLabel: {
    fontSize: 12,
    color: '#666',
  },
  addToCartButton: {
    backgroundColor: '#def4f7',
    paddingVertical: 15,
    paddingHorizontal: 30,
    borderRadius: 10,
    minWidth: 150,
    alignItems: 'center',
  },
  addToCartButtonDisabled: {
    backgroundColor: '#f0f0f0',
  },
  addToCartButtonText: {
    color: '#661a72',
    fontSize: 16,
    fontWeight: '600',
  },
  addToCartButtonTextDisabled: {
    color: '#666',
  },
  // Class instances section styles
  instancesSection: {
    marginBottom: 25,
  },
  instancesCard: {
    backgroundColor: '#fff',
    borderRadius: 15,
    marginTop: 10,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
  },
  instanceItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 15,
    paddingHorizontal: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  instanceLeft: {
    flex: 1,
  },
  instanceRight: {
    alignItems: 'flex-end',
  },
  instanceDate: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
    marginBottom: 4,
  },
  instanceInstructor: {
    fontSize: 14,
    color: '#666',
  },
  instanceTime: {
    fontSize: 16,
    fontWeight: '600',
    color: '#4caf4f',
    marginBottom: 4,
  },
  instanceDuration: {
    fontSize: 14,
    color: '#666',
  },
});

export default ClassDetailsScreen;
