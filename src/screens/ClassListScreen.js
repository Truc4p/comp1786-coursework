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
import ApiService from '../services/api';
import { filterClassesBySearchCriteria } from '../utils/helpers';

const ClassListScreen = ({ navigation }) => {
  const [classes, setClasses] = useState([]);
  const [filteredClasses, setFilteredClasses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [showFilter, setShowFilter] = useState(false);
  const [filters, setFilters] = useState({ dayOfWeek: 'All', timeOfDay: 'All' });

  const loadClasses = useCallback(async () => {
    try {
      setLoading(true);
      console.log('Loading classes from Firebase...');
      const response = await ApiService.getYogaClasses();
    //   console.log('API response received:', response);
      
      // Handle Firebase response format
      let classData = [];
      if (Array.isArray(response)) {
        classData = response;
        // console.log('Response is array, using directly:', classData);
      } else if (response && typeof response === 'object') {
        // If response has a data property
        classData = response.data || response || [];
        console.log('Response is object, extracted data:', classData);
      }
      
    //   console.log('Final class data length:', classData.length);
      
      // If we get data from Firebase, use it
      if (classData.length > 0) {
        // console.log('Using Firebase data:', classData);
        setClasses(classData);
      } else {
        console.log('No classes found in Firebase, using demo data');
        // Use mock data if no data in Firebase yet
        const mockClasses = [
          {
            id: 'demo-1',
            name: 'Hatha Yoga Fundamentals',
            instructor: 'Sarah Johnson',
            date: '2025-07-22',
            time: '09:00',
            duration: 60,
            capacity: 15,
            availableSpots: 8,
            price: 25,
            level: 'Beginner',
            dayOfWeek: 'Monday',
            description: 'A gentle introduction to basic yoga postures and breathing techniques.',
            image: null,
          },
          {
            id: 'demo-2',
            name: 'Vinyasa Flow',
            instructor: 'Mike Chen',
            date: '2025-07-22',
            time: '18:30',
            duration: 75,
            capacity: 20,
            availableSpots: 12,
            price: 30,
            level: 'Intermediate',
            dayOfWeek: 'Monday',
            description: 'Dynamic flowing sequences that link movement with breath.',
            image: null,
          },
          {
            id: 'demo-3',
            name: 'Restorative Yoga',
            instructor: 'Emma Davis',
            date: '2025-07-23',
            time: '19:00',
            duration: 90,
            capacity: 12,
            availableSpots: 5,
            price: 35,
            level: 'All Levels',
            dayOfWeek: 'Tuesday',
            description: 'Relaxing poses held for longer periods to promote deep relaxation.',
            image: null,
          },
          {
            id: 'demo-4',
            name: 'Power Yoga',
            instructor: 'Jake Wilson',
            date: '2025-07-24',
            time: '07:00',
            duration: 60,
            capacity: 18,
            availableSpots: 0,
            price: 28,
            level: 'Advanced',
            dayOfWeek: 'Wednesday',
            description: 'High-intensity yoga workout that builds strength and flexibility.',
            image: null,
          },
          {
            id: 'demo-5',
            name: 'Yin Yoga',
            instructor: 'Lisa Park',
            date: '2025-07-25',
            time: '10:30',
            duration: 75,
            capacity: 15,
            availableSpots: 10,
            price: 32,
            level: 'All Levels',
            dayOfWeek: 'Thursday',
            description: 'Passive, meditative practice targeting deep connective tissues.',
            image: null,
          },
        ];
        setClasses(mockClasses);
        Alert.alert(
          'Demo Mode',
          'Connected to Firebase but no classes found. Using demo data. Check console logs for debugging info.'
        );
      }
    } catch (error) {
      console.error('Error loading classes:', error);
      // Use mock data as fallback
      const mockClasses = [
        {
          id: 'demo-1',
          name: 'Hatha Yoga Fundamentals',
          instructor: 'Sarah Johnson',
          date: '2025-07-22',
          time: '09:00',
          duration: 60,
          capacity: 15,
          availableSpots: 8,
          price: 25,
          level: 'Beginner',
          dayOfWeek: 'Monday',
          description: 'A gentle introduction to basic yoga postures and breathing techniques.',
          image: null,
        },
      ];
      setClasses(mockClasses);
      Alert.alert(
        'Connection Error',
        `Failed to connect to Firebase: ${error.message}. Using demo data. Check console logs for more details.`
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

  const handleBookClass = (yogaClass) => {
    if (yogaClass.availableSpots <= 0) {
      Alert.alert('Sorry', 'This class is fully booked.');
      return;
    }
    navigation.navigate('BookClass', { yogaClass });
  };

  const handleApplyFilters = (newFilters) => {
    setFilters(newFilters);
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
      onBook={handleBookClass}
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
      <View style={styles.header}>
        <Text style={styles.title}>Yoga Classes</Text>
        <TouchableOpacity
          style={styles.filterButton}
          onPress={() => setShowFilter(true)}
        >
          <Text style={styles.filterButtonText}>Filter</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.filterInfo}>
        <Text style={styles.filterInfoText}>
          Showing: {getActiveFilterText()} ({filteredClasses.length} classes)
        </Text>
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
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 15,
    backgroundColor: '#F3E5F5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
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
  filterInfo: {
    paddingHorizontal: 20,
    paddingVertical: 10,
    backgroundColor: '#F3E5F5',
  },
  filterInfoText: {
    fontSize: 14,
    color: '#666',
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
