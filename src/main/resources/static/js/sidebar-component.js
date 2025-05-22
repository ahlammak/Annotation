/**
 * sidebar-component.js - Injects a reusable sidebar component
 * This script replaces the existing sidebar with a standardized one
 * without modifying the overall page structure
 */
document.addEventListener('DOMContentLoaded', function() {
  // Only apply to admin pages
  if (!window.location.pathname.includes('/admin/')) {
    return;
  }

  // Find the existing sidebar
  const existingSidebar = document.querySelector('.app-sidebar');
  if (!existingSidebar) {
    console.warn('No sidebar found to replace');
    return;
  }

  // Déterminer quelle page est active
  const isActive = {
    dashboard: window.location.pathname.includes('/admin/admin'),
    datasets: window.location.pathname.includes('/admin/Dataset'),
    annotateurs: window.location.pathname.includes('/admin/listeAnnotateur') || window.location.pathname.includes('/admin/ListeAnnotateur'),
    tasks: window.location.pathname.includes('/admin/tasks')
  };

  // Créer le contenu de la sidebar
  const sidebarContent = `
    <div class="sidebar-header">
      <div class="logo">
        <i class="material-icons">storage</i>
        <span>Annotation App</span>
      </div>
      <button id="sidebar-toggle" class="sidebar-toggle">
        <i class="material-icons">chevron_left</i>
      </button>
    </div>
    <div class="sidebar-menu">
      <ul>
        <li class="${isActive.dashboard ? 'active' : ''}">
          <a href="/admin/admin">
            <i class="material-icons">dashboard</i>
            <span>Tableau de bord</span>
          </a>
        </li>
        <li class="${isActive.datasets ? 'active' : ''}">
          <a href="/admin/Dataset">
            <i class="material-icons">storage</i>
            <span>Datasets</span>
          </a>
        </li>
        <li class="${isActive.annotateurs ? 'active' : ''}">
          <a href="/admin/listeAnnotateur">
            <i class="material-icons">people</i>
            <span>Annotateurs</span>
          </a>
        </li>
        <li class="${isActive.tasks ? 'active' : ''}">
          <a href="/admin/tasks">
            <i class="material-icons">assignment</i>
            <span>Tâches</span>
          </a>
        </li>
        <li>
          <a href="/logout">
            <i class="material-icons">exit_to_app</i>
            <span>Déconnexion</span>
          </a>
        </li>
      </ul>
    </div>
  `;

  // Remplacer le contenu de la sidebar
  existingSidebar.innerHTML = sidebarContent;

  // Ajouter les écouteurs d'événements
  const sidebarToggle = document.getElementById('sidebar-toggle');
  const menuToggle = document.getElementById('menu-toggle');
  const appContainer = document.querySelector('.app-container');

  // Fonction pour basculer la sidebar
  if (sidebarToggle) {
    sidebarToggle.addEventListener('click', function() {
      appContainer.classList.toggle('sidebar-collapsed');
    });
  }

  if (menuToggle) {
    menuToggle.addEventListener('click', function() {
      appContainer.classList.toggle('sidebar-mobile-open');
    });
  }

  // User dropdown functionality
  const userDropdown = document.querySelector('.user-dropdown');
  if (userDropdown) {
    userDropdown.addEventListener('click', function() {
      this.classList.toggle('open');
    });
  }

  // Close dropdown when clicking outside
  document.addEventListener('click', function(event) {
    if (userDropdown && !userDropdown.contains(event.target)) {
      userDropdown.classList.remove('open');
    }
  });
});
