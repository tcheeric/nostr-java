# Git Commit Instructions (commitlint-compatible)

You are writing Git commit messages. Follow these rules strictly:

## FORMAT

    <type>(<scope>): <subject>
    <blank line>
    <body>
    <blank line>
    <footer>

## REQUIRED & ALLOWED

-   type: one of \[build, ci, chore, docs, feat, fix, perf, refactor,
    revert, style, test\]
-   scope: lower-case single token (optional, but if present must be
    lower-case)
-   subject: required, lower-case, no trailing period
-   header (everything before first newline) MUST be ≤ 100 characters
-   If a body exists, there MUST be ONE blank line before it
-   Body lines MUST be wrapped to ≤ 100 chars
-   If a footer exists, there MUST be ONE blank line before it
-   Footer lines MUST be wrapped to ≤ 100 chars

## CASE RULES

-   type: lower-case
-   scope: lower-case
-   subject: MUST NOT be sentence-case, Start-Case, PascalCase, or
    UPPER-CASE (use concise lower-case phrasing)

## PROHIBITED

-   Empty type or empty subject
-   Subject ending with a period "."
-   Headers \> 100 chars
-   Mixed or capitalized subject case styles listed above

## FOOTERS

-   Use for references and metadata (e.g., "Refs #123", "Closes #45").
-   For breaking changes, include a footer block starting with:
    `BREAKING CHANGE: <summary of the breaking change>`

## TEMPLATES

### No scope

    <type>: <short, lower-case subject with no trailing period>

### With scope

    <type>(<scope>): <short, lower-case subject with no trailing period>

### With body and footer

    <type>(<scope>): <subject>

    - Explain what changed and why, wrapped at ≤ 100 chars per line.
    - Note user impact or implementation details if helpful.

    Refs #<issue>; Co-authored-by: <name> <email>

## GOOD EXAMPLES

    feat: add endpoint for user session refresh

    fix(auth): handle expired tokens during refresh flow

    refactor(core): simplify config loader and drop unused flags

    docs(readme): clarify setup steps and add troubleshooting section

    perf(api): cache user profiles to reduce db queries

    revert: revert "feat(auth): add magic link login"

    This reverts commit abcdef1234567890 because it caused auth regressions.

## BAD EXAMPLES (do not output)

    Feat: Add Feature.             # wrong case + trailing period
    fix(Auth): Handle Error        # scope capitalized, subject TitleCase
    docs:                          # missing subject
    style(ui): remove ;.           # trailing period in subject

------------------------------------------------------------------------

# BREAKING CHANGES (MANDATORY FORMAT)

## WHEN to mark breaking

-   Any change that requires user action or migration (API
    removal/rename, behavior change, config changes).

## HOW to signal

1.  Put `!` in the header right after type or scope.
2.  Add a footer block starting with `BREAKING CHANGE:` (exact phrase),
    with details and migration steps.

### HEADER examples (choose one)

    feat!: drop support for node 14
    refactor(auth)!: remove legacy token flow

### BODY rules

-   Precede body with exactly one blank line.
-   Wrap each line to ≤ 100 chars.
-   Explain what changed and why (concise).

### FOOTER rules

-   Precede footer with exactly one blank line.
-   Start with: `BREAKING CHANGE: <short summary>`
-   Follow with impact and migration instructions, wrapped to ≤ 100
    chars per line.

### TEMPLATE (full commit with body + footer)

    <type>(<scope>)!: <short, lower-case subject without trailing period>

    - What changed and why (≤ 100 chars per line).
    - Add context, links, and rationale if helpful.

    BREAKING CHANGE: <1–2 line summary of the breaking behavior change>
    Impact: <who is affected and how>
    Migration: <exact steps or examples to upgrade>
    Refs: <issue/PR links>

## GOOD examples

    feat!: remove deprecated v1 endpoints

    - All v1 routes are removed in favor of v2 equivalents for consistency and security.

    BREAKING CHANGE: v1 API routes are no longer available.
    Impact: clients calling /v1/* will receive 404 responses.
    Migration: switch to /v2/* endpoints. See docs/api-migration.md for path mappings.

    refactor(config)!: rename "whitelist" to "allowlist"

    - Align terminology with inclusive language guidelines.

    BREAKING CHANGE: "whitelist" config key is removed.
    Impact: configs using "whitelist" will be ignored.
    Migration: rename key to "allowlist" in your config files.

------------------------------------------------------------------------

# CHECKLIST (before finalizing)

-   [ ] type ∈ {build, ci, chore, docs, feat, fix, perf, refactor,
    revert, style, test}
-   [ ] type, scope, subject are lower-case
-   [ ] subject present, no trailing "."
-   [ ] header ≤ 100 chars
-   [ ] body/footers (if present) preceded by a blank line
-   [ ] body and footer lines ≤ 100 chars
-   [ ] "!" in header if breaking change
-   [ ] Footer with "BREAKING CHANGE:" if breaking change
-   [ ] Clear migration steps provided
