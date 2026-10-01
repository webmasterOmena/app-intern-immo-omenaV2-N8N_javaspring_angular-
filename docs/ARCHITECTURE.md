# Architecture Omena Immo V2

Le backend Java est la source de verite du produit.

## Responsabilites

Angular :
- interface et formulaires ;
- criteres de recherche ;
- affichage des opportunites.

Spring Boot :
- API ;
- logique metier ;
- calculs ;
- validation ;
- persistance ;
- droits futurs.

n8n :
- planification ;
- orchestration ;
- appels de connecteurs ;
- services IA externes si necessaire ;
- mails, Drive et notifications.

Scrapers :
- un connecteur isole par source ;
- aucune logique financiere ;
- sortie normalisee.

## Flux cible

    Angular
       |
       v
    Spring Boot ---- PostgreSQL
       ^
       |
      n8n ---- services externes
       |
       v
    Connecteurs de collecte

## Architecture hexagonale Java

    com.omena.immo
    +-- domain
    +-- application
    |   +-- port/in
    |   +-- port/out
    |   +-- service
    +-- adapter
        +-- in/web
        +-- out/persistence

Le domaine ne depend ni de Spring MVC ni de JPA.

## Regles

1. Les donnees metier durables appartiennent au backend Java.
2. Les calculs financiers appartiennent au backend Java.
3. Les integrations et orchestrations appartiennent plutot a n8n.
4. Un scraper doit pouvoir etre remplace sans modifier le domaine.
5. Les secrets ne sont jamais versionnes.
