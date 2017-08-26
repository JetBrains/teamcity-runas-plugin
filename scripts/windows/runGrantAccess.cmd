@echo off

if "%~1" equ "" goto INVALID_ARGS
@echo Important to note: Administrative privileges are required to run this script
@pause

set tempDirectory="%temp%\%random%"
powershell -ExecutionPolicy ByPass -File grantAccess.ps1 -teamCityAgentUserName "%1" -tempDirectory "%tempDirectory%"
rd %tempDirectory% /S /Q
exit /b 0

:INVALID_ARGS
@echo Invalid arguments.
@echo Usage: runGrantAccess.cmd TeamCityAgentUserName
@echo   where TeamCityAgentUserName is the user name of a local Windows user account or the user name of a Windows domain user account in the format: domain\username
@exit /b -1