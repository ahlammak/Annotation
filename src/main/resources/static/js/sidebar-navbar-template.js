/**
 * sidebar-navbar-template.js
 * Ce fichier contient le code pour insérer la sidebar et navbar dans n'importe quelle page.
 * Il suffit d'inclure ce script dans votre page HTML et d'ajouter un élément avec l'id "app-container".
 */
document.addEventListener('DOMContentLoaded', function() {
  // Vérifier si la page a déjà une sidebar (pour éviter les doublons)
  if (document.querySelector('.app-sidebar') || document.querySelector('.app-navbar')) {
    return;
  }

  // Trouver le conteneur où insérer la sidebar et navbar
  const container = document.getElementById('app-container');
  if (!container) {
    console.warn('Aucun élément avec l\'id "app-container" n\'a été trouvé. La sidebar et navbar ne seront pas insérées.');
    return;
  }

  // Déterminer quelle page est active
  const isActive = {
    dashboard: window.location.pathname.includes('/admin/admin'),
    datasets: window.location.pathname.includes('/admin/Dataset'),
    annotateurs: window.location.pathname.includes('/admin/listeAnnotateur') || window.location.pathname.includes('/admin/ListeAnnotateur'),
    tasks: window.location.pathname.includes('/admin/tasks')
  };

  // Créer la sidebar
  const sidebar = document.createElement('div');
  sidebar.className = 'app-sidebar';
  sidebar.innerHTML = `
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

  // Créer la navbar
  const navbar = document.createElement('div');
  navbar.className = 'app-navbar';
  navbar.innerHTML = `
    <div class="navbar-left">
      <button id="menu-toggle" class="menu-toggle">
        <i class="material-icons">menu</i>
      </button>
      <h2 class="page-title">${getPageTitle()}</h2>
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
  `;

  // Créer le wrapper pour le contenu
  const contentWrapper = document.createElement('div');
  contentWrapper.className = 'content-wrapper';
  
  // Déplacer le contenu existant dans le wrapper
  const mainContent = document.createElement('div');
  mainContent.className = 'main-content';
  
  // Déplacer tous les enfants du conteneur dans le contenu principal
  while (container.firstChild) {
    mainContent.appendChild(container.firstChild);
  }
  
  // Assembler la structure
  contentWrapper.appendChild(navbar);
  contentWrapper.appendChild(mainContent);
  
  // Vider le conteneur et ajouter la nouvelle structure
  container.appendChild(sidebar);
  container.appendChild(contentWrapper);
  
  // Ajouter la classe app-container au conteneur
  container.classList.add('app-container');
  
  // Ajouter les écouteurs d'événements
  setupEventListeners();
  
  /**
   * Détermine le titre de la page en fonction de l'URL
   */
  function getPageTitle() {
    if (isActive.dashboard) return 'Tableau de bord';
    if (isActive.datasets) return 'Gestion des Datasets';
    if (isActive.annotateurs) return 'Gestion des Annotateurs';
    if (isActive.tasks) return 'Gestion des Tâches';
    
    // Essayer de récupérer le titre depuis la balise title
    const titleElement = document.querySelector('title');
    if (titleElement) {
      const title = titleElement.textContent.split('-')[0].trim();
      return title;
    }
    
    return 'Administration';
  }
  
  /**
   * Configure les écouteurs d'événements pour la sidebar et la navbar
   */
  function setupEventListeners() {
    // Sidebar toggle
    const sidebarToggle = document.getElementById('sidebar-toggle');
    if (sidebarToggle) {
      sidebarToggle.addEventListener('click', function() {
        container.classList.toggle('sidebar-collapsed');
      });
    }
    
    // Menu toggle (mobile)
    const menuToggle = document.getElementById('menu-toggle');
    if (menuToggle) {
      menuToggle.addEventListener('click', function() {
        container.classList.toggle('sidebar-mobile-open');
      });
    }
    
    // User dropdown
    const userDropdown = document.querySelector('.user-dropdown');
    if (userDropdown) {
      userDropdown.addEventListener('click', function() {
        this.classList.toggle('open');
      });
    }
    
    // Fermer le dropdown quand on clique ailleurs
    document.addEventListener('click', function(event) {
      if (userDropdown && !userDropdown.contains(event.target)) {
        userDropdown.classList.remove('open');
      }
    });
  }
});
