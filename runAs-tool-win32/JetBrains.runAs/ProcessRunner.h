#pragma once
#include "ProcessTracker.h"
#include "Environment.h"
#include "StreamWriter.h"
#include "StubWriter.h"
#include <sstream>
#include "StringWriter.h"
class StringWriter;
class StubWriter;
class StreamWriter;
class IProcess;

template<class TProcess>
class ProcessRunner: std::conditional<std::is_base_of<IProcess, TProcess>::value, std::true_type, std::false_type>::type
{
	static std::wstring CreateTeamCityMessage(std::wstring text);

public:
	ProcessRunner();
	~ProcessRunner();
	
	Result<ExitCode> Run(Settings settings) const;	
};

template<typename TProcess>
ProcessRunner<TProcess>::ProcessRunner()
{
}


template<typename TProcess>
ProcessRunner<TProcess>::~ProcessRunner()
{
}

template <typename TProcess>
Result<ExitCode> ProcessRunner<TProcess>::Run(Settings settings) const
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

	TProcess getEnvVarsProcessValue;
	auto getEnvVarsProcess = static_cast<IProcess*>(&getEnvVarsProcessValue);
	Environment emptyEnvironment;
	std::stringstream envVars;	
	StringWriter envVarsWriter(envVars);
	StubWriter stubWriter;
	ProcessTracker getEnvVarsProcessTracker(envVarsWriter, stubWriter);
	auto getEnvVarsExitCode = getEnvVarsProcess->Run(getEnvVarsProcessSettings, emptyEnvironment, getEnvVarsProcessTracker);
	if (getEnvVarsExitCode.HasError())
	{
		return Result<ExitCode>(getEnvVarsExitCode.GetErrorCode(), getEnvVarsExitCode.GetErrorDescription());
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
	if (currentEnvironment.TryGetValue(L"TEAMCITY_VERSION") != L"")
	{
		std::wcout << CreateTeamCityMessage(L"Starting: " + settings.GetCommandLine());
		std::wcout << std::endl;
		std::wcout << CreateTeamCityMessage(L"in directory: " + settings.GetWorkingDirectory());
		std::wcout << std::endl;
	}

	// Run process
	TProcess executableProcessValue;
	auto executableProcess = static_cast<IProcess*>(&executableProcessValue);
	StreamWriter stdOutput(GetStdHandle(STD_OUTPUT_HANDLE));
	StreamWriter stdError(GetStdHandle(STD_ERROR_HANDLE));
	ProcessTracker executableProcessTracker(stdOutput, stdError);
	return executableProcess->Run(settings, mergedEnvironment, executableProcessTracker);
}

template <typename TProcess>
std::wstring ProcessRunner<TProcess>::CreateTeamCityMessage(std::wstring text)
{
	return L"##teamcity[message text = '" + text + L"']";
}