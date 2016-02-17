#pragma once
#include "IProcess.h"
#include "Settings.h"
#include "ExitCode.h"
#include "Result.h"
class Settings;

class ProcessAsUser: public IProcess
{		
public:	
	virtual Result<ExitCode> Run(Settings& settings, ProcessTracker& processTracker) const override;
};