#pragma once
#include "ProcessTracker.h"
#include "ProcessAsUser.h"
#include "ProcessWithLogon.h"

class Job;

class Runner
{
	ProcessAsUser _processAsUserToRun;
	ProcessWithLogon _processWithLogonToRun = ProcessWithLogon(LOGON_NETCREDENTIALS_ONLY);
	ProcessWithLogon _processWithLogonToRunWithProfile = ProcessWithLogon(LOGON_WITH_PROFILE);
	list<IProcess*> _processes;
	Result<ExitCode> RunProcessAsUser(const Settings& settings) const;

public:
	Runner();
	Result<ExitCode> Run(const Settings& settings) const;
};
