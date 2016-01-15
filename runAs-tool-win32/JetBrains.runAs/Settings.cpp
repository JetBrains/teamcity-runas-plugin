#include "stdafx.h"
#include "Settings.h"
#include "ExitCode.h"

Settings::Settings(): _exitCodeBase(DEFAULT_EXIT_CODE_BASE)
{
}

Settings::Settings(const std::wstring userName, const std::wstring domain, const std::wstring password, const std::wstring executable, const std::wstring workingDirectory, int exitCodeBase, const std::wstring args)
{
	_userName = userName;
	_domain = domain;
	_password = password;
	_executable = executable;
	_workingDirectory = workingDirectory;
	_exitCodeBase = exitCodeBase;
	_args = args;
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
	return _executable + L" " + _args;
}


std::wstring Settings::GetWorkingDirectory() const
{
	return _workingDirectory;
}

int Settings::GetExitCodeBase() const
{
	return _exitCodeBase;
}
