# Repository Instructions for Copilot

- Follow the Nostr protocol (NIP-xx) specifications.
  - Spec index: https://github.com/nostr-protocol/nips
  - Each NIP is at `https://github.com/nostr-protocol/nips/blob/master/XX.md` (e.g. NIP-01 → https://github.com/nostr-protocol/nips/blob/master/01.md)
- All changes must include unit tests and update relevant docs.
- Use clear names and remove unused imports.
- Prefer readable, maintainable code over clever shortcuts.
- Run `mvn -q verify` locally before pushing.
