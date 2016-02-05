#pragma once
#include "IProcess.h"
#include "Settings.h"
#include "ExitCode.h"
#include "Result.h"
#include "Handle.h"
class Settings;

class ProcessAsUser: public IProcess
{	
	static Result<ExitCode> SetPrivileges(Handle& hToken);

public:	
	virtual Result<ExitCode> Run(Settings& settings, Environment& environment, ProcessTracker& processTracker) const override;
};