// Script to add sample yoga classes to Firebase
// Run this in your browser console or use it as reference to add data via your admin app

const FIREBASE_URL = 'https://yogaapp-12d2b-default-rtdb.asia-southeast1.firebasedatabase.app';

const sampleClasses = [
  {
    name: 'Morning Hatha Yoga',
    instructor: 'Sarah Johnson',
    date: '2025-07-22',
    time: '09:00',
    duration: 60,
    capacity: 15,
    availableSpots: 8,
    price: 25,
    level: 'Beginner',
    dayOfWeek: 'Tuesday',
    description: 'A gentle introduction to basic yoga postures and breathing techniques.',
  },
  {
    name: 'Evening Vinyasa Flow',
    instructor: 'Mike Chen',
    date: '2025-07-22',
    time: '18:30',
    duration: 75,
    capacity: 20,
    availableSpots: 12,
    price: 30,
    level: 'Intermediate',
    dayOfWeek: 'Tuesday',
    description: 'Dynamic flowing sequences that link movement with breath.',
  },
  {
    name: 'Restorative Yoga',
    instructor: 'Emma Davis',
    date: '2025-07-23',
    time: '19:00',
    duration: 90,
    capacity: 12,
    availableSpots: 5,
    price: 35,
    level: 'All Levels',
    dayOfWeek: 'Wednesday',
    description: 'Relaxing poses held for longer periods to promote deep relaxation.',
  },
];

// Function to add classes to Firebase (use this in your admin app or browser console)
async function addSampleClasses() {
  for (let i = 0; i < sampleClasses.length; i++) {
    const classData = sampleClasses[i];
    
    try {
      const response = await fetch(`${FIREBASE_URL}/yogaClasses.json`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(classData),
      });
      
      if (response.ok) {
        const result = await response.json();
        console.log(`Added class: ${classData.name}`, result);
      } else {
        console.error(`Failed to add class: ${classData.name}`);
      }
    } catch (error) {
      console.error('Error adding class:', error);
    }
  }
}

// To use this script:
// 1. Open your browser developer console
// 2. Copy and paste this entire script
// 3. Run: addSampleClasses()
// 4. Refresh your React Native app to see the data

console.log('Sample classes ready to add to Firebase');
console.log('Run addSampleClasses() to add them to your database');

// Export for use in Node.js environment if needed
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { sampleClasses, addSampleClasses };
}
