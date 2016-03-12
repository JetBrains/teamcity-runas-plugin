#pragma once
#include "ProcessTracker.h"
#include "ProcessAsUser.h"
#include "ProcessWithLogon.h"

class Job;

class Runner
{
	ProcessAsUser _processAsUserToRun;
	ProcessWithLogon _processWithLogonToRun;
	list<IProcess*> _processes;
	Result<ExitCode> RunProcessAsUser(const Settings& settings) const;

public:
	Runner();
	Result<ExitCode> Run(const Settings& settings) const;
};
