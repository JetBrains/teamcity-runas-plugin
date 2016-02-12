@ECHO OFF
SETLOCAL
SET /A ARGS_COUNT=0    
FOR %%A in (%*) DO SET /A "ARGS_COUNT+=1"
IF %ARGS_COUNT% NEQ 2 (
	@ECHO Invalid arguments.
	@ECHO Usage: runAs.cmd credentials_file command
	@EXIT -1
)
ENDLOCAL

REM Define OS bitness
SET "RUN_AS_PATH_TO_BIN=%~dp0"
SET "RUN_AS_PATH_TO_TOOL=JetBrains.runAs.exe"
%RUN_AS_PATH_TO_BIN%JetBrains.getOSBitness.exe -s
SET "OS_BITNESS=%errorlevel%"

IF %OS_BITNESS% EQU 64 SET "RUN_AS_PATH_TO_TOOL=%RUN_AS_PATH_TO_BIN%x64\%RUN_AS_PATH_TO_TOOL%"
IF %OS_BITNESS% EQU 32 SET "RUN_AS_PATH_TO_TOOL=%RUN_AS_PATH_TO_BIN%x86\%RUN_AS_PATH_TO_TOOL%"

REM Override environment variables
SET RUN_AS_GET_VARS=%RUN_AS_PATH_TO_TOOL% -c:%1 -i:false -l:off cmd.exe /C %RUN_AS_PATH_TO_BIN%getEnvVars.cmd
FOR /F %%G IN ('%RUN_AS_GET_VARS%') do SET "%%G"

REM Run command line
%RUN_AS_PATH_TO_TOOL% -c:%1 -i:true -l:errors -b:-10000 cmd.exe /C %2
SET "EXIT_CODE=%ERRORLEVEL%"

ECHO.
IF %EXIT_CODE% EQU -10000 ECHO ##teamcity[message text='Unknown error occurred.' status='ERROR']
IF %EXIT_CODE% EQU -10001 ECHO ##teamcity[message text='Invalid usage of the tool.' status='ERROR']
IF %EXIT_CODE% EQU -10002 ECHO ##teamcity[message text='Security error occurred.' status='ERROR']
IF %EXIT_CODE% EQU -10003 ECHO ##teamcity[message text='WIN32 API error occurred.' status='ERROR']

EXIT /B %EXIT_CODE%