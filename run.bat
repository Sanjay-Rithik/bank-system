@echo off
echo Building NexBank...

if not exist out mkdir out
if not exist frontend mkdir frontend
if not exist frontend\index.html copy /Y index.html frontend\index.html >nul

javac -d out *.java
if %errorlevel% neq 0 (
    echo Build failed.
    pause
    exit /b 1
)

echo Build successful! Starting server...
echo Open http://localhost:8081 in your browser.

REM Kill any process already using port 8081
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8081 "') do (
    taskkill /PID %%a /F >nul 2>&1
)

java -cp out bank.Server
pause
