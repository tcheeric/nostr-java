# Configure Release Secrets

This guide explains how to configure the GitHub secrets required to publish releases to Maven Central and sign artifacts.

The release workflow reads the following secrets:

- `CENTRAL_USERNAME` — Sonatype (OSSRH) username
- `CENTRAL_PASSWORD` — Sonatype (OSSRH) password
- `GPG_PRIVATE_KEY` — ASCII‑armored GPG private key used for signing
- `GPG_PASSPHRASE` — Passphrase for the above private key

Prerequisites:

- A Sonatype (OSSRH) account with publishing permissions for this groupId
- A GPG keypair suitable for signing (RSA/ECC)

Steps:

1) Export your GPG private key in ASCII‑armored form

   - List keys: `gpg --list-secret-keys --keyid-format LONG`
   - Export: `gpg --armor --export-secret-keys <KEY_ID> > private.key.asc`

2) Add repository secrets

   - Open GitHub → Settings → Secrets and variables → Actions → New repository secret
   - Add the following secrets:
     - `CENTRAL_USERNAME` — your Sonatype username
     - `CENTRAL_PASSWORD` — your Sonatype password
     - `GPG_PRIVATE_KEY` — contents of `private.key.asc`
     - `GPG_PASSPHRASE` — your GPG key passphrase

3) Verify workflow configuration

   - The workflow `.github/workflows/release.yml` verifies that all four secrets are present
   - It configures Maven settings and GPG using `actions/setup-java@v4`

4) Trigger a release

   - Tag the repo: `git tag v<version>` where `<version>` matches the POM version
   - Push the tag: `git push origin v<version>`
   - The workflow validates tag/version parity and publishes artifacts if tests pass

Troubleshooting:

- Missing secret: the workflow fails early with a clear error message
- GPG key format: ensure the key is ASCII‑armored, not binary
- Staging errors on Central: check Sonatype UI for staging repository status

