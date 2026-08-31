# 05: Synthetic EOSE and mid-stream recovery

**What to build:** A developer can tell when the stored-event backlog is drained across all
relays, and a long-lived subscription repairs itself instead of quietly degrading.

A REQ returns stored events, then `EOSE`, then live events. Across five relays that is five
separate `EOSE` frames, but an application needs one answer to "is the backlog drained" to hide
a loading spinner. The pool aggregates them into a single synthetic EOSE, emitted once every
participating relay has reported or a timeout expires. The timeout matters: one dead relay must
not leave a UI loading forever.

The characteristic multi-relay failure is a firehose that degrades from five relays to one over
a day while the caller is never told. So a relay dropping mid-stream notifies an error callback
and, when that relay reconnects, is re-subscribed from the stored filter. Subscriptions are
therefore stateful objects holding their filter, not fire-and-forget handles.

See ADR-0004 and ADR-0005.

**Blocked by:** 04 (Fan-in subscriptions with de-duplication).

**Status:** done

- [x] One synthetic end-of-stored-events signal is emitted after every participating relay has
      sent its own `EOSE`
- [x] The synthetic signal is still emitted when a relay never responds, bounded by a timeout
- [x] Per-relay `EOSE` frames are not exposed to callers
- [x] A relay dropping mid-stream notifies the caller's error callback
- [x] A dropped relay is automatically re-subscribed from the stored filter on reconnect
- [x] A malformed relay payload is reported without ending the subscription
- [x] Tests cover EOSE after all relays report, EOSE despite one silent relay, drop-notify-
      resubscribe, and a malformed payload not killing the stream
- [x] `mvn -q verify` passes
