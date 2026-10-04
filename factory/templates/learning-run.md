# Learning Run — <TICKET-ID>

CURRENT_STAGE: UNDERSTAND

- **Started**: <YYYY-MM-DD>
- **Last updated**: <YYYY-MM-DD>
- **Ticket source**: <path-or-url>

> Valeurs autorisées : `UNDERSTAND`, `LEARN`, `LEARNING_GATE`, `CHALLENGE`, `DESIGN_GATE`, `BUILD`, `REVIEW`, `FIX_VERIFY`, `REFLECT`, `PR`, `DONE`.

## Ticket

<Identifier, title, type, dependencies and scope.>

## Business context

<Reformulation of the business need and expected value.>

## Acceptance Criteria

- [ ] <AC-1>

## Code map

| Area / flow | Files / symbols | Role | Evidence |
| --- | --- | --- | --- |
| <area> | <paths> | <responsibility> | <how confirmed> |

## CONFIRMED

- <Fact verified from code, ticket or reproducible result; include the source.>

## ASSUMPTIONS

- <Assumption that still requires validation.>

## UNKNOWNS

- <Open question, owner and how it will be resolved.>

## Conflicts

### CONFLICT: <short name>

- **Source A**: <claim and source>
- **Source B**: <conflicting claim and source>
- **Impact**: <consequence for this ticket>
- **Decision required**: <owner and decision>
- **Blocking next step**: <YES / NO>

## Concepts to learn

### <Concept>

- **C'est quoi ?** <simple definition>
- **Pourquoi ici ?** <direct link to this ticket>
- **Exemple minimal**: <small example independent from the ticket>
- **Pièges importants**: <only relevant pitfalls>
- **Application immédiate**: <next decision enabled by this concept>

## Learning Gate

**Status**: NOT PASSED

### Questions

<Two to four essential questions maximum unless more are genuinely required.>

1. <Question testing readiness for the next decision, in the learner's own words.>

### Mes réponses

1. <Learner response; never filled in by the AI.>

### Corrections

1. <Concise correction and remaining gap.>

### Exit decision

- [ ] Je peux expliquer les notions essentielles avec mes propres mots.
- **Decision**: <STOP / PASSED>
- **Evidence**: <why the gate is or is not passed>

## My proposed design

<Learner's proposal; never replaced by an unsolicited final architecture.>

### Alternatives and trade-offs

- <Alternative, benefits, costs and reason for decision.>

## AI challenge

| Dimension | Challenge / evidence | Learner decision |
| --- | --- | --- |
| Correctness | <relevant challenge> | <response or change> |
| Edge cases | <relevant challenge> | <response or change> |
| Testing | <relevant challenge> | <response or change> |

<Add only the relevant dimensions: performance, concurrency, transactions, database, resilience, security, observability, maintainability, architecture.>

## Design Gate

**Status**: NOT PASSED

- [ ] J'explique ma solution.
- [ ] J'explique pourquoi elle fonctionne et satisfait les Acceptance Criteria.
- [ ] J'explique une alternative crédible.
- [ ] J'explique le principal trade-off.
- **Decision**: <STOP / PASSED>
- **Evidence**: <learner explanation and validation notes>

## Implementation steps

Chaque ligne représente un `SMALL VERTICAL STEP` : une unité cohérente de comportement ou d'apprentissage, jamais une ligne de code ni un gros lot de fichiers.

| # | Decision / intent | Coherent vertical change | Test | Feedback / understanding | Status |
| --- | --- | --- | --- | --- | --- |
| 1 | <why> | <bounded change> | <targeted check> | <learner explanation> | TODO |

## Coding Moments

### Vertical step <N> — Coding Moment <1 or 2>

- **Learning value**: <why this deserves learner implementation>
- **Small objective**: <bounded behavior to implement>
- **Constraints / invariants**: <rules that must remain true>
- **Files concerned**: <paths>
- **Expected signature, if useful**: `<signature without implementation>`
- **Useful test command**: `<command>`
- **Status**: AWAITING USER CODE
- **Learner implementation**: <compact summary, not chat transcript>
- **AI review**: <brief findings>
- **Learner fixes**: <pedagogically valuable corrections made by learner>
- **Mechanical work handled by AI**: <imports, boilerplate, formatting, tests, similar adaptations>

## Tests

| Scope | Command / method | Expected | Actual | Status |
| --- | --- | --- | --- | --- |
| <scope> | `<command>` | <result> | <result or not run reason> | NOT RUN |

## Review

### BLOCKER

- None.

### MAJOR

- None.

### MINOR

- None.

### NIT

- None.

## Fixes

| Review finding | Decision | Fix | Verification |
| --- | --- | --- | --- |
| <finding> | <fix / accepted with reason> | <change> | <evidence> |

## Learnings

- **Ce que j'ai appris**: <learner synthesis>
- **Décisions prises**: <decision and rationale>
- **Erreurs ou mauvaises intuitions initiales**: <what changed and why>
- **Ce que je peux maintenant expliquer**: <transferable understanding>
- **Code que j'ai réellement écrit**: <coding moments and why>
- **Erreurs rencontrées et corrigées**: <important mistakes, learner fixes and lesson>

> Garder uniquement `DECISIONS`, `LEARNINGS`, `EVIDENCE` et `IMPORTANT MISTAKES`. Ne pas recopier la conversation ni conserver les répétitions et explications longues déjà assimilées.

## Interview questions

1. <Question directly linked to the ticket>
   - **Ma réponse**: <learner answer>

## PR proof

### Title

<PR title>

### Description

<Business need and outcome.>

### Changes

- <Change>

### Tests

- <Reproducible evidence>

### Technical decisions

- <Decision and trade-off>

### Risks

- <Known risk, limitation or None>

### Mastery check

- [ ] Tous les Acceptance Criteria sont couverts.
- [ ] Aucun BLOCKER ou MAJOR n'est ouvert.
- [ ] Je peux expliquer le code, les décisions et les trade-offs.
- [ ] Les preuves de test sont reproductibles.
- [ ] La PR proof est relue et validée.

## Completion

- **Final stage**: <PR / DONE>
- **Completed**: <YYYY-MM-DD or NOT YET>
- **Remaining external actions**: <for example create PR, or None>
