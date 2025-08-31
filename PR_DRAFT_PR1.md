refactor: remove redundant rethrow and reuse HttpClient

Summary
- Remove redundant catch-and-rethrow blocks flagged by static analysis.
- Reuse a single HttpClient instance in Nip05Validator instead of per-call creation.

Changes
- F:nostr-java-event/src/main/java/nostr/event/impl/CreateOrUpdateStallEvent.java†L49-L62: remove `catch (AssertionError e) { throw e; }`; preserve wrapping of non-assertion exceptions.
- F:nostr-java-event/src/main/java/nostr/event/impl/MerchantEvent.java†L43-L56: remove `catch (AssertionError e) { throw e; }`; preserve wrapping of non-assertion exceptions.
- F:nostr-java-util/src/main/java/nostr/util/validator/Nip05Validator.java†L32-L49,L82: add cached `HttpClient` and `client()` accessor; replace per-call `HttpClient.newHttpClient()` with reuse.

Testing
- ✅ `mvn -q -DskipITs=false verify`
  - Passed locally. Notable logs (tests ran, no failures):
    - Spring WebSocket client tests executed with retries and expected exceptions in tests.
    - Testcontainers pulled and started `scsibug/nostr-rs-relay:latest` containers successfully.

Network Access
- No blocked domains encountered. Maven dependencies and Testcontainers images resolved successfully.

Notes
- SLF4J no-provider warnings are informational and unchanged by this PR.
- Mockito agent warnings are also informational and unrelated to these changes.

Protocol Compliance
- No event schema or protocol behavior changed. Validations remain aligned with NIP-15 content expectations (stall and merchant entity checks remain intact).

