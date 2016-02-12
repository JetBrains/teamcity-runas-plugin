#pragma once
#include <list>
#include <map>
#include "Result.h"

class Environment
{		
	std::list<LPVOID*> _environmentBlocks;
	std::map<std::wstring, std::wstring> _vars;
	bool _empty = true;

	void CreateVariableMap(LPVOID environment);
	LPVOID* CreateEnvironmentFromMap();	

public:
	static Result<Environment> CreateForCurrentProcess();	
	~Environment();
	LPVOID* CreateEnvironment();
	std::wstring TryGetValue(std::wstring variableName);
};