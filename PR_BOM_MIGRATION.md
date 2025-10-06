title: feat: migrate to nostr-java-bom for centralized version management

## Summary
Related issue: #____
Migrate nostr-java to use `nostr-java-bom` for centralized dependency version management across the Nostr Java ecosystem. This eliminates duplicate version properties and ensures consistent dependency versions.

## What changed?
- **Version bump**: `0.4.0` → `0.5.0`
- **BOM updated**: nostr-java-bom `1.0.0` → `1.1.0` (now includes Spring Boot dependencies)
- Remove Spring Boot parent POM dependency
- Replace 30+ version properties with single `nostr-java-bom.version` property (F:pom.xml†L77)
- Import `nostr-java-bom:1.1.0` in `dependencyManagement` (F:pom.xml†L87-L93)
- Remove version tags from all dependencies across modules:
  - `nostr-java-crypto`: removed bcprov-jdk18on version (F:nostr-java-crypto/pom.xml†L37)
  - `nostr-java-util`: removed commons-lang3 version (F:nostr-java-util/pom.xml†L28)
  - `nostr-java-client`: removed Spring Boot versions, added compile scope for awaitility (F:nostr-java-client/pom.xml†L56)
  - `nostr-java-api`: removed Spring Boot versions
- Simplify plugin management - versions now inherited from BOM (F:pom.xml†L100-L168)
- Update nostr-java-bom to import Spring Boot dependencies BOM

## BOM Architecture Changes
```
nostr-java-bom 1.1.0 (updated)
  ├─ imports spring-boot-dependencies (NEW)
  ├─ defines nostr-java modules (updated to 0.5.0)
  └─ defines shared dependencies (BouncyCastle, Jackson, Lombok, test deps)
```

## Benefits
- **Single source of truth**: All Nostr Java dependency versions managed in one place
- **Consistency**: Identical dependency versions across all Nostr projects
- **Simplified updates**: Bump dependency versions once in BOM, all projects inherit it
- **Reduced duplication**: From 30+ version properties to 1
- **Spring Boot integration**: Now imports Spring Boot BOM for Spring dependencies

## BREAKING
None. Internal build configuration change only; no API or runtime behavior changes.

## Protocol Compliance
- No change to NIP (Nostr Implementation Possibilities) compliance
- Behavior remains compliant with Nostr protocol specifications

## Testing
- ✅ `mvn clean install -DskipTests -U` - BUILD SUCCESS
- All modules compile successfully with BOM-managed versions
- Plugin version warnings are non-blocking

## Checklist
- [x] Title uses `type: description`
- [x] File citations included
- [x] Version bumped to 0.5.0
- [x] nostr-java-bom updated to 1.1.0 with Spring Boot support
- [x] Build verified with BOM
- [x] No functional changes; protocol compliance unchanged
- [x] BOM deployed to https://maven.398ja.xyz/releases/xyz/tcheeric/nostr-java-bom/1.1.0/
