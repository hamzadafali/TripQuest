# Workflow de la Learning Factory

Chaque ticket suit automatiquement cette state machine :

```text
UNDERSTAND
  ↓
LEARN
  ↓
LEARNING_GATE
  ↓
CHALLENGE
  ↓
DESIGN_GATE
  ↓
BUILD
  ↓
REVIEW
  ↓
FIX_VERIFY
  ↓
REFLECT
  ↓
PR
  ↓
DONE
```

`CURRENT_STAGE` dans le run est l'état autoritatif de reprise. Quand la condition de sortie d'une étape est satisfaite, l'AI met à jour `CURRENT_STAGE` et commence immédiatement l'étape suivante, sauf si celle-ci exige une intervention humaine selon la policy. L'utilisateur n'a jamais à demander la transition.

## 1. UNDERSTAND

- **Objectif** : construire une compréhension factuelle du besoin et du code concerné.
- **Inputs** : ticket, backlog et roadmap, repository actuel, documentation existante.
- **Actions AI** : reformuler le besoin métier ; extraire les Acceptance Criteria ; explorer le code et reconstruire le flow ; rechercher les dépendances et usages ; classer les informations en `CONFIRMED`, `ASSUMPTION` et `UNKNOWN` ; identifier uniquement les notions requises pour la prochaine décision.
- **Actions humaines** : corriger la reformulation ; préciser le contexte indisponible ; valider ou invalider les hypothèses métier.
- **Output** : sections Ticket, Business context, Acceptance Criteria, Code map, `CONFIRMED`, `ASSUMPTIONS`, `UNKNOWNS` et Concepts to learn renseignées.
- **Condition de sortie** : besoin, périmètre, critères et inconnues sont explicites ; aucune hypothèse n'est présentée comme un fait.

## 2. LEARN

- **Objectif** : acquérir le minimum théorique nécessaire à la décision suivante.
- **Inputs** : concepts retenus pendant `UNDERSTAND` et code réel du repository.
- **Actions AI** : pour chaque concept, donner une définition simple, expliquer pourquoi il apparaît ici, fournir au besoin un exemple minimal indépendant du ticket, puis signaler les pièges importants et le lien utile avec Java/Spring/backend/architecture.
- **Actions humaines** : poser des questions, relier le concept au ticket et préparer une reformulation personnelle.
- **Output** : notes ciblées dans Concepts to learn et questions du `Learning Gate`.
- **Condition de sortie** : seules les notions indispensables ont été enseignées et chaque notion peut être appliquée immédiatement au ticket.

## 3. LEARNING_GATE

- **Objectif** : vérifier la compréhension avant tout design.
- **Inputs** : notions enseignées et questions préparées.
- **Actions AI** : poser deux à quatre questions essentielles au maximum, sauf nécessité réelle ; demander une reformulation avec les mots de l'apprenant ; attendre ses réponses ; corriger précisément les lacunes sans donner le design du ticket.
- **Actions humaines** : répondre sans simple récitation et expliquer comment les notions s'appliquent au ticket.
- **Output** : questions, réponses et corrections consignées dans Learning Gate.
- **Condition de sortie** : l'apprenant comprend suffisamment pour prendre la décision suivante, sans devoir réciter un cours. **STOP obligatoire tant que ce n'est pas le cas.** Après une réponse suffisante, l'AI consigne l'évaluation, passe automatiquement `CURRENT_STAGE` à `CHALLENGE` et poursuit sans attendre « continue ».

## 4. CHALLENGE

- **Objectif** : faire émerger puis éprouver un design proposé par l'apprenant.
- **Inputs** : compréhension validée, contraintes du ticket et code existant.
- **Actions AI** : demander « Comment toi tu l'implémenterais ? » ; challenger uniquement les dimensions pertinentes parmi correctness, edge cases, performance, concurrency, transactions, database, resilience, security, observability, testing, maintainability et architecture ; comparer les alternatives sans confisquer la décision.
- **Actions humaines** : proposer une solution concrète, répondre aux objections, ajuster le design et expliciter les trade-offs.
- **Output** : My proposed design et AI challenge renseignés.
- **Condition de sortie** : une proposition cohérente couvre les Acceptance Criteria et ses risques principaux sont connus.

## 5. DESIGN_GATE

- **Objectif** : confirmer que le design appartient intellectuellement à l'apprenant.
- **Inputs** : design challengé, alternatives et risques.
- **Actions AI** : demander l'explication finale ; vérifier la cohérence avec le code et les contraintes ; relever les failles restantes.
- **Actions humaines** : expliquer 1) la solution, 2) pourquoi elle fonctionne, 3) une alternative crédible et 4) le principal trade-off.
- **Output** : décision et validation consignées dans Design Gate.
- **Condition de sortie** : ces quatre éléments sont compris et le design est suffisamment sûr pour avancer, sans rechercher la perfection théorique. **STOP obligatoire avant toute implémentation si la maîtrise est insuffisante.** Après une réponse suffisante, l'AI passe automatiquement `CURRENT_STAGE` à `BUILD` et commence l'implémentation.

## 6. BUILD

- **Objectif** : faire implémenter à l'apprenant les parties pédagogiquement fortes, par incréments compréhensibles et vérifiables.
- **Inputs** : design validé et ordre d'implémentation proposé par l'apprenant.
- **Guided Build Loop** : `THINK → USER CODES → REVIEW → USER FIXES when pedagogically valuable → AI handles mechanical work → NEXT STEP`.
- **Actions AI** : découper en `SMALL VERTICAL STEPS` ; identifier au maximum un ou deux `CODING MOMENTS` par step ; fournir objectif, invariants, fichiers, éventuelle signature et commande de test sans révéler l'implémentation ; **STOPPER** pour laisser coder ; review le diff ; rendre à l'apprenant les corrections pédagogiques ; automatiser imports, boilerplate, répétitions comprises, formatting, navigation, compilation, tests, adaptations similaires, documentation et preuves.
- **Actions humaines** : coder chaque `CODING MOMENT`, expliquer son intention, lire la review et corriger les erreurs à valeur pédagogique avant la suite.
- **Output** : Implementation steps, Coding Moments et Tests mis à jour, avec contribution de l'apprenant identifiable sans transformer le run en journal de chat.
- **Condition de sortie** : tous les Acceptance Criteria sont implémentés ; chaque coding moment est codé puis compris par l'apprenant ; chaque vertical step possède une preuve ; aucun gros diff produit par l'AI n'a absorbé l'apprentissage. L'AI passe ensuite automatiquement à `REVIEW`.

## 7. REVIEW

- **Objectif** : évaluer le résultat au niveau d'exigence Senior/Tech Lead.
- **Inputs** : implémentation complète, tests, diff et Acceptance Criteria.
- **Actions AI** : inspecter correction, edge cases, tests, design, lisibilité, dette inutile et régressions ; vérifier chaque Acceptance Criterion ; classer les constats `BLOCKER`, `MAJOR`, `MINOR` ou `NIT`.
- **Actions humaines** : comprendre chaque constat, contester avec des preuves si nécessaire et décider avec l'AI des corrections.
- **Output** : Review renseignée, avec localisation, impact et recommandation pour chaque constat.
- **Condition de sortie** : revue complète ; aucun critère omis ; constats compris et arbitrés. L'AI passe automatiquement à `FIX_VERIFY`.

## 8. FIX_VERIFY

- **Objectif** : corriger les constats validés et produire les preuves finales.
- **Inputs** : revue arbitrée.
- **Actions AI** : aider aux corrections selon la boucle incrémentale ; relancer les tests pertinents ; inspecter le diff final ; signaler toute validation non exécutable.
- **Actions humaines** : valider les décisions de correction et expliquer les changements significatifs.
- **Output** : Fixes et Tests complétés, résultats des commandes et état du diff final.
- **Condition de sortie** : aucun `BLOCKER` ou `MAJOR` ouvert ; critères satisfaits ; tests pertinents réussis ou limites explicitement acceptées. L'AI passe automatiquement à `REFLECT`.

## 9. REFLECT

- **Objectif** : transformer le travail livré en compétence réutilisable.
- **Inputs** : run complet, décisions, erreurs rencontrées et preuves.
- **Actions AI** : demander en priorité à l'apprenant d'expliquer le code qu'il a réellement écrit, les erreurs rencontrées, ses corrections et ses décisions ; compléter seulement si utile avec des questions d'entretien directement liées au ticket, puis **STOPPER**.
- **Actions humaines** : expliquer ses coding moments à partir du diff, les erreurs rencontrées, les corrections et les décisions transférables.
- **Output** : Learnings et Interview questions complétés.
- **Condition de sortie** : l'apprenant peut expliquer le code, ses décisions et les enseignements transférables. Après validation des réponses, l'AI passe automatiquement à `PR`.

## 10. PR

- **Objectif** : présenter une preuve concise du changement maîtrisé.
- **Inputs** : Acceptance Criteria vérifiés, diff final, tests et réflexion validée.
- **Actions AI** : préparer automatiquement titre, description, changements, tests, décisions techniques et risques éventuels ; ne créer ni publier la PR sans demande explicite.
- **Actions humaines** : relire la preuve, confirmer sa fidélité au changement et autoriser séparément toute publication.
- **Output** : section PR proof prête à être copiée dans une PR.
- **Condition de sortie** : preuve complète, compréhensible par un reviewer et maîtrise validée ; aucune PR avant maîtrise. L'AI passe `CURRENT_STAGE` à `DONE` et présente le bilan final. La création ou publication réelle d'une PR reste une action externe séparée.

## DONE

- **Objectif** : marquer le workflow pédagogique terminé.
- **Actions AI** : vérifier la cohérence finale du run et rapporter les preuves, risques et éventuelles actions externes restantes.
- **Condition de sortie** : aucune transition automatique supplémentaire ; un nouveau travail nécessite un nouveau ticket ou une demande explicite.
