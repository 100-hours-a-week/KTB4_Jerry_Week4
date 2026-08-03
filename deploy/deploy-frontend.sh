#!/usr/bin/env bash

set -euo pipefail
cd "$(dirname "$0")"

TAG="${1:?Usage: ./deploy-frontend.sh <image-tag>}"
IMAGE="jerryhy/talktalk-frontend:${TAG}"
FE_ROOT="./fe-data"
RELEASES="${FE_ROOT}/releases"

echo "▶ 프론트 배포 ${TAG}"
docker pull "$IMAGE"

mkdir -p "${RELEASES}/${TAG}"
CID=$(docker create "$IMAGE")
docker cp "${CID}:/usr/share/nginx/html/." "${RELEASES}/${TAG}/"
docker rm "$CID" >/dev/null

ln -sfn "releases/${TAG}" "${FE_ROOT}/current"
docker compose exec -T frontend nginx -s reload
echo "✔ 프론트가 ${TAG} 서빙 중"

ls -1dt "${RELEASES}"/*/ 2>/dev/null | tail -n +4 | xargs -r rm -rf || true
echo "✔ 완료."