#!/usr/bin/env bash
# Syncs the project to the build Mac and runs a command there, in the project directory.
#   scripts/mac.sh                      # sync only
#   scripts/mac.sh ./gradlew :core:tasks
# The Mac's address comes from DHCP; override with MAC_HOST when it changes.
set -euo pipefail

MAC_HOST="${MAC_HOST:-evgenijandrianov@192.168.129.190}"
REMOTE_DIR="${MAC_REMOTE_DIR:-elapsy}"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

rsync -az --delete \
  --exclude '.git/' --exclude '.gradle/' --exclude '.kotlin/' --exclude '.idea/' \
  --exclude 'build/' --exclude 'local.properties' --exclude 'docs/' \
  --exclude '*.xcodeproj/' --exclude 'DerivedData/' --exclude 'xcuserdata/' \
  "$ROOT/" "$MAC_HOST:$REMOTE_DIR/"

if [ "$#" -gt 0 ]; then
  # caffeinate keeps the laptop awake for the duration of the command.
  ssh -o BatchMode=yes "$MAC_HOST" "cd '$REMOTE_DIR' && export JAVA_HOME=\$(/usr/libexec/java_home -v 21) ANDROID_HOME=\$HOME/Library/Android/sdk PATH=/opt/homebrew/bin:\$PATH && caffeinate -i $(printf '%q ' "$@")"
fi
