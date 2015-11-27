#include "stdafx.h"
#include "Settings.h"
#include <iterator>
#include <sstream>

Settings::Settings()
{
}

Settings::Settings(const std::wstring userName, const std::wstring domain, const std::wstring password, const std::wstring executable, const std::wstring workingDirectory, const std::list<std::wstring> args)
{
	_userName = userName;
	_domain = domain;
	_password = password;
	_executable = executable;
	_workingDirectory = workingDirectory;
	copy(args.begin(), args.end(), back_inserter(_args));	
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
	std::wstringstream result;
	result << _executable;
	for (auto iterator = _args.begin(); iterator != _args.end(); ++iterator)
	{
		result << " ";
		result << *iterator;
	}

	return result.str();
}


std::wstring Settings::GetWorkingDirectory() const
{
	return _workingDirectory;
}