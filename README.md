# Omena Immo V2

Socle V2 de l'application interne de sourcing immobilier Omena.

## Stack

- Angular 22 pour l'interface.
- Java 21 + Spring Boot 4.1 pour l'API.
- Architecture hexagonale cote Java.
- PostgreSQL pour les donnees metier.
- n8n pour les automatisations et l'orchestration.
- Connecteurs de scraping separes, ajoutes progressivement.

## Demarrage

Deux stacks sont conservees : developpement et production.

### Developpement

Depuis la racine :

    docker compose -f compose.dev.yml up -d --build

Pour verifier :

    docker compose -f compose.dev.yml ps

Pour suivre les logs :

    docker compose -f compose.dev.yml logs -f

Pour arreter :

    docker compose -f compose.dev.yml down

### Production

Depuis la racine :

    docker compose -f compose.prod.yml up -d --build

Pour verifier :

    docker compose -f compose.prod.yml ps

Pour arreter :

    docker compose -f compose.prod.yml down

Les volumes PostgreSQL et n8n sont conserves.

### Fichier .env

Le fichier .env est optionnel en local.

Pour modifier les valeurs par defaut :

    cp .env.example .env

Sous PowerShell :

    Copy-Item .env.example .env

Le vrai .env est ignore par Git.

## URLs locales

- Angular : http://localhost:4200
- API : http://localhost:8080/api/health
- Etat des services : http://localhost:8080/api/system/health
- n8n : http://localhost:5679

n8n utilise volontairement le port 5679 cote hote afin de pouvoir cohabiter avec une autre instance exposee sur 5678.

## Architecture

    Angular
       |
       v
    Spring Boot -------- PostgreSQL metier
       ^
       |
      n8n -------------- PostgreSQL n8n
       |
       +---- Gmail / Drive / APIs externes
       |
       v
    Connecteurs de collecte
       +---- Leboncoin
       +---- SeLoger
       +---- agences
       +---- generic

### Angular

Angular gere l'experience utilisateur :

- zones et criteres de recherche ;
- consultation des annonces ;
- favoris et statuts futurs ;
- tableaux de bord ;
- etat des services.

Angular passe par l'API Java pour les donnees metier.

### Spring Boot

Le backend Java reste la source de verite :

- donnees metier ;
- validations ;
- calculs financiers ;
- historique ;
- droits futurs ;
- contrats API avec n8n.

Le premier module fonctionnel gere les criteres de recherche.

Endpoints :

    GET  /api/health
    GET  /api/system/health
    GET  /api/search-criteria
    GET  /api/search-criteria/active
    POST /api/search-criteria

### n8n

n8n sert d'orchestrateur :

- planification des collectes ;
- recuperation des criteres actifs depuis Java ;
- appel des futurs scrapers ;
- services externes ;
- Gmail / Drive ;
- notifications ;
- retries et workflows.

Depuis n8n, l'API Java est accessible avec :

    http://immo-api:8080

## Scrapers

Les scrapers restent separes du backend et de n8n.

Structure cible :

    scrapers/
      leboncoin/
      seloger/
      orpi/
      generic/

Chaque connecteur doit uniquement collecter et normaliser les donnees. La rentabilite et les decisions metier restent en Java.

## Architecture hexagonale Java

    immo-api/src/main/java/com/omena/immo/
      domain/
      application/
        port/
          in/
          out/
        service/
      adapter/
        in/web/
        out/persistence/
      config/

La couche domain ne depend pas de JPA ou de Spring MVC.

## Base de donnees

Deux PostgreSQL separes sont utilises :

- immo-db : donnees du logiciel ;
- n8n-db : donnees internes n8n.

Cela permet de sauvegarder, migrer ou remplacer n8n sans melanger ses tables avec le domaine immobilier.

## CI

GitHub Actions verifie automatiquement :

- les tests Spring Boot avec PostgreSQL ;
- le build Angular ;
- compose.yml ;
- compose.dev.yml ;
- compose.prod.yml.

## Branches

- main : version stable.
- dev : integration et developpement courant.
- feature/* : fonctionnalites importantes.

Le travail courant doit partir de dev. Les changements valides remontent ensuite vers main.
