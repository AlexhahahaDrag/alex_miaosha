@echo off
rem ========================================================
rem Alex Miaosha Backend Jacoco Coverage & Report Runner
rem ========================================================
setlocal
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-17" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-17"
    )
)
echo [INFO] Running backend tests and Jacoco coverage report generation...
call mvn clean test jacoco:report -pl alex_miaosha_finance/finance_boot -am -DfailIfNoTests=false
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Coverage report generation failed!
    exit /b %ERRORLEVEL%
)
echo [INFO] Coverage report generated successfully at:
echo alex_miaosha_finance\finance_boot\target\site\jacoco\index.html
endlocal
