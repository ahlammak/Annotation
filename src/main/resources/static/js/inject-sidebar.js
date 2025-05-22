/**
 * inject-sidebar.js - Redirects to the unified sidebar-navbar.js implementation
 * This file exists for backward compatibility with templates that reference it
 */
document.addEventListener('DOMContentLoaded', function() {
  // Load the unified sidebar-navbar.js implementation
  if (!document.querySelector('script[src="/js/sidebar-navbar.js"]')) {
    const script = document.createElement('script');
    script.src = '/js/sidebar-navbar.js';
    document.head.appendChild(script);
  }
});
