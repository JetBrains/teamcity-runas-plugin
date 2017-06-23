@ECHO OFF

IF "%~1" EQU "" GOTO INVALID_ARGS
IF "%~2" EQU "" GOTO INVALID_ARGS
IF "%~3" EQU "" GOTO INVALID_ARGS
IF "%~4" EQU "" GOTO INVALID_ARGS
IF "%~5" NEQ "" GOTO INVALID_ARGS

REM Define OS bitness
"%~dp0x86\JetBrains.runAs.exe" -t -l:errors
SET "EXIT_CODE=%ERRORLEVEL%"

IF %EXIT_CODE% EQU 64 (
	GOTO RUN_AS
)

IF %EXIT_CODE% EQU 32 (
	GOTO RUN_AS
)

ECHO.
IF %EXIT_CODE% EQU 1 ECHO ##teamcity[message text='Invoker has no administrative privileges, when running under the Windows service.' status='ERROR']
IF %EXIT_CODE% EQU 2 ECHO ##teamcity[message text='Invoker has no SeAssignPrimaryTokenPrivilege (Replace a process-level token), when running under the Windows service.' status='ERROR']
IF %EXIT_CODE% EQU 3 ECHO ##teamcity[message text='Invoker has no SeTcbPrivilege (Act as part of the operating system), when running under the Windows service.' status='ERROR']
EXIT /B %EXIT_CODE%

:RUN_AS
IF %EXIT_CODE% EQU 64 (
	SET RUN_AS_PATH="%~dp0x64\JetBrains.runAs.exe"
	IF %3 EQU 64 (
		SET CMD_PATH="cmd.exe"		
	) ELSE (
		SET CMD_PATH="%windir%\SysWOW64\cmd.exe"
	)
) ELSE (
	SET RUN_AS_PATH="%~dp0x86\JetBrains.runAs.exe"
	SET CMD_PATH="cmd.exe"
)

REM Check an agent will be able to remove temporary files
SET RUNAS_TMP_FILE=runAs-%RANDOM%-%RANDOM%.tmp
PUSHD %TMP%
%RUN_AS_PATH% -i:auto -l:errors "-p:%~4" "-c:%~1" -b:-10000 %CMD_PATH% /C "type nul > %RUNAS_TMP_FILE%"
del %RUNAS_TMP_FILE% 2>nul

IF EXIST %RUNAS_TMP_FILE% (
	%RUN_AS_PATH% -i:auto -l:errors "-p:%~4" "-c:%~1" -b:-10000 %CMD_PATH% /C "del %RUNAS_TMP_FILE% > nul"
    ECHO ##teamcity[message text='Incorrect runAs configuration: agent won't be able to remove temporary files created by the build step, see teamcity-agent.log for details .' status='ERROR']
	@EXIT -1
)

set RUNAS_TMP_FILE=
POPD

REM Run as user
%RUN_AS_PATH% -i:auto -l:errors "-p:%~4" "-c:%~1" -b:-10000 %CMD_PATH% /C "%~2"

SET "EXIT_CODE=%ERRORLEVEL%"

ECHO.
IF %EXIT_CODE% EQU -10000 ECHO ##teamcity[message text='Unknown error occurred.' status='ERROR']
IF %EXIT_CODE% EQU -10001 ECHO ##teamcity[message text='Invalid usage of the tool.' status='ERROR']
IF %EXIT_CODE% EQU -10002 ECHO ##teamcity[message text='Security error occurred.' status='ERROR']
IF %EXIT_CODE% EQU -10003 ECHO ##teamcity[message text='WIN32 API error occurred.' status='ERROR']

EXIT /B %EXIT_CODE%

:INVALID_ARGS
@ECHO Invalid arguments.
@ECHO Usage: runAs.cmd settings_file_name command_file_name bitness password
@EXIT -1