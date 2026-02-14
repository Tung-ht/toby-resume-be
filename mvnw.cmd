@REM Maven Wrapper script for Windows
@echo off
setlocal
set "MAVEN_PROJECTBASEDIR=%~dp0"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set "MAVEN_OPTS=%MAVEN_OPTS% -Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%"

if not exist "%WRAPPER_JAR%" (
  echo Error: Could not find maven-wrapper.jar
  exit /b 1
)

java %MAVEN_OPTS% -jar "%WRAPPER_JAR%" %*
endlocal
