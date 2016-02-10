#pragma once
#include <string>
#include <list>

class Settings
{
	std::wstring _userName = L"";
	std::wstring _domain = L"";
	std::wstring _password = L"";
	std::wstring _executable = L"";
	std::wstring _workingDirectory = L"";
	std::list<std::wstring> _args;
	int _exitCodeBase = 0;	
	bool _inheritEnvironment = true;

	static std::wstring AddQuotes(std::wstring str);

public:	
	Settings();
	Settings(const std::wstring userName, const std::wstring domain, const std::wstring password, const std::wstring executable, const std::wstring workingDirectory, int exitCodeBase, const std::list<std::wstring> args, const bool inheritEnvironment);
	std::wstring GetUserName() const;
	std::wstring GetDomain() const;
	std::wstring GetPassword() const;
	std::wstring GetExecutable() const;
	std::wstring GetCommandLine() const;
	std::wstring GetWorkingDirectory() const;
	int GetExitCodeBase() const;
	bool GetInheritEnvironment() const;	
};
