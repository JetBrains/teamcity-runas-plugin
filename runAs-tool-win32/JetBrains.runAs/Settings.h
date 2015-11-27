#pragma once
#include <string>
#include <list>

class Settings
{
	std::wstring _userName;
	std::wstring _domain;
	std::wstring _password;
	std::wstring _executable;
	std::wstring _workingDirectory;
	std::list<std::wstring> _args;

public:
	Settings();
	Settings(const std::wstring userName, const std::wstring domain, const std::wstring password, const std::wstring executable, const std::wstring workingDirectory, const std::list<std::wstring> args);
	std::wstring GetUserName() const;
	std::wstring GetDomain() const;
	std::wstring GetPassword() const;
	std::wstring GetExecutable() const;
	std::wstring GetCommandLine() const;
	std::wstring GetWorkingDirectory() const;
};