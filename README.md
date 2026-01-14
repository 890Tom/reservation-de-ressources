# Système de Réservation de Ressources

Application web de gestion de réservations de ressources (salles de réunion, équipements, véhicules) avec authentification JWT et interface Thymeleaf.

---

## Auteur

Développé dans le cadre d'un test technique.

---

## Licence

Ce projet est à usage éducatif.

## Table des matières

- [Technologies utilisées](#-technologies-utilisées)
- [Fonctionnalités](#-fonctionnalités)
- [Installation et démarrage](#-installation-et-démarrage)
- [Comptes de test](#-comptes-de-test)
- [Architecture](#-architecture)
- [Endpoints API](#-endpoints-api)
- [Règles métier](#-règles-métier)

---

## Technologies utilisées

- **Backend :** Java 21, Spring Boot 4.0.1
- **Sécurité :** Spring Security 7.0.2, JWT (jjwt 0.12.5)
- **Base de données :** H2 (en mémoire)
- **ORM :** Spring Data JPA, Hibernate
- **Frontend :** Thymeleaf, Bootstrap 5.3.2
- **Build :** Maven

---

## Fonctionnalités

### Authentification & Autorisation
- Inscription avec validation des données
- Connexion avec génération de token JWT
- Gestion de 3 rôles : **USER**, **MANAGER**, **ADMIN**
- Gestion de profil utilisateur (consultation et modification)

### Gestion des Ressources
- Créer une ressource (MANAGER/ADMIN uniquement)
-  Lister les ressources avec filtres (type, statut) et pagination
-  Consulter le détail d'une ressource
-  Modifier une ressource (MANAGER/ADMIN)
-  Supprimer une ressource (ADMIN uniquement)
-  Vérifier la disponibilité d'une ressource sur un créneau

### Gestion des Réservations
-  Créer une réservation avec validation des conflits
-  Lister ses réservations
-  Consulter les réservations d'une ressource (calendrier)
-  Modifier une réservation (propriétaire ou MANAGER)
-  Annuler une réservation
-  Confirmer une réservation (MANAGER uniquement)
-  Voir toutes les réservations (MANAGER/ADMIN)

---

## Installation et démarrage

### Prérequis
- Java 21+
- Maven 3.8+

### Étapes

1. **Cloner le projet**
```bash
git clone https://github.com/890Tom/reservation-de-ressources.git
cd reservation-de-ressources
```

2. **Lancer l'application**
**MacOS / Linux :**
```bash
./mvnw spring-boot:run
```

**Windows (CMD) :**
```cmd
mvnw.cmd spring-boot:run
```

**Windows (PowerShell) :**
```powershell
.\mvnw.cmd spring-boot:run
```

3. **Accéder à l'application**
- **Interface web :** http://localhost:8080
- **API REST :** http://localhost:8080/api
- **Console H2 :** http://localhost:8080/h2-console
  - JDBC URL : `jdbc:h2:mem:reservation_db`
  - Username : `sa`
  - Password : *(laisser vide)*

---

## Comptes de test

Des comptes sont automatiquement créés au démarrage :

| Username | Password | Rôle | Accès |
|----------|----------|------|-------|
| `admin` | `admin123` | ADMIN | Toutes les fonctionnalités |
| `manager` | `manager123` | MANAGER | Gestion ressources + confirmation réservations |
| `user` | `user123` | USER | Consultation + réservations |
| `john` | `john123` | USER | Consultation + réservations |

---

## Architecture
```
src/main/
├── java/com/reservation/
│   ├── config/              # Configuration (Security, DataInitializer)
│   ├── controller/
│   │   ├── mvc/            # Controllers MVC (Thymeleaf)
│   │   └── ...             # Controllers REST API
│   ├── dto/
│   │   ├── request/        # DTOs pour les requêtes
│   │   └── response/       # DTOs pour les réponses
│   ├── entity/             # Entités JPA (User, Resource, Reservation)
│   ├── enums/              # Énumérations (Role, ResourceType, Status...)
│   ├── exception/          # Exceptions métier
│   ├── repository/         # Repositories Spring Data JPA
│   ├── security/
│   │   ├── jwt/           # JWT Utils, Filters, EntryPoint
│   │   └── service/       # UserDetailsService
│   └── service/            # Services métier
└── resources/
    ├── templates/          # Pages Thymeleaf
    │   ├── auth/          # Pages authentification
    │   ├── user/          # Pages utilisateur
    │   ├── resource/      # Pages ressources
    │   ├── reservation/   # Pages réservations
    │   ├── admin/         # Pages administration
    │   └── layout/        # Layouts (header, footer)
    ├── static/            # CSS, JS
    └── application.properties
```

---

## Endpoints API

### Authentification
```
POST   /api/auth/register    # Inscription
POST   /api/auth/login       # Connexion (retourne JWT)
```

### Utilisateurs
```
GET    /api/users/me         # Mon profil
PUT    /api/users/me         # Modifier mon profil
GET    /api/users            # Liste utilisateurs (MANAGER/ADMIN)
GET    /api/users/{id}       # Détail utilisateur (MANAGER/ADMIN)
PUT    /api/users/{id}       # Modifier utilisateur (ADMIN)
DELETE /api/users/{id}       # Supprimer utilisateur (ADMIN)
PATCH  /api/users/{id}/role  # Changer rôle (ADMIN)
PATCH  /api/users/{id}/status # Activer/désactiver (ADMIN)
```

### Ressources
```
POST   /api/resources        # Créer ressource (MANAGER/ADMIN)
GET    /api/resources        # Liste ressources (filtres: type, status)
GET    /api/resources/{id}   # Détail ressource
PUT    /api/resources/{id}   # Modifier ressource (MANAGER/ADMIN)
DELETE /api/resources/{id}   # Supprimer ressource (ADMIN)
GET    /api/resources/{id}/availability # Vérifier disponibilité
```

### Réservations
```
POST   /api/reservations     # Créer réservation
GET    /api/reservations/my  # Mes réservations
GET    /api/reservations     # Toutes (MANAGER/ADMIN)
GET    /api/reservations/resource/{id} # Réservations d'une ressource
GET    /api/reservations/{id} # Détail réservation
PUT    /api/reservations/{id} # Modifier (propriétaire/MANAGER)
PATCH  /api/reservations/{id}/cancel  # Annuler
PATCH  /api/reservations/{id}/confirm # Confirmer (MANAGER)
```

### Exemple d'utilisation API

**1. Connexion**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user123"}'
```

**2. Créer une réservation (avec le token reçu)**
```bash
curl -X POST http://localhost:8080/api/reservations \
  -H "Authorization: Bearer VOTRE_TOKEN_ICI" \
  -H "Content-Type: application/json" \
  -d '{
    "resourceId": 1,
    "startTime": "2026-01-20T10:00:00",
    "endTime": "2026-01-20T12:00:00",
    "purpose": "Réunion d'\''équipe"
  }'
```

---

## Règles métier

### Réservations
-  **Anti-conflit :** Une ressource ne peut être réservée qu'une fois par créneau
-  **Durée :** Minimum 30 minutes, maximum 8 heures
-  **Anticipation :** Réservation jusqu'à 30 jours à l'avance
-  **Annulation :** Possible jusqu'à 2 heures avant le début
-  **Validation :** Les MANAGER peuvent confirmer les réservations PENDING

### Types de ressources
- **MEETING_ROOM** : Salles de réunion
- **EQUIPMENT** : Équipements (laptops, projecteurs...)
- **VEHICLE** : Véhicules de service

### Statuts des ressources
- **AVAILABLE** : Disponible pour réservation
- **MAINTENANCE** : En maintenance
- **UNAVAILABLE** : Indisponible

### Statuts des réservations
- **PENDING** : En attente de confirmation
- **CONFIRMED** : Confirmée par un MANAGER
- **CANCELLED** : Annulée
- **COMPLETED** : Terminée

---

## Sécurité

- **Double authentification :**
  - JWT pour l'API REST (stateless)
  - Sessions pour l'interface web (Thymeleaf)
- **Mots de passe hashés** avec BCrypt
- **Protection CSRF** activée pour les pages web
- **Contrôle d'accès basé sur les rôles** avec `@PreAuthorize`

---

## Données initiales

Au démarrage, l'application crée automatiquement :
- **4 utilisateurs** (1 ADMIN, 1 MANAGER, 2 USERS)
- **12 ressources** (4 salles, 4 équipements, 4 véhicules)

---

## Tests

### Interface web
1. Accédez à http://localhost:8080
2. Connectez-vous avec un compte de test
3. Testez les fonctionnalités selon votre rôle

### API REST
Utilisez les exemples curl ci-dessus

---

