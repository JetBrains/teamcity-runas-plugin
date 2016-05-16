#pragma once
#include "IProcess.h"
#include "ExitCode.h"
#include "Result.h"
class Trace;
class Settings;

class ProcessWithLogon : public IProcess
{
	DWORD _logonFlags;
	Result<ExitCode> RunInternal(Trace& trace, const Settings& settings, ProcessTracker& processTracker, Environment& environment) const;

public:
	explicit ProcessWithLogon(DWORD logonFlags);
	Result<ExitCode> Run(const Settings& settings, ProcessTracker& processTracker) const override;	
};
