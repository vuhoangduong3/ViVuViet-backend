#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

config_dir=/var/lib/vivuviet/config
mkdir -p "$config_dir"

# Persist generated credentials alongside the database, outside PGDATA so initdb
# still receives an empty data directory on first boot.
persist_setting() {
    local name="$1" supplied="$2" fallback="$3" path="$config_dir/$1"
    if [[ -f "$path" ]]; then
        local saved
        saved=$(cat "$path")
        if [[ -n "$supplied" && "$supplied" != "$saved" ]]; then
            echo "Setting $name differs from the existing volume. Reuse its original settings." >&2
            exit 1
        fi
        printf '%s' "$saved"
    else
        local value="${supplied:-$fallback}"
        printf '%s' "$value" > "$path"
        printf '%s' "$value"
    fi
}

random_secret() { head -c 48 /dev/urandom | base64 -w 0; }
export POSTGRES_USER
export POSTGRES_DB
export POSTGRES_PASSWORD
export AUTH_JWT_SECRET
POSTGRES_USER=$(persist_setting db-user "${DB_USERNAME:-}" vivuviet)
POSTGRES_DB=$(persist_setting db-name "${DB_NAME:-}" project)
POSTGRES_PASSWORD=$(persist_setting db-password "${DB_PASSWORD:-}" "$(random_secret)")
AUTH_JWT_SECRET=$(persist_setting jwt-secret "${AUTH_JWT_SECRET:-}" "$(random_secret)")
if (( ${#AUTH_JWT_SECRET} < 32 )); then
    echo 'AUTH_JWT_SECRET must contain at least 32 characters.' >&2
    exit 1
fi
export DB_USERNAME="$POSTGRES_USER" DB_PASSWORD="$POSTGRES_PASSWORD"
export SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:5432/$POSTGRES_DB"
export SERVER_PORT=8080

pg_pid=''
app_pid=''
cleanup() {
    local result=$?
    trap - EXIT TERM INT
    if [[ -n "$app_pid" ]]; then
        kill -TERM "$app_pid" 2>/dev/null || true
        wait "$app_pid" 2>/dev/null || true
    fi
    if [[ -n "$pg_pid" ]]; then
        # Fast shutdown rolls back open transactions and flushes database state.
        kill -INT "$pg_pid" 2>/dev/null || true
        wait "$pg_pid" 2>/dev/null || true
    fi
    exit "$result"
}
trap cleanup EXIT
trap 'exit 143' TERM
trap 'exit 130' INT

/usr/local/bin/docker-entrypoint.sh postgres -c listen_addresses=127.0.0.1 &
pg_pid=$!

ready=false
for ((attempt=0; attempt<90; attempt++)); do
    if ! kill -0 "$pg_pid" 2>/dev/null; then
        echo 'PostgreSQL stopped during initialization.' >&2
        exit 1
    fi
    if PGPASSWORD="$POSTGRES_PASSWORD" psql -h 127.0.0.1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c 'SELECT 1' >/dev/null 2>&1; then
        ready=true
        break
    fi
    sleep 1
done
if [[ "$ready" != true ]]; then
    echo 'PostgreSQL did not become ready within 90 seconds.' >&2
    exit 1
fi

gosu app java -jar /app/app.jar &
app_pid=$!

# Stop the whole container if either backend or database exits.
set +e
wait -n "$pg_pid" "$app_pid"
result=$?
set -e
if [[ "$result" == 0 ]]; then result=1; fi
exit "$result"
