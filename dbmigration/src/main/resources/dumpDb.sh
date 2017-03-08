#/bin/bash
# Generate a local psql database from an existing one on the playground by piping the output of pg_dump
# Runtime arguments: username on playground, database name

createdb "$2"
ssh "$1"@playground pg_dump -C "$2" | psql "$2"
