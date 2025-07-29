// Test script to verify Firebase API connection
const fetch = require('node-fetch');

const API_BASE_URL = "https://yogaapp-12d2b-default-rtdb.asia-southeast1.firebasedatabase.app";

async function testAPI() {
  try {
    console.log('🔄 Testing Firebase connection...');
    
    const url = `${API_BASE_URL}/yoga-classes.json`;
    console.log('📡 Fetching from:', url);
    
    const response = await fetch(url);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data = await response.json();
    console.log('📊 Raw Firebase data:', JSON.stringify(data, null, 2));
    console.log('📈 Data type:', typeof data);
    console.log('📏 Data length:', Array.isArray(data) ? data.length : 'Not an array');
    
    // Filter out null values
    if (Array.isArray(data)) {
      const filteredData = data.filter(item => item !== null && item !== undefined);
      console.log('✅ Filtered data length:', filteredData.length);
      console.log('🎯 Filtered classes:', filteredData.map(c => c.classType));
      
      // Map to customer format
      const mapped = filteredData.map(yogaClass => ({
        id: yogaClass.id || Math.random().toString(),
        name: yogaClass.classType || 'Yoga Class',
        instructor: yogaClass.instructor || 'Instructor',
        date: new Date().toISOString().split('T')[0],
        time: yogaClass.time || '09:00',
        duration: yogaClass.duration || 60,
        capacity: yogaClass.capacity || 10,
        availableSpots: Math.max(0, (yogaClass.capacity || 10) - Math.floor(Math.random() * 5)),
        price: yogaClass.price || 25,
        level: yogaClass.difficulty || 'All Levels',
        dayOfWeek: yogaClass.dayOfWeek || 'Monday',
        description: yogaClass.description || 'A wonderful yoga class experience.',
        image: null,
        locationAddress: yogaClass.locationAddress,
        latitude: yogaClass.latitude,
        longitude: yogaClass.longitude
      }));
      
      console.log('🎨 Mapped data for app:');
      mapped.forEach(cls => {
        console.log(`- ${cls.name} (${cls.level}) - ${cls.dayOfWeek} at ${cls.time}`);
      });
      
      return mapped;
    }
    
    return [];
    
  } catch (error) {
    console.error('❌ Error testing API:', error);
    return null;
  }
}

testAPI();
