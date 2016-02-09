#include "stdafx.h"
#include "ProcessRunner.h"
#include "Environment.h"
#include "ProcessWithLogon.h"
#include "StreamWriter.h"

Result<ExitCode> ProcessRunner::Run(Settings settings) const
{
	// Get environment of parent process
	auto parentProcessEnvironmentResult = Environment::CreateForCurrentProcess();
	if (parentProcessEnvironmentResult.HasError())
	{
		return Result<ExitCode>(parentProcessEnvironmentResult.GetErrorCode(), parentProcessEnvironmentResult.GetErrorDescription());
	}

	auto parentProcessEnvironment = parentProcessEnvironmentResult.GetResultValue();
	Environment defaultEnvironment;

	// Run process
	auto newProcessEnvironment = settings.GetInheritEnvironment() ? parentProcessEnvironment : defaultEnvironment;
	StreamWriter stdOutput(GetStdHandle(STD_OUTPUT_HANDLE));
	StreamWriter stdError(GetStdHandle(STD_ERROR_HANDLE));
	ProcessTracker processTracker(stdOutput, stdError);
	
	ProcessAsUser processAsUserToRun;
	auto runResult = processAsUserToRun.Run(settings, newProcessEnvironment, processTracker);
	if (!runResult.HasError() && runResult.GetResultValue() != STATUS_DLL_INIT_FAILED)
	{
		return runResult.GetResultValue();
	}
	
	ProcessWithLogon processWithLogonToRun;
	return processWithLogonToRun.Run(settings, newProcessEnvironment, processTracker);
}