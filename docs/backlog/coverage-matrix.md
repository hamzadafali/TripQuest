# Livrable 1 — Matrice de couverture des 30 sujets de roadmap

Chaque sujet de la roadmap est relié à une Issue principale, à d'éventuelles Issues secondaires, à une capacité attendue et à une preuve finale attendue. Les preuves listées sont des **attentes** : elles seront produites par la Factory dans `evidence.md`, pas ici.

Légende cycle 1 : les sujets sont regroupés deux par deux (Bloc A = S1+S2, Bloc C = S5+S6, Bloc B = S3+S4). L'ordre retenu est A → C → B (justification dans `roadmap.md`).

| # | Roadmap Subject | Primary Issue | Secondary Issues | Expected Capability | Expected Final Evidence |
| --- | --- | --- | --- | --- | --- |
| S1 | Java moderne (records, sealed types, pattern matching, immutabilité, exceptions, generics) | **TQ-C1-01** (Bloc A, avec S2) | TQ-C1-02, TQ-C3-02 | Modèle métier TripQuest exprimant états, transitions et invariants de façon fermée et immuable | Tests métier passant sur les règles TRIP/MEMBER/ACTIVITY/VOTE/ITINERARY ; exhaustivité des transitions vérifiée à la compilation ; politique erreurs métier vs erreurs techniques documentée |
| S2 | Collections et modèles mémoire (égalité, hashing, complexité, mutabilité, références, allocation) | **TQ-C1-01** (Bloc A, avec S1) | TQ-C1-03, TQ-C3-04 | Identités, ensembles de participants/votes sans doublon, collections internes non exposées, complexité du calcul de score et de conflits documentée | Tests d'égalité/hachage ; tests d'immuabilité des collections exposées ; note de complexité des calculs de score et de chevauchement |
| S3 | JVM internals (bytecode, class loading, JIT, heap, stacks, metaspace, escape analysis) | **TQ-C1-03** (Bloc B, avec S4) | TQ-C5-04 | Capacité à expliquer le comportement d'exécution de TripQuest sous charge (warm-up, allocation, piles) | Enregistrement JFR interprété ; mesure d'escape analysis sur un chemin chaud ; thread dump et heap dump capturés et lus ; baseline JVM documentée |
| S4 | Garbage Collection et profiling (G1, ZGC, pauses, allocation, JFR, JMC, dumps) | **TQ-C1-03** (Bloc B, avec S3) | TQ-C5-02, TQ-C5-04 | Diagnostic reproductible distinguant CPU, mémoire, contention et I/O ; comparaison G1/ZGC sur un scénario TripQuest | Quatre scénarios de panne induits et identifiés ; tableau comparatif G1/ZGC ; un point chaud d'allocation réduit avec mesure avant/après ; runbook de diagnostic |
| S5 | Concurrence Java (JMM, visibilité, atomicité, locks, atomics, concurrent collections) | **TQ-C1-02** (Bloc C, avec S6) | TQ-C3-03, TQ-C4-05 | Votes et acceptations d'invitation cohérents sous actions simultanées (VOTE-08, MEMBER-03, AC-5 MVP) | Race condition reproduite puis éliminée ; tests concurrents répétés sans perte ni doublon ; justification atomicité/visibilité/ordering par règle |
| S6 | Virtual Threads (executors, CompletableFuture, virtual threads, pinning, backpressure, limites) | **TQ-C1-02** (Bloc C, avec S5) | TQ-C1-03, TQ-C2-02 | Simulation de nombreux participants simultanés avec threads virtuels, détection du pinning, charge bornée | Harnais de simulation platform vs virtual threads avec mesures ; rapport de pinning ; limite de backpressure démontrée |
| S7 | Spring Core (IoC, bean lifecycle, scopes, injection, post-processors, context) | **TQ-C2-01** | TQ-C2-02, TQ-C2-06 | Modules TripQuest assemblés dans un contexte Spring sans dépendance du domaine au framework | Test de contexte ; règles de dépendance entre modules vérifiées ; cycle de vie des composants à état documenté |
| S8 | Spring Boot internals (auto-configuration, conditional beans, starters, properties, Actuator) | **TQ-C2-02** | TQ-C5-03, TQ-C5-04 | Démarrage reproductible, configuration typée et validée, santé exposée | Propriétés validées au démarrage ; rapport d'auto-configuration revu ; endpoint de santé ; décision threads virtuels côté serveur tracée |
| S9 | Spring Web (MVC, servlet lifecycle, filters, interceptors, validation, exception mapping, API design) | **TQ-C2-03** | TQ-C4-01 | Parcours MVP 1 à 4 réalisables via API HTTP avec validation et erreurs cohérentes | Parcours bout en bout via HTTP testé ; refus métier mappés ; trace d'une requête dans la chaîne Spring |
| S10 | AOP et transactions (proxies, self-invocation, propagation, rollback, isolation, boundaries) | **TQ-C2-06** | TQ-C3-03 | Décisions de l'organisateur atomiques et tracées (clôture, sélection, finalisation, réouverture) | Test de rollback sur échec partiel ; piège de self-invocation démontré et évité ; frontières transactionnelles documentées |
| S11 | Authentication (Spring Security, filter chain, OAuth2, OIDC, JWT, sessions, resource server) | **TQ-C2-04** | TQ-C4-02 | Identité authentifiée dérivée uniquement du jeton (ACCOUNT-04), comptes uniques par email (ACCOUNT-01) | Resource server OIDC configuré ; 401 sans jeton ; compte lié à l'identité ; unicité email testée |
| S12 | Authorization et sécurité applicative (RBAC/ABAC, method security, ownership, CORS, CSRF, SSRF, secrets, OWASP) | **TQ-C2-05** | TQ-C2-03, TQ-C4-01, TQ-C5-03 | Isolation des voyages (AC-2 MVP), rôles organisateur/participant, propriété des propositions, durcissement | Matrice de tests d'autorisation ; threat model simple ; test SSRF sur lien externe ; secrets hors dépôt |
| S13 | Modélisation PostgreSQL (relationnel, contraintes, normalisation, types, ownership) | **TQ-C3-01** | TQ-C1-01, TQ-C3-05 | État de TripQuest persistant, invariants clés garantis par le schéma | Schéma sous contrôle de version ; tests de violation de contraintes (email unique, vote unique, invitation active unique) |
| S14 | JPA/Hibernate (persistence context, dirty checking, fetch plans, N+1, batching, locking) | **TQ-C3-02** | TQ-C3-04 | Vues du voyage chargées sans N+1, cycle de vie du persistence context maîtrisé | Assertions sur le nombre de requêtes ; fetch plans documentés ; batching démontré |
| S15 | SQL et performance (index, sélectivité, statistiques, EXPLAIN ANALYZE, pagination) | **TQ-C3-04** | TQ-C5-02 | Tableau de bord et listes de propositions rapides sur un volume généré | EXPLAIN ANALYZE avant/après ; index justifiés par sélectivité ; pagination testée |
| S16 | Transactions et concurrence (MVCC, isolation, lost update, deadlocks, verrouillage) | **TQ-C3-03** | TQ-C1-02, TQ-C2-06 | Cohérence des votes et décisions au niveau base, y compris avec plusieurs instances | Anomalie concurrente reproduite en base puis corrigée ; deadlock reproduit et résolu ; stratégie de verrouillage tracée |
| S17 | Migrations (Flyway, expand/contract, zero-downtime, backfill, versions mixtes) | **TQ-C3-05** | TQ-C3-01 | Évolution du schéma compatible avec deux versions de l'application | Migration en phases expand/contract ; backfill ; test versions mixtes |
| S18 | Spring Batch (job, step, chunk, reader/processor/writer, restart, skip/retry, partitioning) | **TQ-C3-06** (enabler, besoin `UNKNOWN / TO VALIDATE`) | TQ-C4-05 | Si validé : traitement périodique reprenable sans double effet | Job restartable ; preuve d'absence de double effet après reprise ; skip/retry ; ou décision humaine documentée de remplacement |
| S19 | APIs robustes (REST, OpenAPI, versioning, pagination, idempotency keys, Problem Details) | **TQ-C4-01** | TQ-C2-03 | Contrat d'API stable et documenté, créations rejouables sans doublon | OpenAPI validée ; Problem Details ; clé d'idempotence testée sur création ; ADR versionnement |
| S20 | Clients HTTP résilients (timeouts, pools, retries, backoff, circuit breaker, bulkhead) | **TQ-C4-02** (enabler, besoin `UNKNOWN / TO VALIDATE`) | TQ-C2-04, TQ-C5-01 | Si validé : appels sortants (candidat : fournisseur OIDC) bornés en temps et en pannes | Tous les timeouts définis ; tests de pannes simulées ; circuit breaker observé ; ou décision humaine documentée |
| S21 | Kafka fondamental (broker, topic, partition, offset, consumer group, rebalance, ordering) | **TQ-C4-03** (enabler, besoin `UNKNOWN / TO VALIDATE`) | TQ-C4-04 | Si validé : découplage asynchrone d'un flux TripQuest (candidat : notifications) | Rebalance observé et expliqué ; ordre par clé démontré ; ou décision humaine documentée |
| S22 | Contrats événementiels (domain/integration events, schemas, compatibilité, partition keys) | **TQ-C4-04** (enabler, dépend de TQ-C4-03) | TQ-C2-06 | Si validé : contrats d'événements versionnés et compatibles | Évolution de schéma sans casser un consommateur ; choix de clé de partition tracé |
| S23 | Garanties de traitement (at-least-once, doublons, idempotence, retry, DLQ, poison messages) | **TQ-C4-05** (enabler, dépend de TQ-C4-03) | TQ-C3-06, TQ-C1-02 | Si validé : reprise après crash d'un consommateur sans double effet | Crash/reprise démontrés ; DLQ ; consommateur idempotent testé |
| S24 | Cohérence distribuée (dual write, Outbox, Saga, compensation, réconciliation) | **TQ-C4-06** (enabler, dépend de TQ-C4-03) | TQ-C2-06 | Si validé : choix argumenté entre transaction locale, Outbox, Saga et réconciliation pour TripQuest | Problème de double écriture reproduit ; solution retenue par ADR ; ou décision humaine documentée |
| S25 | Stratégie de tests (JUnit 5, Mockito raisonné, intégration, Testcontainers, WireMock) | **TQ-C5-01** | Toutes les Issues (secondaire) | Suite de tests structurée, rapide et fiable sur PostgreSQL réel et dépendances simulées | Stratégie de tests documentée ; Testcontainers ; WireMock ; budget de temps de suite respecté |
| S26 | Observabilité (logs structurés, Micrometer, Prometheus, traces, OpenTelemetry, correlation IDs) | **TQ-C5-02** | TQ-C1-03, TQ-C2-03 | Chaque requête et décision TripQuest traçable de bout en bout ; indicateurs produit (§11) mesurés | Correlation ID propagé ; métriques exposées ; trace distribuée ; tableau de bord local |
| S27 | CI/CD et conteneurs (Gradle, quality gates, Docker non-root, SBOM, scans, pipeline) | **TQ-C5-03** | TQ-C2-02 | Build, vérification et empaquetage reproductibles | Pipeline avec quality gates ; image non-root ; SBOM ; scan de vulnérabilités |
| S28 | Cloud et Kubernetes minimal (AWS concepts, déploiement, probes, ressources, configuration, rollback) | **TQ-C5-04** (enabler, cible `UNKNOWN / TO VALIDATE`) | TQ-C1-03, TQ-C5-03 | Si validé : TripQuest déployé avec probes, ressources et rollback | Déploiement reproductible ; probes reliées à la santé ; rollback démontré ; ou décision humaine documentée |
| S29 | Quarkus vs Spring Boot (CDI, configuration, REST, Panache, Dev Services, native image, compromis) | **TQ-C5-05** (enabler, périmètre `UNKNOWN / TO VALIDATE`) | — | Évaluation comparée sur une tranche bornée de TripQuest, sans migration décidée | Comparatif mesuré (démarrage, mémoire, expérience dev) ; ADR de conclusion |
| S30 | Architecture et incident final (modular monolith, DDD, microservices, ADR, performance, incident, post-mortem) | **TQ-C5-06** | TQ-C2-01 | Architecture consolidée et documentée ; capacité à tenir et analyser un incident | Revue des frontières de modules ; ADR ; incident simulé ; post-mortem |

## Contrôle de couverture

- nombre de sujets attendus : **30** ;
- nombre de sujets couverts : **30** ;
- nombre de sujets sans Issue principale : **0** ;
- nombre d'Issues Jira : **27** (3 détaillées + 24 outlines) ;
- résultat : **`COVERAGE COMPLETE`**.

Regroupements du cycle 1 visibles dans la matrice : S1+S2 → TQ-C1-01, S5+S6 → TQ-C1-02, S3+S4 → TQ-C1-03.

Sujets couverts par un `TECHNICAL ENABLER` dont le besoin produit reste `UNKNOWN / TO VALIDATE` : S18, S20, S21, S22, S23, S24, S28, S29. Ils sont couverts et traçables, mais ne constituent pas des décisions architecturales approuvées.

---

## Final Coverage Report

- Roadmap subjects expected: **30**
- Roadmap subjects covered: **30**
- Detailed Cycle 1 Issues: **3** (TQ-C1-01, TQ-C1-02, TQ-C1-03), deux sujets principaux chacune
- Outline Issues: **24** (six par cycle, cycles 2 à 5)
- Total Jira Issues: **27**
- Missing subjects: **aucun**
- Duplicated or overloaded subjects:
  - aucun sujet dupliqué ;
  - sujets transversaux volontairement présents en secondaire : S5 (concurrence), S12 (sécurité), S25 (tests), S26 (observabilité) ;
  - point d'attention : TQ-C2-06 (S10) porte à la fois les transactions et l'introduction probable d'un premier support transactionnel, ce qui empiète sur S13 ; la frontière exacte est `UNKNOWN / TO DECIDE` (voir `roadmap.md`, cycle 2).
- Important unknowns:
  - moment d'introduction de PostgreSQL : fin du cycle 2 (TQ-C2-06) ou début du cycle 3 (TQ-C3-01) ;
  - fournisseur d'identité OIDC pour le développement ;
  - besoin produit réel justifiant Spring Batch, Kafka et les sujets distribués (S18, S21–S24) ; hypothèse candidate : notifications in-app et expiration des invitations, non validée ;
  - appels HTTP sortants réels de TripQuest (S20) ; candidat : fournisseur OIDC ;
  - cible de déploiement (S28) : local ou cloud ;
  - interface utilisateur : hors roadmap backend, non couverte ;
  - utilisation de JMH/jcstress pour les mesures du cycle 1 ;
  - exécution du serveur HTTP sur threads virtuels (Spring Boot) ;
  - découpage exact des modules du monolithe.
- Final result: **`READY FOR HUMAN REVIEW`**
