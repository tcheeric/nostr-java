#!/usr/bin/env bash
set -euo pipefail

# Thin wrapper to ensure running from repo root works
exec "$(dirname "$0")/scripts/create-roadmap-project.sh" "$@"

