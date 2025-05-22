// Script pour rediriger de /editUsers vers /admin/GererAnnotateur/editUsersRedirect
document.addEventListener('DOMContentLoaded', function() {
    // Vérifier si nous sommes sur la page /editUsers
    if (window.location.pathname === '/editUsers') {
        // Récupérer les paramètres de l'URL
        const urlParams = new URLSearchParams(window.location.search);
        const id = urlParams.get('id');
        const keyword = urlParams.get('keyword') || '';
        const page = urlParams.get('page') || '0';
        const size = urlParams.get('size') || '5';
        
        // Construire l'URL de redirection
        const redirectUrl = `/admin/GererAnnotateur/editUsersRedirect?id=${id}&keyword=${keyword}&page=${page}&size=${size}`;
        
        // Rediriger vers la nouvelle URL
        window.location.href = redirectUrl;
    }
});
