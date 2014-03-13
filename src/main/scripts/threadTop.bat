@echo off
set d=%~dp0
java -cp "%JAVA_HOME%/lib/tools.jar";%d%/threadTop-1.1.jar com.performizeit.threadtop.Main %*
