# Contributing to nostr-java

Thank you for contributing to nostr-java! This project implements the Nostr protocol. For a complete index of current Nostr Implementation Possibilities (NIPs), see [AGENTS.md](AGENTS.md).

## Table of Contents

- [Getting Started](#getting-started)
- [Development Guidelines](#development-guidelines)
- [Coding Standards](#coding-standards)
- [Architecture Guidelines](#architecture-guidelines)
- [Adding New NIPs](#adding-new-nips)
- [Testing Requirements](#testing-requirements)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Guidelines](#pull-request-guidelines)

## Getting Started

### Prerequisites

- **Java 21+** - Required for building and running the project
- **Maven 3.8+** - For dependency management and building
- **Git** - For version control

### Setup

1. Fork the repository on GitHub
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/nostr-java.git`
3. Add upstream remote: `git remote add upstream https://github.com/tcheeric/nostr-java.git`
4. Build: `mvn clean install`
5. Run tests: `mvn test`

## Development Guidelines

- All changes must include unit tests and update relevant documentation.
- Use clear, descriptive names and remove unused imports.
- Prefer readable, maintainable code over clever shortcuts.
- Run `mvn -q verify` from the repository root before committing.
- Submit pull requests against the `main` branch.

### Before Submitting

✅ All tests pass: `mvn test`
✅ Code compiles: `mvn clean install`
✅ JavaDoc complete for public APIs
✅ Branch up-to-date with latest `main`

## Coding Standards

This project follows **Clean Code** principles. Key guidelines:

- **Single Responsibility Principle** - Each class should have one reason to change
- **DRY (Don't Repeat Yourself)** - Avoid code duplication
- **Meaningful Names** - Use descriptive, intention-revealing names
- **Small Functions** - Functions should do one thing well

### Naming Conventions

**Classes:**
- Entities: Noun names (e.g., `GenericEvent`, `UserProfile`)
- Builders: End with `Builder` (e.g., `NIP01EventBuilder`)
- Factories: End with `Factory` (e.g., `NIP01TagFactory`)
- Validators: End with `Validator` (e.g., `EventValidator`)
- Serializers: End with `Serializer` (e.g., `EventSerializer`)
- NIP implementations: Use `NIPxx` format (e.g., `NIP01`, `NIP57`)

**Methods:**
- Getters: `getKind()`, `getPubKey()`
- Setters: `setContent()`, `setTags()`
- Booleans: `isEphemeral()`, `hasTag()`
- Factory methods: `createEventTag()`, `buildTextNote()`

**Variables:**
- Use camelCase (e.g., `eventId`, `publicKey`)
- Constants: UPPER_SNAKE_CASE (e.g., `REPLACEABLE_KIND_MIN`)

### Code Formatting

- **Indentation:** 2 spaces (no tabs)
- **Line length:** Max 100 characters (soft limit)
- **Use Lombok:** `@Data`, `@Builder`, `@NonNull`, `@Slf4j`
- **Remove unused imports**

## Architecture Guidelines

This project follows **Clean Architecture**. See [docs/explanation/architecture.md](docs/explanation/architecture.md) for details.

### Module Organization

```
nostr-java/
├── nostr-java-base/       # Domain entities
├── nostr-java-crypto/     # Cryptography
├── nostr-java-event/      # Event implementations
├── nostr-java-api/        # NIP facades
├── nostr-java-client/     # Relay clients
```

### Design Patterns

- **Facade:** NIP implementation classes (e.g., NIP01, NIP57)
- **Builder:** Complex object construction
- **Factory:** Creating instances (tags, messages)
- **Template Method:** Validation with overrideable steps
- **Utility:** Stateless helper classes

## Adding New NIPs

### Quick Guide

1. **Read the NIP spec** at https://github.com/nostr-protocol/nips
2. **Create event class** (if needed) in `nostr-java-event`
3. **Create facade** in `nostr-java-api`
4. **Write tests** (minimum 80% coverage)
5. **Add JavaDoc** with usage examples
6. **Update README** NIP compliance matrix

### Example Structure

```java
/**
 * Facade for NIP-XX (Feature Name).
 *
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * NIPxx nip = new NIPxx(identity);
 * nip.createEvent("content")
 *    .sign()
 *    .send(relayUri);
 * }</pre>
 *
 * @see <a href="https://github.com/nostr-protocol/nips/blob/master/XX.md">NIP-XX</a>
 * @since 0.x.0
 */
public class NIPxx extends EventNostr {
  // Implementation
}
```

See [docs/explanation/architecture.md](docs/explanation/architecture.md) for detailed step-by-step guide.

## Testing Requirements

- **Minimum coverage:** 80% for new code
- **Test all edge cases:** null values, empty strings, invalid inputs
- **Use descriptive test names** or `@DisplayName`

### Test Example

```java
@Test
@DisplayName("Validator should reject negative kind values")
void testValidateKindRejectsNegative() {
  Integer invalidKind = -1;

  AssertionError error = assertThrows(
      AssertionError.class,
      () -> EventValidator.validateKind(invalidKind)
  );
  assertTrue(error.getMessage().contains("non-negative"));
}
```

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