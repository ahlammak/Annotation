/**
 * admin-layout.js - Script pour ajouter la navbar et la sidebar à toutes les interfaces d'administration
 */
document.addEventListener('DOMContentLoaded', function() {
  // Seulement appliquer aux pages admin
  if (!window.location.pathname.includes('/admin/')) {
    return;
  }

  // Vérifier si la page a déjà une sidebar (pour éviter les doublons)
  if (document.querySelector('.app-sidebar') || document.querySelector('.app-navbar')) {
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

  if (!document.querySelector('link[href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap"]')) {
    const fontInterLink = document.createElement('link');
    fontInterLink.rel = 'stylesheet';
    fontInterLink.href = 'https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap';
    document.head.appendChild(fontInterLink);
  }

  // Déterminer quelle page est active
  const isActive = {
    dashboard: window.location.pathname.includes('/admin/admin'),
    datasets: window.location.pathname.includes('/admin/Dataset'),
    annotateurs: window.location.pathname.includes('/admin/listeAnnotateur') || window.location.pathname.includes('/admin/ListeAnnotateur')
  };

  // Sauvegarder le contenu original du body
  const originalContent = document.body.innerHTML;

  // Vider le body
  document.body.innerHTML = '';

  // Créer la structure de l'app
  const appContainer = document.createElement('div');
  appContainer.className = 'app-container';

  // Créer la sidebar
  appContainer.innerHTML = `
    <!-- Sidebar -->
    <div class="app-sidebar">
      <div class="sidebar-header">
        <div class="logo">
          <i class="material-icons">dashboard</i>
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
          <h2 class="page-title">${document.title.split(' - ')[0] || 'Administration'}</h2>
        </div>
        <div class="navbar-right">
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
        ${originalContent}
      </div>
    </div>
  `;

  // Ajouter la structure au body
  document.body.appendChild(appContainer);

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
    }, 300);
  }

  // Ajouter les écouteurs d'événements
  if (sidebarToggle) {
    sidebarToggle.addEventListener('click', toggleSidebar);
  }

  if (menuToggle) {
    menuToggle.addEventListener('click', function() {
      appContainer.classList.toggle('sidebar-mobile-open');
    });
  }

  if (userDropdown) {
    userDropdown.addEventListener('click', function(e) {
      e.stopPropagation();
      this.classList.toggle('open');
    });

    // Fermer le dropdown quand on clique ailleurs
    document.addEventListener('click', function() {
      if (userDropdown.classList.contains('open')) {
        userDropdown.classList.remove('open');
      }
    });
  }

  // Gérer le redimensionnement de la fenêtre
  window.addEventListener('resize', function() {
    document.body.style.width = '100%';
    if (window.innerWidth <= 768) {
      appContainer.classList.remove('sidebar-collapsed');
    }
  });

  // Déclencher l'événement de redimensionnement pour initialiser les largeurs
  window.dispatchEvent(new Event('resize'));
});
