#!/usr/bin/env bash
# The app name is provisional: as user-visible text it may only be written in two places
#   - app/src/main/res/values/strings.xml  (app_name)
#   - core/domain/.../AppInfo.kt           (AppInfo.NAME)
# Docs (*.md) and licence files may mention it. Code identifiers such as ResiliaTheme
# are fine: only literals ("Resilia" or >Resilia<) are checked.
set -euo pipefail

NAME="Resilia"
PATTERN="\"${NAME}\"|>${NAME}<"
ALLOWED='^(app/src/main/res/values/strings\.xml|core/domain/src/main/kotlin/com/goncalogoncalves/resilia/core/domain/AppInfo\.kt)$'

hits=$(git ls-files --cached --others --exclude-standard -- '*.kt' '*.kts' '*.xml' '*.json' \
  | grep -v '^build-logic/' \
  | xargs grep -lE "$PATTERN" 2>/dev/null \
  | grep -Ev "$ALLOWED" || true)

if [ -n "$hits" ]; then
  echo "The app name '$NAME' is hard-coded outside the allowed places:"
  echo "$hits"
  echo "Use @string/app_name or AppInfo.NAME instead."
  exit 1
fi
echo "App name check OK"
