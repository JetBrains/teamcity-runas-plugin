#include "stdafx.h"
#include "ProcessesSelector.h"
#include <queue>
#include "IProcess.h"
#include "SelfTest.h"

ProcessesSelector::ProcessesSelector()
{
}

const Result<queue<const IProcess*>> ProcessesSelector::SelectProcesses(const Settings& settings) const
{
	Trace trace(settings.GetLogLevel());
	trace < L"ProcessesSelector::SelectProcesses";

	const SelfTest selfTest;
	auto selfTestStatisticResult = selfTest.GetStatistic(settings);
	if (selfTestStatisticResult.HasError())
	{
		return selfTestStatisticResult.GetError();
	}

	auto selfTestStatistic = selfTestStatisticResult.GetResultValue();
	auto hasMoreThenHighIntegrityLevel = (selfTestStatistic.GetIntegrityLevel() == INTEGRITY_LEVEL_SYSTEM || selfTestStatistic.GetIntegrityLevel() == INTEGRITY_LEVEL_HIGH);

	queue<const IProcess*> processes;	
	if (selfTestStatistic.HasSeAssignPrimaryTokenPrivilege() && hasMoreThenHighIntegrityLevel)
	{
		trace < L"ProcessesSelector::SelectProcesses push ProcessAsUser";
		processes.push(&_processAsUser);
	}

	if (selfTestStatistic.HasLogonSid() && selfTestStatistic.HasAdministrativePrivileges() && settings.GetLogonType() != LOGON_TYPE_INTERACTIVE)
	{
		trace < L"ProcessesSelector::SelectProcesses push ProcessWithLogonElevated";
		processes.push(&_processWithLogonElevated);
	}

	trace < L"ProcessesSelector::SelectProcesses push ProcessWithLogon";
	processes.push(&_processWithLogon);
	return processes;
}
