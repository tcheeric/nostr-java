# Contributing to nostr-java

nostr-java implements the Nostr protocol. For a complete index of current Nostr Implementation Possibilities (NIPs), see [AGENTS.md](AGENTS.md).

## Development Guidelines

- All changes must include unit tests and update relevant documentation.
- Use clear, descriptive names and remove unused imports.
- Prefer readable, maintainable code over clever shortcuts.
- Run `mvn -q verify` from the repository root before committing.
- Submit pull requests against the `develop` branch.

## Commit Guidelines

- All commit messages must follow the requirements in [`commit_instructions.md`](commit_instructions.md).
- PR titles and commit messages must use the `type(scope): description` format and allowed types.
- See the commit instructions file for details and examples.

### Allowed Commit Types

`feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`

### Good Examples

- `feat(auth): add magic-link login`
- `fix(api): handle 429 with exponential backoff`
- `docs(readme): clarify local setup`
- `refactor(search): extract ranking pipeline`

### Issue Linking

- In the PR body, add: `Closes #123` (or `Fixes ABC-456` for Jira). GitHub will auto-close on merge.

## Pull Request Guidelines

- Summaries in pull requests must cite file paths and include testing output.
- Open pull requests using the template at `.github/pull_request_template.md` and complete every section.

By following these conventions, contributors help keep the codebase maintainable and aligned with the Nostr specifications.