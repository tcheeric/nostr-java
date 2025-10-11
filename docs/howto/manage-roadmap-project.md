# Maintain the 1.0 roadmap project

This how-to guide explains how to create and refresh the GitHub Projects board that tracks every task blocking the nostr-java 1.0 release. Use it when spinning up a fresh board or when the backlog has drifted from `docs/explanation/roadmap-1.0.md`.

## Prerequisites

- GitHub CLI (`gh`) 2.32 or newer with the “projects” feature enabled.
- Authenticated session with permissions to create Projects for the repository owner.
- Local clone of `nostr-java` so the script can infer the repository owner.
- `jq` installed (used by the helper script for JSON parsing).

## Steps

1. Authenticate the GitHub CLI if you have not already:
   ```bash
   gh auth login
   ```
2. Enable the projects feature flag if it is not yet active:
   ```bash
   gh config set prompt disabled
   gh config set projects_enabled true
   ```
3. From the repository root, run the helper script to create or update the board:
   ```bash
   ./scripts/create-roadmap-project.sh
   ```
4. Review the board in the GitHub UI. If duplicate draft items appear (for example because the script was re-run), consolidate them manually.
5. When tasks are completed, update both the project item and the canonical checklist in [`docs/explanation/roadmap-1.0.md`](../explanation/roadmap-1.0.md).

## Troubleshooting

- **`gh` reports that the command is unknown** — Upgrade to GitHub CLI 2.32 or later so that `gh project` commands are available.
- **Project already exists but tasks did not change** — The script always adds draft items; to avoid duplicates, delete or convert the older drafts first.
- **Permission denied errors** — Ensure your personal access token has the `project` scope and that you are an owner or maintainer of the repository.
