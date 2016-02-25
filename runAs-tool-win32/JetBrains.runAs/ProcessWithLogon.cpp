#include "stdafx.h"
#include "ProcessWithLogon.h"
#include "Settings.h"
#include "ProcessTracker.h"
#include "ErrorUtilities.h"
#include "Environment.h"
#include "Result.h"
#include "ExitCode.h"
#include "StubWriter.h"
#include "StringWriter.h"
#include <sstream>
#include "Trace.h"
#include "Job.h"

Result<ExitCode> ProcessWithLogon::Run(const Settings& settings, ProcessTracker& processTracker) const
{
	Trace trace(settings.GetLogLevel());
	Environment callingProcessEnvironment;
	Environment targetUserEnvironment;
	Environment environment;
	if (settings.GetInheritanceMode() != INHERITANCE_MODE_OFF)
	{
		trace < L"ProcessWithLogon::Get calling process Environment";
		auto callingProcessEnvironmentResult = Environment::CreateForCurrentProcess(trace);
		if (callingProcessEnvironmentResult.HasError())
		{
			return Result<ExitCode>(callingProcessEnvironmentResult.GetErrorCode(), callingProcessEnvironmentResult.GetErrorDescription());
		}

		callingProcessEnvironment = callingProcessEnvironmentResult.GetResultValue();
		environment = callingProcessEnvironment;
	}

	if (settings.GetInheritanceMode() != INHERITANCE_MODE_ON)
	{		
		trace < L"ProcessWithLogon::Get target user environment";
		Settings getEnvVarsProcessSettings(
			settings.GetUserName(),
			settings.GetDomain(),
			settings.GetPassword(),
			L"cmd.exe",
			settings.GetWorkingDirectory(),
			DEFAULT_EXIT_CODE_BASE,
			{ L"/U", L"/C", L"SET" },
			INHERITANCE_MODE_OFF,
			INTEGRITY_LEVEL_AUTO);

		if(settings.GetLogLevel() == LOG_LEVEL_DEBUG)
		{
			getEnvVarsProcessSettings.SetLogLevel(LOG_LEVEL_DEBUG);
		}
		else
		{
			getEnvVarsProcessSettings.SetLogLevel(LOG_LEVEL_OFF);
		}

		wstringstream getEnvVarsStream;
		StringWriter getEnvVarsWriter(getEnvVarsStream);
		StubWriter nulWriter;
		ProcessTracker getEnvVarsProcessTracker(getEnvVarsWriter, nulWriter);
		auto getEnvVarsResult = RunInternal(trace, getEnvVarsProcessSettings, getEnvVarsProcessTracker, targetUserEnvironment);
		if (getEnvVarsResult.HasError() || getEnvVarsResult.GetResultValue() != 0)
		{
			return getEnvVarsResult;
		}

		targetUserEnvironment = Environment::CreateFormString(getEnvVarsStream.str(), trace);
		environment = targetUserEnvironment;
	}

	if (settings.GetInheritanceMode() == INHERITANCE_MODE_AUTO)
	{
		environment = Environment::Override(callingProcessEnvironment, targetUserEnvironment, trace);
	}
	
	return RunInternal(trace, settings, processTracker, environment);
}

Result<ExitCode> ProcessWithLogon::RunInternal(Trace& trace, const Settings& settings, ProcessTracker& processTracker, Environment& environment)
{
	SECURITY_ATTRIBUTES securityAttributes = {};
	securityAttributes.nLength = sizeof(SECURITY_DESCRIPTOR);
	securityAttributes.bInheritHandle = true;

	STARTUPINFO startupInfo = {};
	startupInfo.dwFlags = STARTF_USESHOWWINDOW;
	startupInfo.wShowWindow = SW_HIDE;
	PROCESS_INFORMATION processInformation = {};

	trace < L"ProcessTracker::Initialize";
	processTracker.Initialize(securityAttributes, startupInfo);	
	auto cmdLine = settings.GetCommandLine();

	trace < L"::CreateProcessWithLogonW";
	if (!CreateProcessWithLogonW(
		settings.GetUserName().c_str(),
		settings.GetDomain().c_str(),
		settings.GetPassword().c_str(),
		LOGON_WITH_PROFILE,
		nullptr,
		const_cast<LPWSTR>(cmdLine.c_str()),
		CREATE_UNICODE_ENVIRONMENT,
		environment.CreateEnvironment(),
		settings.GetWorkingDirectory().c_str(),
		&startupInfo,
		&processInformation))
	{
		return Result<ExitCode>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"CreateProcessWithLogonW"));
	}

	// ReSharper disable once CppInitializedValueIsAlwaysRewritten
	auto processHandle = Handle(L"Process");
	processHandle = processInformation.hProcess;

	// ReSharper disable once CppInitializedValueIsAlwaysRewritten
	auto threadHandle = Handle(L"Thread");
	threadHandle = processInformation.hThread;

	trace < L"ProcessWithLogon::Create a job";
	Job job;
	JOBOBJECT_EXTENDED_LIMIT_INFORMATION jobObjectInfo = {};
	jobObjectInfo.BasicLimitInformation.LimitFlags = JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE;
	trace < L"ProcessWithLogon::Configure all child processes associated with the job to terminate when the parent is terminated";
	trace < L"Job::SetInformation";
	job.SetInformation(JobObjectExtendedLimitInformation, jobObjectInfo);

	trace < L"ProcessWithLogon::Assign the new process to the job";
	trace < L"Job::AssignProcessToJob";
	job.AssignProcessToJob(processHandle);

	trace < L"ProcessTracker::WaiteForExit";
	return processTracker.WaiteForExit(processInformation.hProcess);
}
