#!/usr/bin/env bash
set -euo pipefail

APP_BUILD_GRADLE="${APP_BUILD_GRADLE:-app/build.gradle}"
TAG_PREFIX="${TAG_PREFIX:-v}"

if [ -n "${GITHUB_REF:-}" ] && [[ "$GITHUB_REF" != "refs/heads/master" && "$GITHUB_REF" != "refs/heads/main" ]]; then
  echo "Not on master/main (${GITHUB_REF}); skipping tag creation."
  exit 0
fi

if [ ! -f "$APP_BUILD_GRADLE" ]; then
  echo "Missing ${APP_BUILD_GRADLE}." >&2
  exit 1
fi

version_name="$(
  sed -nE "s/^[[:space:]]*versionName[[:space:]]+'([^']+)'[[:space:]]*$/\1/p" "$APP_BUILD_GRADLE" | head -1
)"
if [ -z "$version_name" ]; then
  version_name="$(
    sed -nE 's/^[[:space:]]*versionName[[:space:]]+"([^"]+)"[[:space:]]*$/\1/p' "$APP_BUILD_GRADLE" | head -1
  )"
fi
version_code="$(
  sed -nE 's/^[[:space:]]*versionCode[[:space:]]+([0-9]+)[[:space:]]*$/\1/p' "$APP_BUILD_GRADLE" | head -1
)"

if ! [[ "$version_name" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Invalid versionName '${version_name}'. Expected a regular X.Y.Z version." >&2
  exit 1
fi
if ! [[ "$version_code" =~ ^[0-9]+$ ]] || [ "$version_code" -le 0 ]; then
  echo "Invalid versionCode '${version_code}'." >&2
  exit 1
fi

tag="${TAG_PREFIX}${version_name}"
echo "Detected versionName=${version_name}, versionCode=${version_code}, tag=${tag}"

extract_version_code() {
  local ref="$1"
  git show "${ref}:${APP_BUILD_GRADLE}" 2>/dev/null \
    | sed -nE 's/^[[:space:]]*versionCode[[:space:]]+([0-9]+)[[:space:]]*$/\1/p' \
    | head -1
}

if git rev-parse -q --verify "refs/tags/${tag}" >/dev/null; then
  existing_code="$(extract_version_code "$tag")"
  if [ -z "$existing_code" ]; then
    echo "Tag ${tag} exists but its versionCode could not be read." >&2
    exit 1
  fi
  if [ "$existing_code" -eq "$version_code" ]; then
    echo "Tag ${tag} already exists for versionCode ${version_code}; nothing to do."
    exit 0
  fi
  echo "Tag ${tag} already exists with versionCode ${existing_code}, but the current versionCode is ${version_code}." >&2
  echo "versionName must be increased when versionCode changes; refusing to move or overwrite an existing tag." >&2
  exit 1
fi

last_version_code=0
last_version_tag=""
while IFS= read -r existing_tag; do
  existing_code="$(extract_version_code "$existing_tag")"
  if [ -z "$existing_code" ]; then
    echo "Skipping ${existing_tag}: no versionCode found." >&2
    continue
  fi
  if [ "$existing_code" -gt "$last_version_code" ]; then
    last_version_code="$existing_code"
    last_version_tag="$existing_tag"
  fi
done < <(
  git tag --list "${TAG_PREFIX}[0-9]*" \
    | grep -E "^${TAG_PREFIX}[0-9]+\.[0-9]+\.[0-9]+$" \
    || true
)

if [ "$version_code" -le "$last_version_code" ]; then
  echo "versionCode ${version_code} is not greater than the last release versionCode ${last_version_code} (${last_version_tag:-none})." >&2
  echo "Increase versionCode for every release." >&2
  exit 1
fi

if ! git config user.name >/dev/null; then
  git config user.name "github-actions[bot]"
fi
if ! git config user.email >/dev/null; then
  git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
fi

git tag -a "$tag" -m "F-Droid release ${version_name} (versionCode ${version_code})"

if [ "${DRY_RUN:-false}" = "true" ]; then
  echo "DRY_RUN=true; created local tag ${tag} without pushing."
  exit 0
fi

git push origin "refs/tags/${tag}"
echo "Created and pushed tag ${tag} on $(git rev-parse HEAD)."
