#include "stdafx.h"
#include "ProcessUnderService.h"
#include "Settings.h"
#include <iostream>
#include "ErrorUtilities.h"
#include "ProcessTracker.h"
#include "Environment.h"

class ProcessTracker;

int ProcessUnderService::Run(Settings& settings) const
{
	// Get current environment
	auto currentSecurityTokenHandle = Handle(L"Current security token");
	if (!OpenProcessToken(GetCurrentProcess(), TOKEN_ADJUST_PRIVILEGES | TOKEN_QUERY, &currentSecurityTokenHandle.Value()))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"OpenProcessToken");
		return ErrorExitCode;
	}

	Environment currentUserEnvironment(currentSecurityTokenHandle.Value(), true);

	// Attempt to log a user on to the local computer
	auto newUserSecurityTokenHandle = Handle(L"New user security token");
	if (!LogonUser(
		settings.GetUserName().c_str(),
		settings.GetDomain().c_str(),
		settings.GetPassword().c_str(),
		LOGON32_LOGON_NETWORK,
		LOGON32_PROVIDER_DEFAULT,
		&newUserSecurityTokenHandle.Value()))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"LogonUser");
		return ErrorExitCode;
	}

	// Load profile
	PROFILEINFO profileInfo = {};
	profileInfo.dwSize = sizeof(PROFILEINFO);
	profileInfo.lpUserName = const_cast<LPWSTR>(settings.GetUserName().c_str());
	if (!LoadUserProfile(newUserSecurityTokenHandle.Value(), &profileInfo))
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
	auto primaryNewUserSecurityTokenHandle = Handle(L"Primary new user security token");
	SECURITY_ATTRIBUTES processSecAttributes = {};
	processSecAttributes.lpSecurityDescriptor = &securityDescriptor;
	processSecAttributes.nLength = sizeof(SECURITY_DESCRIPTOR);
	processSecAttributes.bInheritHandle = true;
	if (!DuplicateTokenEx(
		newUserSecurityTokenHandle.Value(),
		0,
		&processSecAttributes,
		SecurityImpersonation,
		TokenPrimary,
		&primaryNewUserSecurityTokenHandle.Value()))
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
	Environment newUserEnvironment(primaryNewUserSecurityTokenHandle.Value(), false);
	Environment mergedEnvironment;
	Environment::Merge(currentUserEnvironment, newUserEnvironment, mergedEnvironment);

	PROCESS_INFORMATION processInformation = {};
	if (!CreateProcessAsUser(
		primaryNewUserSecurityTokenHandle.Value(),
		nullptr,
		const_cast<LPWSTR>(settings.GetCommandLine().c_str()),
		&processSecAttributes,
		&threadSecAttributes,
		true,
		CREATE_NO_WINDOW | INHERIT_PARENT_AFFINITY | CREATE_NEW_CONSOLE | CREATE_UNICODE_ENVIRONMENT,
		mergedEnvironment.CreateEnvironment(),
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