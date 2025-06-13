@echo off
setlocal EnableDelayedExpansion

:: Configuration variables
set "targetdir=target"
set "libdir=lib"
set "mainclass=core.DTNSim"
set "memory=512M"
:: ^ Modify the memory limit as needed, 512 is kinda low for dtnsim lol, but
:: this is here to not let dtnsim to hog resources when you want to multitask

echo [INFO] Starting DTNSim...

:: Validate target directory exists
if NOT EXIST "%targetdir%" (
    echo [ERROR] Target directory '%targetdir%' not found. Did you compile the project?
    echo [INFO] Try running compile.bat first.
    exit /b 1
)

:: Build classpath from all JAR files
echo [INFO] Building classpath...
set "CLASSPATH=%targetdir%"

:: Check if lib directory exists
if NOT EXIST "%libdir%" (
    echo [WARNING] Library directory '%libdir%' not found. Continuing without external libraries.
) else (
    for %%f in (%libdir%\*.jar) do (
        echo !CLASSPATH! | findstr /C:"%%f" >nul || set "CLASSPATH=!CLASSPATH!;%%f"
    )
)

:: Run the application with all parameters passed to this script
echo [INFO] Running with memory limit: %memory%
echo [INFO] Classpath: %CLASSPATH%
echo [INFO] Main class: %mainclass%

java -Xmx%memory% -cp "%CLASSPATH%" %mainclass% %*

:: Check if Java executed successfully
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Execution failed with error code %ERRORLEVEL%.
    exit /b %ERRORLEVEL%
) else (
    echo [INFO] Execution completed successfully.
)

endlocal
