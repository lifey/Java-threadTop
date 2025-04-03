@echo off
set d=%~dp0
rem Run ThreadTop using the executable JAR
java -jar %d%\threadTop-1.1.jar %*