#pragma once
#include "ProcessTracker.h"
#include "ProcessAsUser.h"
class StringWriter;
class StubWriter;
class StreamWriter;
class IProcess;

class ProcessRunner
{
	static std::wstring CreateTeamCityMessage(std::wstring text);
	static void ShowTeamCityInfo(Settings& settings, Environment& currentEnvironment);

public:
	ProcessRunner();
	~ProcessRunner();	
	Result<ExitCode> Run(Settings settings) const;	
};
