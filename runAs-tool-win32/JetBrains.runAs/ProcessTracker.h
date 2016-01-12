#pragma once
#include "Pipe.h"

class ProcessTracker
{
	Pipe _stdOutPipe = Pipe(L"StdOut");
	Pipe _stdErrorOutPipe = Pipe(L"StdErrorOut");
	Pipe _stdInPipe = Pipe(L"StdIn");	

	static bool RedirectStream(HANDLE hPipeRead, HANDLE hOutput);

public:
	ProcessTracker(SECURITY_ATTRIBUTES& securityAttributes, STARTUPINFO& startupInfo);
	DWORD WaiteForExit(HANDLE processHandle);
};

