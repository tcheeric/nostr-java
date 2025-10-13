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

echo "=== PHASE 1: Critical Blockers ==="

add_task "[BLOCKER] Fix BOM version resolution" \
  "**Status**: CRITICAL - Build currently broken\n\n**Issue**: BOM version 1.1.8 not found in Maven Central (pom.xml:99)\n- Error: mvn test fails immediately with 'Non-resolvable import POM'\n\n**Actions**:\n- [ ] Check available BOM versions in repository\n- [ ] Downgrade to existing version OR\n- [ ] Publish 1.1.8 to Maven repository\n- [ ] Verify 'mvn clean test' succeeds\n\n**Priority**: P0 - Cannot proceed without fixing this"

echo "=== PHASE 2: API Stabilization (Breaking Changes for 1.0) ==="

add_task "Remove deprecated Constants.Kind facade" \
  "**File**: nostr-java-api/src/main/java/nostr/config/Constants.java\n\n**Actions**:\n- [ ] Delete nostr.config.Constants.Kind nested class\n- [ ] Migrate all internal usages to nostr.base.Kind\n- [ ] Search codebase: grep -r 'Constants.Kind' src/\n- [ ] Run tests to verify migration\n\n**Ref**: MIGRATION.md, roadmap-1.0.md"

add_task "Remove Encoder.ENCODER_MAPPER_BLACKBIRD" \
  "**File**: nostr-java-base/src/main/java/nostr/base/Encoder.java\n\n**Actions**:\n- [ ] Remove ENCODER_MAPPER_BLACKBIRD field\n- [ ] Migrate callers to EventJsonMapper.getMapper()\n- [ ] Search: grep -r 'ENCODER_MAPPER_BLACKBIRD' src/\n- [ ] Update tests\n\n**Ref**: MIGRATION.md"

add_task "Remove deprecated NIP01 method overloads" \
  "**File**: nostr-java-api/src/main/java/nostr/api/NIP01.java:152-195\n\n**Actions**:\n- [ ] Remove createTextNoteEvent(Identity, String)\n- [ ] Keep createTextNoteEvent(String) with instance sender\n- [ ] Update all callers\n- [ ] Run NIP01 tests\n\n**Ref**: MIGRATION.md"

add_task "Remove deprecated NIP61 method overload" \
  "**File**: nostr-java-api/src/main/java/nostr/api/NIP61.java:103-156\n\n**Actions**:\n- [ ] Remove old createNutzapEvent signature\n- [ ] Update callers to use slimmer overload + NIP60 tags\n- [ ] Run NIP61 tests\n\n**Ref**: MIGRATION.md"

add_task "Remove deprecated GenericTag constructor" \
  "**Files**:\n- nostr-java-event/src/main/java/nostr/event/tag/GenericTag.java\n- nostr-java-id/src/test/java/nostr/id/EntityFactory.java\n\n**Actions**:\n- [ ] Remove GenericTag(String, Integer) constructor\n- [ ] Remove EntityFactory.Events#createGenericTag(PublicKey, IEvent, Integer)\n- [ ] Update tests\n\n**Ref**: MIGRATION.md"

echo "=== PHASE 3: Critical Bug Fixes (Qodana P1) ==="

add_task "✅ [DONE] Fix NPE risk in NIP01TagFactory#getUuid" \
  "**Status**: COMPLETED\n**File**: nostr-java-api/src/main/java/nostr/api/nip01/NIP01TagFactory.java:78\n\n**Fixed**:\n- Added null check for identifierTag.getUuid()\n- Pattern: String uuid = getUuid(); if (uuid != null) param += uuid;\n\n**Ref**: Session work, QODANA_TODOS.md P1.1"

add_task "Verify coordinate pair order in Point.java:24" \
  "**Status**: HIGH PRIORITY\n**File**: nostr-java-crypto/src/main/java/nostr/crypto/Point.java:24\n\n**Issue**: Variable 'y' may be incorrectly passed as 'elementRight'\n\n**Actions**:\n- [ ] Review Pair.of(x,y) call semantics\n- [ ] Verify parameter order matches coordinate system\n- [ ] Add documentation/comments\n- [ ] Add unit tests for Point coordinate handling\n\n**Ref**: QODANA_TODOS.md P1.2"

add_task "✅ [DONE] Fix always-false condition in AddressableEvent" \
  "**Status**: COMPLETED\n**File**: nostr-java-event/src/main/java/nostr/event/impl/AddressableEvent.java:27\n\n**Fixed**:\n- Clarified validation logic with explicit Integer type\n- Added comprehensive Javadoc per NIP-01 spec\n- Improved error messages with actual kind value\n- Created AddressableEventTest with 6 test cases\n- Verified condition works correctly (was Qodana false positive)\n\n**Ref**: Session work, QODANA_TODOS.md P1.3.1"

add_task "Fix always-false condition in ClassifiedListingEvent" \
  "**Status**: HIGH PRIORITY\n**File**: nostr-java-event/src/main/java/nostr/event/impl/ClassifiedListingEvent.java:159\n\n**Issue**: Condition '30402 <= n && n <= 30403' reported as always false\n\n**Actions**:\n- [ ] Review against NIP-99 specification\n- [ ] Test with kinds 30402 and 30403\n- [ ] Fix validation logic or mark as false positive\n- [ ] Add ClassifiedListingEventTest with edge cases\n- [ ] Document expected kind range\n\n**Ref**: QODANA_TODOS.md P1.3.2"

add_task "Fix always-false condition in EphemeralEvent" \
  "**Status**: HIGH PRIORITY\n**File**: nostr-java-event/src/main/java/nostr/event/impl/EphemeralEvent.java:33\n\n**Issue**: Condition '20_000 <= n && n < 30_000' reported as always false\n\n**Actions**:\n- [ ] Review against NIP-01 ephemeral event spec\n- [ ] Test with kinds 20000-29999 range\n- [ ] Fix validation logic or mark as false positive\n- [ ] Add EphemeralEventTest with edge cases\n- [ ] Add Javadoc explaining ephemeral event kinds\n\n**Ref**: QODANA_TODOS.md P1.3.3"

echo "=== PHASE 4: Test Coverage Gaps ==="

add_task "Complete relay command decoding tests" \
  "**Status**: BLOCKER for 1.0\n**Files**:\n- nostr-java-event/src/test/java/nostr/event/unit/BaseMessageDecoderTest.java:16-117\n- nostr-java-event/src/test/java/nostr/event/unit/BaseMessageCommandMapperTest.java:16-74\n\n**Issue**: Only REQ command tested; missing EVENT, CLOSE, EOSE, NOTICE, OK, AUTH\n\n**Actions**:\n- [ ] Add test fixtures for all relay command types\n- [ ] Extend BaseMessageDecoderTest coverage\n- [ ] Extend BaseMessageCommandMapperTest coverage\n- [ ] Verify all protocol message paths\n\n**Ref**: roadmap-1.0.md"

add_task "Stabilize NIP-52 calendar integration tests" \
  "**Status**: BLOCKER for 1.0\n**File**: nostr-java-api/src/test/java/nostr/api/integration/ApiNIP52RequestIT.java:82-160\n\n**Issue**: Flaky assertions disabled; inconsistent relay responses\n\n**Actions**:\n- [ ] Diagnose relay behavior (EVENT vs EOSE ordering)\n- [ ] Update test expectations to match actual behavior\n- [ ] Re-enable commented assertions\n- [ ] Consider deterministic relay mocking\n- [ ] Verify tests pass consistently (3+ runs)\n\n**Ref**: roadmap-1.0.md"

add_task "Stabilize NIP-99 classifieds integration tests" \
  "**Status**: BLOCKER for 1.0\n**File**: nostr-java-api/src/test/java/nostr/api/integration/ApiNIP99RequestIT.java:71-165\n\n**Issue**: Flaky assertions disabled; NOTICE/EOSE inconsistencies\n\n**Actions**:\n- [ ] Document expected relay response patterns\n- [ ] Fix or clarify NOTICE vs EOSE expectations\n- [ ] Re-enable all assertions\n- [ ] Add retry logic if needed\n- [ ] Verify stability across runs\n\n**Ref**: roadmap-1.0.md"

add_task "✅ [DONE] Fix BOLT11 invoice parsing" \
  "**Status**: COMPLETED\n**File**: nostr-java-api/src/main/java/nostr/api/nip57/Bolt11Util.java:25\n\n**Fixed**:\n- Changed indexOf('1') to lastIndexOf('1') per Bech32 spec\n- Fixed test invoice format in Bolt11UtilTest.parseWholeBtcNoUnit\n- All Bolt11UtilTest tests now pass\n\n**Ref**: Session work"

echo "=== PHASE 5: Collection Usage Issues (Qodana P2) ==="

add_task "Fix CashuToken: proofs queried but never populated" \
  "**File**: nostr-java-event/src/main/java/nostr/event/entities/CashuToken.java:22\n\n**Actions**:\n- [ ] Review if 'proofs' should be initialized/populated\n- [ ] Add initialization logic OR remove query\n- [ ] Add tests for expected behavior\n\n**Ref**: QODANA_TODOS.md P2.1.1"

add_task "Fix NutZap: proofs updated but never queried" \
  "**File**: nostr-java-event/src/main/java/nostr/event/entities/NutZap.java:15\n\n**Actions**:\n- [ ] Add reads for 'proofs' OR remove writes\n- [ ] Add tests verifying usage\n\n**Ref**: QODANA_TODOS.md P2.1.2"

add_task "Fix SpendingHistory: eventTags updated but never queried" \
  "**File**: nostr-java-event/src/main/java/nostr/event/entities/SpendingHistory.java:21\n\n**Actions**:\n- [ ] Add reads for 'eventTags' OR remove writes\n- [ ] Add/adjust tests\n\n**Ref**: QODANA_TODOS.md P2.1.3"

add_task "Fix NIP46: params updated but never queried" \
  "**File**: nostr-java-api/src/main/java/nostr/api/NIP46.java:71\n\n**Actions**:\n- [ ] Align 'params' usage (reads/writes)\n- [ ] Remove if redundant\n- [ ] Add tests\n\n**Ref**: QODANA_TODOS.md P2.1.4"

add_task "Fix CashuToken: destroyed updated but never queried" \
  "**File**: nostr-java-event/src/main/java/nostr/event/entities/CashuToken.java:24\n\n**Actions**:\n- [ ] Align 'destroyed' usage\n- [ ] Remove if redundant\n- [ ] Add tests\n\n**Ref**: QODANA_TODOS.md P2.1.5"

echo "=== PHASE 6: Code Cleanup (Qodana P2) ==="

add_task "Remove serialVersionUID from non-Serializable serializers" \
  "**Files**:\n- nostr-java-event/src/main/java/nostr/event/json/serializer/TagSerializer.java:13\n- nostr-java-event/src/main/java/nostr/event/json/serializer/GenericTagSerializer.java:7\n- nostr-java-event/src/main/java/nostr/event/json/serializer/BaseTagSerializer.java:6\n\n**Actions**:\n- [ ] Remove serialVersionUID fields (recommended)\n- [ ] OR implement Serializable if needed\n\n**Ref**: QODANA_TODOS.md P2.2"

add_task "Fix RelayUri: remove redundant null check" \
  "**File**: nostr-java-base/src/main/java/nostr/base/RelayUri.java:19\n\n**Actions**:\n- [ ] Simplify conditional before equalsIgnoreCase\n- [ ] Add unit test\n\n**Ref**: QODANA_TODOS.md P2.3"

add_task "Fix NIP09: simplify redundant conditions" \
  "**File**: nostr-java-api/src/main/java/nostr/api/NIP09.java:55,61\n\n**Actions**:\n- [ ] Replace redundant GenericEvent.class::isInstance checks\n- [ ] Simplify with null checks\n- [ ] Add tests\n\n**Ref**: QODANA_TODOS.md P2.4"

echo "=== PHASE 7: Release Engineering ==="

add_task "Update version to 1.0.0" \
  "**Status**: Ready when all blockers resolved\n**File**: pom.xml:6\n\n**Actions**:\n- [ ] Change version from 1.0.2-SNAPSHOT to 1.0.0\n- [ ] Update all module POMs if needed\n- [ ] Verify no SNAPSHOT dependencies remain\n- [ ] Run full build: mvn clean verify\n\n**Ref**: docs/howto/version-uplift-workflow.md"

add_task "Publish 1.0.0 to Maven Central" \
  "**Status**: After version bump and tests pass\n\n**Actions**:\n- [ ] Configure release workflow secrets (CENTRAL_USERNAME/PASSWORD, GPG keys)\n- [ ] Tag release: git tag v1.0.0\n- [ ] Push tag: git push origin v1.0.0\n- [ ] Verify GitHub Actions release workflow succeeds\n- [ ] Confirm artifacts published to Maven Central\n\n**Ref**: .github/workflows/release.yml"

add_task "Update BOM and remove module overrides" \
  "**Status**: After 1.0.0 published\n**File**: pom.xml:78,99\n\n**Actions**:\n- [ ] Publish/update BOM with 1.0.0 coordinates\n- [ ] Bump nostr-java-bom.version to matching BOM\n- [ ] Remove temporary module overrides in dependencyManagement\n- [ ] Verify mvn dependency:tree shows BOM-managed versions\n\n**Ref**: docs/explanation/dependency-alignment.md"

add_task "Update documentation version references" \
  "**Status**: Before/during release\n\n**Actions**:\n- [ ] Update GETTING_STARTED.md with 1.0.0 examples\n- [ ] Update docs/howto/use-nostr-java-api.md version refs\n- [ ] Update README.md badges and examples\n- [ ] Update CHANGELOG.md with release notes\n- [ ] Update MIGRATION.md with actual release date\n\n**Ref**: roadmap-1.0.md"

add_task "Create GitHub release and announcement" \
  "**Status**: After successful publish\n\n**Actions**:\n- [ ] Draft GitHub release with CHANGELOG content\n- [ ] Highlight breaking changes and migration guide\n- [ ] Tag as v1.0.0 milestone\n- [ ] Post announcement (if applicable)\n- [ ] Close 1.0 roadmap project\n\n**Ref**: CHANGELOG.md"

echo "=== PHASE 8: Documentation (Qodana P3 - Post-1.0 acceptable) ==="

add_task "Fix Javadoc @link references in Constants.java (82 issues)" \
  "**Priority**: P3 - Can defer post-1.0\n**File**: nostr-java-api/src/main/java/nostr/config/Constants.java\n\n**Actions**:\n- [ ] Resolve broken symbols\n- [ ] Use fully-qualified names where needed\n- [ ] Verify all @link/@see entries\n\n**Ref**: QODANA_TODOS.md P3.1"

add_task "Fix remaining JavadocReference issues (76 issues)" \
  "**Priority**: P3 - Can defer post-1.0\n**Files**: CalendarContent.java, NIP60.java, Identity.java, others\n\n**Actions**:\n- [ ] Address unresolved Javadoc symbols\n- [ ] Fix imports and references\n\n**Ref**: QODANA_TODOS.md P3.1"

add_task "Fix Javadoc declaration syntax issues (12)" \
  "**Priority**: P3 - Can defer post-1.0\n**Scope**: Project-wide\n\n**Actions**:\n- [ ] Repair malformed Javadoc tags\n- [ ] Ensure proper structure\n\n**Ref**: QODANA_TODOS.md P3.2"

add_task "Convert plain text links to {@link} (2)" \
  "**Priority**: P3 - Can defer post-1.0\n**Scope**: Project-wide\n\n**Actions**:\n- [ ] Replace plain links with {@link} tags\n\n**Ref**: QODANA_TODOS.md P3.3"

echo "=== PHASE 9: Code Quality (Qodana P4 - Post-1.0 acceptable) ==="

add_task "Refactor: convert fields to local variables (55)" \
  "**Priority**: P4 - Nice-to-have\n**Scope**: Project-wide, focus on OkMessage and entities\n\n**Actions**:\n- [ ] Inline temporary fields\n- [ ] Reduce class state complexity\n\n**Ref**: QODANA_TODOS.md P4.1"

add_task "Refactor: mark fields final (18)" \
  "**Priority**: P4 - Nice-to-have\n**Scope**: Project-wide\n\n**Actions**:\n- [ ] Add 'final' to fields never reassigned\n\n**Ref**: QODANA_TODOS.md P4.2"

add_task "Refactor: remove unnecessary local variables (12)" \
  "**Priority**: P4 - Nice-to-have\n**Scope**: Project-wide\n\n**Actions**:\n- [ ] Inline trivial temps\n\n**Ref**: QODANA_TODOS.md P4.3"

add_task "Fix unchecked warnings (11)" \
  "**Priority**: P4 - Nice-to-have\n**Scope**: Project-wide\n\n**Actions**:\n- [ ] Add proper generics\n- [ ] Or add justified @SuppressWarnings\n\n**Ref**: QODANA_TODOS.md P4.4"

add_task "Migrate deprecated API usage (4)" \
  "**Priority**: P4 - Nice-to-have\n**Scope**: Project-wide\n\n**Actions**:\n- [ ] Replace deprecated members\n\n**Ref**: QODANA_TODOS.md P4.5"

add_task "Remove unused imports (2)" \
  "**Priority**: P4 - Nice-to-have\n**Scope**: Project-wide\n\n**Actions**:\n- [ ] Delete unused imports\n- [ ] Enable auto-remove in IDE\n\n**Ref**: QODANA_TODOS.md P4.6"

cat <<INFO
Project setup complete.
If tasks already existed as draft items, duplicates may appear; manually consolidate as needed.
INFO
