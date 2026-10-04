# Learning Factory TripQuest

Ce fichier est le point d'entrée opérationnel. La Factory pilote tout le workflow : l'utilisateur déclenche ou reprend un run, puis n'intervient que lorsque sa participation apporte une vraie valeur ou qu'une autorisation est nécessaire.

## Commandes utilisateur

Le workflow normal ne nécessite que quatre commandes :

- `Start factory` suivi du contenu d'un ticket ;
- `Start factory for <ticket>` ;
- `Resume factory` ;
- `Factory status`.

Ces formulations déclenchent automatiquement la lecture et l'application intégrales de :

1. `factory/CONSTITUTION.md` ;
2. `factory/WORKFLOW.md` ;
3. `factory/FACTORY.md` ;
4. `factory/templates/learning-run.md`.

L'utilisateur n'a jamais à demander de passer à `LEARN`, à une gate, au design, à la review ou à l'étape suivante.

## State machine

```text
UNDERSTAND → LEARN → LEARNING_GATE → CHALLENGE → DESIGN_GATE
→ BUILD → REVIEW → FIX_VERIFY → REFLECT → PR → DONE
```

Chaque run commence par une ligne exacte :

```text
CURRENT_STAGE: UNDERSTAND
```

`CURRENT_STAGE` est mis à jour immédiatement après chaque condition de sortie satisfaite. Il permet la reprise sans explication de l'utilisateur. Une transition entraîne automatiquement l'exécution de l'étape suivante jusqu'à une interruption autorisée ou `DONE`.

## Start factory

À réception de `Start factory` ou `Start factory for <ticket>`, l'AI :

1. charge les quatre fichiers de la Factory listés plus haut ;
2. identifie le ticket dans le message, dans `docs/backlog/issues/`, ou depuis la référence fournie ;
3. lit le ticket et uniquement le contexte du repository utile à sa compréhension ;
4. initialise Git si nécessaire selon la policy Git ci-dessous ;
5. cherche `factory/runs/<ticket-id>.md` ;
6. s'il n'existe pas, le crée depuis le template avec `CURRENT_STAGE: UNDERSTAND` ;
7. s'il existe, le traite comme un auto-resume : lit `CURRENT_STAGE`, reconstruit uniquement le contexte nécessaire et reprend à cet état ;
8. exécute automatiquement le workflow jusqu'à la prochaine interruption autorisée ou jusqu'à `DONE` ;
9. met à jour le run de manière compacte après chaque fait, décision, gate, preuve ou transition significative.

Si `Start factory` ne contient ni ticket collé ni référence identifiable, l'AI demande uniquement le ticket : cette absence bloque réellement `UNDERSTAND`.

## Resume factory

`Resume factory` sélectionne le seul run non `DONE`. S'il y en a plusieurs, l'AI demande seulement lequel reprendre. Elle relit les quatre fichiers de la Factory, charge le run, vérifie que les faits techniques nécessaires sont encore valides, puis reprend automatiquement à `CURRENT_STAGE`.

Une réponse de l'utilisateur à une gate, une décision ou une ambiguïté suspendue vaut reprise automatique : aucune commande `Resume factory` ou « continue » n'est nécessaire.

## Factory status

`Factory status` est en lecture seule. L'AI rapporte de façon compacte : ticket, `CURRENT_STAGE`, dernière condition satisfaite, éventuel blocage, prochaine action automatique et prochaine intervention humaine attendue. Cette commande ne fait pas avancer le workflow.

## Human interruption policy

L'AI ne s'arrête que pour :

1. `LEARNING_GATE` ;
2. une décision technique à forte learning value ;
3. `DESIGN_GATE` ;
4. un `CODING MOMENT` sélectionné pendant `BUILD`, puis une correction à forte valeur pédagogique ;
5. une ambiguïté métier réellement bloquante ;
6. une action dangereuse ou irréversible ;
7. un secret ou accès manquant ;
8. un changement important de scope ;
9. `REFLECT` centré sur le code réellement écrit par l'apprenant.

Elle ne s'arrête pas pour imports, boilerplate, formatting, navigation, recherches, compilation, tests, analyse d'erreurs, corrections mécaniques, mise à jour du run ou préparation des preuves. Avant toute pause, elle doit identifier explicitement la catégorie d'interruption qui l'autorise. Sinon, elle continue.

## Git policy

Au premier `Start factory`, si `.git` est absent, l'AI peut et doit automatiquement :

1. inspecter les fichiers présents afin d'identifier les exclusions nécessaires ;
2. créer `.gitignore` s'il est absent, ou le compléter prudemment s'il existe, sans supprimer les règles de l'utilisateur ;
3. exécuter `git init` ;
4. vérifier que les secrets, artefacts de build et fichiers IDE évidents ne sont pas suivis.

Cette initialisation est une autorisation explicite de la Factory et ne nécessite pas d'interruption. L'AI ne configure jamais de remote, ne commit, ne push et ne publie jamais de PR automatiquement.

## Sources of truth

- **BUSINESS TRUTH** : ticket et Acceptance Criteria.
- **TECHNICAL TRUTH** : état réel du repository, du code et résultats reproductibles.
- **DOCUMENTATION** : contexte utile, mais potentiellement historique.
- **TARGET ARCHITECTURE** : intention future, jamais considérée comme déjà implémentée.

Si deux sources divergent, l'AI ne choisit jamais silencieusement. Elle consigne :

```text
CONFLICT:
Source A:
Source B:
Impact:
Decision required:
Blocking next step: YES / NO
```

Un conflit non bloquant reste visible avec une action de résolution et le workflow continue. Un conflit bloquant relève de l'ambiguïté métier ou de la décision technique appropriée et autorise une interruption.

## Run unique et versionné

`factory/runs/<ticket-id>.md` constitue l'engineering learning journal versionné. Il remplace temporairement les quatre artefacts historiques :

- `UNDERSTAND` / investigation → analysis ;
- `LEARN` / concepts → technical brief ;
- `CHALLENGE`, design et implementation steps → plan ;
- `FIX_VERIFY`, `REFLECT` et `PR` → evidence.

Le run conserve `DECISIONS`, `LEARNINGS`, `EVIDENCE` et `IMPORTANT MISTAKES`. Il ne contient ni chat logs, ni répétitions, ni longs cours déjà assimilés. L'AI le met à jour automatiquement ; cette opération n'est jamais un motif d'interruption.

## Guided Build Loop

Après validation de `DESIGN_GATE`, chaque vertical step suit :

```text
THINK → USER CODES → REVIEW → USER FIXES when pedagogically valuable
→ AI handles mechanical work → NEXT STEP
```

L'AI choisit au maximum un ou deux `CODING MOMENTS` à forte valeur par step et attend leur implémentation par l'apprenant. Elle ne donne pas le code complet avant cette tentative. Elle review ensuite le diff et laisse l'apprenant corriger les défauts pédagogiquement utiles. Tout le travail mécanique reste autonome et le workflow reprend automatiquement après chaque contribution.

Après le dernier step, l'AI enchaîne `REVIEW → FIX_VERIFY → REFLECT → PR → DONE`. `REFLECT` vérifie d'abord la maîtrise du code réellement écrit et des erreurs réellement rencontrées, plutôt qu'une récitation théorique.

`PR` prépare uniquement la PR proof. La création ou publication effective d'une PR, tout remote et tout push restent hors automatisation.
