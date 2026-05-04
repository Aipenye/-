// Import the original script
import './app.js';

// Expose the globally defined function
export const solve = window.solve || function() {
  // If solve is defined on window, use console.error to help debug
  if (typeof window.solve !== 'function') {
    console.error('solve function not found on global scope');
  } else {
    return window.solve(...arguments);
  }
};