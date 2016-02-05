#include "stdafx.h"
#include "ProcessRunner.h"
#include <iostream>
#include "Environment.h"
#include "ProcessWithLogon.h"
#include "StreamWriter.h"

Result<ExitCode> ProcessRunner::Run(Settings settings) const
{
	// Get environment of parent process
	auto parentProcessEnvironmentResult = Environment::CreateForCurrentProcess();
	if (parentProcessEnvironmentResult.HasError())
	{
		return Result<ExitCode>(parentProcessEnvironmentResult.GetErrorCode(), parentProcessEnvironmentResult.GetErrorDescription());
	}

	auto parentProcessEnvironment = parentProcessEnvironmentResult.GetResultValue();
	Environment defaultEnvironment;

	// Send info to TeamCity
	SendTeamCityInfo(settings, parentProcessEnvironment);

	// Run process
	auto newProcessEnvironment = settings.GetInheritEnvironment() ? parentProcessEnvironment : defaultEnvironment;
	StreamWriter stdOutput(GetStdHandle(STD_OUTPUT_HANDLE));
	StreamWriter stdError(GetStdHandle(STD_ERROR_HANDLE));
	ProcessTracker processTracker(stdOutput, stdError);
	
	ProcessAsUser processAsUserToRun;
	auto runResult = processAsUserToRun.Run(settings, newProcessEnvironment, processTracker);
	if (!runResult.HasError())
	{
		return runResult.GetResultValue();
	}
	
	ProcessWithLogon processWithLogonToRun;
	return processWithLogonToRun.Run(settings, newProcessEnvironment, processTracker);
}

void ProcessRunner::SendTeamCityInfo(Settings& settings, Environment& environment)
{
	if (environment.TryGetValue(L"TEAMCITY_VERSION") != L"")
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