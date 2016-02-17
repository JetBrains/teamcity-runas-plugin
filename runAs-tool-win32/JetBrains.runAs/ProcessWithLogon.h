#pragma once
#include "IProcess.h"
#include "ExitCode.h"
#include "Result.h"
class Settings;

class ProcessWithLogon : public IProcess
{	
public:
	virtual Result<ExitCode> Run(Settings& settings, ProcessTracker& processTracker) const override;
};
