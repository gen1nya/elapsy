#!/usr/bin/env bash
# Syncs the project to the build Mac and runs a command there, in the project directory.
#   scripts/mac.sh                      # sync only
#   scripts/mac.sh ./gradlew :core:tasks
# The Mac's user@host lives in scripts/mac.local (not committed), or in MAC_HOST.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
[ -f "$ROOT/scripts/mac.local" ] && . "$ROOT/scripts/mac.local"
: "${MAC_HOST:?set MAC_HOST=user@host, or put MAC_HOST=user@host into scripts/mac.local}"
REMOTE_DIR="${MAC_REMOTE_DIR:-elapsy}"

rsync -az --delete \
  --exclude '.git/' --exclude '.gradle/' --exclude '.kotlin/' --exclude '.idea/' \
  --exclude 'build/' --exclude 'local.properties' --exclude 'docs/' \
  --exclude '*.xcodeproj/' --exclude 'DerivedData/' --exclude 'xcuserdata/' \
  "$ROOT/" "$MAC_HOST:$REMOTE_DIR/"

if [ "$#" -gt 0 ]; then
  # caffeinate keeps the laptop awake for the duration of the command.
  ssh -o BatchMode=yes "$MAC_HOST" "cd '$REMOTE_DIR' && export JAVA_HOME=\$(/usr/libexec/java_home -v 21) ANDROID_HOME=\$HOME/Library/Android/sdk PATH=/opt/homebrew/bin:\$PATH && caffeinate -i $(printf '%q ' "$@")"
fi
