#pragma once
#include "IProcess.h"
#include "ExitCode.h"
#include "Result.h"
class Settings;

class ProcessWithLogon : public IProcess
{
	static Result<ExitCode> RunInternal(const Settings& settings, ProcessTracker& processTracker, Environment& environment);

public:
	virtual Result<ExitCode> Run(const Settings& settings, ProcessTracker& processTracker) const override;	
};
