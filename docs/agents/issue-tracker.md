# Issue tracker: Local Markdown

Issues and specs for this repo live as Markdown files in `.scratch/`.

## Conventions

- One feature per directory: `.scratch/<feature-slug>/`.
- The spec is `.scratch/<feature-slug>/spec.md`.
- Implementation issues are separate files at `.scratch/<feature-slug>/issues/<NN>-<slug>.md`, numbered from `01`.
- Record triage state in a `Status:` line near the top of each issue, using the strings in `triage-labels.md`.
- Append comments under `## Comments`.

## Publishing and fetching

When a skill says “publish to the issue tracker,” create a file under `.scratch/<feature-slug>/`, creating directories as needed. When it says “fetch the relevant ticket,” read the referenced issue file.

## Wayfinding operations

For `/wayfinder`, the map is `.scratch/<effort>/map.md`; each child ticket is `.scratch/<effort>/issues/NN-<slug>.md`. Record `Type:` (`research`, `prototype`, `grilling`, or `task`), `Status:` (`claimed` or `resolved`), and optional `Blocked by: NN, NN` near the top. Choose the first numbered open, unblocked, unclaimed ticket. Set `Status: claimed` before work. To resolve, add an `## Answer`, set `Status: resolved`, and append a summary and link to the map’s Decisions-so-far.
