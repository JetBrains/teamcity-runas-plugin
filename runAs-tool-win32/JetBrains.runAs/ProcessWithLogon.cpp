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

Result<ExitCode> ProcessWithLogon::Run(const Settings& settings, ProcessTracker& processTracker) const
{
	Environment callingProcessEnvironment;
	Environment targetUserEnvironment;
	Environment environment;
	if (settings.GetInheritanceMode() != INHERITANCE_MODE_OFF)
	{
		auto callingProcessEnvironmentResult = Environment::CreateForCurrentProcess();
		if (callingProcessEnvironmentResult.HasError())
		{
			return Result<ExitCode>(callingProcessEnvironmentResult.GetErrorCode(), callingProcessEnvironmentResult.GetErrorDescription());
		}

		callingProcessEnvironment = callingProcessEnvironmentResult.GetResultValue();
		environment = callingProcessEnvironment;
	}

	if (settings.GetInheritanceMode() != INHERITANCE_MODE_ON)
	{		
		Settings getEnvVarsProcessSettings(
			settings.GetUserName(),
			settings.GetDomain(),
			settings.GetPassword(),
			L"cmd.exe",
			settings.GetWorkingDirectory(),
			DEFAULT_EXIT_CODE_BASE,
			{ L"/U", L"/C", L"SET" },
			INHERITANCE_MODE_OFF);

		getEnvVarsProcessSettings.SetLogLevel(LOG_LEVEL_OFF);

		wstringstream getEnvVarsStream;
		StringWriter getEnvVarsWriter(getEnvVarsStream);
		StubWriter nulWriter;
		ProcessTracker getEnvVarsProcessTracker(getEnvVarsWriter, nulWriter);
		auto getEnvVarsResult = RunInternal(getEnvVarsProcessSettings, getEnvVarsProcessTracker, targetUserEnvironment);
		if (getEnvVarsResult.HasError() || getEnvVarsResult.GetResultValue() != 0)
		{
			return getEnvVarsResult;
		}

		targetUserEnvironment = Environment::CreateFormString(getEnvVarsStream.str());
		environment = targetUserEnvironment;
	}

	if (settings.GetInheritanceMode() == INHERITANCE_MODE_AUTO)
	{
		environment = Environment::Override(callingProcessEnvironment, targetUserEnvironment);
	}
	
	return RunInternal(settings, processTracker, environment);
}

Result<ExitCode> ProcessWithLogon::RunInternal(const Settings& settings, ProcessTracker& processTracker, Environment& environment)
{
	SECURITY_ATTRIBUTES securityAttributes = {};
	securityAttributes.nLength = sizeof(SECURITY_DESCRIPTOR);
	securityAttributes.bInheritHandle = true;

	STARTUPINFO startupInfo = {};
	PROCESS_INFORMATION processInformation = {};

	processTracker.Initialize(securityAttributes, startupInfo);
	auto cmdLine = settings.GetCommandLine();
	if (!CreateProcessWithLogonW(
		settings.GetUserName().c_str(),
		settings.GetDomain().c_str(),
		settings.GetPassword().c_str(),
		LOGON_WITH_PROFILE,
		nullptr,
		const_cast<LPWSTR>(cmdLine.c_str()),
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
