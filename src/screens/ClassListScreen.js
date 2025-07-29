import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  RefreshControl,
  Alert,
  SafeAreaView,
} from 'react-native';
import YogaClassCard from '../components/YogaClassCard';
import SearchFilter from '../components/SearchFilter';
import LoadingSpinner from '../components/LoadingSpinner';
import PasswordMigrationDebug from '../components/PasswordMigrationDebug';
import ApiService from '../services/api';
import { filterClassesBySearchCriteria } from '../utils/helpers';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';

const ClassListScreen = ({ navigation }) => {
  const [classes, setClasses] = useState([]);
  const [filteredClasses, setFilteredClasses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [showFilter, setShowFilter] = useState(false);
  const [showMenu, setShowMenu] = useState(false);
  const [filters, setFilters] = useState({ dayOfWeek: 'All', timeOfDay: 'All' });
  const { getCartItemCount } = useCart();
  const { user } = useAuth();

  const loadClasses = useCallback(async () => {
    try {
      setLoading(true);
      console.log('Loading classes from Firebase...');
      const response = await ApiService.getYogaClasses();
      // console.log('API response received:', response);
      console.log('Response type:', typeof response, 'Length:', response?.length);
      
      // Handle the response
      if (response && Array.isArray(response) && response.length > 0) {
        console.log('Using Firebase data with', response.length, 'classes');
        setClasses(response);
      } else {
        console.log('No classes found in Firebase response');
        // Show empty state instead of mock data
        setClasses([]);
        Alert.alert(
          'No Classes Available',
          'No yoga classes found in the database. Please check back later or contact support.'
        );
      }
    } catch (error) {
      console.error('Error loading classes:', error);
      // Show empty state with error message
      setClasses([]);
      Alert.alert(
        'Connection Error',
        `Failed to connect to Firebase: ${error.message}. Please check your internet connection and try again.`
      );
    } finally {
      setLoading(false);
    }
  }, []);

  const onRefresh = useCallback(async () => {
    setRefreshing(true);
    await loadClasses();
    setRefreshing(false);
  }, [loadClasses]);

  useEffect(() => {
    loadClasses();
  }, [loadClasses]);

  useEffect(() => {
    const filtered = filterClassesBySearchCriteria(classes, filters);
    setFilteredClasses(filtered);
  }, [classes, filters]);

  const handleClassPress = (yogaClass) => {
    navigation.navigate('ClassDetails', { yogaClass });
  };

  const handleApplyFilters = (newFilters) => {
    setFilters(newFilters);
  };

  const handleAddToCart = (yogaClass) => {
    Alert.alert(
      'Added to Cart',
      `${yogaClass.name} has been added to your cart!`,
      [
        { text: 'Continue Shopping', style: 'cancel' },
        { text: 'View Cart', onPress: () => navigation.navigate('Cart') },
      ]
    );
  };

  const getActiveFilterText = () => {
    const activeFilters = [];
    if (filters.dayOfWeek && filters.dayOfWeek !== 'All') {
      activeFilters.push(filters.dayOfWeek);
    }
    if (filters.timeOfDay && filters.timeOfDay !== 'All') {
      activeFilters.push(filters.timeOfDay);
    }
    return activeFilters.length > 0 ? activeFilters.join(', ') : 'All Classes';
  };

  const renderClass = ({ item }) => (
    <YogaClassCard
      yogaClass={item}
      onPress={handleClassPress}
      onAddToCart={handleAddToCart}
    />
  );

  const renderEmptyComponent = () => (
    <View style={styles.emptyContainer}>
      <Text style={styles.emptyText}>🧘‍♀️</Text>
      <Text style={styles.emptyTitle}>No classes found</Text>
      <Text style={styles.emptySubtitle}>
        Try adjusting your filters or check back later for new classes.
      </Text>
    </View>
  );

  if (loading) {
    return <LoadingSpinner message="Loading yoga classes..." />;
  }

  const renderHeader = () => (
    <>
      <PasswordMigrationDebug />
      <View style={styles.filterInfo}>
        <Text style={styles.filterInfoText}>
          Showing: {getActiveFilterText()} ({filteredClasses.length} classes)
        </Text>
        <TouchableOpacity
          style={styles.filterButton}
          onPress={() => setShowFilter(true)}
        >
          <Text style={styles.filterButtonText}>Filter</Text>
        </TouchableOpacity>
      </View>
    </>
  );

  return (
    <SafeAreaView style={styles.container}>
      <FlatList
        data={filteredClasses}
        renderItem={renderClass}
        keyExtractor={(item) => item.id.toString()}
        ListHeaderComponent={renderHeader}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
        }
        ListEmptyComponent={renderEmptyComponent}
        showsVerticalScrollIndicator={false}
        style={styles.list}
      />

      {/* Floating Cart Button */}
      <TouchableOpacity
        style={styles.floatingCartButton}
        onPress={() => navigation.navigate('Cart')}
      >
        <Text style={styles.floatingCartButtonText}>🛒</Text>
        {getCartItemCount() > 0 && (
          <View style={styles.floatingCartBadge}>
            <Text style={styles.floatingCartBadgeText}>{getCartItemCount()}</Text>
          </View>
        )}
      </TouchableOpacity>

      <SearchFilter
        visible={showFilter}
        onClose={() => setShowFilter(false)}
        onApply={handleApplyFilters}
        currentFilters={filters}
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F3E5F5',
  },
  filterButton: {
    backgroundColor: '#def4f7',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
  },
  filterButtonText: {
    color: '#661a72',
    fontSize: 14,
    fontWeight: '500',
  },
  floatingCartButton: {
    position: 'absolute',
    bottom: 30,
    right: 20,
    width: 50,
    height: 50,
    borderRadius: 25,
    backgroundColor: '#fff',
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 8,
    zIndex: 1000,
  },
  floatingCartButtonText: {
    fontSize: 20,
    color: '#fff',
  },
  floatingCartBadge: {
    position: 'absolute',
    top: -5,
    right: -5,
    backgroundColor: '#fa7575ff',
    borderRadius: 10,
    minWidth: 20,
    height: 20,
    justifyContent: 'center',
    alignItems: 'center',
  },
  floatingCartBadgeText: {
    color: '#fff',
    fontSize: 12,
    fontWeight: 'bold',
  },
  filterInfo: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 5,
    backgroundColor: '#F3E5F5',
  },
  filterInfoText: {
    fontSize: 14,
    color: '#666',
    flex: 1,
  },
  list: {
    paddingVertical: 10,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 40,
    paddingVertical: 60,
  },
  emptyText: {
    fontSize: 60,
    marginBottom: 20,
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 10,
  },
  emptySubtitle: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
    lineHeight: 24,
  },
});

export default ClassListScreen;
