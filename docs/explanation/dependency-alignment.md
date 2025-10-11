# Dependency Alignment Plan

This document explains how nostr-java aligns dependency versions across modules and how we will simplify the setup for the 1.0.0 release.

Purpose: ensure consistent, reproducible builds across all modules (api, client, event, etc.) and for consumers, with clear steps to remove temporary overrides once the BOM includes 1.0.0.

Current state (pre-1.0)
- The aggregator POM imports `nostr-java-bom` to manage third-party versions.
- Temporary overrides pin each reactor module (`nostr-java-*-`) to `${project.version}` so local builds resolve to the in-repo SNAPSHOTs even if the BOM doesn’t yet list matching coordinates.
- Relevant configuration lives in `pom.xml` dependencyManagement.

Goals for 1.0
- Publish 1.0.0 of all modules.
- Bump the imported BOM to the first release that maps to the 1.0.0 module coordinates.
- Remove temporary module overrides so the BOM is the only source of truth.

Plan and steps
1) Before 1.0.0
   - Keep the module overrides in `dependencyManagement` to guarantee the reactor uses `${project.version}`.
   - Keep `nostr-java-bom.version` pointing at the latest stable BOM compatible with current development.

2) Cut 1.0.0
   - Update `<version>` in the root `pom.xml` to `1.0.0`.
   - Build and publish all modules to your repository/Maven Central.
   - Release a BOM revision that references the `1.0.0` artifacts (for example `nostr-java-bom 1.x` aligned to `1.0.0`).

3) After BOM with 1.0.0 is available
   - In the root `pom.xml`:
     - Bump `<nostr-java-bom.version>` to the new BOM that includes `1.0.0`.
     - Remove the module overrides from `<dependencyManagement>` for:
       `nostr-java-util`, `nostr-java-crypto`, `nostr-java-base`, `nostr-java-event`, `nostr-java-id`, `nostr-java-encryption`, `nostr-java-client`, `nostr-java-api`, `nostr-java-examples`.
     - Remove any unused properties (e.g., `nostr-java.version` if not referenced).

Verification
- Ensure the build resolves to 1.0.0 coordinates via the BOM:
  - `mvn -q -DnoDocker=true clean verify`
  - `mvn -q dependency:tree | rg "nostr-java-(api|client|event|base|crypto|util|id|encryption|examples)"`
- Consumers should import the BOM and omit versions on nostr-java dependencies:
  ```xml
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>xyz.tcheeric</groupId>
        <artifactId>nostr-java-bom</artifactId>
        <version>1.0.0+</version>
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

Rollback strategy
- If a BOM update lags a module release, temporarily restore individual module overrides under `<dependencyManagement>` to force-align versions in the reactor, then remove again once the BOM is refreshed.

Outcome
- A single source of truth (the BOM) for dependency versions.
- No per-module overrides in the aggregator once 1.0.0 is published and the BOM is updated.

