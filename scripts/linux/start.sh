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

if [ ! -x "$BACKEND_DIR/mvnw" ]; then
  need_command mvn
fi

echo "Building backend..."
cd "$BACKEND_DIR"
if [ -x ./mvnw ]; then
  ./mvnw clean package -DskipTests
else
  mvn clean package -DskipTests
fi

echo "Building frontend..."
cd "$FRONTEND_DIR"
if [ -f package-lock.json ]; then
  npm ci
else
  npm install
fi
npm run build

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
  if [ -n "${FRONTEND_PID:-}" ] && kill -0 "$FRONTEND_PID" >/dev/null 2>&1; then
    kill "$FRONTEND_PID"
  fi
  wait || true
}

trap stop_children INT TERM EXIT

echo "Starting backend: 127.0.0.1:${BACKEND_PORT}"
cd "$ROOT_DIR"
java -jar "$JAR_FILE" --spring.profiles.active="$SPRING_PROFILE" &
BACKEND_PID=$!

echo "Starting frontend preview: 127.0.0.1:${FRONTEND_PORT}"
cd "$FRONTEND_DIR"
npm run preview -- --host 127.0.0.1 --port "$FRONTEND_PORT" &
FRONTEND_PID=$!

wait -n "$BACKEND_PID" "$FRONTEND_PID"
