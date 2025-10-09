# Follow-up Issues: Operations Documentation

Create the following GitHub issues to track operations docs and examples.

1) Ops: Micrometer integration examples
- Show counters via `MeterRegistry` (simple counters, timers around send)
- Listener wiring (`onSendFailures`) increments counters
- Sample Prometheus scrape via micrometer-registry-prometheus

2) Ops: Prometheus exporter example
- Minimal HTTP endpoint exposing counters
- Translate `DefaultNoteService.FailureInfo` into metrics labels (relay)
- Include guidance on cardinality

3) Ops: Logging patterns and correlation IDs
- MDC usage to correlate sends with subscriptions
- Recommended logger categories & sample filters
- JSON logging example (Logback)

4) Ops: Configuration deep-dive
- Advanced timeouts and backoff strategies (pros/cons)
- When to adjust `await-timeout-ms` / `poll-interval-ms`
- Retry tuning beyond defaults and trade-offs

5) Ops: Diagnostics cookbook
- Common failure scenarios and how to interpret FailureInfo
- Mapping failures to remediation steps
- Cross-relay differences and best practices

Note: Opening issues requires repository permissions; add the above as individual issues with `docs` and `operations` labels.
