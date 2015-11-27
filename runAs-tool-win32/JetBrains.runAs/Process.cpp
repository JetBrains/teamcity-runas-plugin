#include "stdafx.h"
#include "Process.h"
#include "Settings.h"
#include <iostream>
#include "ErrorUtilities.h"

Process::Process(Settings settings)	
{
	// Attempt to log a user on to the local computer
	if (!LogonUser(
		settings.GetUserName().c_str(),
		settings.GetDomain().c_str(),
		settings.GetPassword().c_str(),
		LOGON32_LOGON_NETWORK,
		LOGON32_PROVIDER_DEFAULT,
		&_securityTokenHandle.GetHandle()))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"LogonUser");
		return;
	}

	// Initialize a new security descriptor
	SECURITY_DESCRIPTOR securityDescriptor = {};
	if (!InitializeSecurityDescriptor(
		&securityDescriptor,
		SECURITY_DESCRIPTOR_REVISION))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"InitializeSecurityDescriptor");
		return;
	}

	if (!SetSecurityDescriptorDacl(
		&securityDescriptor,
		true,
		nullptr,
		false))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"SetSecurityDescriptorDacl");
		return;
	}

	// Creates a new access token that duplicates an existing token
	SECURITY_ATTRIBUTES processSecAttributes = {};
	processSecAttributes.lpSecurityDescriptor = &securityDescriptor;
	processSecAttributes.nLength = sizeof(SECURITY_DESCRIPTOR);
	processSecAttributes.bInheritHandle = true;
	if (!DuplicateTokenEx(
		_securityTokenHandle.GetHandle(),
		0,
		&processSecAttributes,
		SecurityImpersonation,
		TokenPrimary,
		&_primarySecurityTokenHandle.GetHandle()))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"DuplicateTokenEx");
		return;
	}

	// Create a new process and its primary thread. The new process runs in the security context of the user represented by the specified token.
	SECURITY_ATTRIBUTES threadSecAttributes = {};
	threadSecAttributes.lpSecurityDescriptor = nullptr;
	threadSecAttributes.nLength = 0;
	threadSecAttributes.bInheritHandle = false;

	// Initialize pipes
	_stdOutPipe.Initialize(processSecAttributes);
	_stdErrorOutPipe.Initialize(processSecAttributes);
	_stdInPipe.Initialize(processSecAttributes);
	
	STARTUPINFO startupInfo = {};
	startupInfo.hStdOutput = _stdOutPipe.GetWriter().GetHandle();
	startupInfo.hStdError = _stdErrorOutPipe.GetWriter().GetHandle();
	startupInfo.hStdInput = _stdInPipe.GetReader().GetHandle();
	startupInfo.dwFlags = STARTF_USESTDHANDLES;	

	PROCESS_INFORMATION processInformation = {};
	if (!CreateProcessAsUser(
		_primarySecurityTokenHandle.GetHandle(),
		nullptr,
		const_cast<LPWSTR>(settings.GetCommandLine().c_str()),
		&processSecAttributes,
		&threadSecAttributes,
		true,
		CREATE_NO_WINDOW | INHERIT_PARENT_AFFINITY | CREATE_NEW_CONSOLE,
		nullptr,
		const_cast<LPWSTR>(settings.GetWorkingDirectory().c_str()),
		&startupInfo,
		&processInformation))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"CreateProcessAsUser");
		return;
	}

	_processHandle.GetHandle() = processInformation.hProcess;
	_threadHandle.GetHandle() = processInformation.hThread;
	_isCreated = true;

	auto hStdOutput = GetStdHandle(STD_OUTPUT_HANDLE);
	auto hStdError = GetStdHandle(STD_ERROR_HANDLE);
	do
	{
		if (!Redirect(_stdOutPipe.GetReader().GetHandle(), hStdOutput))
		{
			return;
		}

		if (!Redirect(_stdErrorOutPipe.GetReader().GetHandle(), hStdError))
		{
			return;
		}

		if (!GetExitCodeProcess(processInformation.hProcess, &_exitCode))
		{
			std::wcerr << ErrorUtilities::GetLastErrorMessage(L"GetExitCodeProcess");
			return;
		}
	} 
	while (_exitCode == STILL_ACTIVE);
}


Process::~Process()
{	
}

bool Process::IsCreated() const
{
	return _isCreated;
}

int Process::GetExitCode() const
{
	return _exitCode;
}

bool Process::Redirect(HANDLE hPipeRead, HANDLE hOutput)
{
	CHAR buffer[256];
	DWORD bytesReaded;
	DWORD bytesWritten;
	DWORD totalBytesAvail;
	DWORD bytesLeftThisMessage;

	if (!PeekNamedPipe(hPipeRead, buffer, sizeof(buffer), &bytesReaded, &totalBytesAvail, &bytesLeftThisMessage))
	{	
		if (GetLastError() == ERROR_BROKEN_PIPE)
		{
			// Pipe done - normal exit path.
			return true;
		}

		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"PeekNamedPipe");
		return false;
	}

	if (totalBytesAvail == 0)
	{
		return true;
	}

	if (!ReadFile(hPipeRead, buffer, bytesReaded, &bytesReaded, nullptr) || !bytesReaded)
	{
		if (GetLastError() == ERROR_BROKEN_PIPE)
		{
			// Pipe done - normal exit path.
			return true;
		}

		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"ReadFile");
		return false;
	}	

	if (!WriteFile(hOutput, buffer, bytesReaded, &bytesWritten, nullptr))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"WriteConsole");
		return false;
	}

	return true;
}
