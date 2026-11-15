#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
SPRING_PROFILES_ACTIVE=dev sh ./mvnw spring-boot:run
