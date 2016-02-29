#include "stdafx.h"
#include "ProcessRunner.h"
#include "StreamWriter.h"
#include "Trace.h"

ProcessRunner::ProcessRunner()
{
	_processes.push_back(&_processAsUserToRun);
	_processes.push_back(&_processWithLogonToRun);
}

Result<ExitCode> ProcessRunner::Run(const Settings& settings) const
{		
	Trace trace(settings.GetLogLevel());

	// Run process
	trace < L"::GetStdHandle(STD_OUTPUT_HANDLE)";
	StreamWriter stdOutput(GetStdHandle(STD_OUTPUT_HANDLE));
	trace < L"::GetStdHandle(STD_ERROR_HANDLE)";
	StreamWriter stdError(GetStdHandle(STD_ERROR_HANDLE));
	auto runResult = Result<ExitCode>(ERROR_CODE_UNKOWN, L"The processes are not available.");
	for (auto processIterrator = _processes.begin(); processIterrator != _processes.end(); ++processIterrator)
	{
		if (processIterrator != _processes.begin())
		{
			trace < L"ProcessRunner::Select other type of process";
		}

		ProcessTracker processTracker(stdOutput, stdError);
		runResult = (*processIterrator)->Run(settings, processTracker);
		if (runResult.HasError())
		{
			trace < L"ProcessRunner::Run failed";
			trace < L"ProcessRunner::Run error code:";
			trace << runResult.GetErrorCode();
			trace < L"ProcessRunner::Run error description:";
			trace << runResult.GetErrorDescription();
		}
		
		if (!runResult.HasError() && runResult.GetResultValue() != STATUS_DLL_INIT_FAILED)
		{
			break;
		}
	}

	return runResult;	
}
