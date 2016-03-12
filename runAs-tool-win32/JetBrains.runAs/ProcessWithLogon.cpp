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
#include "StringBuffer.h"
#include "ShowModeConverter.h"

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
			{ },
			INHERITANCE_MODE_OFF,
			INTEGRITY_LEVEL_AUTO,
			SHOW_MODE_HIDE,
			false);

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

	environment = Environment::Apply(environment, Environment::CreateFormList(settings.GetEnvironmentVariables(), trace), trace);	
	return RunInternal(trace, settings, processTracker, environment);
}

Result<ExitCode> ProcessWithLogon::RunInternal(Trace& trace, const Settings& settings, ProcessTracker& processTracker, Environment& environment)
{
	SECURITY_ATTRIBUTES securityAttributes = {};
	securityAttributes.nLength = sizeof(SECURITY_DESCRIPTOR);
	securityAttributes.bInheritHandle = true;

	STARTUPINFO startupInfo = {};
	startupInfo.dwFlags = STARTF_USESHOWWINDOW;
	startupInfo.wShowWindow = ShowModeConverter::ToShowWindowFlag(settings.GetShowMode());
	PROCESS_INFORMATION processInformation = {};

	trace < L"ProcessTracker::InitializeConsoleRedirection";
	processTracker.InitializeConsoleRedirection(securityAttributes, startupInfo);	

	StringBuffer userName(settings.GetUserName());
	StringBuffer domain(settings.GetDomain());
	StringBuffer password(settings.GetPassword());
	StringBuffer workingDirectory(settings.GetWorkingDirectory());
	StringBuffer commandLine(settings.GetCommandLine());

	trace < L"::CreateProcessWithLogonW";
	if (!CreateProcessWithLogonW(
		userName.GetPointer(),
		domain.GetPointer(),
		password.GetPointer(),
		LOGON_WITH_PROFILE,
		nullptr,
		commandLine.GetPointer(),
		CREATE_UNICODE_ENVIRONMENT,
		environment.CreateEnvironment(),
		workingDirectory.GetPointer(),
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

	return processTracker.WaiteForExit(processInformation.hProcess, trace);
}
