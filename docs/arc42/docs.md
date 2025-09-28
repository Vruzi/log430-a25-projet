# BrokerX - Documentation d'Architecture
Ce document, basé sur le modèle arc42, décrit l’architecture de **BrokerX**, une application de courtage éducative (LOG430).

---

## 1. Introduction et Objectifs

### Panorama des exigences
**BrokerX** est un système **client–serveur** implémenté comme un **monolithe modulaire** suivant un **service-based architecture style interne** (UI → API → components → DB).  
Il illustre :
- Une séparation nette **front (React/TypeScript)** / **back (Java Spring Boot 3)**.
- Une **API REST** contractuelle (DTO) exposant les cas d’utilisation.
- Une **persistence relationnelle** avec **PostgreSQL** (migrations **Flyway**).
- Des qualités visées : **latence** et **disponibilité** via **couche de cache**, **débit** via **Resilience4j** + pagination + index, **observabilité** minimale (sprint ultérieur).

### Objectifs qualité
| Priorité | Objectif qualité | Scénario |
|---|---|---|
| 1 | **Latence** | P95 `GET /api/portfolio/positions` < **50 ms** en dev grâce au cache + pré-agrégation |
| 2 | **Débit (rate)** | Supporter **10 req/s** sur `POST /api/orders` sans doublons (idempotence `clientOrderId`) |
| 3 | **Disponibilité** | En cas d’indispo MarketData simulée, afficher le **dernier snapshot** + bannière d’avertissement |
| 4 | **Observabilité (Sprint 2)** | Chaque requête porte un **traceId** dans les logs ; `/actuator/health` exposé |

### Parties prenantes (Stakeholders)
- **Équipe étudiante** : conception, implémentation, démonstration.
- **Enseignant·e / correcteur·trice** : validation des décisions, traçabilité.
- **Investisseurs (fictifs)** : utilisateurs finaux simulés (comptes, ordres, positions).

---

## 2. Contraintes d’architecture

| Contrainte | Description |
|---|---|
| **Style** | **Client/serveur** en **monolithe modulaire** (service-based interne) |
| **Technologies** | **React + TypeScript** (client), **Java 21 + Spring Boot 3** (serveur), **PostgreSQL** |
| **Sécurité** | Sessions **cookies HttpOnly**, **A2F TOTP** (MFA), CORS maîtrisé |
| **Déploiement** | **Docker** (Postgres, Redis pour cache, serveur Spring Boot) |
| **CI/CD** | Pipelines séparés `client/` et `server/`, **Flyway** pour les migrations |
| **Hors-périmètre** | Pas de microservices ; pas de serveur JS/TS (pas de Next.js SSR) ; pas de NoSQL pour le cœur métier |

---

## 3. Portée et contexte du système

### Contexte métier
- **Ordres & exécutions** : réception, validation pré-trade, routage et exécution simulée.
- **Portefeuilles & règlements (EOD)** : positions agrégées, snapshots quotidiens, rapports.
- **Conformité & surveillance** : contrôles pré-trade (pouvoir d’achat, restrictions), détection post-trade simple.
- **Observabilité & audit** : logs avec `traceId`, métriques de base, **journal d’audit** append-only.

### Contexte technique
- **Client** : SPA **React/TS** → **API REST** HTTP/JSON.
- **Serveur** : **Spring Boot 3** (controllers → services → repositories/DAO).
- **Base de données** : **PostgreSQL** (transactions ACID, index, vues/agrégations).
- **Cache** : **Redis** (lectures rapides positions/portefeuille, TTL court).

---

## 4. Stratégie de solution

| Problème | Approche de solution |
|---|---|
| Séparation UI/serveur | **API REST** + **DTO** (contrat stable, data minces) |
| Cohérence & audit | **PostgreSQL** (ACID) + table **audit_log** append-only |
| Latence | **Cache** Redis lecture + **pré-agrégations** (positions) + DTO minces |
| Débit | **Pagination**, **index SQL**, **HikariCP**, **Resilience4j** (rate-limit, timeouts, retry, circuit-breaker) |
| Disponibilité | **Monolithe** simple + **cache** ; modes dégradés (snapshot si source indispo) |
| Sécurité | Sessions **HttpOnly**, **A2F TOTP**, RBAC simple ; validation `@Valid` |
| Extensibilité | **Service-based interne** (components), interfaces (ports) entre components |

---

## 5. Vue des blocs de construction

> Découpage logique en **components internes** au monolithe (ownership de tables par composant).

- `api/` : contrôleurs REST, validation, mapping DTO.
- `orders/` : use cases d’ordres, machine à états, idempotence (`clientOrderId` unique).
- `portfolio/` : transactions/exécutions → positions agrégées, snapshots EOD, rapports.
- `compliance/` : règles pré-trade synchrones, surveillance post-trade asynchrone.
- `audit/` : `audit_log` append-only (+ option chaînage hash).
- `security/` : login, **A2F TOTP**, sessions, CORS.

**Règles internes**
- **Ownership** : un composant = ses repositories/DAO = ses tables.
- **Ports** : appels inter-components via interfaces (pas d’accès direct aux repos d’un autre composant).
- **DTO** à la frontière API uniquement ; entités JPA internes.

---

## 6. Vue d’exécution

**Flux “Placer un ordre (idempotent)”**
1. `POST /api/orders` (DTO) → `OrderService.placeOrder()`
2. **Compliance pré-trade** (synchrone) ; si OK : état `VALIDATED`
3. Persistance (`orders`, contrainte d’unicité `client_order_id`)
4. Routage/exécution **asynchrones** (scheduler léger)
5. Écritures `executions` + `transactions` ; **invalidation cache** positions utilisateur
6. `GET /api/portfolio/positions` → lecture **cache** (ou DB si miss), DTO réponse

---

## 7. Vue de déploiement

- **Monorepo**
    brokerx/
    ├─ client/ (React + TS)
    └─ server/ (Spring Boot 3)

- **Docker compose** : `postgres`, `redis`, `server` (client servi en statique ou séparé).
- **CI/CD** : jobs distincts pour `client` (lint/build) et `server` (build/tests/Flyway).

---

## 8. Concepts transversaux

- **API REST** versionnée (`/api/v1`) ; **DTO** d’entrée/sortie ; **validation `@Valid`**.
- **Sécurité** : sessions cookies HttpOnly (`SameSite`), **A2F TOTP** (QR + codes de secours).
- **Idempotence** : `clientOrderId` unique (contrainte DB) pour `POST /orders`.
- **Caching** : Redis lecture (TTL court, invalidation sur exécutions).
- **Persistance** : JPA **Repository** pour CRUD ; **DAO (JdbcTemplate)** ciblés pour requêtes/rapports complexes.
- **Migrations** : **Flyway** (`V1__init.sql`, …).
- **Observabilité (Sprint 2)** : logs avec `traceId` (MDC), **Actuator** (`/health`, `/metrics`), métriques Micrometer.

---

## 9. Décisions d’architecture

- **ADR-001** — *Monolithe modulaire (service-based interne) au lieu de microservices* : simplicité, vélocité, moins de complexité ops.
- **ADR-002** — *API REST + DTO vs entités exposées* : contrat stable, sécurité, payloads minces.
- **ADR-003** — *PostgreSQL vs NoSQL pour cœur métier* : ACID, intégrité, requêtes/agrégations fiables.
- **ADR-004** — *Idempotence par `clientOrderId` (contrainte unique)* : robustesse sous retry.
- **ADR-005** — *Cache lecture (Redis) + invalidation ciblée* : latence faible, charge DB réduite.
- **ADR-006** — *(À venir Sprint 2)* *Logs structurés + traceId + Actuator*.

> Les fichiers ADR seront placés sous `/docs/adr/adrXXX.md`.

---

## 10. Exigences qualité

### Maintenabilité
- **Service-based interne** : components isolés, ownership clair des tables.
- **Couches** : Controller → Service → Repository/DAO ; mapping **Entité ↔ DTO**.

### Flexibilité
- **Ports**/interfaces entre components ; remplacement aisé d’adapters (ex. “bourse simulée”).
- **DTO** versionnés ; ajout d’endpoints sans casser les existants.

### Évolutivité (scalabilité)
- **Débit** : pagination, index, HikariCP ; **Resilience4j** (rate-limit, timeouts).
- **Latence** : cache lecture, DTO minces, pré-agrégations, HTTP/2.

### Sécurité
- Sessions **HttpOnly**, **MFA TOTP** (activation + vérification), RBAC simple.
- **Audit** des actions sensibles (login, ordres, réglages).

---

## 11. Risques et dettes techniques

| Risque | Impact | Mitigation |
|---|---|---|
| Cache incohérent (staleness) | Données affichées périmées | TTL court + **invalidation** à l’arrivée d’exécutions |
| Couplage entre components | Difficulté de refactor | **Ports** explicites, interdiction d’accès cross-repo |
| SQL sous-optimal | Latence/débit dégradés | **Index** clés, profiling, DAO dédiés pour requêtes lourdes |
| Observabilité tardive | Debug difficile | MVP logging (traceId) en Sprint 1, Actuator en Sprint 2 |

---

## 12. Glossaire

| Terme | Définition |
|---|---|
| **DTO** | *Data Transfer Object* : objet d’échange API (entrée/sortie), sans logique métier |
| **DAO** | *Data Access Object* : encapsulation de l’accès DB (SQL), complémentaire aux Repository |
| **A2F / MFA TOTP** | Authentification à 2 facteurs par code temporel (RFC 6238) |
| **EOD** | *End Of Day* : clôture journalière (snapshots, rapports) |
| **Idempotence** | Même requête répétée ⇒ même effet ; ici via `clientOrderId` unique |
| **Service-based (interne)** | Découpage en services **logiques** dans un **monolithe** (appels en mémoire) |