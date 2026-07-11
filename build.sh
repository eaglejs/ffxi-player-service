#!/usr/bin/env bash
# build.sh — bump versions, run tests, build and publish both app and service.
# Usage: ./build.sh -v <version>   e.g. ./build.sh -v 0.5.1
# Optional --tag flag will create a git tag for the new version and push it to origin.
# Usage: ./build.sh -v <version> --tag  e.g. ./build.sh -v 0.5.1 --tag
# Optional -e/--environment flag sets the target environment (default: prod).
# Usage: ./build.sh -v <version> -e test  — runs tests and builds for test; deploys to test server; skips Nexus publish and git steps.
#
# Project-specific configuration is loaded from build.conf in the same directory.
# Copy build.conf.example to build.conf and edit it for your project.

set -euo pipefail

# ── Helpers ──────────────────────────────────────────────────────────────────

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # no colour

info()    { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*" >&2; }
die()     { error "$*"; exit 1; }

_BUILD_SUCCESS=false

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ── Load project config ───────────────────────────────────────────────────────
# build.conf is sourced here so it can override any of the defaults below.

CONFIG_FILE="$SCRIPT_DIR/build.conf"
[[ -f "$CONFIG_FILE" ]] && source "$CONFIG_FILE" \
  || warn "No build.conf found — using built-in defaults. Copy build.conf.example to build.conf to configure."

# ── Configuration defaults ────────────────────────────────────────────────────
# All of these can be overridden in build.conf.

# Directory layout
APP_DIR="${APP_DIR:-$SCRIPT_DIR/app}"
SERVICE_DIR="${SERVICE_DIR:-$SCRIPT_DIR/service}"

# Frontend build script names (must match scripts in package.json)
NPM_BUILD_PROD_SCRIPT="${NPM_BUILD_PROD_SCRIPT:-build:prod}"
NPM_BUILD_TEST_SCRIPT="${NPM_BUILD_TEST_SCRIPT:-build:test}"

# npm registry for publishing (leave empty to skip publish)
NPM_REGISTRY="${NPM_REGISTRY:-}"

# Gradle: JAR base name used in build/libs/<JAR_BASE_NAME>-<version>.jar
# Leave empty to derive from the 'archivesBaseName' or 'rootProject.name' in build.gradle.
SERVICE_JAR_BASE_NAME="${SERVICE_JAR_BASE_NAME:-}"

# Gradle extra flags to pass when building for the test environment
GRADLE_TEST_BUILD_FLAGS="${GRADLE_TEST_BUILD_FLAGS:--PuseLocalUI}"

# Deployment — test environment
TEST_SERVER_HOST="${TEST_SERVER_HOST:-}"
TEST_SERVER_USER="${TEST_SERVER_USER:-ec2-user}"
TEST_SERVER_SSH_KEY="${TEST_SERVER_SSH_KEY:-}"
TEST_SERVER_REMOTE_PATH="${TEST_SERVER_REMOTE_PATH:-./}"

# Deployment — production environment
PROD_SERVER_HOST="${PROD_SERVER_HOST:-}"
PROD_SERVER_USER="${PROD_SERVER_USER:-ec2-user}"
PROD_SERVER_SSH_KEY="${PROD_SERVER_SSH_KEY:-}"
PROD_SERVER_REMOTE_PATH="${PROD_SERVER_REMOTE_PATH:-./}"

# Git
GIT_MAIN_BRANCH="${GIT_MAIN_BRANCH:-main}"

# ── Argument parsing ──────────────────────────────────────────────────────────

VERSION=""
TAG=false
ENVIRONMENT="prod"

while [[ $# -gt 0 ]]; do
  case "$1" in
    -v|--version)
      VERSION="$2"
      shift 2
      ;;
    -t|--tag)
      TAG=true
      shift
      ;;
    -e|--environment)
      ENVIRONMENT="$2"
      shift 2
      ;;
    *)
      die "Unknown argument: $1\nUsage: $0 -v <version> [-e <environment>] [--tag]"
      ;;
  esac
done

[[ -z "$VERSION" ]] && die "Version is required.\nUsage: $0 -v <version> [-e <environment>] [--tag]"

if [[ "$ENVIRONMENT" != "prod" && "$ENVIRONMENT" != "test" && "$ENVIRONMENT" != "dev" ]]; then
  die "Invalid environment: '$ENVIRONMENT'. Expected 'dev', 'test' or 'prod'."
fi

# Basic semver check (e.g. 1.2.3 or 1.2.3-SNAPSHOT)
if ! [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9._-]+)?$ ]]; then
  die "Invalid version format: '$VERSION'. Expected semver, e.g. 1.2.3"
fi

PACKAGE_JSON="$APP_DIR/package.json"
BUILD_GRADLE="$SERVICE_DIR/build.gradle"

# ── Verify required files exist ───────────────────────────────────────────────

[[ -f "$PACKAGE_JSON" ]]  || die "package.json not found at $PACKAGE_JSON"
[[ -f "$BUILD_GRADLE" ]]  || die "build.gradle not found at $BUILD_GRADLE"

# ── Derive names from project files if not set in config ─────────────────────

# npm tarball: npm pack produces <name>-<version>.tgz (@ and / stripped from scope)
NPM_PKG_NAME=$(grep '"name"' "$PACKAGE_JSON" | head -1 | sed 's/.*"name": *"\([^"]*\)".*/\1/' | sed 's|^@[^/]*/||' | tr '/' '-')
TARBALL="${NPM_PKG_NAME}-${VERSION}.tgz"

# Gradle JAR: derive from archivesBaseName or rootProject.name if not set in config
if [[ -z "$SERVICE_JAR_BASE_NAME" ]]; then
  SERVICE_JAR_BASE_NAME=$(grep -E "^(archivesBaseName|rootProject\.name)\s*=" "$BUILD_GRADLE" "$SCRIPT_DIR/settings.gradle" 2>/dev/null \
    | head -1 | sed "s/.*= *['\"]\\([^'\"]*\\)['\"].*/\\1/") \
    || true
  [[ -z "$SERVICE_JAR_BASE_NAME" ]] && die \
    "Cannot derive SERVICE_JAR_BASE_NAME. Set it in build.conf or add archivesBaseName to build.gradle."
fi
SERVICE_JAR="${SERVICE_JAR_BASE_NAME}-${VERSION}.jar"

# ── Capture originals for rollback ───────────────────────────────────────────

ORIG_PKG_VERSION=$(grep '"version"' "$PACKAGE_JSON" | head -1 | sed 's/.*"version": *"\([^"]*\)".*/\1/')
ORIG_GRADLE_VERSION=$(grep "^version" "$BUILD_GRADLE" | sed "s/version *= *'\([^']*\)'.*/\1/")

cleanup() {
  if [[ "$_BUILD_SUCCESS" != true ]]; then
    warn "Build failed — reverting version files to $ORIG_PKG_VERSION / $ORIG_GRADLE_VERSION..."
    sed "s/\"version\": \"${VERSION}\"/\"version\": \"${ORIG_PKG_VERSION}\"/" "$PACKAGE_JSON" > "$PACKAGE_JSON.tmp" && mv "$PACKAGE_JSON.tmp" "$PACKAGE_JSON"
    sed "s/^version *= *'${VERSION}'/version      = '${ORIG_GRADLE_VERSION}'/" "$BUILD_GRADLE" > "$BUILD_GRADLE.tmp" && mv "$BUILD_GRADLE.tmp" "$BUILD_GRADLE"
    warn "Version files reverted."
  fi
}

trap 'cleanup' EXIT

# ── Step 1: Bump versions ─────────────────────────────────────────────────────

info "Bumping app/package.json to $VERSION"
# Use a temp file to avoid in-place sed portability issues on macOS
CURRENT_PKG_VERSION="$ORIG_PKG_VERSION"
sed "s/\"version\": \"${CURRENT_PKG_VERSION}\"/\"version\": \"${VERSION}\"/" "$PACKAGE_JSON" > "$PACKAGE_JSON.tmp"
mv "$PACKAGE_JSON.tmp" "$PACKAGE_JSON"
info "  package.json: $CURRENT_PKG_VERSION → $VERSION"

info "Bumping service/build.gradle to $VERSION"
CURRENT_GRADLE_VERSION="$ORIG_GRADLE_VERSION"
sed "s/^version *= *'${CURRENT_GRADLE_VERSION}'/version      = '${VERSION}'/" "$BUILD_GRADLE" > "$BUILD_GRADLE.tmp"
mv "$BUILD_GRADLE.tmp" "$BUILD_GRADLE"
info "  build.gradle: $CURRENT_GRADLE_VERSION → $VERSION"

# ── Step 2: Frontend unit tests ───────────────────────────────────────────────

info "Running frontend unit tests..."
cd "$APP_DIR"
npm run test:unit:ci || die "Frontend unit tests failed. Aborting."
info "Frontend unit tests passed."

# ── Step 3: Build and pack the npm package ────────────────────────────────────

info "Building frontend..."
if [[ "$ENVIRONMENT" == "test" ]]; then
  npm run "$NPM_BUILD_TEST_SCRIPT" || die "npm run $NPM_BUILD_TEST_SCRIPT failed. Aborting."
else
  npm run "$NPM_BUILD_PROD_SCRIPT" || die "npm run $NPM_BUILD_PROD_SCRIPT failed. Aborting."
fi

info "Removing any existing tarballs..."
rm -f "$APP_DIR"/${NPM_PKG_NAME}-*.tgz

info "Packing npm package..."
npm pack || die "npm pack failed. Aborting."

[[ -f "$APP_DIR/$TARBALL" ]] || die "Expected tarball not found: $TARBALL"

# ── Step 4: Publish npm package ───────────────────────────────────────────────

if [[ "$ENVIRONMENT" == "test" ]]; then
  info "Test environment — skipping npm publish."
elif [[ -z "$NPM_REGISTRY" ]]; then
  warn "NPM_REGISTRY not set — skipping npm publish."
else
  info "Publishing npm package to $NPM_REGISTRY..."
  npm publish \
    --registry "$NPM_REGISTRY" \
    --tag latest \
    "$TARBALL" \
    || die "npm publish failed. Aborting."
  info "npm package published: $TARBALL"
fi

# ── Step 5: Service tests ─────────────────────────────────────────────────────

cd "$SERVICE_DIR"

info "Running service tests..."
./gradlew clean test || die "Gradle tests failed. Aborting."
info "Service tests passed."

# ── Step 6: Build shadow JAR ──────────────────────────────────────────────────

if [[ "$ENVIRONMENT" == "test" ]]; then
  info "Building shadow JAR (test — using local UI)..."
  # shellcheck disable=SC2086
  ./gradlew clean shadowJar $GRADLE_TEST_BUILD_FLAGS || die "Gradle shadowJar build failed. Aborting."
else
  info "Building shadow JAR..."
  ./gradlew clean shadowJar || die "Gradle shadowJar build failed. Aborting."
fi
info "Shadow JAR built."

# ── Step 7: Create deployment archive ────────────────────────────────────────

ARCHIVE_ZIP="$SCRIPT_DIR/archive.zip"
info "Creating deployment archive: archive.zip..."
rm -f "$ARCHIVE_ZIP"
zip -j "$ARCHIVE_ZIP" \
  "$SERVICE_DIR/build/libs/$SERVICE_JAR" \
  "$APP_DIR/$TARBALL" \
  || die "Failed to create archive.zip. Aborting."
info "archive.zip created."

# ── Step 8: Publish JAR to Nexus ─────────────────────────────────────────────

if [[ "$ENVIRONMENT" == "test" ]]; then
  info "Test environment — skipping JAR publish."
else
  info "Publishing JAR..."
  ./gradlew publish || die "Gradle publish failed. Aborting."
  info "JAR published."
fi

# ── Step 9: Deploy ────────────────────────────────────────────────────────────

_deploy() {
  local label="$1" host="$2" user="$3" key="$4" remote_path="$5"
  [[ -z "$host" ]] && { warn "$label host not configured (set ${label^^}_SERVER_HOST in build.conf) — skipping deploy."; return; }
  local ssh_opts=()
  [[ -n "$key" ]] && ssh_opts+=(-i "$key")
  info "Pushing to $label server ($host)..."
  rsync -avz -e "ssh ${ssh_opts[*]+"${ssh_opts[*]}"}" \
    "build/libs/$SERVICE_JAR" "${user}@${host}:${remote_path}" \
    || die "Failed to push to $label server. Aborting."
  info "Deployment to $label server complete."
}

if [[ "$ENVIRONMENT" == "test" ]]; then
  _deploy "Test" "$TEST_SERVER_HOST" "$TEST_SERVER_USER" "$TEST_SERVER_SSH_KEY" "$TEST_SERVER_REMOTE_PATH"
  info "Test environment — skipping git steps."
else
  _deploy "Production" "$PROD_SERVER_HOST" "$PROD_SERVER_USER" "$PROD_SERVER_SSH_KEY" "$PROD_SERVER_REMOTE_PATH"

  # ── Step 10: Commit changes to git ────────────────────────────────────────
  info "Committing version bump to git..."
  git add "$PACKAGE_JSON" "$BUILD_GRADLE"
  git commit -m "Bump version to $VERSION"
  git push origin "$GIT_MAIN_BRANCH" || die "Failed to push to git. Aborting."

  # ── Step 11: Git tag and push ─────────────────────────────────────────────
  if [[ "$TAG" == true ]]; then
    info "Tagging git repository with version $VERSION..."
    git tag -a "$VERSION" -m "Release version $VERSION"
    git push origin "$VERSION" || die "Failed to push git tags. Aborting."
    info "Git tag $VERSION pushed."
  else
    warn "Skipping git tag (pass --tag to tag this release)."
  fi
fi

_BUILD_SUCCESS=true  # build succeeded — disarm the revert trap

# ── Done ──────────────────────────────────────────────────────────────────────

echo ""
info "Build complete. Version $VERSION published successfully."
