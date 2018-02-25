# PDE Views v1.6
Compatible avec <b>Eclipse Luna, Mars, Neon, Oxygen</b><br>
<b>Java 8</b>

## Install
```
https://raw.githubusercontent.com/cylagarde/PDE_Views/master/cl.pde.views.update_site
```
<br>

Le plugin propose 4 vues disponibles avec le menu "Window/Show view"<br>
<img src="https://github.com/cylagarde/PDE_Views/blob/master/document/show_views.png"/><br>

## Feature view
Cette vue permet de visualiser le contenu d'une feature en affichant les plugins et autres features.<br>
Un filtre permet de chercher un plugin. Des boites à cocher permettent de visualiser ou pas le contenu de la feature.<br>
<img src="https://github.com/cylagarde/PDE_Views/blob/master/document/feature_view.png"/>
Pour ouvrir la vue, sélectionner un fichier 'features.xml' dans la vue "Project Explorer" ou "Package Explorer" et faire un clic droit pour afficher le menu contextuel : clic sur le menu "Open in feature view"<br><img src="https://github.com/cylagarde/PDE_Views/blob/master/document/open_in_feature_view.png"/><br>
Le bouton <img src="https://github.com/cylagarde/PDE_Views/blob/master/document/getAllFeatures.png"/> permet d'afficher toutes les features contenus dans le workspace et la target platform.<br>
Un double clic sur une feature ou un bundle ouvre l'éditeur automatiquement.

## Launch configuration view
Cette vue permet de visualiser le contenu d'une launch configuration en affichant les plugins et autres features.<br>
Un filtre permet de chercher un plugin. Des boites à cocher permettent de visualiser ou pas le contenu de la launch configuration.<br>
<img src="https://github.com/cylagarde/PDE_Views/blob/master/document/launch_configuration_view.png"/>
Pour ouvrir la vue, sélectionner un fichier '.launch' dans la vue "Project Explorer" ou "Package Explorer" et faire un clic droit pour afficher le menu contextuel : clic sur le menu "Open in launch configuration view"<br><img src="https://github.com/cylagarde/PDE_Views/blob/master/document/open_in_launch_configuration_view.png"/><br>
Le bouton <img src="https://github.com/cylagarde/PDE_Views/blob/master/document/getAllLaunchConfigurations.png"/> permet d'afficher toutes les launch configurations contenues dans le workspace.<br>
Un double clic sur une feature ou un bundle ouvre l'éditeur automatiquement.

## Product view
Cette vue permet de visualiser le contenu d'une product en affichant les plugins et autres features.<br>
Un filtre permet de chercher un plugin. Des boites à cocher permettent de visualiser ou pas le contenu du product.<br>
<img src="https://github.com/cylagarde/PDE_Views/blob/master/document/product_view.png"/>
Pour ouvrir la vue, sélectionner un fichier '.product' dans la vue "Project Explorer" ou "Package Explorer" et faire un clic droit pour afficher le menu contextuel : clic sur le menu "Open in product view"<br><img src="https://github.com/cylagarde/PDE_Views/blob/master/document/open_in_product_view.png"/><br>
Le bouton <img src="https://github.com/cylagarde/PDE_Views/blob/master/document/getAllProducts.png"/> permet d'afficher tous les products contenus dans le workspace.<br>
Un double clic sur une feature ou un bundle ouvre l'éditeur automatiquement.

## Plugin view
Cette vue permet de visualiser le contenu d'un plugin en affichant les plugins de dépendances récursivement.<br>
Un filtre permet de chercher un plugin. Des boites à cocher permettent de visualiser ou pas le workspace et/ou la target platform.<br>
<img src="https://github.com/cylagarde/PDE_Views/blob/master/document/plugin_view.png"/>
Pour ouvrir la vue, utiliser le menu 'show view'.<br>
Le bouton <img src="https://github.com/cylagarde/PDE_Views/blob/master/document/getAllPlugins.png"/> permet d'afficher tous les plugins contenus dans le workspace et la target platform.<br>
Un double clic sur un bundle ouvre l'éditeur automatiquement.

## Contextual menu
Pour chaque vue, un menu contextuel est disponible pour chaque noeud:
* Copy id/version to clipboard : Copie l'id et la version de chaque bundle sélectionné au format text et RTF.
* Copy tree to clipboard : Copie l'arborescence (id et version) de chaque bundle sélectionné au format text et RTF.
* Expand node : Expand l'arborescence de chaque bundle sélectionné

## Search projects to be opened/imported
Un menu contextuel a été rajouté dans la vue "Project Explorer" ou "Package Explorer" pour rechercher et afficher la liste
des projets qui sont fermés et des dossiers qui peuvent être importés en tant que projet.<br>
Faire un clic droit pour afficher le menu contextuel : clic sur le menu "Search projects to be opened/imported"<br>
<img src="https://github.com/cylagarde/PDE_Views/blob/master/document/search_project_to_be_opened.png"/><br>
La liste des projects fermés ou des dossiers s'affiche.<br>
<img src="https://github.com/cylagarde/PDE_Views/blob/master/document/projects_closed_in_workspace.png"/>
<img src="https://github.com/cylagarde/PDE_Views/blob/master/document/search_project_to_be_opened_dialog.png"/><br>
Il suffit de sélectionner les projets à ouvrir ou les dossiers à importer.

