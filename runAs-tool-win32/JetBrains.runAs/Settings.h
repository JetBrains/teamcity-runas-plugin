#pragma once
#include <string>
#include <list>
#include "LogLevel.h"
#include "InheritanceMode.h"

class Settings
{
	wstring _userName = L"";
	wstring _domain = L"";
	wstring _password = L"";
	wstring _executable = L"";
	wstring _workingDirectory = L"";
	list<wstring> _args;
	int _exitCodeBase = 0;
	InheritanceMode _inheritanceMode = INHERITANCE_MODE_AUTO;
	LogLevel _logLevel = LOG_LEVEL_NORMAL;

	static wstring AddQuotes(wstring str);

public:
	Settings();
	Settings(
		const wstring userName,
		const wstring domain,
		const wstring password,
		const wstring executable,
		const wstring workingDirectory,
		int exitCodeBase,
		const list<wstring> args,
		const InheritanceMode inheritanceMode);
	wstring GetUserName() const;
	wstring GetDomain() const;
	wstring GetPassword() const;
	wstring GetExecutable() const;
	wstring GetCommandLine() const;
	wstring GetWorkingDirectory() const;
	int GetExitCodeBase() const;
	InheritanceMode GetInheritanceMode() const;
	LogLevel GetLogLevel() const;
	void SetLogLevel(LogLevel logLevel);
};
