# Dependency Alignment Plan

This document explains how nostr-java aligns dependency versions across modules and how the BOM manages consumer dependencies.

## Current state (2.0.0)

- The aggregator POM imports `nostr-java-bom` to manage third-party versions.
- Temporary overrides pin each reactor module (`nostr-java-core`, `nostr-java-event`, `nostr-java-identity`, `nostr-java-client`) to `${project.version}` so local builds resolve to the in-repo SNAPSHOTs even if the BOM doesn't yet list matching coordinates.
- Relevant configuration lives in `pom.xml` dependencyManagement.

## Module structure

4 modules with a strict dependency chain:

```
nostr-java-core → nostr-java-event → nostr-java-identity → nostr-java-client
```

## BOM alignment

After each release:
1. Publish all module artifacts.
2. Release a BOM revision that references the published module coordinates.
3. Remove temporary module overrides from the aggregator POM so the BOM is the only source of truth.

## Consumer usage

Consumers should import the BOM and omit versions on nostr-java dependencies:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>xyz.tcheeric</groupId>
      <artifactId>nostr-java-bom</artifactId>
      <version>2.0.0+</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
<dependencies>
  <dependency>
    <groupId>xyz.tcheeric</groupId>
    <artifactId>nostr-java-client</artifactId>
  </dependency>
</dependencies>
```

## Verification

Ensure the build resolves to correct coordinates via the BOM:

```bash
mvn -q -DnoDocker=true clean verify
mvn -q dependency:tree | rg "nostr-java-(core|event|identity|client)"
```

## Rollback strategy

If a BOM update lags a module release, temporarily restore individual module overrides under `<dependencyManagement>` to force-align versions in the reactor, then remove again once the BOM is refreshed.
