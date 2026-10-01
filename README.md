# Omena Immo V2

Socle V2 de l'application interne de sourcing immobilier Omena.

## Stack

- Angular 22 pour l'interface.
- Java 21 + Spring Boot 4.1 pour l'API.
- Architecture hexagonale cote Java.
- PostgreSQL pour les donnees metier.
- n8n pour les automatisations et l'orchestration.
- Ollama local prevu pour les analyses IA.
- Connecteurs de scraping separes, ajoutes progressivement.

## Demarrage

Aucun .env n'est obligatoire pour le premier lancement.

Depuis la racine :

    docker compose up -d

Docker construit le frontend et le backend au premier lancement.

Pour suivre :

    docker compose ps
    docker compose logs -f immo-api
    docker compose logs -f immo-web
    docker compose logs -f n8n

Pour arreter :

    docker compose down

Les volumes sont conserves.

## URLs locales

- Angular : http://localhost:4200
- API : http://localhost:8080/api/health
- n8n integre : http://localhost:5679

n8n utilise volontairement 5679 pour pouvoir cohabiter avec une installation n8n independante deja exposee sur 5678.

## Configuration optionnelle

Pour modifier les valeurs par defaut :

    cp .env.example .env

PowerShell :

    Copy-Item .env.example .env

Le vrai .env est ignore par Git.

## Architecture

    Angular
       |
       v
    Spring Boot -------- PostgreSQL metier
       ^
       |
      n8n -------------- PostgreSQL n8n
       |
       +---- Ollama
       +---- Gmail / Drive / APIs
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
- tableaux de bord.

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
    GET  /api/search-criteria
    GET  /api/search-criteria/active
    POST /api/search-criteria

### n8n

n8n sert d'orchestrateur :

- planification des collectes ;
- recuperation des criteres actifs depuis Java ;
- appel des futurs scrapers ;
- Ollama ou autres IA ;
- Gmail / Drive ;
- notifications ;
- retries et workflows externes.

Depuis n8n, l'API Java est accessible avec :

    http://immo-api:8080

Ollama installe sur la machine hote est prevu via :

    http://host.docker.internal:11434

### Scrapers

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
- la validite du Docker Compose.

## Branches

- main : version stable.
- dev : integration et developpement courant.
- feature/* : fonctionnalites importantes.

Le travail courant doit partir de dev. Les changements valides remontent ensuite vers main.
