#pragma once
#include <string>

class Settings
{
	std::wstring _userName = L"";
	std::wstring _domain = L"";
	std::wstring _password = L"";
	std::wstring _executable = L"";
	std::wstring _workingDirectory = L"";
	std::wstring _args = L"";
	int _exitCodeBase = 0;	
	bool _inheritEnvironment = true;

public:	
	Settings();
	Settings(const std::wstring userName, const std::wstring domain, const std::wstring password, const std::wstring executable, const std::wstring workingDirectory, int exitCodeBase, const std::wstring args, const bool inheritEnvironment);
	std::wstring GetUserName() const;
	std::wstring GetDomain() const;
	std::wstring GetPassword() const;
	std::wstring GetExecutable() const;
	std::wstring GetCommandLine() const;
	std::wstring GetWorkingDirectory() const;
	int GetExitCodeBase() const;
	bool GetInheritEnvironment() const;
};