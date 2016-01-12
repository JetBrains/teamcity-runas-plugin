#include "stdafx.h"
#include "ServiceProcess.h"
#include "Settings.h"
#include <iostream>
#include "ErrorUtilities.h"
#include "ProcessTracker.h"
#include "Environment.h"

class ProcessTracker;

int ServiceProcess::Run(Settings& settings) const
{
	// Attempt to log a user on to the local computer
	auto securityTokenHandle = Handle(L"Security Token");
	if (!LogonUser(
		settings.GetUserName().c_str(),
		settings.GetDomain().c_str(),
		settings.GetPassword().c_str(),
		LOGON32_LOGON_NETWORK,
		LOGON32_PROVIDER_DEFAULT,
		&securityTokenHandle.Value()))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"LogonUser");
		return ErrorExitCode;
	}

	// Load profile
	PROFILEINFO profileInfo = {};
	profileInfo.dwSize = sizeof(PROFILEINFO);
	profileInfo.lpUserName = const_cast<LPWSTR>(settings.GetUserName().c_str());
	if (!LoadUserProfile(securityTokenHandle.Value(), &profileInfo))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"LoadUserProfile");
		return ErrorExitCode;
	}

	// Initialize a new security descriptor
	SECURITY_DESCRIPTOR securityDescriptor = {};
	if (!InitializeSecurityDescriptor(
		&securityDescriptor,
		SECURITY_DESCRIPTOR_REVISION))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"InitializeSecurityDescriptor");
		return ErrorExitCode;
	}

	if (!SetSecurityDescriptorDacl(
		&securityDescriptor,
		true,
		nullptr,
		false))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"SetSecurityDescriptorDacl");
		return ErrorExitCode;
	}

	// Creates a new access token that duplicates an existing token
	auto primarySecurityTokenHandle = Handle(L"Primary Security Token");
	SECURITY_ATTRIBUTES processSecAttributes = {};
	processSecAttributes.lpSecurityDescriptor = &securityDescriptor;
	processSecAttributes.nLength = sizeof(SECURITY_DESCRIPTOR);
	processSecAttributes.bInheritHandle = true;
	if (!DuplicateTokenEx(
		securityTokenHandle.Value(),
		0,
		&processSecAttributes,
		SecurityImpersonation,
		TokenPrimary,
		&primarySecurityTokenHandle.Value()))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"DuplicateTokenEx");
		return ErrorExitCode;
	}

	// Create a new process and its primary thread. The new process runs in the security context of the user represented by the specified token.
	SECURITY_ATTRIBUTES threadSecAttributes = {};
	threadSecAttributes.lpSecurityDescriptor = nullptr;
	threadSecAttributes.nLength = 0;
	threadSecAttributes.bInheritHandle = false;	

	STARTUPINFO startupInfo = {};
	ProcessTracker processTracker(processSecAttributes, startupInfo);

	// Get current environment
	Environment environment;

	PROCESS_INFORMATION processInformation = {};
	if (!CreateProcessAsUser(
		primarySecurityTokenHandle.Value(),
		nullptr,
		const_cast<LPWSTR>(settings.GetCommandLine().c_str()),
		&processSecAttributes,
		&threadSecAttributes,
		true,
		CREATE_NO_WINDOW | INHERIT_PARENT_AFFINITY | CREATE_NEW_CONSOLE | CREATE_UNICODE_ENVIRONMENT,
		environment.GetEnvironment(),
		const_cast<LPWSTR>(settings.GetWorkingDirectory().c_str()),
		&startupInfo,
		&processInformation))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"CreateProcessAsUser");
		return ErrorExitCode;
	}

	auto processHandle = Handle(L"Service Process");
	processHandle.Value() = processInformation.hProcess;
	
	auto threadHandle = Handle(L"Thread");
	threadHandle.Value() = processInformation.hThread;	

	return processTracker.WaiteForExit(processInformation.hProcess);
}