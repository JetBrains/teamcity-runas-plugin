#include "stdafx.h"
#include "ProcessRunner.h"
#include "StreamWriter.h"
#include "Trace.h"
#include "Job.h"

ProcessRunner::ProcessRunner()
{
	_processes.push_back(&_processAsUserToRun);
	_processes.push_back(&_processWithLogonToRun);
}

Result<ExitCode> ProcessRunner::Run(const Settings& settings) const
{		
	Trace trace(settings.GetLogLevel());

	trace < L"ProcessWithLogon::Create a job";	
	Job job(false);
	JOBOBJECT_EXTENDED_LIMIT_INFORMATION jobObjectInfo = {};
	jobObjectInfo.BasicLimitInformation.LimitFlags = JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE;
	trace < L"ProcessWithLogon::Configure all child processes associated with the job to terminate when the parent is terminated";
	trace < L"Job::SetInformation";	
	job.SetInformation(JobObjectExtendedLimitInformation, jobObjectInfo);

	trace < L"ProcessWithLogon::Assign the current process to the job";
	trace < L"Job::AssignProcessToJob";
	// ReSharper disable once CppInitializedValueIsAlwaysRewritten
	Handle currentProcess(L"Current process");
	currentProcess = GetCurrentProcess();
	job.AssignProcessToJob(currentProcess);
	
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
			trace < L"ProcessRunner::Run error code: ";
			trace << runResult.GetErrorCode();
			trace < L"ProcessRunner::Run error description: ";
			trace << runResult.GetErrorDescription();
			continue;
		}
		
		if (runResult.GetResultValue() != STATUS_DLL_INIT_FAILED)
		{
			break;
		}
	}

	trace < L"ProcessRunner::Run finished";
	return runResult;	
}
