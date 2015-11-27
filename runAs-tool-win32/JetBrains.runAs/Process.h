#pragma once
#include "Handle.h"
#include "Pipe.h"
class Settings;

class Process
{
	Handle _securityTokenHandle = Handle(L"Security Token");
	Handle _primarySecurityTokenHandle = Handle(L"Primary Security Token");
	Handle _threadHandle = Handle(L"Thread");
	Handle _processHandle = Handle(L"Process");
	bool _isCreated = false;
	Pipe _stdOutPipe = Pipe(L"StdOut");
	Pipe _stdErrorOutPipe = Pipe(L"StdErrorOut");
	Pipe _stdInPipe = Pipe(L"StdIn");
	DWORD _exitCode = STILL_ACTIVE;

	static bool Redirect(HANDLE hPipeRead, HANDLE hOutput);

public:
	explicit Process(Settings settings);
	~Process();	
	bool IsCreated() const;
	int GetExitCode() const;
};