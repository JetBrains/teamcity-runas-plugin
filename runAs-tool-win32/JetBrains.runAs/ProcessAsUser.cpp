#include "stdafx.h"
#include "ProcessAsUser.h"
#include "Settings.h"
#include "ErrorUtilities.h"
#include "ProcessTracker.h"
#include "Result.h"
#include "ExitCode.h"
#include "Environment.h"
#include "Trace.h"
#include "Job.h"
class Trace;
class ProcessTracker;

Result<ExitCode> ProcessAsUser::Run(const Settings& settings, ProcessTracker& processTracker) const
{
	Trace trace(settings.GetLogLevel());
	trace < L"Use ProcessAsUser";
	trace < L"Attempt to log a user on to the local computer";
	trace < L"::LogonUser";

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
	
	trace < L"Initialize a new security descriptor";
	trace < L"::InitializeSecurityDescriptor";
	SECURITY_DESCRIPTOR securityDescriptor = {};
	if (!InitializeSecurityDescriptor(
		&securityDescriptor,
		SECURITY_DESCRIPTOR_REVISION))
	{
		return Result<ExitCode>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"InitializeSecurityDescriptor"));
	}

	trace < L"::SetSecurityDescriptorDacl";
	if (!SetSecurityDescriptorDacl(
		&securityDescriptor,
		true,
		nullptr,
		false))
	{
		return Result<ExitCode>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"SetSecurityDescriptorDacl"));
	}

	trace < L"Creates a new access primary token that duplicates new process's token";
	auto primaryNewUserSecurityTokenHandle = Handle(L"Primary new user security token");
	SECURITY_ATTRIBUTES processSecAttributes = {};
	processSecAttributes.lpSecurityDescriptor = &securityDescriptor;
	processSecAttributes.nLength = sizeof(SECURITY_DESCRIPTOR);
	processSecAttributes.bInheritHandle = true;
	trace < L"::DuplicateTokenEx";
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

	trace < L"ProcessTracker::Initialize";
	auto error = processTracker.Initialize(processSecAttributes, startupInfo);
	if(error.HasError())
	{
		return Result<ExitCode>(error.GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"DuplicateTokenEx"));
	}

	trace < L"::LoadUserProfile";
	PROFILEINFO profileInfo = {};
	profileInfo.dwSize = sizeof(PROFILEINFO);
	profileInfo.lpUserName = const_cast<LPWSTR>(settings.GetUserName().c_str());
	if (!LoadUserProfile(primaryNewUserSecurityTokenHandle, &profileInfo))
	{
		return Result<ExitCode>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"LoadUserProfile"));
	}

	auto newProcessEnvironmentResult = GetEnvironment(primaryNewUserSecurityTokenHandle, settings.GetInheritanceMode());
	if (newProcessEnvironmentResult.HasError())
	{
		UnloadUserProfile(primaryNewUserSecurityTokenHandle, profileInfo.hProfile);
		return Result<ExitCode>(newProcessEnvironmentResult.GetErrorCode(), newProcessEnvironmentResult.GetErrorDescription());
	}
	
	trace < L"Create a new process and its primary thread. The new process runs in the security context of the user represented by the specified token.";
	PROCESS_INFORMATION processInformation = {};
	auto cmdLine = settings.GetCommandLine();
	trace < L"::CreateProcessAsUser";
	if (!CreateProcessAsUser(
		primaryNewUserSecurityTokenHandle,
		nullptr,
		const_cast<LPWSTR>(cmdLine.c_str()),
		&processSecAttributes,
		&threadSecAttributes,
		true,
		CREATE_NO_WINDOW | CREATE_UNICODE_ENVIRONMENT,
		newProcessEnvironmentResult.GetResultValue().CreateEnvironment(),
		settings.GetWorkingDirectory().c_str(),
		&startupInfo,
		&processInformation))
	{		
		auto result = Result<ExitCode>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"CreateProcessAsUser"));
		UnloadUserProfile(primaryNewUserSecurityTokenHandle, profileInfo.hProfile);
		return result;
	}

	auto processHandle = Handle(L"Service Process");
	processHandle = processInformation.hProcess;
	
	auto threadHandle = Handle(L"Thread");
	threadHandle = processInformation.hThread;

	trace < L"Create a job";
	Job job;
	JOBOBJECT_EXTENDED_LIMIT_INFORMATION jobObjectInfo = {};
	jobObjectInfo.BasicLimitInformation.LimitFlags = JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE;
	trace < L"Configure all child processes associated with the job to terminate when the parent is terminated";
	trace < L"Job::SetInformation";
	job.SetInformation(JobObjectExtendedLimitInformation, jobObjectInfo);

	trace < L"ProcessTracker::WaiteForExit";
	auto exitCode = processTracker.WaiteForExit(processInformation.hProcess);
	UnloadUserProfile(primaryNewUserSecurityTokenHandle, profileInfo.hProfile);	

	return exitCode;
}

Result<Environment> ProcessAsUser::GetEnvironment(Handle& userToken, const InheritanceMode inheritanceMode)
{
	Environment callingProcessEnvironment;
	if (inheritanceMode == INHERITANCE_MODE_ON)
	{
		return callingProcessEnvironment;
	}

	// Get target user's environment
	auto targetUserEnvironmentResult = Environment::CreateForUser(userToken, false);
	if (inheritanceMode == INHERITANCE_MODE_OFF || targetUserEnvironmentResult.HasError())
	{
		return targetUserEnvironmentResult;
	}
	
	auto targetUserEnvironment = targetUserEnvironmentResult.GetResultValue();
	return Environment::Override(callingProcessEnvironment, targetUserEnvironment);	
}