#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
export SPRING_PROFILES_ACTIVE=postgres
export DB_URL="${DB_URL:-jdbc:postgresql://localhost:5432/xyz_auto_service}"
export DB_USER="${DB_USER:-postgres}"
export DB_PASSWORD="${DB_PASSWORD:-postgres}"
sh ./mvnw spring-boot:run
