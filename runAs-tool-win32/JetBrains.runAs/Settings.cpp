#include "stdafx.h"
#include "Settings.h"
#include "ExitCode.h"

Settings::Settings(): _exitCodeBase(DEFAULT_EXIT_CODE_BASE)
{
}

Settings::Settings(const std::wstring userName, const std::wstring domain, const std::wstring password, const std::wstring executable, const std::wstring workingDirectory, int exitCodeBase, const std::wstring args, const bool inheritEnvironment)
{
	_userName = userName;
	_domain = domain;
	_password = password;
	_executable = executable;
	_workingDirectory = workingDirectory;
	_exitCodeBase = exitCodeBase;
	_args = args;
	_inheritEnvironment = inheritEnvironment;
}

std::wstring Settings::GetUserName() const
{	
	return _userName;
}

std::wstring Settings::GetDomain() const
{
	return _domain;
}


std::wstring Settings::GetPassword() const
{
	return _password;
}

std::wstring Settings::GetExecutable() const
{
	return _executable;
}

std::wstring Settings::GetCommandLine() const
{
	return _executable + (_args.size() > 0 ? L" " + _args : L"");
}


std::wstring Settings::GetWorkingDirectory() const
{
	return _workingDirectory;
}

int Settings::GetExitCodeBase() const
{
	return _exitCodeBase;
}

bool Settings::GetInheritEnvironment() const
{
	return _inheritEnvironment;
}
