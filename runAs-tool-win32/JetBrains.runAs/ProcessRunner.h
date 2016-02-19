#pragma once
#include "ProcessTracker.h"
#include "ProcessAsUser.h"
#include "ProcessWithLogon.h"

class ProcessRunner
{
	ProcessAsUser _processAsUserToRun;
	ProcessWithLogon _processWithLogonToRun;
	list<IProcess*> _processes;

public:
	ProcessRunner();
	Result<ExitCode> Run(const Settings& settings) const;
};
