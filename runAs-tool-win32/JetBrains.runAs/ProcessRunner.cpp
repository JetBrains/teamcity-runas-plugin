#include "stdafx.h"
#include "ProcessRunner.h"
#include "ProcessWithLogon.h"
#include "StreamWriter.h"
#include <queue>

Result<ExitCode> ProcessRunner::Run(const Settings& settings) const
{	
	ProcessAsUser processAsUserToRun;
	ProcessWithLogon processWithLogonToRun;
	queue<IProcess*> processes;
	processes.push(&processAsUserToRun);
	processes.push(&processWithLogonToRun);
	
	// Run process
	StreamWriter stdOutput(GetStdHandle(STD_OUTPUT_HANDLE));
	StreamWriter stdError(GetStdHandle(STD_ERROR_HANDLE));
	auto runResult = Result<ExitCode>(ERROR_CODE_UNKOWN, L"The processes are not available.");
	while (processes.size() > 0)
	{
		ProcessTracker processTracker(stdOutput, stdError);
		runResult = processes.front()->Run(settings, processTracker);
		if (runResult.HasError() || runResult.GetResultValue() != STATUS_DLL_INIT_FAILED)
		{
			processes.pop();
		}
	}

	return runResult;	
}
