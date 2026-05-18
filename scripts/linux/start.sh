#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"

if [ -f "$ROOT_DIR/.env.production" ]; then
  set -a
  # shellcheck source=/dev/null
  . "$ROOT_DIR/.env.production"
  set +a
fi

if [ -z "${DB_HOST:-}" ] || [ -z "${DB_NAME:-}" ] || [ -z "${DB_USERNAME:-}" ] || [ -z "${DB_PASSWORD:-}" ]; then
  echo "Missing database environment variables. Check $ROOT_DIR/.env.production." >&2
  echo "Required: DB_HOST, DB_NAME, DB_USERNAME, DB_PASSWORD." >&2
  exit 1
fi

need_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing command: $1" >&2
    exit 1
  fi
}

need_command java
need_command node
need_command npm
need_command find
need_command sha256sum

if [ ! -x "$BACKEND_DIR/mvnw" ]; then
  need_command mvn
fi

FORCE_BUILD="${FORCE_BUILD:-0}"
FRONTEND_MODE="${FRONTEND_MODE:-static}"
BACKEND_STAMP="$BACKEND_DIR/target/.ledgerflow-build-stamp"
FRONTEND_STAMP="$FRONTEND_DIR/dist/.ledgerflow-build-stamp"
NODE_MODULES_STAMP="$FRONTEND_DIR/node_modules/.ledgerflow-install-stamp"

hash_files() {
  dir="$1"
  shift
  (
    cd "$dir"
    find "$@" -type f -print0 | sort -z | xargs -0 sha256sum | sha256sum | awk '{print $1}'
  )
}

backend_hash() {
  hash_files "$BACKEND_DIR" pom.xml src
}

frontend_hash() {
  if [ -f "$FRONTEND_DIR/package-lock.json" ]; then
    hash_files "$FRONTEND_DIR" package.json package-lock.json index.html vite.config.js src
  else
    hash_files "$FRONTEND_DIR" package.json index.html vite.config.js src
  fi
}

should_build_backend() {
  if [ "$FORCE_BUILD" = "1" ]; then
    return 0
  fi
  if ! find "$BACKEND_DIR/target" -maxdepth 1 -name '*.jar' ! -name '*sources.jar' ! -name '*javadoc.jar' | grep -q .; then
    return 0
  fi
  current_hash="$(backend_hash)"
  if [ ! -f "$BACKEND_STAMP" ] || [ "$(cat "$BACKEND_STAMP")" != "$current_hash" ]; then
    return 0
  fi
  return 1
}

should_build_frontend() {
  if [ "$FORCE_BUILD" = "1" ]; then
    return 0
  fi
  if [ ! -f "$FRONTEND_DIR/dist/index.html" ]; then
    return 0
  fi
  current_hash="$(frontend_hash)"
  if [ ! -f "$FRONTEND_STAMP" ] || [ "$(cat "$FRONTEND_STAMP")" != "$current_hash" ]; then
    return 0
  fi
  return 1
}

install_frontend_dependencies() {
  cd "$FRONTEND_DIR"
  if [ -d node_modules ] && [ -f "$NODE_MODULES_STAMP" ] && [ package-lock.json -ot "$NODE_MODULES_STAMP" ]; then
    echo "Frontend dependencies are already installed."
    return
  fi

  echo "Installing frontend dependencies..."
  if [ -f package-lock.json ]; then
    npm ci --prefer-offline --no-audit --no-fund
  else
    npm install --prefer-offline --no-audit --no-fund
  fi
  touch "$NODE_MODULES_STAMP"
}

if should_build_backend; then
  echo "Building backend..."
  export MAVEN_OPTS="${MAVEN_OPTS:--Xmx384m -XX:MaxMetaspaceSize=192m}"
  current_backend_hash="$(backend_hash)"
  cd "$BACKEND_DIR"
  if [ -x ./mvnw ]; then
    ./mvnw package -DskipTests
  else
    mvn package -DskipTests
  fi
  mkdir -p "$(dirname "$BACKEND_STAMP")"
  printf '%s\n' "$current_backend_hash" > "$BACKEND_STAMP"
else
  echo "Backend build is up to date."
fi

if should_build_frontend; then
  current_frontend_hash="$(frontend_hash)"
  install_frontend_dependencies
  echo "Building frontend..."
  export NODE_OPTIONS="${NODE_OPTIONS:---max-old-space-size=512}"
  cd "$FRONTEND_DIR"
  npm run build
  mkdir -p "$(dirname "$FRONTEND_STAMP")"
  printf '%s\n' "$current_frontend_hash" > "$FRONTEND_STAMP"
else
  echo "Frontend build is up to date."
fi

cd "$BACKEND_DIR"

JAR_FILE="$(find "$BACKEND_DIR/target" -maxdepth 1 -name '*.jar' ! -name '*sources.jar' ! -name '*javadoc.jar' | head -n 1)"
if [ -z "$JAR_FILE" ]; then
  echo "Backend JAR file not found." >&2
  exit 1
fi

BACKEND_PORT="${SERVER_PORT:-8080}"
FRONTEND_PORT="${FRONTEND_PORT:-4173}"
SPRING_PROFILE="${SPRING_PROFILES_ACTIVE:-prod}"

stop_children() {
  echo "Stopping services..."
  if [ -n "${BACKEND_PID:-}" ] && kill -0 "$BACKEND_PID" >/dev/null 2>&1; then
    kill "$BACKEND_PID"
  fi
  if [ "${FRONTEND_MODE}" = "preview" ] && [ -n "${FRONTEND_PID:-}" ] && kill -0 "$FRONTEND_PID" >/dev/null 2>&1; then
    kill "$FRONTEND_PID"
  fi
  wait || true
}

trap stop_children INT TERM EXIT

echo "Starting backend: 127.0.0.1:${BACKEND_PORT}"
cd "$ROOT_DIR"
java -jar "$JAR_FILE" --spring.profiles.active="$SPRING_PROFILE" &
BACKEND_PID=$!

if [ "$FRONTEND_MODE" = "preview" ]; then
  echo "Starting frontend preview: 127.0.0.1:${FRONTEND_PORT}"
  cd "$FRONTEND_DIR"
  npm run preview -- --host 127.0.0.1 --port "$FRONTEND_PORT" &
  FRONTEND_PID=$!

  wait -n "$BACKEND_PID" "$FRONTEND_PID"
else
  echo "Frontend static files are ready: $FRONTEND_DIR/dist"
  echo "Use nginx to serve the dist directory. Set FRONTEND_MODE=preview to run Vite preview."
  wait "$BACKEND_PID"
fi
