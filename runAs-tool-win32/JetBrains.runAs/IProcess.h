#pragma once
#include "Result.h"
#include "Settings.h"
#include "ExitCode.h"

class ProcessTracker;
class Environment;

class IProcess
{
public:
	virtual ~IProcess()	{}

	virtual Result<ExitCode> Run(Settings& settings, Environment& environment, ProcessTracker& processTracker) const = 0;
};
