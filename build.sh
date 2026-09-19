#!/usr/bin/env bash
# Render build script for Spring Boot application
# exit on error
set -o errexit

echo "Starting build process..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven not found. Installing Maven..."
    apt-get update && apt-get install -y maven
fi

echo "Maven version:"
mvn --version

# Clean and build the application (skip tests for faster builds)
echo "Building application..."
mvn clean package -DskipTests

# Verify the JAR was created
JAR_FILE=$(find target -name "*.jar" ! -name "*-sources.jar" ! -name "*-javadoc.jar" -type f)
if [ -z "$JAR_FILE" ]; then
    echo "ERROR: JAR file not found in target directory"
    exit 1
fi

echo "Build complete! JAR file: $JAR_FILE"
echo "File size: $(du -h $JAR_FILE | cut -f1)"
