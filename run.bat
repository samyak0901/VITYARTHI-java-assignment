@echo off
title MEDICARE - Medical Shop Inventory Management System
echo ========================================================
echo   Starting MEDICARE Pharmacy Management System
echo ========================================================
echo Compiling source files...
javac -d bin -sourcepath src src/Main.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %errorlevel%
)
echo Launching GUI...
start javaw -cp bin Main
echo MEDICARE is running!
