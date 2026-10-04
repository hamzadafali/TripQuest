# Livrable 2 — Ordonnancement global des 5 cycles

Vue synthétique des 27 Issues, puis détail par cycle : objectif, capacité TripQuest obtenue, dépendances, sujets différés, risques de conception, éléments `UNKNOWN`.

## Tableau global

| Cycle | Issue | Title | Main Topic | TripQuest Outcome | Dependencies | Maturity |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | TQ-C1-01 | Modéliser le cœur métier de TripQuest et ses invariants (voyage, participants, propositions, votes, itinéraire) | S1 Java moderne + S2 Collections et modèles mémoire | Règles du MVP exécutables et testées en Java pur, parcours complet jouable en mémoire | — | DETAILED |
| 1 | TQ-C1-02 | Garantir la cohérence des votes et des adhésions sous actions simultanées | S5 Concurrence Java + S6 Virtual Threads | VOTE-08 / MEMBER-03 / AC-5 tenus sous concurrence, simulation de nombreux participants | TQ-C1-01 | DETAILED |
| 1 | TQ-C1-03 | Rendre le comportement d'exécution de TripQuest observable et diagnosticable (CPU, mémoire, contention, I/O) | S3 JVM internals + S4 GC et profiling | Diagnostic reproductible, baseline JVM, runbook, un point chaud corrigé | TQ-C1-01, TQ-C1-02 | DETAILED |
| 2 | TQ-C2-01 | Assembler les modules de TripQuest dans un conteneur Spring maîtrisé | S7 Spring Core | Application assemblée, frontières de modules explicites | TQ-C1-01..03 | OUTLINE |
| 2 | TQ-C2-02 | Démarrer et configurer TripQuest de manière reproductible (profils, propriétés, santé) | S8 Spring Boot internals | Configuration typée, santé exposée, auto-configuration comprise | TQ-C2-01 | OUTLINE |
| 2 | TQ-C2-03 | Exposer les parcours du MVP via une API HTTP validée et cohérente en erreurs | S9 Spring Web | Parcours 1 à 4 réalisables via HTTP | TQ-C2-02 | OUTLINE |
| 2 | TQ-C2-04 | Authentifier les utilisateurs de TripQuest via un fournisseur OIDC et lier leur compte | S11 Authentication | Module A (comptes), ACCOUNT-01/04 | TQ-C2-03 | OUTLINE |
| 2 | TQ-C2-05 | Isoler chaque voyage : autorisations par rôle et par appartenance, durcissement applicatif | S12 Authorization et sécurité applicative | AC-2 MVP, rôles, propriété, threat model | TQ-C2-04 | OUTLINE |
| 2 | TQ-C2-06 | Rendre atomiques et traçables les décisions de l'organisateur (clôture, sélection, finalisation, réouverture) | S10 AOP et transactions | AC-10 MVP, frontières transactionnelles, premier support transactionnel | TQ-C2-05 | OUTLINE |
| 3 | TQ-C3-01 | Persister TripQuest dans PostgreSQL avec un modèle relationnel contraint | S13 Modélisation PostgreSQL | État durable, invariants garantis par le schéma | TQ-C2-06 | OUTLINE |
| 3 | TQ-C3-02 | Charger les vues du voyage avec JPA/Hibernate sans surprise (N+1, fetch, dirty checking) | S14 JPA/Hibernate | Vues du voyage efficaces et prévisibles | TQ-C3-01 | OUTLINE |
| 3 | TQ-C3-03 | Garantir la cohérence des votes et décisions concurrentes au niveau de la base | S16 Transactions et concurrence | Garanties du cycle 1 reportées sur la base, multi-instances | TQ-C3-02 | OUTLINE |
| 3 | TQ-C3-04 | Garder le tableau de bord et les listes de propositions rapides à grande échelle | S15 SQL et performance | Requêtes clés indexées et paginées | TQ-C3-02 | OUTLINE |
| 3 | TQ-C3-05 | Faire évoluer le schéma de TripQuest sans interruption ni perte (expand/contract) | S17 Migrations | Migration compatible deux versions | TQ-C3-01 | OUTLINE |
| 3 | TQ-C3-06 | Traiter en lot et de façon reprenable les tâches périodiques de TripQuest (enabler à valider) | S18 Spring Batch | Si validé : traitement restartable sans double effet | TQ-C3-03 | OUTLINE |
| 4 | TQ-C4-01 | Stabiliser le contrat de l'API TripQuest : OpenAPI, versionnement, pagination, idempotence, Problem Details | S19 APIs robustes | Contrat stable, créations rejouables | TQ-C2-03, TQ-C3-04 | OUTLINE |
| 4 | TQ-C4-02 | Borner les appels sortants de TripQuest : timeouts, retries, circuit breaker (enabler à valider) | S20 Clients HTTP résilients | Si validé : dépendances externes maîtrisées | TQ-C2-04 | OUTLINE |
| 4 | TQ-C4-03 | Découpler un flux TripQuest par messagerie asynchrone (enabler à valider) | S21 Kafka fondamental | Si validé : premier flux asynchrone | TQ-C2-06 | OUTLINE |
| 4 | TQ-C4-04 | Définir et faire évoluer les contrats d'événements de TripQuest (enabler à valider) | S22 Contrats événementiels | Si validé : événements versionnés et compatibles | TQ-C4-03 | OUTLINE |
| 4 | TQ-C4-05 | Garantir la reprise sans double effet du traitement asynchrone (enabler à valider) | S23 Garanties de traitement | Si validé : at-least-once maîtrisé, DLQ | TQ-C4-04 | OUTLINE |
| 4 | TQ-C4-06 | Choisir et prouver la stratégie de cohérence entre base et messagerie (enabler à valider) | S24 Cohérence distribuée | Si validé : Outbox / Saga / réconciliation argumentés | TQ-C4-05 | OUTLINE |
| 5 | TQ-C5-01 | Consolider la stratégie de tests de TripQuest (pyramide, Testcontainers, WireMock) | S25 Stratégie de tests | Suite fiable, rapide, sur dépendances réelles ou simulées | TQ-C3-01, TQ-C4-01 | OUTLINE |
| 5 | TQ-C5-02 | Rendre chaque requête et chaque décision de TripQuest observable (logs, métriques, traces) | S26 Observabilité | Traçabilité technique bout en bout, indicateurs produit mesurés | TQ-C2-03, TQ-C1-03 | OUTLINE |
| 5 | TQ-C5-03 | Construire, vérifier et empaqueter TripQuest de façon reproductible (pipeline, image non-root, SBOM) | S27 CI/CD et conteneurs | Livraison reproductible | TQ-C5-01 | OUTLINE |
| 5 | TQ-C5-04 | Déployer TripQuest sur une cible minimale avec probes, ressources et rollback (enabler à valider) | S28 Cloud et Kubernetes minimal | Si validé : déploiement reproductible | TQ-C5-03, TQ-C1-03 | OUTLINE |
| 5 | TQ-C5-05 | Évaluer Quarkus face à Spring Boot sur une tranche bornée de TripQuest (enabler à valider) | S29 Quarkus vs Spring Boot | Comparatif mesuré, aucune migration décidée | TQ-C5-03 | OUTLINE |
| 5 | TQ-C5-06 | Consolider l'architecture de TripQuest et prouver sa tenue en incident (ADR, post-mortem) | S30 Architecture et incident final | Frontières validées, ADR, incident simulé et analysé | Toutes | OUTLINE |

---

## Cycle 1 — Java moderne, JVM et performance (3 semaines restantes)

**Objectif du cycle** : disposer d'un noyau métier TripQuest fiable en Java 21 pur, correct sous concurrence, et diagnosticable.

**Capacité TripQuest en fin de cycle** : le parcours « créer un voyage → inviter → proposer → voter → clôturer → construire l'itinéraire → détecter les conflits → finaliser → consulter » est jouable en mémoire, sans HTTP ni base, avec ses règles, ses garanties de concurrence et un diagnostic de performance reproductible.

**Pourquoi l'ordre A → C → B**

1. **Bloc A d'abord (Java moderne + Collections)** : rien n'existe. Les états, transitions, identités, égalités et règles temporelles sont le socle de tout le reste. Sans modèle, il n'y a ni concurrence à protéger ni performance à mesurer.
2. **Bloc C ensuite (Concurrence + Virtual Threads)** : le MVP impose explicitement la cohérence des votes simultanés (VOTE-08, AC-5) et l'acceptation unique d'invitation (MEMBER-03). Ces garanties doivent exister dans le noyau avant que le cycle 2 n'expose l'application sur un serveur HTTP multi-threadé. Le harnais de simulation construit ici sert aussi de générateur de charge pour le bloc B.
3. **Bloc B en dernier (JVM internals + GC/profiling)** : profiler suppose un système qui tourne et qui charge quelque chose. Le harnais du bloc C fournit la charge ; le modèle du bloc A fournit les points chauds réels (calcul de score, détection de chevauchement). Diagnostiquer avant d'avoir du code produirait un exercice artificiel.

Cet ordre s'écarte de l'ordre initial de la roadmap (S3–S4 avant S5–S6) pour que chaque semaine produise un changement TripQuest cohérent et vérifiable.

**Dépendances principales** : TQ-C1-01 → TQ-C1-02 → TQ-C1-03, en chaîne.

**Sujets explicitement différés au cycle 2** : composition Spring, exposition HTTP, authentification, persistance. Les notifications in-app et l'UI restent hors cycle 1.

**Risques de conception actuels**
- Sur-modéliser en trois semaines : le périmètre de TQ-C1-01 couvre les six modules du MVP mais uniquement leurs règles, sans exposition ni persistance.
- Choisir en cycle 1 un mécanisme de synchronisation en mémoire qui deviendra caduc quand PostgreSQL sera la source de vérité (cycle 3). Ce risque est accepté et tracé dans TQ-C1-02.
- Mesures de performance dépendantes de la machine ; TQ-C1-03 demande des mesures contextualisées, pas des seuils absolus.

**UNKNOWN**
- DSL Gradle, structure de modules initiale ;
- représentation de la monnaie et du temps (exact, mais forme à décider) ;
- utilisation ou non de JMH / jcstress ;
- modélisation ou non d'événements métier dès le cycle 1.

---

## Cycle 2 — Spring Boot et sécurité en profondeur

**Objectif du cycle** : exposer TripQuest comme une application Spring Boot 3 sécurisée, en comprenant chaque maillon de la chaîne.

**Capacité en fin de cycle** : les parcours MVP 1 à 4 sont réalisables via API HTTP par des utilisateurs authentifiés, chaque voyage est isolé de ceux qui n'en sont pas membres, et les décisions de l'organisateur sont atomiques et tracées.

**Ordre retenu et justification** : S7 → S8 → S9 → S11 → S12 → S10. Spring Core, Boot puis Web construisent l'exposition. L'authentification (S11) suit immédiatement l'API pour ne pas laisser longtemps des endpoints sans identité fiable (ACCOUNT-04). L'autorisation (S12) s'appuie sur l'identité. AOP et transactions (S10) sont placés en fin de cycle parce qu'une démonstration de rollback réclame un support transactionnel réel, ce qui fait la jonction avec le cycle 3.

**Dépendances principales** : chaîne linéaire TQ-C2-01 → … → TQ-C2-06, toutes sur le cycle 1.

**Sujets différés au cycle 3** : modélisation relationnelle complète, JPA, verrouillage en base, migrations.

**Risques de conception**
- TQ-C2-06 introduit probablement un premier support transactionnel (PostgreSQL minimal, `PROPOSED`) avant l'Issue de modélisation TQ-C3-01. Recommandation : périmètre minimal en C2-06 (trace d'audit et décisions de l'organisateur), modélisation complète en C3-01. Décision `UNKNOWN / TO DECIDE` au début du cycle 2.
- Tension entre un domaine immuable (records) et les attentes de Spring/JPA ; la séparation domaine / adaptateurs est à décider en C2-01.
- Le choix « sessions vs stateless JWT » influence CSRF et CORS (C2-04 / C2-05).

**UNKNOWN**
- fournisseur OIDC pour le développement ;
- exécution du serveur sur threads virtuels ;
- outillage de vérification des frontières de modules ;
- forme de l'UI qui consommera l'API (hors roadmap).

---

## Cycle 3 — PostgreSQL, persistance et batch

**Objectif du cycle** : rendre l'état de TripQuest durable, cohérent et performant, puis le faire évoluer sans interruption.

**Capacité en fin de cycle** : un voyage survit au redémarrage ; ses invariants sont garantis par la base ; les votes concurrents restent cohérents même avec plusieurs instances ; les listes clés sont rapides sur un volume réaliste ; le schéma peut évoluer en présence de deux versions.

**Ordre retenu et justification** : S13 → S14 → S16 → S15 → S17 → S18. La correction (S16, concurrence en base) passe avant la performance (S15) : la cohérence des votes est un critère MVP, la vitesse du tableau de bord n'en est pas un. Les migrations avancées (S17) viennent quand un schéma existe et a besoin d'évoluer. Spring Batch (S18) clôt le cycle en enabler à valider.

**Dépendances principales** : TQ-C2-06 → TQ-C3-01 → TQ-C3-02 → {TQ-C3-03, TQ-C3-04} ; TQ-C3-05 sur TQ-C3-01 ; TQ-C3-06 sur TQ-C3-03.

**Sujets différés au cycle 4** : idempotence côté API (clés), messagerie.

**Risques de conception**
- Dupliquer les règles métier entre domaine et schéma ; l'intention est que le schéma garantisse les invariants d'unicité, le domaine les transitions.
- Spring Batch sans besoin réel : candidats à valider (expiration des invitations, calcul des indicateurs produit §11, archivage). Si aucun n'est retenu, remplacement par un ticket technique équivalent sur décision humaine.

**UNKNOWN**
- identifiants (UUID vs séquence), représentation des états en base ;
- volumes cibles pour S15 ;
- changement de schéma servant de support à S17 ;
- besoin réel pour S18.

---

## Cycle 4 — APIs, Kafka et systèmes distribués

**Objectif du cycle** : rendre le contrat d'API de TripQuest robuste, puis n'introduire de communication asynchrone que pour un problème vérifié.

**Capacité en fin de cycle** : contrat OpenAPI stable, erreurs normalisées, créations rejouables sans doublon. Si les enablers sont validés : un flux TripQuest découplé par messagerie, avec reprise sans double effet et cohérence base/messagerie argumentée.

**Ordre retenu** : S19 → S20 → S21 → S22 → S23 → S24, ordre naturel de la roadmap. S19 est la seule Issue `FEATURE` ferme du cycle ; S20 à S24 sont des `TECHNICAL ENABLER` dont le problème produit et le design sont `UNKNOWN / TO VALIDATE`.

**Dépendances principales** : TQ-C4-01 sur TQ-C2-03 et TQ-C3-04 ; TQ-C4-02 sur TQ-C2-04 ; chaîne TQ-C4-03 → 04 → 05 → 06.

**Sujets différés au cycle 5** : observabilité du flux asynchrone, tests avec brokers conteneurisés.

**Risques de conception**
- Introduire Kafka pour un MVP qui n'en a pas besoin. Hypothèse candidate unique : les notifications in-app (§7), dont l'échec ne doit pas annuler une décision métier. Cette hypothèse n'est pas validée. Si elle est rejetée, S21–S24 doivent être traités par un ticket technique équivalent sur décision humaine, sans être supprimés.
- Appels sortants réels limités (S20) : candidat vérifiable, le fournisseur OIDC (découverte, JWKS). L'envoi d'emails est hors MVP.

**UNKNOWN**
- validation du besoin asynchrone ; alternative in-process (événements applicatifs) ;
- appels sortants réellement présents au début du cycle ;
- stratégie de versionnement d'API.

---

## Cycle 5 — Qualité, production, Quarkus et architecture

**Objectif du cycle** : pouvoir livrer, observer et posséder TripQuest en production, puis consolider l'architecture.

**Capacité en fin de cycle** : suite de tests structurée, observabilité bout en bout avec indicateurs produit, pipeline et image reproductibles, déploiement minimal si validé, comparatif Quarkus documenté, architecture revue et éprouvée par un incident simulé.

**Ordre retenu** : S25 → S26 → S27 → S28 → S29 → S30. Les tests consolidés (S25) précèdent le pipeline (S27) qui les exécute. L'observabilité (S26) précède le déploiement (S28) pour que les probes et tableaux de bord aient un contenu. Quarkus (S29) est une évaluation bornée. S30 ferme la roadmap.

**Dépendances principales** : TQ-C5-01 → TQ-C5-03 → {TQ-C5-04, TQ-C5-05} ; TQ-C5-02 sur TQ-C2-03 et TQ-C1-03 ; TQ-C5-06 sur toutes.

**Sujets différés** : aucun ; les hors-MVP (§8 des spécifications) restent hors périmètre.

**Risques de conception**
- Cible de déploiement (S28) non décidée : local (kind/minikube) ou AWS. Coût et accès `UNKNOWN`.
- Quarkus (S29) : risque de dériver vers une migration ; l'Issue borne explicitement à une évaluation comparative.
- S30 : la revue d'architecture ne doit pas déboucher sur une extraction en microservices, explicitement hors MVP.

**UNKNOWN**
- cible cloud et budget ;
- périmètre de la tranche Quarkus ;
- scénario d'incident retenu.
