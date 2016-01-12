#include "stdafx.h"
#include "ProcessWithLogon.h"
#include "Settings.h"
#include "ProcessTracker.h"
#include "ErrorUtilities.h"
#include <iostream>
#include "Environment.h"

int ProcessWithLogon::Run(Settings& settings) const
{
	SECURITY_ATTRIBUTES securityAttributes = {};
	securityAttributes.nLength = sizeof(SECURITY_DESCRIPTOR);
	securityAttributes.bInheritHandle = true;

	STARTUPINFO startupInfo = {};
	PROCESS_INFORMATION processInformation = {};
	ProcessTracker processTracker(securityAttributes, startupInfo);

	// Get current environment
	Environment environment;

	if (!CreateProcessWithLogonW(
		const_cast<LPWSTR>(settings.GetUserName().c_str()),
		const_cast<LPWSTR>(settings.GetDomain().c_str()),
		const_cast<LPWSTR>(settings.GetPassword().c_str()),
		LOGON_WITH_PROFILE,
		nullptr,
		const_cast<LPWSTR>(settings.GetCommandLine().c_str()),
		CREATE_NO_WINDOW | INHERIT_PARENT_AFFINITY | CREATE_NEW_CONSOLE | CREATE_UNICODE_ENVIRONMENT,
		environment.GetEnvironment(),
		const_cast<LPWSTR>(settings.GetWorkingDirectory().c_str()),
		&startupInfo,
		&processInformation))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"CreateProcessWithLogonW");
		return ErrorExitCode;
	}

	auto processHandle = Handle(L"Service Process");
	processHandle.Value() = processInformation.hProcess;

	auto threadHandle = Handle(L"Thread");
	threadHandle.Value() = processInformation.hThread;
	
	return processTracker.WaiteForExit(processInformation.hProcess);
}
