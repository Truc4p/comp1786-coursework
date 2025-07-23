// Image mapping for yoga classes
// Local image assets for different yoga class types

const yogaImages = {
  'Hatha Yoga': require('../../assets/images/hatha-yoga.jpg'),
  'Vinyasa Yoga': require('../../assets/images/vinyasa-yoga.jpg'),
  'Restorative Yoga': require('../../assets/images/restorative-yoga.jpg'),
  'Flow Yoga': require('../../assets/images/flow-yoga.jpg'),
  'Aerial Yoga': require('../../assets/images/aerial-yoga.jpg'),
  'Family Yoga': require('../../assets/images/family-yoga.jpg'),
};

// Default fallback image
const defaultImage = require('../../assets/images/default-yoga.jpg');

/**
 * Get image for a yoga class
 * @param {Object} yogaClass - The yoga class object
 * @returns {any} - Local image asset
 */
export const getYogaClassImage = (yogaClass) => {
  // If the class already has a local image asset, use it
  if (yogaClass.image && typeof yogaClass.image !== 'string') {
    return yogaClass.image;
  }

  // Try to match by class name (case insensitive, partial match)
  const className = yogaClass.name || yogaClass.classType || '';
  
  for (const [key, imageAsset] of Object.entries(yogaImages)) {
    if (className.toLowerCase().includes(key.toLowerCase())) {
      return imageAsset;
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
