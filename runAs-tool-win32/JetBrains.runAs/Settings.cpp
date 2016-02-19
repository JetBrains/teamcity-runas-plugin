#include "stdafx.h"
#include "Settings.h"
#include "ExitCode.h"
#include <sstream>

Settings::Settings(): _exitCodeBase(DEFAULT_EXIT_CODE_BASE)
{
}

Settings::Settings(
	const wstring userName,
	const wstring domain,
	const wstring password,
	const wstring executable,
	const wstring workingDirectory,
	int exitCodeBase,
	list<wstring> args,
	const InheritanceMode inheritanceMode)
{
	_userName = userName;
	_domain = domain;
	_password = password;
	_executable = executable;
	_workingDirectory = workingDirectory;
	_exitCodeBase = exitCodeBase;
	_args = list<wstring>(args);
	_inheritanceMode = inheritanceMode;
}

wstring Settings::GetUserName() const
{	
	return _userName;
}

wstring Settings::GetDomain() const
{
	return _domain;
}


wstring Settings::GetPassword() const
{
	return _password;
}

wstring Settings::GetExecutable() const
{
	return AddQuotes(_executable);	
}

wstring Settings::GetCommandLine() const
{
	wstringstream commandLine;
	commandLine << AddQuotes(_executable);
	for (auto argsIterrator = _args.begin(); argsIterrator != _args.end(); ++argsIterrator)
	{
		commandLine << L" " << AddQuotes(*argsIterrator);
	}
	
	return commandLine.str();
}


wstring Settings::GetWorkingDirectory() const
{
	return _workingDirectory;
}

int Settings::GetExitCodeBase() const
{
	return _exitCodeBase;
}

InheritanceMode Settings::GetInheritanceMode() const
{
	return _inheritanceMode;
}

LogLevel Settings::GetLogLevel() const
{
	return _logLevel;
}

void Settings::SetLogLevel(LogLevel logLevel)
{
	_logLevel = logLevel;
}

wstring Settings::AddQuotes(wstring str)
{
	if (str.find(L' ') != string::npos && str.size() > 0 && !(str[0] == L'\"' && str[str.size() - 1] == L'\"'))
	{
		return L'\"' + str + L'\"';
	}

	return str;
}
