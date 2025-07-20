// Image mapping for yoga classes
// You can add actual image files to assets/images/ and reference them here

const yogaImages = {
  'Hatha Yoga': 'https://images.pexels.com/photos/6648794/pexels-photo-6648794.jpeg?auto=compress&cs=tinysrgb&w=1000&h=1500&dpr=1',
  'Vinyasa Yoga': 'https://images.pexels.com/photos/6246673/pexels-photo-6246673.jpeg?auto=compress&cs=tinysrgb&w=1000&h=1500&dpr=1',
  'Restorative Yoga': 'https://images.pexels.com/photos/10223022/pexels-photo-10223022.jpeg?auto=compress&cs=tinysrgb&w=1000&h=1500&dpr=1',
  "Flow Yoga": 'https://images.pexels.com/photos/4534585/pexels-photo-4534585.jpeg?auto=compress&cs=tinysrgb&w=1000&h=1500&dpr=1',
  "Aerial Yoga": 'https://images.pexels.com/photos/4323301/pexels-photo-4323301.jpeg?auto=compress&cs=tinysrgb&w=1000&h=1500&dpr=1',
  "Family Yoga": 'https://images.pexels.com/photos/3094230/pexels-photo-3094230.jpeg?auto=compress&cs=tinysrgb&w=1000&h=1500&dpr=1'
};

// Default fallback image
const defaultImage = 'https://images.pexels.com/photos/8018973/pexels-photo-8018973.jpeg?auto=compress&cs=tinysrgb&w=1000&h=1500&dpr=1';

/**
 * Get image URL for a yoga class
 * @param {Object} yogaClass - The yoga class object
 * @returns {string} - Image URL
 */
export const getYogaClassImage = (yogaClass) => {
  // If the class already has an image URL, use it
  if (yogaClass.image && yogaClass.image.startsWith('http')) {
    return yogaClass.image;
  }

  // Try to match by class name (case insensitive, partial match)
  const className = yogaClass.name || yogaClass.classType || '';
  
  for (const [key, imageUrl] of Object.entries(yogaImages)) {
    if (className.toLowerCase().includes(key.toLowerCase())) {
      return imageUrl;
    }
  }

  // Final fallback
  return defaultImage;
};

export default {
  getYogaClassImage,
  yogaImages,
  defaultImage,
};
