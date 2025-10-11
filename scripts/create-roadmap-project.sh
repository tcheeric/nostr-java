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

ASSIGNEE="${ASSIGNEE:-$(gh api user --jq .login 2>/dev/null || true)}"
if [[ -z "${ASSIGNEE}" ]]; then
  echo "WARN: Could not resolve current GitHub user; set ASSIGNEE env var to your login to assign tasks." >&2
fi

# Create or update a draft task item, assign to $ASSIGNEE and set Status=Todo (if such a field exists).
add_task() {
  local title="$1"
  local body="$2"
  echo "Ensuring task: ${title}"

  # Try to find an existing item by title to avoid duplicates
  local existing_id
  existing_id=$(gh project item-list "${project_number}" --owner "${repo_owner}" --format json 2>/dev/null \
    | jq -r --arg t "${title}" '.items[]? | select(.title == $t) | .id' 2>/dev/null || true)

  local item_id
  if [[ -n "${existing_id}" ]]; then
    item_id="${existing_id}"
    echo "Found existing item for '${title}' (${item_id}); updating fields."
  else
    # Create a draft issue item in the project and capture its id
    item_id=$(gh project item-create "${project_number}" --owner "${repo_owner}" \
      --title "${title}" --body "${body}" --format json | jq -r '.id')
    echo "Created item ${item_id}"
  fi

  # Best-effort: set Status to Todo and assign to ASSIGNEE if possible.
  # The 'gh project item-edit' command resolves field names (e.g., Status) and user logins.
  if [[ -n "${item_id}" ]]; then
    if [[ -n "${ASSIGNEE}" ]]; then
      gh project item-edit "${project_number}" --owner "${repo_owner}" --id "${item_id}" \
        --field "Assignees=@${ASSIGNEE}" >/dev/null || true
    fi
    # Set status to Todo if the project has a Status field with that option.
    gh project item-edit "${project_number}" --owner "${repo_owner}" --id "${item_id}" \
      --field "Status=Todo" >/dev/null || true
  fi
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
#add_task "Configure release workflow secrets" "Set CENTRAL_USERNAME/PASSWORD, GPG_PRIVATE_KEY/PASSPHRASE for .github/workflows/release.yml."
#add_task "Validate tag/version parity in release" "Ensure pushed tags match POM version; workflow enforces v<version> format."
#add_task "Update docs version references" "Refresh GETTING_STARTED.md and howto/use-nostr-java-api.md to current version and BOM usage."
#add_task "Publish CI + IT stability plan" "Keep Docker-based IT job green; document no-docker profile and failure triage."

# Qodana-derived tasks (from QODANA_TODOS.md)
# Priority 1: Critical Issues
add_task "Fix NPE risk in NIP01TagFactory#getUuid" \
  "nostr-java-api/src/main/java/nostr/api/nip01/NIP01TagFactory.java:78\n- Ensure null-safe handling of IdentifierTag.getUuid().\n- Add null check or use Objects.requireNonNullElse.\n- Add/adjust unit tests."

add_task "Verify coordinate pair order in Point.java:24" \
  "nostr-java-crypto/src/main/java/nostr/crypto/Point.java:24\n- Review Pair.of(x,y) usage and parameter semantics.\n- Confirm coordinates match expected order and document."

add_task "Fix always-false condition in AddressableEvent" \
  "nostr-java-event/src/main/java/nostr/event/impl/AddressableEvent.java:27\n- Condition '30_000 <= n && n < 40_000' reported as always false.\n- Correct validation logic per NIP-01.\n- Add unit test coverage."

add_task "Fix always-false condition in ClassifiedListingEvent" \
  "nostr-java-event/src/main/java/nostr/event/impl/ClassifiedListingEvent.java:159\n- Condition '30402 <= n && n <= 30403' reported as always false.\n- Verify expected kinds per NIP-99; correct logic.\n- Add unit tests."

add_task "Fix always-false condition in EphemeralEvent" \
  "nostr-java-event/src/main/java/nostr/event/impl/EphemeralEvent.java:33\n- Condition '20_000 <= n && n < 30_000' reported as always false.\n- Correct range checks per spec; add tests."

# Priority 2: Important Issues
add_task "CashuToken: proofs queried but never populated" \
  "nostr-java-event/src/main/java/nostr/event/entities/CashuToken.java:22\n- Initialize or populate 'proofs' where required, or remove query.\n- Add tests for expected behavior."

add_task "NutZap: proofs updated but never queried" \
  "nostr-java-event/src/main/java/nostr/event/entities/NutZap.java:15\n- Ensure 'proofs' has corresponding reads or remove writes.\n- Add tests verifying usage."

add_task "SpendingHistory: eventTags updated but never queried" \
  "nostr-java-event/src/main/java/nostr/event/entities/SpendingHistory.java:21\n- Add reads for 'eventTags' or remove dead writes.\n- Add/adjust tests."

add_task "NIP46: params updated but never queried" \
  "nostr-java-api/src/main/java/nostr/api/NIP46.java:71\n- Align 'params' usage (reads/writes) or remove redundant code.\n- Add tests."

add_task "CashuToken: destroyed updated but never queried" \
  "nostr-java-event/src/main/java/nostr/event/entities/CashuToken.java:24\n- Align 'destroyed' usage or remove redundant updates.\n- Add tests."

add_task "Remove serialVersionUID from non-Serializable serializers" \
  "Files:\n- TagSerializer.java:13\n- GenericTagSerializer.java:7\n- BaseTagSerializer.java:6\nActions:\n- Remove serialVersionUID or implement Serializable if needed."

add_task "RelayUri: remove redundant null check before equalsIgnoreCase" \
  "nostr-java-base/src/main/java/nostr/base/RelayUri.java:19\n- Simplify conditional logic; remove pointless null check.\n- Add small unit test."

add_task "NIP09: simplify redundant conditions" \
  "nostr-java-api/src/main/java/nostr/api/NIP09.java:55,61\n- Replace 'GenericEvent.class::isInstance' redundant checks with simpler logic.\n- Add tests to cover branches."

# Priority 3: Documentation Issues
add_task "Fix Javadoc @link references in Constants.java (82 issues)" \
  "nostr-java-api/src/main/java/nostr/config/Constants.java\n- Resolve broken symbols and use fully-qualified names where needed.\n- Verify all @link/@see entries."

add_task "Fix remaining JavadocReference issues across API/event modules" \
  "Multiple files (see QODANA_TODOS.md)\n- Address unresolved Javadoc symbols and imports.\n- Focus on CalendarContent.java and NIP60.java next."

add_task "Fix Javadoc declaration syntax issues (12 occurrences)" \
  "Project-wide\n- Repair malformed tags and ensure proper structure."

add_task "Convert plain text links to {@link} (2 occurrences)" \
  "Project-wide\n- Replace plain links with proper {@link} tags where appropriate."

# Priority 4: Code Quality Improvements
add_task "Refactor: convert fields to local variables (55 issues)" \
  "Project-wide\n- Reduce class state by inlining temporary fields.\n- Prioritize OkMessage and entities package."

add_task "Refactor: mark fields final where applicable (18 issues)" \
  "Project-wide\n- Add 'final' to fields never reassigned."

add_task "Refactor: remove unnecessary local variables (12 issues)" \
  "Project-wide\n- Inline trivial temps; improve readability."

add_task "Fix unchecked warnings (11 occurrences)" \
  "Project-wide\n- Add generics or justified @SuppressWarnings with comments."

add_task "Migrate deprecated API usage (4 occurrences)" \
  "Project-wide\n- Replace deprecated members with supported alternatives."

add_task "Remove unused imports (2 occurrences)" \
  "Project-wide\n- Delete unused imports; enable auto-remove in IDE."

cat <<INFO
Project setup complete.
If tasks already existed as draft items, duplicates may appear; manually consolidate as needed.
INFO
