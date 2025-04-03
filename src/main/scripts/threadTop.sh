#!/bin/sh
# Run ThreadTop using the executable JAR
# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
# If BASH_SOURCE is empty (e.g., in standard sh), fall back to another approach
if [ -z "$SCRIPT_DIR" ]; then
    SCRIPT_DIR="$( cd "$( dirname "$0" )" &> /dev/null && pwd )"
fi

# --add-exports and --add-opens flags are needed to allow JDK modules access for the JMX attach API
java --add-exports java.base/jdk.internal.perf=ALL-UNNAMED \
     --add-exports jdk.management.agent/jdk.internal.agent=ALL-UNNAMED \
     --add-exports java.management/sun.management=ALL-UNNAMED \
     --add-exports jdk.attach/sun.tools.attach=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     --add-opens java.management/javax.management.remote=ALL-UNNAMED \
     --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED \
     --add-modules jdk.attach,jdk.management.agent \
     -Djdk.attach.allowAttachSelf=true \
     -XX:+IgnoreUnrecognizedVMOptions \
     -jar "${SCRIPT_DIR}/threadTop-1.1.jar" "$@"