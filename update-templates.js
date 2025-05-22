/**
 * Script pour mettre à jour les templates HTML avec la nouvelle structure de sidebar et navbar
 */

const fs = require('fs');
const path = require('path');

// Liste des fichiers à modifier
const filesToUpdate = [
    'src/main/resources/templates/admin/Dataset/DatasetChoiceNew.html',
    'src/main/resources/templates/admin/Dataset/AssignTasks.html',
    'src/main/resources/templates/admin/Dataset/viewDataset.html',
    'src/main/resources/templates/admin/Dataset/viewAnnotations.html',
    'src/main/resources/templates/admin/GererAnnotateur/ListeAnnotateur.html',
    'src/main/resources/templates/admin/GererAnnotateur/addAnnotateur.html',
    'src/main/resources/templates/admin/GererAnnotateur/editUsersRedirect.html'
];

// Fonction pour mettre à jour un fichier
function updateFile(filePath) {
    console.log(`Mise à jour de ${filePath}...`);
    
    try {
        // Lire le contenu du fichier
        let content = fs.readFileSync(filePath, 'utf8');
        
        // 1. Remplacer la déclaration HTML et les liens CSS
        content = content.replace(
            /<!DOCTYPE html>[\s\S]*?<script src="\/js\/inject-sidebar\.js"><\/script>/,
            `<!DOCTYPE html>
<html lang="fr"
      xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${getTitle(content)}</title>
    <link rel="stylesheet" type="text/css" href="/webjars/bootstrap/5.3.3/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="/webjars/bootstrap-icons/1.11.3/font/bootstrap-icons.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons" />
    <link rel="stylesheet" href="/css/sidebar-navbar.css">`
        );
        
        // 2. Remplacer la structure du body
        content = content.replace(
            /<body>[\s\S]*?<div layout:fragment="content1">/,
            `<body>
<div id="app-container">`
        );
        
        // 3. Ajouter le script sidebar-navbar-template.js
        content = content.replace(
            /<script src="\/webjars\/bootstrap\/5\.3\.3\/js\/bootstrap\.bundle\.min\.js"><\/script>[\s\S]*?<\/body>\s*<\/html>/,
            `<script src="/webjars/bootstrap/5.3.3/js/bootstrap.bundle.min.js"></script>
<script src="/js/sidebar-navbar-template.js"></script>
</body>
</html>`
        );
        
        // Écrire le contenu mis à jour dans le fichier
        fs.writeFileSync(filePath, content, 'utf8');
        console.log(`✅ ${filePath} mis à jour avec succès.`);
    } catch (error) {
        console.error(`❌ Erreur lors de la mise à jour de ${filePath}:`, error);
    }
}

// Fonction pour extraire le titre de la page
function getTitle(content) {
    const titleMatch = content.match(/<title>(.*?)<\/title>/);
    return titleMatch ? titleMatch[1] : 'Page d\'administration';
}

// Mettre à jour tous les fichiers
filesToUpdate.forEach(updateFile);

console.log('Mise à jour terminée.');
