#pragma once
#include <list>
#include "Result.h"

class Handle;

class PrivilegeManager
{
	static Result<LUID> LookupPrivilegeValue(const wstring& privilegeName);

public:
	void TrySetAllPrivileges(const bool enablePrivileges);
	Result<bool> SetPrivileges(const list<wstring>& privileges, const bool enablePrivileges);
	Result<bool> SetPrivileges(
		const Handle& token,          // access token handle
		const list<wstring>& privileges,  // names of privileges to enable/disable
		const bool enablePrivileges   // to enable or disable privilege
		);
};

