#!/usr/bin/env bash
set -euo pipefail
config_dir=/var/lib/vivuviet/config
export PGPASSWORD
PGPASSWORD=$(cat "$config_dir/db-password")
psql -h 127.0.0.1 -U "$(cat "$config_dir/db-user")" \
    -d "$(cat "$config_dir/db-name")" -c 'SELECT 1' >/dev/null
curl --fail --silent --max-time 3 http://127.0.0.1:8080/api/v1/areas >/dev/null
