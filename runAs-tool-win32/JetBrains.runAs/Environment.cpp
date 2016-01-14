#include "stdafx.h"
#include "Environment.h"
#include "ErrorUtilities.h"
#include <iostream>
#include <regex>
#include <set>

static const std::set<std::wstring> Overrides = {
	L"appdata",
	L"clientname",
	L"homedrive",
	L"homepath",
	L"localappdata",
	L"logonserver",
	L"userdomain",
	L"userdomain_roamingprofile",
	L"username",
	L"userprofile",
};

static const std::wregex EnvVarRegex = std::wregex(L"(.+)=(.*)$");

Environment::Environment()
{
}

Environment::Environment(bool inherit)
{
	LPVOID environment;
	if (!CreateEnvironmentBlock(&environment, nullptr, inherit))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"CreateEnvironmentBlock");		
	}

	try
	{
		CreateVariableMap(environment);
	}
	catch(...)
	{
	}

	if (!DestroyEnvironmentBlock(environment))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"DestroyEnvironmentBlock");
	}
}

Environment::Environment(HANDLE token, bool inherit)
{
	LPVOID environment;
	if (!CreateEnvironmentBlock(&environment, token, inherit))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"CreateEnvironmentBlock");
	}

	try
	{
		CreateVariableMap(environment);
	}
	catch (...)
	{
	}

	if (!DestroyEnvironmentBlock(environment))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"DestroyEnvironmentBlock");
	}
}

void Environment::Merge(Environment& baseEnvironment, Environment& mergingEnvironment, Environment& targetEnvironment)
{
	for (auto varsIterator = baseEnvironment._vars.begin(); varsIterator != baseEnvironment._vars.end(); ++varsIterator)
	{
		targetEnvironment._vars[varsIterator->first] = varsIterator->second;
	}	

	for (auto varsIterator = mergingEnvironment._vars.begin(); varsIterator != mergingEnvironment._vars.end(); ++varsIterator)
	{		
		std::wstring varNameInLowCase;
		varNameInLowCase.resize(varsIterator->first.size());
		transform(varsIterator->first.begin(), varsIterator->first.end(), varNameInLowCase.begin(), tolower);
		if (Overrides.find(varNameInLowCase) != Overrides.end())
		{
			targetEnvironment._vars[varsIterator->first] = varsIterator->second;
		}
	}
}

Environment::~Environment()
{	
	for (auto environmentBlockIterator = _environmentBlocks.begin(); environmentBlockIterator != _environmentBlocks.end(); ++environmentBlockIterator)
	{		
		delete *environmentBlockIterator;
	}

	_environmentBlocks.clear();
}

void Environment::CreateVariableMap(LPVOID environment)
{
	_vars.clear();

	auto curVar = static_cast<LPCWSTR>(environment);
	size_t len;
	do
	{
		std::wstring curVarValue(curVar);
		len = curVarValue.size();
		if (len == 0)
		{
			continue;
		}

		curVar += len + 1;
		std::wsmatch matchResult;
		if (!regex_search(curVarValue, matchResult, EnvVarRegex))
		{
			std::wcerr << "Invalid format of environment variable \"" << curVarValue << "\"";
		}

		auto envName = matchResult._At(1).str();
		auto envValue = matchResult._At(2).str();
		_vars[envName] = envValue;
	} while (len > 0);	
}

LPVOID* Environment::CreateEnvironmentFromMap()
{
	size_t size = 0;
	for (auto varsIterator = _vars.begin(); varsIterator != _vars.end(); ++varsIterator)
	{		
		size += varsIterator->first.size() + varsIterator->second.size() + 2;
	}

	size++;

	auto environment = new _TCHAR[size];
	memset(environment, 0, sizeof(_TCHAR) * size);

	size_t pointer = 0;
	for (auto varsIterator = _vars.begin(); varsIterator != _vars.end(); ++varsIterator)
	{
		auto curSize = varsIterator->first.size();		
		memcpy(environment + pointer, varsIterator->first.c_str(), curSize * sizeof(_TCHAR));
		pointer += curSize;

		environment[pointer] = _TCHAR('=');
		pointer ++;

		curSize = varsIterator->second.size();
		memcpy(environment + pointer, varsIterator->second.c_str(), curSize * sizeof(_TCHAR));
		pointer += curSize;		

		pointer ++;
	}	

	return reinterpret_cast<LPVOID*>(environment);
}

LPVOID* Environment::CreateEnvironment()
{
	auto environment = CreateEnvironmentFromMap();
	_environmentBlocks.push_back(environment);
	return environment;
}
