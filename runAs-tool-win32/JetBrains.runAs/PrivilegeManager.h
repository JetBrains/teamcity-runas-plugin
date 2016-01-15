#pragma once
#include <list>
#include "Result.h"

class Handle;

class PrivilegeManager
{
	static Result<LUID> LookupPrivilegeValue(std::wstring privilegeName);

public:
	PrivilegeManager();
	~PrivilegeManager();
	Result<bool> SetPrivileges(
		Handle& token,          // access token handle
		std::list<std::wstring> privileges,  // names of privileges to enable/disable
		bool enablePrivilege   // to enable or disable privilege
		);
};

