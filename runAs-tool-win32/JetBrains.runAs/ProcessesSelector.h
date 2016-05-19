#pragma once
#include <queue>
#include "IProcess.h"
#include "ProcessAsUser.h"
#include "ProcessWithLogon.h"

class ProcessesSelector
{
	const ProcessAsUser _processAsUser;
	const ProcessWithLogon _processWithLogonElevated = ProcessWithLogon(true);
	const ProcessWithLogon _processWithLogon = ProcessWithLogon(false);

public:
	ProcessesSelector();
	const Result<queue<const IProcess*>> SelectProcesses(const Settings& settings) const;
};

