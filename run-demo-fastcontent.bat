@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo Running FastContentParse Demo (FastChunk)...
echo Building module...
call mvn -DskipTests=true package
if ERRORLEVEL 1 (
    echo.
    echo Maven build failed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Running DemoFastContent...
cd examples\DemoFastContent
call mvn compile exec:java
cd ..\..
pause
