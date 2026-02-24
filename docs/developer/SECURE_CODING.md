# Secure Coding Guidelines for nostr-java

This document outlines the mandatory secure coding practices for the `nostr-java` project. These guidelines are derived from industry best practices (OWASP, Oracle, etc.) and must be followed for all contributions.

## 1. Input Validation and Output Encoding

### Input Validation
*   **Validate All Inputs:** Adopt a "deny by default" approach. Define strictly what is allowed (allow-listing) rather than what is forbidden (block-listing).
*   **Context-Aware:** Validation must be appropriate for the data type (e.g., email, UUID, payment amount).
*   **Boundary Checks:** Check for length, range, and format constraints.
*   **Sanitization:** Sanitize input *before* processing, but prefer validation over sanitization where possible.

### Output Encoding
*   **Context-Aware Encoding:** Encode data based on where it will be displayed (HTML body, HTML attribute, JavaScript, CSS, URL).
*   **Prevention:** This is the primary defense against Cross-Site Scripting (XSS).
*   **Libraries:** Use established libraries like OWASP Java Encoder.

## 2. Injection Prevention

### SQL/Database Injection
*   **Parameterized Queries:** ALWAYS use `PreparedStatement` in JDBC or parameterized queries in JPA/Hibernate.
*   **No Concatenation:** NEVER concatenate user input directly into query strings.
*   **ORM Usage:** Use criteria APIs or named queries where possible.

### Command Injection
*   **Avoid OS Commands:** Do not use `Runtime.exec()` or `ProcessBuilder` with user-supplied arguments.
*   **APIs:** Use Java API equivalents (e.g., `java.nio.file`) instead of shell commands (e.g., `ls`, `rm`).

### Log Injection
*   **Sanitize Logs:** Ensure user input written to logs does not contain newline characters (`\n`, `\r`) to prevent log forging.

## 3. Cryptography & Secrets Management

### Cryptography
*   **Standard Libraries:** Use `BouncyCastle` (already in dependencies) or `Google Tink`. **NEVER** implement custom cryptographic algorithms.
*   **Algorithms:**
    *   **Hashing:** SHA-256 or higher.
    *   **Password Hashing:** Argon2id (preferred) or BCrypt.
    *   **Encryption:** AES-GCM (256-bit).
*   **Randomness:** Use `SecureRandom` for security-critical random numbers (keys, nonces, tokens), not `java.util.Random`.

### Secrets Management
*   **No Hardcoded Secrets:** NEVER commit API keys, passwords, or tokens to the repository.
*   **Environment Variables:** Load secrets from environment variables or a secure vault.
*   **Git Hooks:** Use `git-secrets` or similar tools to prevent accidental commits of sensitive data.

## 4. Authentication & Authorization

### Authentication
*   **Strong Defaults:** Enforce strong password policies (length, complexity).
*   **Session Management:** Use secure, HTTP-only, SameSite cookies for session identifiers.
*   **MFA:** Support Multi-Factor Authentication where applicable.

### Authorization
*   **Least Privilege:** Grant the minimum necessary permissions for a user or service to function.
*   **Vertical & Horizontal:** Check permissions at every access point (controller, service method). Ensure users can only access their *own* data (horizontal privilege escalation prevention).

## 5. Dependency Management

*   **Vulnerability Scanning:** Regularly scan dependencies for known vulnerabilities (CVEs) using `OWASP Dependency Check` or similar plugins.
*   **Updates:** Keep libraries (Spring Boot, BouncyCastle, etc.) up-to-date.
*   **Transitive Dependencies:** Be aware of and manage transitive dependencies.

## 6. Error Handling & Logging

*   **Generic Errors:** specific error details (stack traces, internal paths) should NEVER be exposed to the client/API response. Return generic error codes/messages.
*   **Audit Logging:** Log security-critical events (login attempts, failed authorization, sensitive data access).
*   **Exception Blocks:** Do not suppress exceptions silently. Log them with sufficient context (internally).

## 7. XML & Serialization

*   **XXE Prevention:** Disable DTDs and external entity processing in XML parsers (`DocumentBuilderFactory`, `SAXParserFactory`, `XMLInputFactory`).
*   **Deserialization:** Avoid Java native serialization if possible. If necessary, use strict allow-lists for classes that can be deserialized.

## References
*   [OWASP Java Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Java_Security_Cheat_Sheet.html)
*   [Oracle Secure Coding Guidelines](https://www.oracle.com/java/technologies/javase/seccodeguide.html)
*   [TechOral Java Security Best Practices](https://techoral.com/java/java-security-best-practices.html)
