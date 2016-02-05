#include "stdafx.h"
#include "ProcessRunner.h"
#include <iostream>
#include "Environment.h"
#include "ProcessWithLogon.h"
#include <sstream>
#include "StringWriter.h"
#include "StubWriter.h"
#include "StreamWriter.h"

ProcessRunner::ProcessRunner()
{
}


ProcessRunner::~ProcessRunner()
{
}

Result<ExitCode> ProcessRunner::Run(Settings settings) const
{
	// Get current environment
	auto currentUserEnvironment = Environment::CreateForCurrentProcess();
	if (currentUserEnvironment.HasError())
	{
		return Result<ExitCode>(currentUserEnvironment.GetErrorCode(), currentUserEnvironment.GetErrorDescription());
	}

	auto currentEnvironment = currentUserEnvironment.GetResultValue();

	// Get new user's environment
	TCHAR path[MAX_PATH];
	GetCurrentDirectory(MAX_PATH, path);

	Settings getEnvVarsProcessSettings(
		settings.GetUserName(),
		settings.GetDomain(),
		settings.GetPassword(),
		L"cmd.exe",
		path,
		DEFAULT_EXIT_CODE_BASE,
		L"/C SET");

	ProcessWithLogon processWithLogonToGetEnvVars;
	ProcessWithLogon processWithLogonToRun;
	ProcessAsUser processAsUserToGetEnvVars;
	ProcessAsUser processAsUserToRun;
	IProcess* processToGetEnvVars = &processAsUserToGetEnvVars;
	IProcess* processToRun = &processAsUserToRun;

	Environment emptyEnvironment;
	std::stringstream envVars;
	StringWriter envVarsWriter(envVars);
	StubWriter stubWriter;
	ProcessTracker getEnvVarsProcessTracker(envVarsWriter, stubWriter);
	auto getEnvVarsExitCode = processToGetEnvVars->Run(getEnvVarsProcessSettings, emptyEnvironment, getEnvVarsProcessTracker);
	if (getEnvVarsExitCode.HasError() || getEnvVarsExitCode.GetResultValue() == STATUS_DLL_INIT_FAILED)
	{
		processToGetEnvVars = &processWithLogonToGetEnvVars;
		processToRun = &processWithLogonToRun;

		getEnvVarsExitCode = processToGetEnvVars->Run(getEnvVarsProcessSettings, emptyEnvironment, getEnvVarsProcessTracker);
		if (getEnvVarsExitCode.HasError())
		{
			return Result<ExitCode>(getEnvVarsExitCode.GetErrorCode(), getEnvVarsExitCode.GetErrorDescription());
		}
	}

	auto envVarsStr = envVars.str();
	auto newUserEnvironmetResult = Environment::CreateFormString(std::wstring(envVarsStr.begin(), envVarsStr.end()));
	if (newUserEnvironmetResult.HasError())
	{
		return Result<ExitCode>(newUserEnvironmetResult.GetErrorCode(), newUserEnvironmetResult.GetErrorDescription());
	}

	auto newUserEnvironmet = newUserEnvironmetResult.GetResultValue();

	// Merge environments
	auto mergedEnvironment = Environment::Merge(currentEnvironment, newUserEnvironmet);

	// Show TeamCity info
	ShowTeamCityInfo(settings, currentEnvironment);

	// Run process
	StreamWriter stdOutput(GetStdHandle(STD_OUTPUT_HANDLE));
	StreamWriter stdError(GetStdHandle(STD_ERROR_HANDLE));
	ProcessTracker executableProcessTracker(stdOutput, stdError);
	return processToRun->Run(settings, mergedEnvironment, executableProcessTracker);
}

void ProcessRunner::ShowTeamCityInfo(Settings& settings, Environment& currentEnvironment)
{
	if (currentEnvironment.TryGetValue(L"TEAMCITY_VERSION") != L"")
	{
		std::wcout << CreateTeamCityMessage(L"Starting: " + settings.GetCommandLine());
		std::wcout << std::endl;
		std::wcout << CreateTeamCityMessage(L"in directory: " + settings.GetWorkingDirectory());
		std::wcout << std::endl;
	}
}

std::wstring ProcessRunner::CreateTeamCityMessage(std::wstring text)
{
	return L"##teamcity[message text = '" + text + L"']";
}