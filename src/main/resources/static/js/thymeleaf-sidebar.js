/**
 * thymeleaf-sidebar.js - Injecte la sidebar pour les pages Thymeleaf
 * Ce script préserve le contenu Thymeleaf tout en ajoutant la sidebar
 */
document.addEventListener('DOMContentLoaded', function() {
  // Seulement appliquer aux pages admin
  if (!window.location.pathname.includes('/admin/')) {
    return;
  }

  // Vérifier si la page a déjà une sidebar (pour éviter les doublons)
  if (document.querySelector('.app-sidebar')) {
    return;
  }

  // Ajouter les CSS requis s'ils ne sont pas déjà présents
  if (!document.querySelector('link[href="/css/sidebar-navbar.css"]')) {
    const styleLink = document.createElement('link');
    styleLink.rel = 'stylesheet';
    styleLink.href = '/css/sidebar-navbar.css';
    document.head.appendChild(styleLink);
  }

  if (!document.querySelector('link[href="https://fonts.googleapis.com/icon?family=Material+Icons"]')) {
    const fontLink = document.createElement('link');
    fontLink.rel = 'stylesheet';
    fontLink.href = 'https://fonts.googleapis.com/icon?family=Material+Icons';
    document.head.appendChild(fontLink);
  }

  // Déterminer quelle page est active
  const isActive = {
    dashboard: window.location.pathname.includes('/admin/admin'),
    datasets: window.location.pathname.includes('/admin/Dataset'),
    annotateurs: window.location.pathname.includes('/admin/listeAnnotateur') || window.location.pathname.includes('/admin/ListeAnnotateur'),
    tasks: window.location.pathname.includes('/admin/tasks')
  };

  // Créer la structure de l'app
  const appContainer = document.createElement('div');
  appContainer.className = 'app-container';

  // Récupérer le contenu original du body
  const originalContent = document.body.innerHTML;
  
  // Créer la sidebar
  const sidebarHTML = `
    <!-- Sidebar -->
    <div class="app-sidebar">
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
        </ul>
      </div>
      <div class="sidebar-footer">
        <div class="user-info">
          <div class="avatar">A</div>
          <div class="user-details">
            <div class="user-name">Admin</div>
            <div class="user-email">admin@example.com</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Content Wrapper -->
    <div class="content-wrapper">
      <!-- Navbar -->
      <div class="app-navbar">
        <div class="navbar-left">
          <button id="menu-toggle" class="menu-toggle">
            <i class="material-icons">menu</i>
          </button>
          <div class="page-title">${document.title.split(' - ')[0] || 'Administration'}</div>
        </div>
        <div class="navbar-right">
          <div class="navbar-item">
            <i class="material-icons">notifications</i>
          </div>
          <div class="navbar-item user-dropdown">
            <div class="avatar">A</div>
            <div class="dropdown-menu">
              <a href="#" class="dropdown-item">
                <i class="material-icons">person</i>
                <span>Profil</span>
              </a>
              <a href="/logout" class="dropdown-item">
                <i class="material-icons">exit_to_app</i>
                <span>Déconnexion</span>
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div class="main-content">
        <div id="thymeleaf-content"></div>
      </div>
    </div>
  `;

  // Définir le HTML de l'appContainer
  appContainer.innerHTML = sidebarHTML;
  
  // Remplacer le contenu du body
  document.body.innerHTML = '';
  document.body.appendChild(appContainer);
  
  // Insérer le contenu original dans le conteneur approprié
  document.getElementById('thymeleaf-content').innerHTML = originalContent;

  // Ajouter les écouteurs d'événements
  const sidebarToggle = document.getElementById('sidebar-toggle');
  const menuToggle = document.getElementById('menu-toggle');
  const userDropdown = document.querySelector('.user-dropdown');
  const contentWrapper = document.querySelector('.content-wrapper');

  // Fonction pour basculer la sidebar
  function toggleSidebar() {
    appContainer.classList.toggle('sidebar-collapsed');

    // Forcer un recalcul pour s'assurer que la largeur du contenu est recalculée
    setTimeout(function() {
      window.dispatchEvent(new Event('resize'));
      document.body.style.width = '100%';
      contentWrapper.style.width = appContainer.classList.contains('sidebar-collapsed')
        ? `calc(100% - ${getComputedStyle(document.documentElement).getPropertyValue('--sidebar-width')})`
        : `calc(100% - ${getComputedStyle(document.documentElement).getPropertyValue('--sidebar-collapsed-width')})`;
    }, 300);
  }

  if (sidebarToggle) sidebarToggle.addEventListener('click', toggleSidebar);
  if (menuToggle) menuToggle.addEventListener('click', toggleSidebar);

  // Fonctionnalité du menu déroulant utilisateur
  if (userDropdown) {
    userDropdown.addEventListener('click', function(event) {
      event.stopPropagation();
      this.classList.toggle('active');
    });
  }

  // Fermer le menu déroulant en cliquant à l'extérieur
  document.addEventListener('click', function(event) {
    if (userDropdown && !userDropdown.contains(event.target)) {
      userDropdown.classList.remove('active');
    }
  });

  // Gérer le redimensionnement de la fenêtre
  window.addEventListener('resize', function() {
    document.body.style.width = '100%';
    if (window.innerWidth > 768) {
      contentWrapper.style.width = appContainer.classList.contains('sidebar-collapsed')
        ? `calc(100% - ${getComputedStyle(document.documentElement).getPropertyValue('--sidebar-width')})`
        : `calc(100% - ${getComputedStyle(document.documentElement).getPropertyValue('--sidebar-collapsed-width')})`;
    } else {
      contentWrapper.style.width = '100%';
    }
  });

  // Déclencher l'événement de redimensionnement pour initialiser les largeurs
  window.dispatchEvent(new Event('resize'));
});
