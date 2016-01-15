#pragma once
#include "Pipe.h"
#include "ExitCode.h"
#include "IStreamWriter.h"

class IStreamWriter;

class ProcessTracker
{
	Pipe _stdOutPipe = Pipe(L"StdOut");
	Pipe _stdErrorOutPipe = Pipe(L"StdErrorOut");
	Pipe _stdInPipe = Pipe(L"StdIn");	
	IStreamWriter& _outputWriter;
	IStreamWriter& _errorWriter;

	static Result<bool> RedirectStream(HANDLE hPipeRead, IStreamWriter& writer);

public:
	ProcessTracker(IStreamWriter& outputWriter, IStreamWriter& errorWriter);
	Result<bool> Initialize(SECURITY_ATTRIBUTES& securityAttributes, STARTUPINFO& startupInfo);
	Result<ExitCode> WaiteForExit(HANDLE processHandle);
};

