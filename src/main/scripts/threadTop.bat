@echo off
set d=%~dp0
rem Run ThreadTop using the executable JAR
rem --add-exports and --add-opens flags are needed to allow JDK modules access for the JMX attach API
java --add-exports java.base/jdk.internal.perf=ALL-UNNAMED ^
     --add-exports jdk.management.agent/jdk.internal.agent=ALL-UNNAMED ^
     --add-exports java.management/sun.management=ALL-UNNAMED ^
     --add-exports jdk.attach/sun.tools.attach=ALL-UNNAMED ^
     --add-opens java.base/java.util=ALL-UNNAMED ^
     --add-opens java.management/javax.management.remote=ALL-UNNAMED ^
     --add-opens jdk.management/com.sun.management.internal=ALL-UNNAMED ^
     --add-modules jdk.attach,jdk.management.agent ^
     -Djdk.attach.allowAttachSelf=true ^
     -XX:+IgnoreUnrecognizedVMOptions ^
     -jar "%d%threadTop-1.1.jar" %*