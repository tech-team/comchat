@echo off

set JAVA_HOME="C:\Program Files\Java\jre8\bin"

if "%PROCESSOR_ARCHITECTURE%"=="AMD64" goto 64BIT
REM x86
set lib_path="lib/x86"
goto EXECUTION

:64BIT
REM x64
set lib_path="lib/x64"

:EXECUTION
%JAVA_HOME%\java -jar -Djava.library.path=%lib_path% comchat.jar