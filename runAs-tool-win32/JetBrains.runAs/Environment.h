#pragma once
#include <list>
#include <map>
#include "Result.h"
#include "Handle.h"
#include <set>

class Environment
{		
	list<LPVOID*> _environmentBlocks;
	map<wstring, wstring> _vars;
	bool _empty = true;

	void CreateVariableMap(LPVOID environment);
	LPVOID* CreateEnvironmentFromMap();

public:
	static Result<Environment> CreateForCurrentProcess();
	static Result<Environment> CreateForUser(Handle& token, bool inherit);
	static Environment CreateFormString(wstring variables);
	static Environment Override(Environment& baseEnvironment, Environment& mergingEnvironment);
	~Environment();
	LPVOID* CreateEnvironment();
	wstring TryGetValue(wstring variableName);
	static set<wstring> GetAutoOverrides();	
};
