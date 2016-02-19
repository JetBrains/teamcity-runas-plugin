#include "stdafx.h"
#include "Environment.h"
#include <regex>
#include <set>
#include "Handle.h"
#include "ErrorUtilities.h"
#include "StringUtilities.h"

static const wregex EnvVarRegex = wregex(L"(.+)=(.*)");

// In upper case
static const set<wstring> AutoOverrides = {
	L"APPDATA",
	L"HOMEPATH",
	L"LOCALAPPDATA",
	L"USERDOMAIN",
	L"USERDOMAIN_ROAMINGPROFILE",
	L"USERNAME",
	L"USERPROFILE"
};

Result<Environment> Environment::CreateForCurrentProcess()
{
	auto environment = GetEnvironmentStringsW();
	Environment newEnvironment;
	try
	{
		newEnvironment.CreateVariableMap(environment);
	}
	catch(...)
	{		
	}	

	FreeEnvironmentStringsW(environment);
	return newEnvironment;
}

Result<Environment> Environment::CreateForUser(Handle& token, bool inherit)
{
	Environment newEnvironment;
	LPVOID environment;
	if (!CreateEnvironmentBlock(&environment, token, inherit))
	{
		return Result<Environment>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"CreateEnvironmentBlock"));
	}

	try
	{
		newEnvironment.CreateVariableMap(environment);
	}
	catch (...)
	{
	}

	DestroyEnvironmentBlock(environment);
	return newEnvironment;
}

Environment Environment::CreateFormString(wstring variables)
{
	auto vars = StringUtilities::Split(variables, L"\n");

	Environment environment;
	wsmatch matchResult;
	for (auto varsIterrator = vars.begin(); varsIterrator != vars.end(); ++varsIterrator)
	{
		if (!regex_search(*varsIterrator, matchResult, EnvVarRegex))
		{
			continue;
		}

		auto envName = matchResult._At(1).str();
		auto envValue = matchResult._At(2).str();
		environment._vars[envName] = envValue;
		environment._empty = false;
	}

	return environment;
}

Environment Environment::Override(Environment& destinarionEnvironment, Environment& sourceEnvironment)
{
	Environment targetEnvironment;
	for (auto varsIterator = destinarionEnvironment._vars.begin(); varsIterator != destinarionEnvironment._vars.end(); ++varsIterator)
	{
		targetEnvironment._vars[varsIterator->first] = varsIterator->second;
		targetEnvironment._empty = false;
	}

	auto autoOverrides = GetAutoOverrides();
	for (auto varsIterator = sourceEnvironment._vars.begin(); varsIterator != sourceEnvironment._vars.end(); ++varsIterator)
	{
		auto varNameInLowCase = StringUtilities::Convert(varsIterator->first, toupper);
		if (autoOverrides.find(varNameInLowCase) != autoOverrides.end())
		{
			targetEnvironment._vars[varsIterator->first] = varsIterator->second;
		}
	}

	return targetEnvironment;
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
		wstring curVarValue(curVar);
		len = curVarValue.size();
		if (len == 0)
		{
			continue;
		}

		curVar += len + 1;
		wsmatch matchResult;
		if (!regex_search(curVarValue, matchResult, EnvVarRegex))
		{
			continue;
		}

		auto envName = matchResult._At(1).str();
		auto envValue = matchResult._At(2).str();
		_vars[envName] = envValue;
		_empty = false;
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
	if(_empty)
	{
		return nullptr;
	}

	auto environment = CreateEnvironmentFromMap();
	_environmentBlocks.push_back(environment);
	return environment;
}

wstring Environment::TryGetValue(wstring variableName)
{
	auto curVarNameInLowCase = StringUtilities::Convert(variableName, tolower);
	for (auto varsIterator = _vars.begin(); varsIterator != _vars.end(); ++varsIterator)
	{
		auto varNameInLowCase = StringUtilities::Convert(varsIterator->first, tolower);
		if (varNameInLowCase == curVarNameInLowCase)
		{
			return varsIterator->second;
		}
	}

	return L"";
}

set<wstring> Environment::GetAutoOverrides()
{
	return AutoOverrides;
}