# Contributing to nostr-java

nostr-java implements the Nostr protocol. A complete index of current Nostr Implementation Possibilities (NIPs) is listed in [AGENTS.md](AGENTS.md).

## Development Guidelines

- Run `mvn -q verify` from the repository root before committing.
- Use `rg` for code searches instead of `ls -R` or `grep -R`.
- PR titles and commit messages must follow the `type: description` format.
  - Allowed types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `ci`, `build`, `perf`, `style`.
  - The description must be a concise verb + object phrase (e.g., `refactor: Refactor auth middleware to async`).
- Summaries in pull requests must cite file paths and include testing output.
- Open pull requests using the template at `.github/pull_request_template.md` and complete every section.

By following these conventions, contributors help keep the codebase maintainable and aligned with the Nostr specifications.

