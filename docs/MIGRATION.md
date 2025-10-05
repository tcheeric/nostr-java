# Migration Guide

Navigation: [Docs index](README.md) · [Getting started](GETTING_STARTED.md) · [Troubleshooting](TROUBLESHOOTING.md)

This guide helps you upgrade your nostr-java applications between versions.

## Table of Contents

- [0.4.0 → 0.5.0](#040--050)
- [General Migration Tips](#general-migration-tips)

---

## 0.4.0 → 0.5.0

**Release Date**: January 2025

### Overview

Version 0.5.0 introduces a major dependency management change: **nostr-java now uses its own BOM (Bill of Materials)** instead of inheriting from Spring Boot's parent POM. This provides better control over dependencies and reduces conflicts with user applications.

### Breaking Changes

#### 1. BOM Migration (Maven)

**Impact**: Medium - Affects all Maven users

**In 0.4.0**, nostr-java used Spring Boot as a parent POM:

```xml
<!-- 0.4.0 - OLD -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.5</version>
</parent>
```

**In 0.5.0**, nostr-java uses its own BOM via dependency management:

```xml
<!-- 0.5.0 - NEW -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>xyz.tcheeric</groupId>
            <artifactId>nostr-java-bom</artifactId>
            <version>1.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**Migration Steps:**

1. **Update the version** in your `pom.xml`:
   ```xml
   <dependency>
       <groupId>xyz.tcheeric</groupId>
       <artifactId>nostr-java-api</artifactId>
       <version>0.5.0</version>
   </dependency>
   ```

2. **If you're using Spring Boot** in your own application, you can continue using Spring Boot as your parent:
   ```xml
   <!-- Your application's pom.xml -->
   <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>3.5.5</version> <!-- or your preferred version -->
   </parent>

   <dependencies>
       <dependency>
           <groupId>xyz.tcheeric</groupId>
           <artifactId>nostr-java-api</artifactId>
           <version>0.5.0</version>
       </dependency>
   </dependencies>
   ```

3. **If you're NOT using Spring Boot**, no additional changes needed - just update the version.

4. **Clean and rebuild**:
   ```bash
   mvn clean install
   ```

#### 2. Dependency Version Management

**Impact**: Low - Only affects users who manually specified dependency versions

**In 0.4.0**, individual dependency versions were managed in the parent POM:

```xml
<!-- 0.4.0 - OLD -->
<properties>
    <bcprov-jdk18on.version>1.81</bcprov-jdk18on.version>
    <commons-lang3.version>3.18.0</commons-lang3.version>
    <!-- ... etc -->
</properties>
```

**In 0.5.0**, all dependency versions are managed by `nostr-java-bom`.

**Migration Steps:**

If you explicitly referenced nostr-java's internal dependency versions, remove those references. The BOM will manage them automatically.

```xml
<!-- 0.4.0 - OLD (remove this) -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>${bcprov-jdk18on.version}</version> <!-- Remove version -->
</dependency>

<!-- 0.5.0 - NEW -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <!-- Version managed by nostr-java-bom -->
</dependency>
```

### API Changes

#### No Breaking API Changes

The public API remains **100% compatible** between 0.4.0 and 0.5.0. All existing code will continue to work:

```java
// This code works in both 0.4.0 and 0.5.0
Identity identity = Identity.generateRandomIdentity();
Map<String, String> relays = Map.of("damus", "wss://relay.398ja.xyz");

new NIP01(identity)
    .createTextNoteEvent("Hello nostr")
    .sign()
    .send(relays);
```

### Gradle Users

**Impact**: None

If you're using Gradle, simply update the version:

```gradle
dependencies {
    implementation 'xyz.tcheeric:nostr-java-api:0.5.0'  // Update version
}
```

No other changes required.

### Verification Steps

After migration, verify your setup:

1. **Build your project**:
   ```bash
   mvn clean verify
   # or
   gradle clean build
   ```

2. **Run your tests**:
   ```bash
   mvn test
   # or
   gradle test
   ```

3. **Check for dependency conflicts**:
   ```bash
   mvn dependency:tree
   # or
   gradle dependencies
   ```

4. **Verify no Spring Boot version conflicts** if you use Spring Boot:
   ```bash
   mvn dependency:tree | grep spring-boot
   ```

### Common Issues

#### Issue: Spring Boot Version Conflict

**Symptom**: `java.lang.NoSuchMethodError` or `ClassNotFoundException` for Spring classes

**Solution**: Ensure your Spring Boot version is compatible. nostr-java 0.5.0 is tested with Spring Boot 3.5.x.

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.5</version> <!-- Use 3.5.x or compatible -->
</parent>
```

#### Issue: Maven Build Fails with "Cannot resolve nostr-java-bom"

**Symptom**: Build error about missing BOM artifact

**Solution**: Ensure you've added the custom repository:

```xml
<repositories>
    <repository>
        <id>nostr-java</id>
        <url>https://maven.398ja.xyz/releases</url>
    </repository>
</repositories>
```

#### Issue: Dependency Resolution Errors

**Symptom**: Conflicting dependency versions

**Solution**: Use dependency management to override versions if needed:

```xml
<dependencyManagement>
    <dependencies>
        <!-- Import nostr-java BOM first -->
        <dependency>
            <groupId>xyz.tcheeric</groupId>
            <artifactId>nostr-java-bom</artifactId>
            <version>1.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- Override specific versions if needed -->
        <dependency>
            <groupId>org.bouncycastle</groupId>
            <artifactId>bcprov-jdk18on</artifactId>
            <version>1.81</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### Benefits of 0.5.0

- **Better dependency control**: No longer tied to Spring Boot's versioning
- **Reduced conflicts**: Your application can use any Spring Boot version
- **Cleaner builds**: Less transitive dependency noise
- **Future-proof**: Easier to update nostr-java independently

---

## General Migration Tips

### Before Upgrading

1. **Read the release notes**: Check the [releases page](https://github.com/tcheeric/nostr-java/releases) for detailed changes

2. **Backup your code**: Commit your changes or create a branch:
   ```bash
   git checkout -b upgrade-nostr-java
   ```

3. **Review deprecation warnings**: Fix any deprecated API usage before upgrading

4. **Check your dependencies**:
   ```bash
   mvn dependency:tree > before.txt
   ```

### During Upgrade

1. **Update version** in your build file (`pom.xml` or `build.gradle`)

2. **Clean build**:
   ```bash
   mvn clean
   # or
   gradle clean
   ```

3. **Rebuild**:
   ```bash
   mvn verify
   # or
   gradle build
   ```

4. **Run tests**:
   ```bash
   mvn test
   # or
   gradle test
   ```

5. **Compare dependencies** to check for unexpected changes:
   ```bash
   mvn dependency:tree > after.txt
   diff before.txt after.txt
   ```

### After Upgrade

1. **Test key functionality**:
   - Event creation and signing
   - Relay connections
   - Subscriptions
   - Encryption/decryption (if used)

2. **Monitor for issues**:
   - Check logs for warnings or errors
   - Verify performance is unchanged
   - Test edge cases specific to your application

3. **Update documentation**: Document any code changes you made

### Rollback Plan

If you encounter issues:

1. **Revert to previous version**:
   ```xml
   <dependency>
       <groupId>xyz.tcheeric</groupId>
       <artifactId>nostr-java-api</artifactId>
       <version>0.4.0</version> <!-- Previous version -->
   </dependency>
   ```

2. **Clean and rebuild**:
   ```bash
   mvn clean install
   ```

3. **Report the issue**: [Open an issue](https://github.com/tcheeric/nostr-java/issues) with:
   - nostr-java versions (old and new)
   - Java version
   - Build tool (Maven/Gradle) and version
   - Full error stack trace
   - Minimal reproduction code

### Testing Checklist

After any migration, verify:

- [ ] Project builds successfully
- [ ] All tests pass
- [ ] Event creation works
- [ ] Event signing works
- [ ] Relay connections work
- [ ] Subscriptions receive events
- [ ] Encryption/decryption works (if used)
- [ ] No new deprecation warnings
- [ ] No unexpected dependency changes
- [ ] Application starts and runs normally
- [ ] Performance is acceptable

### Getting Help

If you need assistance with migration:

1. **Check the docs**: [docs/README.md](README.md)
2. **Search issues**: [GitHub Issues](https://github.com/tcheeric/nostr-java/issues)
3. **Ask for help**: Open a new issue with the `question` label
4. **Review examples**: Check the [`nostr-java-examples`](../nostr-java-examples) module for updated code patterns

---

## Version History

| Version | Release Date | Key Changes |
|---------|--------------|-------------|
| 0.5.0   | Jan 2025     | BOM migration, dependency management improvements |
| 0.4.0   | Dec 2024     | Spring Boot 3.5.5, streaming subscriptions |

See the [releases page](https://github.com/tcheeric/nostr-java/releases) for complete version history.
