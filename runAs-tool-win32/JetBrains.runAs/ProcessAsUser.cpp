#include "stdafx.h"
#include "ProcessAsUser.h"
#include "Settings.h"
#include "ErrorUtilities.h"
#include "ProcessTracker.h"
#include "Environment.h"
#include "Result.h"
#include "ExitCode.h"
class ProcessTracker;

Result<ExitCode> ProcessAsUser::Run(Settings& settings, Environment& environment, ProcessTracker& processTracker) const
{
	// Attempt to log a user on to the local computer
	auto newUserSecurityTokenHandle = Handle(L"New user security token");
	if (!LogonUser(
		settings.GetUserName().c_str(),
		settings.GetDomain().c_str(),
		settings.GetPassword().c_str(),
		LOGON32_LOGON_NETWORK,
		LOGON32_PROVIDER_DEFAULT,
		&newUserSecurityTokenHandle))
	{
		return Result<ExitCode>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"LogonUser"));
	}
	
	// Initialize a new security descriptor
	SECURITY_DESCRIPTOR securityDescriptor = {};
	if (!InitializeSecurityDescriptor(
		&securityDescriptor,
		SECURITY_DESCRIPTOR_REVISION))
	{
		return Result<ExitCode>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"InitializeSecurityDescriptor"));
	}

	if (!SetSecurityDescriptorDacl(
		&securityDescriptor,
		true,
		nullptr,
		false))
	{
		return Result<ExitCode>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"SetSecurityDescriptorDacl"));
	}

	// Creates a new access token that duplicates an existing token
	auto primaryNewUserSecurityTokenHandle = Handle(L"Primary new user security token");
	SECURITY_ATTRIBUTES processSecAttributes = {};
	processSecAttributes.lpSecurityDescriptor = &securityDescriptor;
	processSecAttributes.nLength = sizeof(SECURITY_DESCRIPTOR);
	processSecAttributes.bInheritHandle = true;
	if (!DuplicateTokenEx(
		newUserSecurityTokenHandle,
		0,
		&processSecAttributes,
		SecurityImpersonation,
		TokenPrimary,
		&primaryNewUserSecurityTokenHandle))
	{
		return Result<ExitCode>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"DuplicateTokenEx"));
	}

	SECURITY_ATTRIBUTES threadSecAttributes = {};
	threadSecAttributes.lpSecurityDescriptor = nullptr;
	threadSecAttributes.nLength = 0;
	threadSecAttributes.bInheritHandle = false;	
	STARTUPINFO startupInfo = {};	

	auto error = processTracker.Initialize(processSecAttributes, startupInfo);
	if(error.HasError())
	{
		return Result<ExitCode>(error.GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"DuplicateTokenEx"));
	}

	// Load profile
	PROFILEINFO profileInfo = {};
	profileInfo.dwSize = sizeof(PROFILEINFO);
	profileInfo.lpUserName = const_cast<LPWSTR>(settings.GetUserName().c_str());
	if (!LoadUserProfile(primaryNewUserSecurityTokenHandle, &profileInfo))
	{
		return Result<ExitCode>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"LoadUserProfile"));
	}

	// Create a new process and its primary thread. The new process runs in the security context of the user represented by the specified token.
	PROCESS_INFORMATION processInformation = {};
	auto cmdLine = settings.GetCommandLine();
	if (!CreateProcessAsUser(
		primaryNewUserSecurityTokenHandle,
		nullptr,
		const_cast<LPWSTR>(cmdLine.c_str()),
		&processSecAttributes,
		&threadSecAttributes,
		true,
		CREATE_NO_WINDOW | CREATE_UNICODE_ENVIRONMENT,
		environment.CreateEnvironment(),
		settings.GetWorkingDirectory().c_str(),
		&startupInfo,
		&processInformation))
	{
		return Result<ExitCode>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"CreateProcessAsUser"));
	}

	auto processHandle = Handle(L"Service Process");
	processHandle = processInformation.hProcess;
	
	auto threadHandle = Handle(L"Thread");
	threadHandle = processInformation.hThread;	

	return processTracker.WaiteForExit(processInformation.hProcess);
}