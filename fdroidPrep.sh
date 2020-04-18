#!/bin/bash
find . | grep build.gradle | xargs sed -i '/excludeInFDroid/s/^/\/\//'
echo "android.enableJetifier=true" >> gradle.properties
echo "android.useAndroidX=true" >> gradle.properties
echo "org.gradle.configureondemand=true" >> gradle.properties
echo "org.gradle.jvmargs=-Xmx3g -XX:MaxPermSize=2048m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8" >> gradle.properties
echo "org.gradle.caching=true" >> gradle.properties
echo "org.gradle.daemon=true" >> gradle.properties
echo "org.gradle.parallel=true" >> gradle.properties
