#!/bin/sh
set -e

CONFIG_DIR=/app/config

if [ ! -f "$CONFIG_DIR/application.yml" ]; then
    mkdir -p "$CONFIG_DIR"
    cp /app/application.yml.default "$CONFIG_DIR/application.yml"
fi

exec java -Xms256m -Xmx512m -jar /app/app.jar
