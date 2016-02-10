#include "stdafx.h"
#include "Settings.h"
#include "ExitCode.h"
#include <sstream>

Settings::Settings(): _exitCodeBase(DEFAULT_EXIT_CODE_BASE)
{
}

Settings::Settings(const std::wstring userName, const std::wstring domain, const std::wstring password, const std::wstring executable, const std::wstring workingDirectory, int exitCodeBase, std::list<std::wstring> args, const bool inheritEnvironment)
{
	_userName = userName;
	_domain = domain;
	_password = password;
	_executable = executable;
	_workingDirectory = workingDirectory;
	_exitCodeBase = exitCodeBase;
	_args = std::list<std::wstring>(args);
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
	return AddQuotes(_executable);	
}

std::wstring Settings::GetCommandLine() const
{
	std::wstringstream commandLine;
	commandLine << AddQuotes(_executable);
	for (auto argsIterrator = _args.begin(); argsIterrator != _args.end(); ++argsIterrator)
	{
		commandLine << L" " << AddQuotes(*argsIterrator);
	}
	
	return commandLine.str();
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

std::wstring Settings::AddQuotes(std::wstring str)
{
	if (str.find(L' ') != std::string::npos && str.size() > 0 && !(str[0] == L'\"' && str[str.size() - 1] == L'\"'))
	{
		return L'\"' + str + L'\"';
	}

	return str;
}