#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
if [ -e .env ]; then echo ".env already exists; keeping your secrets." >&2; exit 1; fi
umask 077
{
  printf 'ITEMS_DB_PASSWORD=%s\n' "$(openssl rand -hex 32)"
  printf 'ACCOUNTS_DB_PASSWORD=%s\n' "$(openssl rand -hex 32)"
  printf 'CSS_JWT_SECRET=%s\n' "$(openssl rand -hex 32)"
  printf 'CSS_CUSTODIAN_INVITE=%s\n' "$(openssl rand -hex 32)"
  printf 'APP_PORT=8080\nCAMPUS_DOMAIN=localhost\n'
} > .env
echo "Created .env. Keep it private and set CAMPUS_DOMAIN before public deployment."
