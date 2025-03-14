#!/bin/bash
pg_restore -U postgres -d journeyhub --verbose /docker-entrypoint-initdb.d/journeyhub_dump.backup