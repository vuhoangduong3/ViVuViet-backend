#!/usr/bin/env bash
set -euo pipefail
curl --fail --silent --max-time 3 http://127.0.0.1:8080/api/v1/areas >/dev/null
