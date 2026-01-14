# API de Réservation de Ressources

Application REST API pour la gestion de réservations de ressources (salles de réunion, équipements, véhicules) avec authentification JWT.

## Technologies utilisées

- Java 21
- Spring Boot 4.0.1
- Spring Security (JWT)
- Spring Data JPA
- H2 Database (en mémoire)
- Maven
- Lombok

## Prérequis

- Java 21 ou supérieur

## Installation et démarrage
```bash
# Cloner le projet
git clone https://github.com/890Tom/reservation-de-ressources.git

# Accéder au dossier
cd reservation-de-ressources

# Lancer l'application
./mvnw spring-boot:run
```

L'API sera accessible sur : `http://localhost:8080`

## Rôles disponibles

- `USER` : Utilisateur standard
- `MANAGER` : Gestionnaire de ressources
- `ADMIN` : Administrateur système

## Fonctionnalités

- Authentification et autorisation (JWT)
- Gestion des ressources (CRUD)
- Gestion des réservations avec détection de conflits
- Système de rôles et permissions
- Validation des données

## Licence

Projet réalisé dans le cadre d'un test technique.