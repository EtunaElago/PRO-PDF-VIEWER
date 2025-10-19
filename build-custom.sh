#!/bin/bash
cd /data/data/com.termux/files/home/PRO-Pdf-Viewer

# Use Java directly with Gradle, skipping the problematic agent
/data/data/com.termux/files/usr/lib/jvm/java-21-openjdk/bin/java \
    -cp /data/data/com.termux/files/home/.gradle/wrapper/dists/gradle-8.13-bin/5xuhj0ry160q40clulazy9h7d/gradle-8.13/lib/gradle-launcher-8.13.jar \
    org.gradle.launcher.GradleMain \
    --no-daemon \
    --max-workers=1 \
    "$@"
