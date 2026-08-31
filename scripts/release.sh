#!/usr/bin/env bash
set -euo pipefail

# Automates common release tasks for nostr-java.
# Subcommands:
#   bump --version <x.y.z>            Set root version to x.y.z and commit
#   verify [--no-docker]              Run mvn clean verify (optionally -Pno-docker)
#   tag --version <x.y.z> [--push]    Create annotated tag vX.Y.Z (and optionally push)
#   publish [--no-docker] [--repo central|398ja]
#                                    Deploy artifacts to selected repository profile
#   next-snapshot --version <x.y.z>   Set next SNAPSHOT (e.g., 1.0.1-SNAPSHOT) and commit
#
# Notes:
# - This script does not modify the BOM; see docs/explanation/dependency-alignment.md
# - Credentials and GPG must be pre-configured for publishing

DRYRUN=false

usage() {
  cat <<USAGE
Usage: $(basename "$0") <command> [options]

Commands:
  bump --version <x.y.z>            Set root version to x.y.z and commit
  verify [--no-docker] [--skip-tests] [--dry-run]
                                    Run mvn clean verify (optionally -Pno-docker)
  tag --version <x.y.z> [--push]    Create annotated tag vX.Y.Z (and optionally push)
  publish [--no-docker] [--skip-tests] [--repo central|398ja] [--dry-run]
                                    Deploy artifacts to selected repository profile
  next-snapshot --version <x.y.z>   Set next SNAPSHOT version and commit

Examples:
  scripts/release.sh bump --version 1.0.0
  scripts/release.sh verify --no-docker
  scripts/release.sh tag --version 1.0.0 --push
  scripts/release.sh publish --no-docker
  scripts/release.sh next-snapshot --version 1.0.1-SNAPSHOT
USAGE
}

run_cmd() {
  echo "+ $*"
  if ! $DRYRUN; then
    eval "$@"
  fi
}

# Resolve optional Maven settings
MVN_SETTINGS_OPTS=""
if [[ -f .mvn/settings.xml ]]; then
  MVN_SETTINGS_OPTS="-s .mvn/settings.xml"
fi

require_clean_tree() {
  if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "Working tree is not clean. Commit or stash changes first." >&2
    exit 1
  fi
}

cmd_bump() {
  local version=""
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --version) version="$2"; shift 2 ;;
      *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
    esac
  done
  [[ -n "$version" ]] || { echo "--version is required" >&2; exit 1; }
  require_clean_tree
  echo "Setting root version to ${version}"
  run_cmd mvn -q versions:set -DnewVersion="${version}"
  run_cmd mvn -q versions:commit
  update_documented_version "${version}"
  run_cmd git add pom.xml */pom.xml docs || true
  run_cmd git commit -m "chore(release): bump project version to ${version}"
}

# Keeps copyable install snippets in step with the version just set.
#
# DocumentationAccuracyTest fails the build when a snippet a reader would copy names a version
# other than the one being built, which is correct but means every bump would otherwise need a
# manual edit somebody eventually forgets. Only concrete versions are touched: placeholders such
# as X.Y.Z, ranges, and a migration guide's "previous version" are all deliberate.
update_documented_version() {
  local version="$1"
  echo "Updating documented install snippets to ${version}"
  if $DRY_RUN; then
    echo "+ update install snippets in docs/ to ${version}"
    return
  fi
  python3 - "$version" <<'PYTHON'
import pathlib, re, sys

version = sys.argv[1]
snippet = re.compile(
    r"(<artifactId>nostr-java-[a-z]+</artifactId>\s*\n\s*<version>)(\d+\.\d+\.\d+)(</version>)")

for document in pathlib.Path("docs").rglob("*.md"):
    text = document.read_text()
    # A version followed by a comment is illustrative: a previous release, or a placeholder the
    # reader is told to replace. Leave those alone.
    updated = snippet.sub(
        lambda m: m.group(0) if "<!--" in text[m.end():m.end() + 40] else m.group(1) + version + m.group(3),
        text)
    if updated != text:
        document.write_text(updated)
        print(f"  {document} -> {version}")
PYTHON
}

cmd_verify() {
  local no_docker=false skip_tests=false
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --no-docker) no_docker=true; shift ;;
      --skip-tests) skip_tests=true; shift ;;
      --dry-run) DRYRUN=true; shift ;;
      *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
    esac
  done
  local mvn_args=(-q)
  if $no_docker; then
    mvn_args+=(-Pno-docker)
  fi
  $skip_tests && mvn_args+=(-DskipTests)
  run_cmd mvn $MVN_SETTINGS_OPTS "${mvn_args[@]}" clean verify
}

cmd_tag() {
  local version="" push=false
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --version) version="$2"; shift 2 ;;
      --push) push=true; shift ;;
      *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
    esac
  done
  [[ -n "$version" ]] || { echo "--version is required" >&2; exit 1; }
  require_clean_tree
  run_cmd git tag -a "v${version}" -m "nostr-java ${version}"
  if $push; then
    run_cmd git push origin "v${version}"
  else
    echo "Tag v${version} created locally. Use --push to push to origin."
  fi
}

cmd_publish() {
  local no_docker=false skip_tests=false repo="central"
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --no-docker) no_docker=true; shift ;;
      --skip-tests) skip_tests=true; shift ;;
      --repo) repo="$2"; shift 2 ;;
      --dry-run) DRYRUN=true; shift ;;
      *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
    esac
  done
  local profile
  case "$repo" in
    central) profile=release-central ;;
    398ja|reposilite) profile=release-398ja ;;
    *) echo "Unknown repo '$repo'. Use 'central' or '398ja'." >&2; exit 1 ;;
  esac
  local mvn_args=(-q -P "$profile" deploy)
  # -Pno-docker is what actually excludes the container-backed tests. The old -DnoDocker=true
  # set a property nothing reads, so --no-docker silently ran them anyway.
  $no_docker && mvn_args=(-q -P "no-docker,$profile" deploy)
  $skip_tests && mvn_args=(-q -DskipTests -P "$profile" deploy)
  if $no_docker && $skip_tests; then mvn_args=(-q -DskipTests -P "no-docker,$profile" deploy); fi
  # For Central publishing, prefer the settings provided by actions/setup-java (no -s override)
  if [[ "$profile" == "release-central" ]]; then
    run_cmd mvn "${mvn_args[@]}"
  else
    run_cmd mvn $MVN_SETTINGS_OPTS "${mvn_args[@]}"
  fi
}

cmd_next_snapshot() {
  local version=""
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --version) version="$2"; shift 2 ;;
      *) echo "Unknown option: $1" >&2; usage; exit 1 ;;
    esac
  done
  [[ -n "$version" ]] || { echo "--version is required (e.g., 1.0.1-SNAPSHOT)" >&2; exit 1; }
  require_clean_tree
  echo "Setting next development version to ${version}"
  run_cmd mvn -q versions:set -DnewVersion="${version}"
  run_cmd mvn -q versions:commit
  run_cmd git add pom.xml */pom.xml || true
  run_cmd git commit -m "chore(release): start ${version}"
}

main() {
  local cmd="${1:-}"; shift || true
  case "$cmd" in
    bump) cmd_bump "$@" ;;
    verify) cmd_verify "$@" ;;
    tag) cmd_tag "$@" ;;
    publish) cmd_publish "$@" ;;
    next-snapshot) cmd_next_snapshot "$@" ;;
    -h|--help|help|"") usage ;;
    *) echo "Unknown command: $cmd" >&2; usage; exit 1 ;;
  esac
}

main "$@"
