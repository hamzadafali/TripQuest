# Backlog TripQuest — Learning Factory

Ce dossier contient le backlog Jira de construction de TripQuest, dérivé de deux sources :

1. les « Spécifications fonctionnelles — TripQuest MVP » ;
2. la « Roadmap Backend Java Senior — 30 semaines » (5 cycles de 6 sujets).

Il décrit **le besoin**, jamais l'exécution. La Learning Factory produit séparément, pour chaque Issue, `analysis.md`, `technical-brief.md`, `plan.md` et `evidence.md`. Aucun cours, gate, plan d'implémentation, commande de test, preuve ou statut `DONE` ne figure ici.

Date de rédaction : 25 septembre 2026. État vérifié du workspace à cette date : répertoire vide, non versionné, aucun code.

## Structure

```
docs/backlog/
├── README.md              ← ce fichier : conventions et mode d'emploi
├── coverage-matrix.md     ← Livrable 1 : matrice de couverture des 30 sujets + Final Coverage Report
├── roadmap.md             ← Livrable 2 : ordonnancement des 5 cycles, objectifs, risques, UNKNOWN
└── issues/
    ├── TQ-C1-01.md … TQ-C1-03.md   ← Livrable 3 : 3 Issues DETAILED (cycle 1)
    └── TQ-C2-01.md … TQ-C5-06.md   ← Livrable 4 : 24 Issues OUTLINE (cycles 2 à 5)
```

## Comptage

| Élément | Nombre |
| --- | --- |
| Sujets de roadmap | 30 |
| Issues détaillées (cycle 1, deux sujets chacune) | 3 |
| Issues en outline (cycles 2 à 5, un sujet chacune) | 24 |
| Total Issues Jira | 27 |

Les six sujets du cycle 1 sont regroupés deux par deux parce que le cycle a démarré le 7 septembre 2026 et qu'il ne reste que trois semaines. Aucune Issue artificielle n'a été ajoutée pour atteindre 30.

## Vocabulaire d'état

Chaque affirmation sur l'architecture porte l'un de ces marqueurs :

- `CURRENT` : vérifié dans le repository au moment de la rédaction ;
- `PROPOSED` : ce que l'Issue introduira probablement, à confirmer par l'analyse ;
- `TARGET` : direction future, non décidée ;
- `UNKNOWN` : information non déterminée, à résoudre par la Factory à partir du code réel.

Le projet étant vide, aucune classe, table, endpoint, événement ou module n'existe. Tout nom de concept employé dans les Issues (« voyage », « invitation », « proposition », « vote », « itinéraire ») désigne une notion métier, pas un identifiant de code.

## Nature des Issues

`FEATURE` / `TECHNICAL ENABLER` / `RELIABILITY` / `SECURITY` / `DATA` / `OPERATIONS`.

Règle appliquée aux technologies imposées par la roadmap sans besoin produit vérifié (Kafka, Spring Batch, clients HTTP résilients, Outbox/Saga, Kubernetes, Quarkus) : l'Issue est un `TECHNICAL ENABLER` en `OUTLINE`, son problème produit et son design sont marqués `UNKNOWN / TO VALIDATE`. Le sujet reste traçable dans la matrice, mais **il ne s'agit pas d'une décision architecturale approuvée**. La validation ou le remplacement par un ticket technique équivalent relève d'une décision humaine au début du cycle concerné.

## Architecture de départ (PROPOSED)

- Java 21, build Gradle ;
- cycle 1 : noyau métier en Java pur, état en mémoire, sans framework ;
- cycle 2 : Spring Boot 3, monolithe modulaire, API HTTP, sécurité OIDC ;
- cycle 3 : PostgreSQL ;
- cycles 4 et 5 : uniquement ce qui répond à un problème explicitement décrit dans l'Issue.

Rien de ceci n'est figé au-delà du cycle en cours. Les cycles 2 à 5 sont détaillés au début de chaque cycle après inspection du repository réel.

## Sujets transversaux

Sécurité (S12), tests (S25), concurrence (S5), observabilité (S26) et résilience apparaissent en sujets secondaires dans plusieurs Issues. Leur Issue principale reste unique et identifiable dans la matrice.

## Points hors roadmap signalés

- **Interface utilisateur** : les spécifications décrivent dix écrans web responsive. La roadmap est exclusivement backend. La réalisation de l'UI est `UNKNOWN / TO DECIDE` et n'est couverte par aucune Issue de ce backlog. Les Issues d'API (TQ-C2-03, TQ-C4-01) sont rédigées pour qu'une UI simple puisse s'y brancher.
- **Notifications in-app** (§7 des spécifications) : le besoin est réel, le mécanisme n'est pas décidé. Il est traité comme hypothèse candidate dans les Issues Kafka (cycle 4), sans être imposé.
- **Envoi d'emails** : hors MVP, non planifié.

## Correspondance avec le tableau GitHub Projects

Le tableau `hamzadafali/projects/4` est vide et expose les champs Status (Todo / In progress / Done), Priority (P0–P2), Size (XS–XL), Iteration, Start date, Target date. Proposition de correspondance, à valider :

- une Iteration par cycle (ou par semaine pour le cycle 1) ;
- Title = `[TQ-Cx-yy] Titre` ;
- Size et Priority : `UNKNOWN`, à estimer lors de l'analyse de chaque Issue ;
- corps de l'Issue = contenu du fichier `issues/TQ-Cx-yy.md`.

Aucun item n'a été créé sur le tableau. La création attend la revue humaine du backlog.

## Calendrier indicatif

| Cycle | Semaines roadmap | Période (indicative) |
| --- | --- | --- |
| 1 | S1–S6 (S1–S3 écoulées) | 7 sept. – 18 oct. 2026 ; trois semaines restantes : 28 sept., 5 oct., 12 oct. |
| 2 | S7–S12 | 19 oct. – 29 nov. 2026 |
| 3 | S13–S18 | 30 nov. 2026 – 10 janv. 2027 |
| 4 | S19–S24 | 11 janv. – 21 févr. 2027 |
| 5 | S25–S30 | 22 févr. – 4 avr. 2027 |

Les dates des cycles 2 à 5 sont une projection à raison d'une semaine par sujet, sans pause. `UNKNOWN` : congés, ajustements de rythme.
