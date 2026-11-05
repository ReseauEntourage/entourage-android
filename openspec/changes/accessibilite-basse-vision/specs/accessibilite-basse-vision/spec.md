## Purpose

Garantir que les écrans d'Entourage restent lisibles et compréhensibles par les personnes malvoyantes : texte agrandi, contraste suffisant, information qui ne repose pas sur la couleur seule. Le RGAA 4.1.2 (WCAG 2.1 AA) sert de référence. Les exigences s'appliquent parcours par parcours, en commençant par l'accueil.

## ADDED Requirements

### Requirement: Texte lisible avec la police agrandie
Sur un écran couvert, tout texte MUST rester entièrement visible, sans être coupé ni superposé à un autre élément, quand la taille de police et la taille d'affichage système sont réglées à leur maximum.

#### Scenario: Cartes de l'accueil avec la police au maximum
- **WHEN** la taille de police et la taille d'affichage système sont au maximum et que l'utilisateur fait défiler l'accueil
- **THEN** le texte de chaque carte (événement, groupe, action, aide, contenu pédagogique, suggestion, outils) est affiché en entier dans sa carte, sans être coupé verticalement

#### Scenario: Badge de notifications avec la police au maximum
- **WHEN** l'utilisateur a des notifications non lues et que la taille de police est au maximum
- **THEN** le nombre de notifications est lisible en entier

#### Scenario: Rendu inchangé à taille normale
- **WHEN** la taille de police et la taille d'affichage système sont aux valeurs par défaut
- **THEN** les cartes de l'accueil gardent leurs dimensions et leur disposition visuelle actuelles

### Requirement: Informations clés jamais tronquées
Un lieu, un nom de personne ou un nom d'auteur MUST NOT être tronqué à une seule ligne. Un titre de carte MAY être tronqué seulement si l'écran de détail atteint depuis cette carte l'affiche en entier.

#### Scenario: Lieu long d'un événement
- **WHEN** un événement affiché sur l'accueil a un lieu plus long qu'une ligne
- **THEN** le lieu s'affiche sur plusieurs lignes au lieu d'être coupé par des points de suspension après la première

#### Scenario: Nom long dans une suggestion de contact
- **WHEN** une suggestion de contact concerne une personne au nom plus long qu'une ligne
- **THEN** le nom s'affiche sans être coupé après la première ligne

### Requirement: Contraste suffisant du texte
Sur un écran couvert, le texte MUST avoir un contraste d'au moins 4,5:1 avec son fond. Le gros texte (18sp et plus, ou 14sp et plus en gras) MUST avoir un contraste d'au moins 3:1. Les couleurs orange de la marque MUST NOT être modifiées. Elles peuvent rester utilisées comme fond, accent ou gros texte quand elles respectent ces seuils.

#### Scenario: Texte qui était orange sur fond blanc
- **WHEN** l'accueil affiche un texte de taille courante qui était auparavant en orange (sous-titre ou distance d'une action, compteur d'étapes, badge, tag pédagogique, en-tête « prochaine étape »)
- **THEN** ce texte a un contraste d'au moins 4,5:1 avec son fond

#### Scenario: Texte posé sur un fond orange
- **WHEN** un bouton ou un badge a un fond orange de la marque
- **THEN** son libellé a un contraste d'au moins 4,5:1 avec ce fond

#### Scenario: Vérification par outil
- **WHEN** Accessibility Scanner analyse l'accueil
- **THEN** il ne signale aucun défaut de contraste de texte

### Requirement: Information jamais portée par la couleur seule
Un état ou une catégorie (sélectionné, urgent, réservé aux femmes, nouveau) MUST être perceptible sans distinguer les couleurs, par un libellé texte ou un second indice visuel.

#### Scenario: Tag d'urgence sur un événement
- **WHEN** un événement urgent est affiché sur l'accueil
- **THEN** son caractère urgent est indiqué par un libellé texte lisible, pas seulement par une couleur

#### Scenario: Élément sélectionné
- **WHEN** un filtre ou une étape est sélectionné ou terminé
- **THEN** cet état est indiqué par un indice autre que la couleur (graisse, icône, coche ou libellé)

### Requirement: Taille minimale du texte porteur de sens
Un texte porteur d'information MUST avoir une taille de base d'au moins 12sp, avant l'agrandissement choisi par l'utilisateur.

#### Scenario: Tags d'un événement
- **WHEN** un événement de l'accueil affiche des tags (urgence, femmes uniquement, Entourage)
- **THEN** ces tags ont une taille de base d'au moins 12sp

### Requirement: Alternatives textuelles des images
Sur un écran couvert, une image porteuse d'information MUST avoir une description lue par le lecteur d'écran. Une image décorative MUST être ignorée par le lecteur d'écran.

#### Scenario: Navigation TalkBack sur l'accueil
- **WHEN** l'utilisateur parcourt l'accueil avec TalkBack
- **THEN** aucun élément n'est annoncé comme une image ou un bouton sans libellé
- **AND** les icônes purement décoratives (fonds, flèches, icônes doublant un texte) ne sont pas annoncées

#### Scenario: Cloche de notifications
- **WHEN** TalkBack donne le focus à la cloche de notifications
- **THEN** il annonce sa fonction et, le cas échéant, le nombre de notifications non lues

### Requirement: Prévention des régressions
Le build MUST signaler en erreur tout nouveau défaut d'accessibilité détecté par Android Lint (description d'image manquante, étiquette de champ manquante, texte trop petit, zone de clic trop petite) hors de la baseline existante.

#### Scenario: Nouvelle image sans description
- **WHEN** un développeur ajoute une `ImageView` sans `contentDescription` dans un layout et lance lint
- **THEN** le rapport lint signale une erreur d'accessibilité pour cette image
