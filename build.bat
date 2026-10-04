@echo off
echo ============================================
echo ExamForge - Build Script
echo ============================================
echo.

set SRC_DIR=src
set LIB_DIR=lib
set OUT_DIR=out

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"
if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"

echo Checking for MySQL JDBC Driver...
if not exist "%LIB_DIR%\mysql-connector-j-*.jar" (
    echo.
    echo WARNING: MySQL JDBC Driver not found in lib/ directory.
    echo Please download mysql-connector-j from:
    echo https://dev.mysql.com/downloads/connector/j/
    echo and place the .jar file in the lib/ directory.
    echo.
    echo Attempting to compile without it (may fail at runtime)...
    echo.
)

echo Compiling Java sources...
dir /s /b "%SRC_DIR%\com\examforge\*.java" > sources.txt
javac -d "%OUT_DIR%" -sourcepath "%SRC_DIR%" @sources.txt 2>&1

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo BUILD FAILED - Compilation errors detected.
    echo Please check the errors above.
    pause
    exit /b 1
)

echo.
echo BUILD SUCCESSFUL!
echo.
echo To run the application:
echo   java -cp "%OUT_DIR%;%LIB_DIR%\*" com.examforge.main.ExamForgeApp
echo.
echo Before running:
echo   1. Set up MySQL and run sql/schema.sql
echo   2. Update database credentials in config/DatabaseConfig.java
echo   3. Place mysql-connector-j-*.jar in lib/ folder
echo.
pause
