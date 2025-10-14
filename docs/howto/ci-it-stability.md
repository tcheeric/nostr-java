# CI and Integration Test Stability

This how‑to explains how we keep CI green across environments and how to run integration tests (ITs) locally with Docker or fall back to unit tests only.

## Goals
- Fast feedback on pull requests (no Docker dependency)
- Deterministic end‑to‑end coverage on main via Docker/Testcontainers
- Clear triage when relay behavior differs (EVENT vs EOSE/NOTICE ordering)

## CI Layout
- Matrix build on Java 21 and 17
  - JDK 21: full build without Docker (`-DnoDocker=true`)
  - JDK 17: POM validation only (project targets 21)
- Separate IT job on pushes uses Docker/Testcontainers to run end‑to‑end tests

See `.github/workflows/ci.yml` for the configuration and artifact uploads (Surefire/Failsafe/JaCoCo).

## Running locally
- Full build with ITs (requires Docker):
  ```bash
  mvn clean verify
  ```
- Unit tests only (no Docker):
  ```bash
  mvn -DnoDocker=true clean verify
  ```
- Using helper script:
  ```bash
  scripts/release.sh verify            # with Docker
  scripts/release.sh verify --no-docker
  scripts/release.sh verify --no-docker --skip-tests  # quick sanity
  ```

## Triage guidance
- If a REQ roundtrip returns EOSE/NOTICE before EVENT, adjust the test to select the first EVENT response rather than assuming order (see `ApiNIP99RequestIT`).
- For calendar (NIP‑52) tests, do not override `created_at` to fixed values, since this causes duplicate IDs and `OK false` responses.
- If relays diverge on semantics, prefer deterministic assertions on the minimal required fields and tags.

## Stability checklist
- CI green on PR (no Docker profile)
- Integration job green on main (Docker)
- Artifacts uploaded for failed runs to ease debugging
- Document changes in `CHANGELOG.md` and migrate brittle tests to deterministic patterns

