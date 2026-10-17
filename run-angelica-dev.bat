@echo off
setlocal
cd /d "%~dp0"
if not defined MBO_JAVA8_HOME set "MBO_JAVA8_HOME=C:\Program Files\Java\jdk1.8.0_202"
set "JAVA_HOME=%MBO_JAVA8_HOME%"
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo Set MBO_JAVA8_HOME to your JDK 8 directory.
    exit /b 1
)
call gradlew.bat --offline runClient -x getVersionJson -x getVersionJsonIndex -x downloadClient -x downloadServer %*
exit /b %errorlevel%
