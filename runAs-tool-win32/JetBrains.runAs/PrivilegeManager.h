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
	void TrySetAllPrivileges(bool enablePrivileges);
	Result<bool> SetPrivileges(std::list<std::wstring> privileges, bool enablePrivileges);
	Result<bool> SetPrivileges(
		Handle& token,          // access token handle
		std::list<std::wstring> privileges,  // names of privileges to enable/disable
		bool enablePrivileges   // to enable or disable privilege
		);
};

