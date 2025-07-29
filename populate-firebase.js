// Script to populate Firebase with sample data
// Run this once to add sample yoga classes to your database

import { CONFIG } from './src/utils/config.js';

const API_BASE_URL = CONFIG.API_BASE_URL;

const sampleClasses = [
  {
    classType: "Hatha Yoga",
    instructor: "Sarah Johnson", 
    date: "30/07/2025",
    time: "09:00",
    duration: 60,
    capacity: 15,
    price: 25,
    difficulty: "Beginner",
    dayOfWeek: "Wednesday",
    description: "A gentle introduction to basic yoga postures and breathing techniques. Perfect for beginners who want to start their yoga journey.",
    locationAddress: "123 Wellness Street, Downtown",
    latitude: 10.8231,
    longitude: 106.6297,
    instances: [
      {
        date: "30/07/2025",
        instructor: "Sarah Johnson",
        time: "09:00"
      }
    ]
  },
  {
    classType: "Vinyasa Flow",
    instructor: "Mike Chen",
    date: "30/07/2025", 
    time: "18:30",
    duration: 75,
    capacity: 12,
    price: 35,
    difficulty: "Intermediate",
    dayOfWeek: "Wednesday",
    description: "Dynamic flowing sequences that link breath with movement. Build strength, flexibility, and mindfulness.",
    locationAddress: "456 Harmony Avenue, City Center",
    latitude: 10.8431,
    longitude: 106.6497,
    instances: [
      {
        date: "30/07/2025",
        instructor: "Mike Chen", 
        time: "18:30"
      }
    ]
  }
  // Add more classes as needed
];

async function populateFirebase() {
  try {
    for (const yogaClass of sampleClasses) {
      const response = await fetch(`${API_BASE_URL}/yoga-classes.json`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(yogaClass),
      });
      
      if (response.ok) {
        const result = await response.json();
        console.log(`Added class: ${yogaClass.classType} with ID: ${result.name}`);
      } else {
        console.error(`Failed to add ${yogaClass.classType}`);
      }
    }
    console.log('✅ Sample data added successfully!');
  } catch (error) {
    console.error('❌ Error populating Firebase:', error);
  }
}

// Uncomment to run:
// populateFirebase();

export { populateFirebase };
