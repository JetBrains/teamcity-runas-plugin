#pragma once
#include <list>
#include <map>

class Environment
{		
	std::list<LPVOID*> _environmentBlocks;
	std::map<std::wstring, std::wstring> _vars;

	void CreateVariableMap(LPVOID environment);
	LPVOID* CreateEnvironmentFromMap();	

public:
	Environment();
	explicit Environment(bool inherit);
	Environment(HANDLE token, bool inherit);
	static void Environment::Merge(Environment& baseEnvironment, Environment& newEnvironment, Environment& targetEnvironment);
	~Environment();

	LPVOID* CreateEnvironment();
};
