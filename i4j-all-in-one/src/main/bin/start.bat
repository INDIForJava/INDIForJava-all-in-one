@echo off
@REM enable echoing my setting MAVEN_BATCH_ECHO to 'on'

@REM set %HOME% to equivalent of $HOME
if "%HOME%" == "" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

set ERROR_CODE=0

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto OkJHome

echo.
echo ERROR: JAVA_HOME not found in your environment.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto chkMHome

echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error

:chkMHome
if not "%I4J_HOME%"=="" goto valMHome

if "%OS%"=="Windows_NT" SET "I4J_HOME=%~dp0.."
if "%OS%"=="WINNT" SET "I4J_HOME=%~dp0.."
if not "%I4J_HOME%"=="" goto valMHome

echo.
echo ERROR: I4J_HOME not found in your environment.
echo Please set the I4J_HOME variable in your environment to match the
echo location of the Maven installation
echo.
goto error

:valMHome

:stripMHome
if not "_%I4J_HOME:~-1%"=="_\" goto checkMBat
set "I4J_HOME=%I4J_HOME:~0,-1%"
goto stripMHome

:checkMBat
if exist "%I4J_HOME%\bin\mvn.bat" goto init

echo.
echo ERROR: I4J_HOME is set to an invalid directory.
echo I4J_HOME = "%I4J_HOME%"
echo Please set the I4J_HOME variable in your environment to match the
echo location of the Maven installation
echo.
goto error
@REM ==== END VALIDATION ====

:init
@REM Decide how to startup depending on the version of windows

@REM -- Windows NT with Novell Login
if "%OS%"=="WINNT" goto WinNTNovell

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

:WinNTNovell

@REM -- 4NT shell
if "%@eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
set I4J_CMD_LINE_ARGS=%*
goto endInit

@REM The 4NT Shell from jp software
:4NTArgs
set I4J_CMD_LINE_ARGS=%$
goto endInit

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of agruments (up to the command line limit, anyway).
set I4J_CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto endInit
set I4J_CMD_LINE_ARGS=%I4J_CMD_LINE_ARGS% %1
shift
goto Win9xApp

@REM Reaching here means variables are defined and arguments have been captured
:endInit
SET I4J_JAVA_EXE="%JAVA_HOME%\bin\java.exe"

@REM -- 4NT shell
if "%@eval[2+2]" == "4" goto 4NTCWJars

@REM -- Regular WinNT shell
for %%i in ("%I4J_HOME%"\lib\i4j-server-main*) do set I4J_SERVER_MAIN_JAR="%%i"
goto runI4J

@REM The 4NT Shell from jp software
:4NTCWJars
for %%i in ("%I4J_HOME%\lib\i4j-server-main-*") do set I4J_SERVER_MAIN_JAR="%%i"
goto runI4J

@REM Start MAVEN2
:runI4J

%I4J_JAVA_EXE% "-Dlogback.configurationFile=%I4J_HOME%\etc\logback.xml" "-DI4J_HOME=%I4J_HOME%" "-jar=%I4J_SERVER_MAIN_JAR%" "-d %I4J_HOME%" %I4J_CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal
set ERROR_CODE=1

:end
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT
if "%OS%"=="WINNT" goto endNT

@REM For old DOS remove the set variables from ENV - we assume they were not set
@REM before we started - at least we don't leave any baggage around
set I4J_JAVA_EXE=
set I4J_CMD_LINE_ARGS=

:endNT
@endlocal & set ERROR_CODE=%ERROR_CODE%

