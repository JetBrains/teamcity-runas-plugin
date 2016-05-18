#pragma once
#include "ProcessTracker.h"
#include "ProcessAsUser.h"
#include "ProcessWithLogon.h"

class Job;

class Runner
{
	ProcessAsUser _processAsUserToRun;
	ProcessWithLogon _processWithLogonElevated = ProcessWithLogon(true);
	ProcessWithLogon _processWithLogonInteractive = ProcessWithLogon(false);
	list<IProcess*> _processes;
	Result<ExitCode> RunProcessAsUser(const Settings& settings) const;

public:
	Runner(const Settings& settings);
	Result<ExitCode> Run(const Settings& settings) const;
};
