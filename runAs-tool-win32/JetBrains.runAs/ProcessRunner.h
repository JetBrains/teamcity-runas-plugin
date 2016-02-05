#pragma once
#include "ProcessTracker.h"
#include "ProcessAsUser.h"

class ProcessRunner
{
	static std::wstring CreateTeamCityMessage(std::wstring text);
	static void SendTeamCityInfo(Settings& settings, Environment& currentEnvironment);

public:
	Result<ExitCode> Run(Settings settings) const;	
};
