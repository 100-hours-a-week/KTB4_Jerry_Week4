#!/usr/bin/env bash

set -euo pipefail
cd "$(dirname "$0")"

TAG="${1:?Usage: ./deploy-backend.sh <image-tag>}"
POINTER="nginx/active/backend.conf"
ENV_FILE=".env"

if grep -q backend-blue "$POINTER" 2>/dev/null; then
  ACTIVE=blue; IDLE=green
else
  ACTIVE=green; IDLE=blue
fi
IDLE_UPPER=$(printf '%s' "$IDLE" | tr '[:lower:]' '[:upper:]')
echo "▶ 현재 활성=$ACTIVE / 배포 대상=$IDLE (tag=$TAG)"

if grep -q "^${IDLE_UPPER}_TAG=" "$ENV_FILE"; then
  sed -i "s|^${IDLE_UPPER}_TAG=.*|${IDLE_UPPER}_TAG=${TAG}|" "$ENV_FILE"
else
  echo "${IDLE_UPPER}_TAG=${TAG}" >> "$ENV_FILE"
fi

docker compose up -d redis
docker compose pull "backend-${IDLE}"
docker compose up -d --no-deps "backend-$IDLE"

echo "⏳ backend-$IDLE 헬스체크 대기..."
STATUS=starting
for _ in $(seq 1 60); do
  STATUS=$(docker inspect -f '{{.State.Health.Status}}' "backend-$IDLE" 2>/dev/null || echo starting)
  [ "$STATUS" = healthy ] && break
  sleep 5
done
if [ "$STATUS" != healthy ]; then
  echo "x backend-$IDLE $STATUS. 전환 취소, 기존 $ACTIVE 유지."
  docker compose stop "backend-$IDLE" || true
  exit 1
fi

printf 'set $backend_host backend-%s;\n' "$IDLE" > "$POINTER"
docker compose exec -T nginx nginx -s reload
echo "✔ 트래픽을 backend-$IDLE 로 전환"

sleep 5
docker compose stop "backend-$ACTIVE"
echo "✔ backend-$ACTIVE 정지. 배포 완료."
