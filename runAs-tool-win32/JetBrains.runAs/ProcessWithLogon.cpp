#include "stdafx.h"
#include "ProcessWithLogon.h"
#include "Settings.h"
#include "ProcessTracker.h"
#include "ErrorUtilities.h"
#include "Environment.h"
#include "Result.h"
#include "ExitCode.h"

Result<ExitCode> ProcessWithLogon::Run(Settings& settings, Environment& environment, ProcessTracker& processTracker) const
{
	SECURITY_ATTRIBUTES securityAttributes = {};
	securityAttributes.nLength = sizeof(SECURITY_DESCRIPTOR);
	securityAttributes.bInheritHandle = true;

	STARTUPINFO startupInfo = {};
	PROCESS_INFORMATION processInformation = {};

	processTracker.Initialize(securityAttributes, startupInfo);
	if (!CreateProcessWithLogonW(
		settings.GetUserName().c_str(),
		settings.GetDomain().c_str(),
		settings.GetPassword().c_str(),
		LOGON_WITH_PROFILE,
		nullptr,
		const_cast<LPWSTR>(settings.GetCommandLine().c_str()),
		CREATE_NO_WINDOW | CREATE_UNICODE_ENVIRONMENT,
		environment.CreateEnvironment(),
		settings.GetWorkingDirectory().c_str(),
		&startupInfo,
		&processInformation))
	{
		return Result<ExitCode>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"CreateProcessWithLogonW"));
	}

	auto processHandle = Handle(L"Process");
	processHandle = processInformation.hProcess;

	auto threadHandle = Handle(L"Thread");
	threadHandle = processInformation.hThread;
	
	return processTracker.WaiteForExit(processInformation.hProcess);
}
