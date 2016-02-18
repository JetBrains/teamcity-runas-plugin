#include "stdafx.h"
#include "ProcessRunner.h"
#include "ProcessWithLogon.h"
#include "StreamWriter.h"

Result<ExitCode> ProcessRunner::Run(Settings settings) const
{
	// Run process
	StreamWriter stdOutput(GetStdHandle(STD_OUTPUT_HANDLE));
	StreamWriter stdError(GetStdHandle(STD_ERROR_HANDLE));
	ProcessTracker processTracker(stdOutput, stdError);
	
	ProcessAsUser processAsUserToRun;
	auto runResult = processAsUserToRun.Run(settings, processTracker);
	if (!runResult.HasError() && runResult.GetResultValue() != STATUS_DLL_INIT_FAILED)
	{
		return runResult.GetResultValue();
	}
	
	ProcessWithLogon processWithLogonToRun;
	return processWithLogonToRun.Run(settings, processTracker);
}
