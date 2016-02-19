#pragma once
#include <list>
#include "Result.h"

class Handle;

class PrivilegeManager
{
	static Result<LUID> LookupPrivilegeValue(wstring privilegeName);

public:
	PrivilegeManager();
	~PrivilegeManager();
	void TrySetAllPrivileges(bool enablePrivileges);
	Result<bool> SetPrivileges(list<wstring> privileges, bool enablePrivileges);
	Result<bool> SetPrivileges(
		Handle& token,          // access token handle
		list<wstring> privileges,  // names of privileges to enable/disable
		bool enablePrivileges   // to enable or disable privilege
		);
};

