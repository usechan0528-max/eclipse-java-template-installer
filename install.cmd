@echo off
setlocal
cd /d "%~dp0"
echo Eclipse Java Templates - Windows Installer
echo Requires Java 17 or newer. Close Eclipse before installing.
echo.
set "COURSE_JAVA=java"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "COURSE_JAVA=%JAVA_HOME%\bin\java.exe"
"%COURSE_JAVA%" -version >nul 2>&1
if errorlevel 1 (
  echo Java was not found. Set JAVA_HOME to your JDK 17 folder or add its bin folder to PATH.
  echo You can also run: "C:\path\to\jdk-17\bin\java.exe" -jar "%~dp0installer.jar"
  pause
  exit /b 1
)
"%COURSE_JAVA%" -jar "%~dp0installer.jar" %*
set "COURSE_RESULT=%ERRORLEVEL%"
if not "%COURSE_RESULT%"=="0" echo Installation failed. Check the message above. Java 17 or newer is required.
echo.
pause
exit /b %COURSE_RESULT%
