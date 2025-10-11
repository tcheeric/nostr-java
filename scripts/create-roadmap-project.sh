#!/usr/bin/env bash
set -euo pipefail

# Create or update a GitHub Projects (beta) board that tracks the nostr-java 1.0 roadmap.
# Requires: GitHub CLI 2.32+ with project commands enabled and an authenticated session.

project_title="nostr-java 1.0 Roadmap"

if ! command -v gh >/dev/null 2>&1; then
  echo "GitHub CLI (gh) is required to run this script." >&2
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required to parse GitHub CLI responses." >&2
  exit 1
fi

repo_json=$(gh repo view --json nameWithOwner,owner --jq '{nameWithOwner, owner_login: .owner.login}' 2>/dev/null || true)
if [[ -z "${repo_json}" ]]; then
  echo "Unable to determine repository owner via 'gh repo view'. Ensure you are within a cloned repo or pass --repo." >&2
  exit 1
fi

repo_name_with_owner=$(jq -r '.nameWithOwner' <<<"${repo_json}")
repo_owner=$(jq -r '.owner_login' <<<"${repo_json}")

# Look up an existing project with the desired title.
project_number=$(gh project list --owner "${repo_owner}" --format json |
  jq -r --arg title "${project_title}" '[.. | objects | select(has("title")) | select(.title == $title)] | first? | .number // empty')

if [[ -z "${project_number}" ]]; then
  echo "Creating project '${project_title}' for owner ${repo_owner}"
  gh project create --owner "${repo_owner}" --title "${project_title}" --format json >/tmp/project-create.json
  project_number=$(jq -r '.number' /tmp/project-create.json)
  echo "Created project #${project_number}"
else
  echo "Project '${project_title}' already exists as #${project_number}."
fi

add_task() {
  local title="$1"
  local body="$2"
  echo "Ensuring draft item: ${title}"
  # Create a draft issue item in the project (idempotency not guaranteed by CLI; duplicates may occur)
  gh project item-create "${project_number}" --owner "${repo_owner}" --title "${title}" --body "${body}" --format json >/dev/null
}

#add_task "Remove deprecated constants facade" "Delete nostr.config.Constants.Kind before 1.0. See docs/explanation/roadmap-1.0.md."
#add_task "Retire legacy encoder singleton" "Drop Encoder.ENCODER_MAPPER_BLACKBIRD after migrating callers to EventJsonMapper."
#add_task "Drop deprecated NIP overloads" "Purge for-removal overloads in NIP01 and NIP61 to stabilize fluent APIs."
#add_task "Remove deprecated tag constructors" "Clean up GenericTag and EntityFactory compatibility constructors."
#add_task "Cover all relay command decoding" "Extend BaseMessageDecoderTest and BaseMessageCommandMapperTest fixtures beyond REQ."
#add_task "Stabilize NIP-52 calendar integration" "Re-enable flaky assertions in ApiNIP52RequestIT with deterministic relay handling."
#add_task "Stabilize NIP-99 classifieds integration" "Repair ApiNIP99RequestIT expectations for NOTICE/EOSE relay responses."
#add_task "Complete migration checklist" "Fill MIGRATION.md deprecated API removals section before cutting 1.0."
#add_task "Document dependency alignment plan" "Record and streamline parent POM overrides tied to 0.6.5-SNAPSHOT."
#add_task "Plan version uplift workflow" "Outline tagging and publishing steps for the 1.0.0 release in docs."

# Newly documented release engineering tasks
add_task "Configure release workflow secrets" "Set CENTRAL_USERNAME/PASSWORD, GPG_PRIVATE_KEY/PASSPHRASE for .github/workflows/release.yml."
add_task "Validate tag/version parity in release" "Ensure pushed tags match POM version; workflow enforces v<version> format."
add_task "Update docs version references" "Refresh GETTING_STARTED.md and howto/use-nostr-java-api.md to current version and BOM usage."
add_task "Publish CI + IT stability plan" "Keep Docker-based IT job green; document no-docker profile and failure triage."

cat <<INFO
Project setup complete.
If tasks already existed as draft items, duplicates may appear; manually consolidate as needed.
INFO
