# Version Uplift Workflow (to 1.0.0)

This how-to guide outlines the exact steps to bump nostr-java to a new release (e.g., 1.0.0), publish artifacts, and align the BOM while keeping the repository and consumers in sync.

## Prerequisites

- GPG key configured for signing and available to Maven (see maven-gpg-plugin in the root POM)
- Sonatype Central credentials configured (see central-publishing-maven-plugin in the root POM)
- Docker available for integration tests, or use the `no-docker` profile
- Clean working tree on the default branch

## Step 1 — Finalize code and docs

- Ensure all roadmap blockers are done: see Explanation → Roadmap: ../explanation/roadmap-1.0.md
- Update MIGRATION.md with the final removal list and dates: ../../MIGRATION.md#deprecated-apis-removed
- Make sure docs build links are valid (no broken relative links)

## Step 2 — Bump project version

In the root `pom.xml`:
- Set `<version>` to `1.0.0`
- Keep BOM import as-is for now (see alignment plan below)

```xml
<!-- pom.xml -->
<version>1.0.0</version>
```

Commit: chore(release): bump project version to 1.0.0

Automation:
```bash
scripts/release.sh bump --version 1.0.0
```

## Step 3 — Verify build and tests

- With Docker available (recommended):
  ```bash
  mvn -q clean verify
  ```
- Without Docker (skips Testcontainers-backed ITs):
  ```bash
  mvn -q -DnoDocker=true clean verify
  ```

If any module fails, address it before proceeding.

Automation:
```bash
scripts/release.sh verify               # with Docker
scripts/release.sh verify --no-docker   # without Docker
```

## Step 4 — Tag the release

- Create and push an annotated tag:
  ```bash
  git tag -a v1.0.0 -m "nostr-java 1.0.0"
  git push origin v1.0.0
  ```

Automation:
```bash
scripts/release.sh tag --version 1.0.0 --push
```

## Step 5 — Publish artifacts

- Publish to Central using the configured plugin (root POM):
  ```bash
  mvn -q -DskipTests -DnoDocker=true -P release deploy
  ```
  Notes:
  - The root POM already configures `central-publishing-maven-plugin` to wait until artifacts are published
  - Ensure `gpg.keyname` and credentials are set in your environment/settings.xml

Automation:
```bash
scripts/release.sh publish --no-docker
```

## Step 6 — Update and publish the BOM

- Release a new `nostr-java-bom` that maps all `nostr-java-*` artifacts to `1.0.0`
- Once the BOM is published, update the root `pom.xml` to use the new BOM version
- Remove the temporary module overrides from `<dependencyManagement>` so the BOM becomes the single source of truth
  - See Explanation → Dependency Alignment: ../explanation/dependency-alignment.md

Commit: chore(bom): align BOM to nostr-java 1.0.0 and remove overrides

## Step 7 — Create GitHub Release

- Draft a release for tag `v1.0.0` including:
  - Summary of changes (breaking: deprecated APIs removed)
  - Link to MIGRATION.md and key docs
  - Notable test and integration stability improvements

## Step 8 — Post-release hygiene

- Bump the project version to the next `-SNAPSHOT` on main (e.g., `1.0.1-SNAPSHOT`):
  ```bash
  mvn -q versions:set -DnewVersion=1.0.1-SNAPSHOT
  mvn -q versions:commit
  git commit -am "chore(release): start 1.0.1-SNAPSHOT"
  git push
  ```

Automation:
```bash
scripts/release.sh next-snapshot --version 1.0.1-SNAPSHOT
```
- Verify consumers can depend on the new release via BOM:
  ```xml
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>xyz.tcheeric</groupId>
        <artifactId>nostr-java-bom</artifactId>
        <version>1.0.0</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>xyz.tcheeric</groupId>
      <artifactId>nostr-java-api</artifactId>
    </dependency>
  </dependencies>
  ```

Tips
- Use `-DnoDocker=true` only when you cannot run ITs; prefer full verify before releasing
- Keep commit messages conventional (e.g., chore, docs, fix, feat) to generate clean changelogs later
- If Central publishing fails, rerun with `-X` and consult plugin docs; do not create partial releases

## Checklist

- [ ] Roadmap tasks closed and docs updated
- [ ] Root POM version set to 1.0.0
- [ ] Build and tests pass (`mvn verify`)
- [ ] Tag pushed (`v1.0.0`)
- [ ] Artifacts published to Central
- [ ] BOM updated to reference 1.0.0
- [ ] Module overrides removed from dependencyManagement
- [ ] GitHub Release published
- [ ] Main bumped to next `-SNAPSHOT`
