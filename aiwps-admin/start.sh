#!/bin/bash
set -e
APP_DIR="/home/openclaw/ClawTeam-OpenClaw/projects/AIWPS/aiwps-admin"
JAR_FILE="$APP_DIR/target/aiwps-admin-1.0.0.jar"
LOG_FILE="$APP_DIR/aiwps.log"
cd "$APP_DIR"
echo "Stopping old java process if exists..."
pkill -f "aiwps-admin" || true
echo "Starting new java process in background..."
nohup java -Xmx512m -jar "$JAR_FILE" --spring.profiles.active=prod > "$LOG_FILE" 2>&1 &
echo "App started. PID: $(pgrep -f "aiwps-admin" || echo 'unknown')"
exit 0
