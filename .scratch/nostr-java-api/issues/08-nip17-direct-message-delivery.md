# 08: NIP-17 direct message delivery

**What to build:** A developer sends a private direct message in one call and learns who
received it.

Today `Nip17DirectMessageService.planDelivery` produces a `List<MessageDelivery>` that nothing
executes: the plan names each recipient, their gift wrap, and the relays it should go to, and
then stops. That is by design, because `identity` must stay transport-free. This ticket
executes the plan, which is exactly the orchestration a capability layer exists for.

For each recipient the delivery resolves their relays, adds them to the pool, publishes their
gift wrap there, and releases the transient connections afterwards. Recipients with no
published relay list are reported as unreachable, never silently skipped, so the application
can tell the user their message did not arrive.

Reading is symmetrical: an incoming gift wrap is read back into a chat message through the
same service.

See ADR-0003 and ADR-0005.

**Blocked by:** 06 (Runtime pool membership), 07 (Relay list lookup).

**Status:** done

- [x] Sending a direct message composes the plan and delivers each gift wrap to its own
      recipient's relays
- [x] Delivery returns a per-recipient outcome so a group message reports who received it
- [x] A recipient with no published relay list is reported unreachable, not skipped silently
- [x] Relays connected solely for a delivery are released afterwards
- [x] An incoming gift wrap can be read back into a chat message through the same service
- [x] `nostr-java-identity` gains no new dependencies
- [x] Tests cover single-recipient delivery, group delivery with mixed outcomes, an unreachable
      recipient, transient relay release, and the read-back path
- [x] `mvn -q verify` passes
