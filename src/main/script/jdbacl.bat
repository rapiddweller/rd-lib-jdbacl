@echo off
rem %~dp0 is expanded pathname of the current script under NT
set DEFAULT_JDBACL_HOME=%~dp0..

if "%JDBACL_HOME%"=="" set JDBACL_HOME=%DEFAULT_JDBACL_HOME%
set DEFAULT_JDBACL_HOME=

set _USE_CLASSPATH=yes

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
set JDBACL_CMD_LINE_ARGS=%1
if ""%1""=="""" goto doneStart
shift
:setupArgs
if ""%1""=="""" goto doneStart
if ""%1""==""-noclasspath"" goto clearclasspath
set JDBACL_CMD_LINE_ARGS=%JDBACL_CMD_LINE_ARGS% %1
shift
goto setupArgs

rem here is there is a -noclasspath in the options
:clearclasspath
set _USE_CLASSPATH=no
shift
goto setupArgs

rem This label provides a place for the argument list loop to break out
rem and for NT handling to skip to.

:doneStart
rem check the value of JDBACL_HOME
if exist "%JDBACL_HOME%\license.txt" goto setLocalClassPath

:noJdbaclHome
echo JDBACL_HOME is set incorrectly. 
echo Please set the JDBACL_HOME environment variable to the path where you installed Jdbacl.
goto endsetup

:setLocalClassPath
set LOCALCLASSPATH=.;%JDBACL_HOME%\bin;%JDBACL_HOME%\lib\*
echo Local classpath: %LOCALCLASSPATH%

:checkJava
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto endsetup

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe

:endsetup

if "%_JAVACMD%"=="" goto end

if "%_USE_CLASSPATH%"=="no" goto runNoClasspath
if not "%CLASSPATH%"=="" goto runWithClasspath

:runNoClasspath
"%_JAVACMD%" %JDBACL_OPTS% -classpath "%LOCALCLASSPATH%" com.rapiddweller.jdbacl.swing.JdbaclGUI %*
goto end

:runWithClasspath
"%_JAVACMD%" %JDBACL_OPTS% -classpath "%CLASSPATH%;%LOCALCLASSPATH%" com.rapiddweller.jdbacl.swing.JdbaclGUI %*
goto end

:end
set _JAVACMD=
set JDBACL_CMD_LINE_ARGS=
