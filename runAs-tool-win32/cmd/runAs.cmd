@ECHO OFF
SETLOCAL
SET /A ARGS_COUNT=0    
FOR %%A in (%*) DO SET /A "ARGS_COUNT+=1"
IF %ARGS_COUNT% NEQ 4 (
	@ECHO Invalid arguments.
	@ECHO Usage: runAs.cmd base_directory credential_args other_args teamcity_messages_file
	@EXIT -1
)
ENDLOCAL

REM Define OS bitness
SET "RUN_AS_PATH_TO_BIN=%~dp0"
SET "RUN_AS_PATH_TO_TOOL=JetBrains.runAs.exe"
%RUN_AS_PATH_TO_BIN%JetBrains.getOSBitness.exe -s

IF %errorlevel% EQU 64 SET "RUN_AS_PATH_TO_TOOL=%RUN_AS_PATH_TO_BIN%x64\%RUN_AS_PATH_TO_TOOL%"
IF %errorlevel% EQU 32 SET "RUN_AS_PATH_TO_TOOL=%RUN_AS_PATH_TO_BIN%x86\%RUN_AS_PATH_TO_TOOL%"

PUSHD %1

REM Override environment variables
SET RUN_AS_GET_VARS=%RUN_AS_PATH_TO_TOOL% %2 -i:false -l:off cmd.exe /C %RUN_AS_PATH_TO_BIN%getEnvVars.cmd
FOR /F %%G IN ('%RUN_AS_GET_VARS%') do SET "%%G"

REM Send TeamCity messages
TYPE %4

REM Run command line
%RUN_AS_PATH_TO_TOOL% %1\%2 -i:true -l:errors -b:-10000 %3
SET "EXIT_CODE=%ERRORLEVEL%"

POPD
EXIT /B %EXIT_CODE%