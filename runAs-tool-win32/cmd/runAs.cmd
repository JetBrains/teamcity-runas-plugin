@ECHO OFF
SETLOCAL
SET /A ARGS_COUNT=0    
FOR %%A in (%*) DO SET /A "ARGS_COUNT+=1"
IF %ARGS_COUNT% NEQ 2 (
	@ECHO Invalid arguments.
	@ECHO Usage:
	@ECHO 	runAs.cmd args_for_JetBrains.runAs.exe cmd_to_run
	@ECHO 	where:
	@ECHO 		args_for_JetBrains.runAs.exe	- runAs arguments
	@ECHO 		cmd_to_run			- command line to run
	@EXIT -1
)
ENDLOCAL

REM Define OS bitness
SET "RUN_AS_PATH_TO_BIN=%~dp0"
SET "RUN_AS_PATH_TO_TOOL=JetBrains.runAs.exe"
%RUN_AS_PATH_TO_BIN%JetBrains.getOSBitness.exe -s

IF %errorlevel% EQU 64 SET "RUN_AS_PATH_TO_TOOL=%RUN_AS_PATH_TO_BIN%x64\%RUN_AS_PATH_TO_TOOL%"
IF %errorlevel% EQU 32 SET "RUN_AS_PATH_TO_TOOL=%RUN_AS_PATH_TO_BIN%x86\%RUN_AS_PATH_TO_TOOL%"

REM Override environment variables
SET RUN_AS_GET_VARS=%RUN_AS_PATH_TO_TOOL% %1 -i:false %RUN_AS_PATH_TO_BIN%getEnvVars.cmd
FOR /F %%G IN ('%RUN_AS_GET_VARS%') do SET "%%G"

REM Run command line
@ECHO ##teamcity[message text='Starting: %~2']
%RUN_AS_PATH_TO_TOOL% %1 -i:true %2