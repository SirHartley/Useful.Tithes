# Changelog guide

How agents write entries in the root `Changelog`. Read it for every implementation task. Version numbers, release steps and how the release tooling reads the file are in [RELEASE.md](RELEASE.md).

## When to write an entry

- Every implementation task adds or amends its entries in the current unreleased section before merge, in the same commit as the change.
- Read-only work and documentation-only changes add nothing. Say why in the PR when a change needs no entry.
- Check whether the feature has been released: look for it under every version heading without `UNRELEASED`.
  - Not released yet: edit its `Added -` entry so it describes the feature as it now stands. No `Fixed -`, `Adjusted -` or `Removed -` line for it.
  - Released: the change gets its own entry.
- Record only completed changes from the actual diff. Never list plans or unfinished work.

## File layout

`Changelog` at the repository root, plain text, no extension. Versions newest first, a blank line between them. This changelog has no `Known issues` or `Notes` header sections; keep its shape and do not add them unless asked.

```text
1.0.b - UNRELEASED
Fixed - <what the player saw that no longer happens>

1.0.a
Added - Initial Release
```

- Version heading: the version alone on its line, optionally followed by ` - UNRELEASED`. No `v` prefix, no date, no Markdown. The release tooling reads these headings, so keep them in this form.
- Entries: one change per line, starting `Added - `, `Adjusted - `, `Fixed - ` or `Removed - `.
- A `Note - ` line directly below an entry explains a limit or something the player must do.
- If no unreleased section exists, add one above the newest version. Ask the user which version comes next if it has not been chosen; do not invent a new version per commit.

## Wording

The user's change note voice, from house-style's "Changelog entries" section, which wins over this guide on wording. Entries are player-facing: what the player sees, never the call chain or class names.

- Order inside a version: `Added`, `Adjusted`, `Fixed`, `Removed`.
- Short: stay under 80 characters; go past 100 only for a new feature that needs its numbers.
- Capital letter after the dash, no full stop on a one-clause line. A second sentence is fine for a new feature; then both end in a full stop.
- In-game names and capitals (`Luddic Path`, `Pather fleets`). Parentheses carry the short extra: `(was 30 days)`, `(vanilla game limitation)`.
- No semicolons, no curly quotes, no hyphen as a dash inside the text.
- Added: name, colon, what it does, or `Added - Setting to adjust tithe duration`.
- Adjusted: numbers as old `->` new with the unit (`Adjusted - Tithe protection 30 days -> 60 days`), otherwise a direction word (Reduced, Increased, Halved, Doubled).
- Fixed: what the player saw, in the user's forms: `Fixed - <thing> no longer <wrong behavior>`, `Fixed - <thing> is now properly <right behavior>`, `Fixed - Crash when <situation>`, or a short noun phrase for small things (`Fixed - Typos`).
- Removed: the thing, then the reason or replacement in parentheses.
- Credits name real people only, never an agent. No first person, jokes, caps, exclamation marks or hedges in agent lines; untested status goes in the PR.
- No agent or model names, links, validation logs or commit hashes.
- Keep the user's own lines verbatim (`Added - Initial Release`); never tidy, shorten or merge them unasked.

## Amending versus adding

- Combine entries when several edits fix the same player-visible problem.
- An unreleased feature removed again before release loses its `Added -` entry; it gets no `Removed -` line.
- Never repeat a change once per file, step or PR.

## Maintenance

Update this guide when the `Changelog` layout or entry format changes. Release mechanics belong in [RELEASE.md](RELEASE.md).
