# Integration Test Bug Analysis: Relay Container Panic

## Summary

The integration tests in `nostr-java-api` fail with timeout errors because the `nostr-rs-relay` Docker container used for testing has a critical bug that causes it to crash when handling WebSocket messages.

## Symptoms

Tests fail with the following errors:

```
ApiNIP52EventIT.testNIP52CalendarTimeBasedEventEventUsingSpringWebSocketClient:63 » NoSuchElement No value present
ApiEventIT.testNIP01SendTextNoteEvent » Runtime No message received
```

All integration tests that send messages to the relay time out after 60 seconds without receiving a response.

## Root Cause

The `scsibug/nostr-rs-relay:latest` Docker image (15 months old, image ID: `64025dc3b517`) contains a bug in the `quanta` crate (version 0.9.3) that causes a panic when handling WebSocket connections:

```
thread 'tokio-ws-10' panicked at quanta-0.9.3/src/lib.rs:274:13:
po2_denom was zero!
```

This is a known issue with the `quanta` crate when running in certain virtualized environments (Docker containers). The panic occurs in the WebSocket message handling thread, causing:

1. The relay to accept WebSocket connections successfully
2. The relay to log incoming client connections
3. But message processing fails silently due to the thread panic
4. No response is ever sent back to the client

## Technical Analysis

### Container Startup and Wait Strategy

The test configuration in `BaseRelayIntegrationTest.java` correctly:
- Starts a Testcontainers-managed relay container
- Waits for the log message "listening on:" to confirm startup
- Uses a 30-second startup timeout

The container starts successfully and logs:
```
INFO nostr_rs_relay::server listening on: 0.0.0.0:8080
INFO nostr_rs_relay::server db writer created
INFO nostr_rs_relay::server control message listener started
```

### The Crash Point

Immediately after startup, when the first WebSocket connection attempts to send a message, the quanta crate panics:

```
thread 'tokio-ws-10' panicked at quanta-0.9.3/src/lib.rs:274:13:
po2_denom was zero!
```

This occurs because `quanta` is a high-resolution timing library used by the relay, and it fails to properly detect CPU timing capabilities in the Docker environment.

### Test Flow

1. Test creates `StandardWebSocketClient` connected to relay container
2. WebSocket connection establishes successfully
3. Test sends an EVENT message via WebSocket
4. Relay receives the connection but the handler thread panics
5. No response is sent back
6. Test waits 60 seconds (configured timeout)
7. `client.send()` returns an empty list
8. Test fails with `NoSuchElement` or `No message received`

## Additional Issue Found

A secondary issue was discovered: The `ApiEventIT` tests use Spring's `@Autowired` to inject relay configuration from `relays.properties`, which contains a hardcoded URL (`ws://127.0.0.1:5555`). This bypassed the dynamic Testcontainers port.

**Fix applied:** Modified `ApiEventIT` to use `getTestRelays()` from `BaseRelayIntegrationTest` instead of the autowired map.

## Affected Files

- `nostr-java-api/src/test/java/nostr/api/integration/BaseRelayIntegrationTest.java`
- `nostr-java-api/src/test/java/nostr/api/integration/ApiEventIT.java`
- `nostr-java-api/src/test/java/nostr/api/integration/ApiNIP52EventIT.java`
- `nostr-java-api/src/test/java/nostr/api/integration/ApiNIP99EventIT.java`
- `nostr-java-api/src/test/java/nostr/api/integration/ApiEventTestUsingSpringWebSocketClientIT.java`

## Recommended Solutions

### Option 1: Update nostr-rs-relay Image (Preferred)

Update to a newer version of the relay image that includes a fix for the quanta crate issue. Check the [nostr-rs-relay GitHub repository](https://github.com/scsibug/nostr-rs-relay) for recent releases.

### Option 2: Use an Alternative Relay Image

Consider using an alternative relay implementation:

- [strfry](https://github.com/dockur/strfry) - `dockurr/strfry:latest`
- [nostream](https://github.com/nostream/nostream) - TypeScript-based relay

Update `relay-container.properties` to use the new image:
```properties
relay.container.image=<new-relay-image>:<tag>
```

Adjust the wait strategy in `BaseRelayIntegrationTest.java` to match the new relay's log output format.

### Option 3: Build Custom nostr-rs-relay Image

Build a custom Docker image with an updated version of the quanta crate that fixes the `po2_denom` issue.

## Current Status

### With strfry (dockurr/strfry:latest)
- Container starts and runs successfully
- WebSocket connections work
- Tests complete in ~24 seconds (vs 960+ seconds with nostr-rs-relay)
- **11 of 21 ApiEventIT tests pass**
- Remaining failures are due to relay behavior differences (not infrastructure issues):
  - Some tests expect `success: true` but strfry returns `false` for certain event types
  - Filter queries return fewer results than expected

### With nostr-rs-relay (scsibug/nostr-rs-relay:latest)
- Container starts successfully
- WebSocket connections are established
- Message handling crashes due to quanta panic
- All integration tests that require relay responses timeout after 60 seconds

## Known Status

This is a **known unresolved issue** with the `nostr-rs-relay` Docker image. All available versions (0.8.9, 0.8.13, 0.9.0, latest) contain the same `quanta` 0.9.3 dependency with the calibration bug.

The quanta crate's TSC (Time Stamp Counter) calibration fails in certain virtualized/Docker environments where:
- The TSC is not available or unreliable
- CPU timing information is not properly exposed to the container
- The calibration process cannot compute a valid power-of-two denominator

Alternative relay implementations like `strfry` require higher file descriptor limits (1,000,000+) that may not be available in all Docker environments.

## References

- [quanta crate documentation](https://docs.rs/quanta)
- [quanta crate GitHub issues](https://github.com/metrics-rs/quanta/issues)
- [nostr-rs-relay Docker Hub](https://hub.docker.com/r/scsibug/nostr-rs-relay)
- [nostr-rs-relay GitHub](https://github.com/scsibug/nostr-rs-relay)
- [strfry Docker image](https://github.com/dockur/strfry)
- [Testcontainers documentation](https://www.testcontainers.org/)

## Changes Made (Partial Fixes)

1. Added `getTestRelays()` method to `BaseRelayIntegrationTest` for dynamic relay URL access
2. Modified `ApiEventIT` to use `@BeforeEach` setup instead of `@Autowired` relays
3. Increased container startup timeout from 3s to 30s
4. Updated wait strategy to use log message matching
5. Made relay port configurable via `relay-container.properties`

These changes fix the relay URL configuration but do not resolve the underlying container crash issue.

## Workaround Options

### 1. Skip Integration Tests in CI
Add `-DnoDocker=true` to Maven commands in CI environments where Docker doesn't support TSC properly:
```bash
mvn test -DnoDocker=true
```

### 2. Use a Different Host/Docker Configuration
The tests may work on hosts with proper TSC support (physical machines vs. VMs).

### 3. Build Custom Relay Image
Build a custom `nostr-rs-relay` image with an updated version of the `quanta` crate that includes a fix for the calibration issue.

### 4. Wait for Upstream Fix
Monitor the [quanta crate issues](https://github.com/metrics-rs/quanta/issues) for a fix and update `nostr-rs-relay` when available.
