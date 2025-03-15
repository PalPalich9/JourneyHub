#!/bin/bash
echo "Waiting for database initialization to complete..."
until psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT 1 FROM pg_trigger WHERE tgname = 'tr_update_route_travel_duration';" > /dev/null 2>&1; do
  echo "Database is not fully initialized yet, waiting..."
  sleep 2
done
echo "Database is fully initialized!"