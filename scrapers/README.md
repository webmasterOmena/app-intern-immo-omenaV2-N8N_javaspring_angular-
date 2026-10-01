# Scrapers / connecteurs

Ce dossier accueillera les connecteurs de collecte independants.

Structure cible :

    scrapers/
      leboncoin/
      seloger/
      orpi/
      generic/

Un connecteur complexe pourra etre un petit service Playwright appele par n8n.

Principes :
- aucune logique financiere dans le scraper ;
- sortie JSON normalisee ;
- appels limites au necessaire ;
- arret propre en cas de 403, 429 ou challenge ;
- chaque source reste interchangeable.

Aucun scraper n'est lance dans le compose initial.
