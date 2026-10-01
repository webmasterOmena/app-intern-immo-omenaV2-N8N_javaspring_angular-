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

Deux stacks simples sont disponibles.

### Developpement

La stack de developpement lance Angular, Spring Boot, les deux PostgreSQL, n8n et Ollama :

    docker compose -f compose.dev.yml up -d --build

Ollama demarre sans aucun modele. Aucun modele n'est telecharge automatiquement.

Le volume Ollama existe pour conserver les modeles si vous en telechargez un manuellement plus tard, mais il reste pratiquement vide au premier lancement.

Pour verifier :

    docker compose -f compose.dev.yml ps

Pour arreter :

    docker compose -f compose.dev.yml down

### Production

En production, le modele Ollama doit etre choisi explicitement.

Exemple :

    OLLAMA_MODEL=qwen3:8b docker compose -f compose.prod.yml up -d --build

Au premier lancement, le service ollama-model-init verifie si le modele existe dans le volume persistant. S'il manque, il le telecharge. Aux lancements suivants, le modele deja present est reutilise.

Autre exemple :

    OLLAMA_MODEL=gemma3:12b docker compose -f compose.prod.yml up -d --build

Aucun modele par defaut n'est impose volontairement, afin d'eviter un telechargement volumineux accidentel.

Pour arreter :

    docker compose -f compose.prod.yml down

Les volumes PostgreSQL, n8n et Ollama sont conserves.

### Fichier .env

Le fichier .env reste optionnel en developpement.

Pour eviter de saisir le modele a chaque lancement en production, vous pouvez copier .env.example vers .env puis definir :

    OLLAMA_MODEL=qwen3:8b

Ensuite :

    docker compose -f compose.prod.yml up -d --build

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
