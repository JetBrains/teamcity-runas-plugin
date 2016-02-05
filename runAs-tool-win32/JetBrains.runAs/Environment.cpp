#include "stdafx.h"
#include "Environment.h"
#include <regex>
#include <set>
#include "StringUtilities.h"

static const std::wregex EnvVarRegex = std::wregex(L"(.+)=(.*)");

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

Result<Environment> Environment::CreateFormString(std::wstring variables)
{
	auto vars = StringUtilities::Split(variables, L"\n");

	Environment environment;
	std::wsmatch matchResult;	
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

std::wstring Environment::TryGetValue(std::wstring variableName)
{
	std::wstring curVarNameInLowCase;
	curVarNameInLowCase.resize(variableName.size());
	transform(variableName.begin(), variableName.end(), curVarNameInLowCase.begin(), tolower);

	for (auto varsIterator = _vars.begin(); varsIterator != _vars.end(); ++varsIterator)
	{
		std::wstring varNameInLowCase;
		varNameInLowCase.resize(varsIterator->first.size());
		transform(varsIterator->first.begin(), varsIterator->first.end(), varNameInLowCase.begin(), tolower);
		if (varNameInLowCase == curVarNameInLowCase)
		{
			return varsIterator->second;
		}
	}

	return L"";
}