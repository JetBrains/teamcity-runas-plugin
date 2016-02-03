#include "stdafx.h"
#include "Environment.h"
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
	std::vector<std::wstring> vars;
	SplitString(variables, L"\n", vars);

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

Environment Environment::Merge(Environment& baseEnvironment, Environment& mergingEnvironment)
{
	Environment targetEnvironment;
	for (auto varsIterator = baseEnvironment._vars.begin(); varsIterator != baseEnvironment._vars.end(); ++varsIterator)
	{
		targetEnvironment._vars[varsIterator->first] = varsIterator->second;
		targetEnvironment._empty = false;
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

void Environment::SplitString(std::wstring &str, const std::wstring delim, std::vector<std::wstring>& strs)
{
	size_t i = 0;
	auto pos = str.find(delim);
	while (pos != std::wstring::npos) 
	{
		strs.push_back(str.substr(i, pos - i));
		i = ++pos;
		pos = str.find(delim, pos);
		if (pos == std::string::npos)
		{
			strs.push_back(str.substr(i, str.length()));
		}
	}
}
