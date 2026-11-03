# Implementation Plan: [FEATURE]

**Date**: [DATE] | **Specification**: [.scratch/<feature-slug>/spec.md]

## Summary and Scope

- Desired outcome and people affected: [derived from the specification]
- In scope / out of scope: [boundaries]
- Assumptions and decisions needing clarification: [cite the evidence or question]

## Technical Context

- Language, dependencies, storage and profiles: [verify current configuration]
- Affected frontend, backend, data and external interfaces: [paths or N/A]
- Constraints, accessibility, security and performance: [measurable where applicable]

## Constitution Check

Evaluate `.specify/memory/constitution.md` before design and again afterward.
Record each applicable principle, the supporting evidence, and any justified
exception; do not claim an unverified constraint is satisfied.

## Current Behavior and Impact

- Existing flow and entry points: [use CodeGraph when the project is indexed]
- Data, roles, state transitions and affected callers: [only what this change touches]
- Compatibility, migration and rollback considerations: [when applicable]

## Alternatives and Decision

Compare reasonable options when a decision affects scope, compatibility, security,
or complexity. Record the chosen option, evidence, tradeoffs and rejected alternatives.

## Design and Contracts

Describe the smallest suitable design. Link to `research.md`, `data-model.md`,
`contracts/` and `quickstart.md` in `.scratch/<feature-slug>/` when relevant;
identify validation, authorization, error handling and concurrency implications.

## Project Structure

Identify the existing source, test and feature-document directories affected by
the plan, and explain any proposed additions to that structure.

## Acceptance and Verification

| Requirement or scenario | Observable result | Verification | Evidence or limitation |
|---|---|---|---|
| [spec reference] | [expected result] | [test or manual check] | [to be recorded] |

Include critical failure paths and role boundaries. Distinguish H2 checks from
PostgreSQL-specific behavior; state any environment that could not be verified.

## Implementation Order

List dependency-ordered, reviewable slices and their verification checkpoints.
Identify the files or modules expected to change without treating that list as fixed.

## Risks and Open Questions

For each material risk, record likelihood or trigger, impact, mitigation, and any
decision that cannot be resolved from available evidence.

## Complexity Tracking

| Additional complexity | Why it is needed | Simpler option considered |
|---|---|---|
| [if any] | [reason] | [tradeoff] |
