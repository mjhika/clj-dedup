#!/bin/bash

check_commands() {
  for cmd in "$@"; do
    if ! command -v "$cmd" &> /dev/null; then
      echo "Error: $cmd is not installed or not in PATH"
      return 1
    fi
  done
  return 0
}

if ! check_commands java clj native-image; then
  echo "Please install necessary commands."
  exit 1
fi

mkdir target &> /dev/null

clj -T:build uber | tee -ai target/build-logs.txt

native-image \
  -march=native \
  --no-server \
  --no-fallback \
  --features=clj_easy.graal_build_time.InitClojureClasses \
  -o target/$(basename $(pwd)) \
  -jar target/$(basename $(pwd))-0.0.$(git rev-list HEAD --count)-standalone.jar \
  | tee -ai target/build-logs.txt

echo -e "Do not forget to add $(basename $(pwd)) to your PATH"
