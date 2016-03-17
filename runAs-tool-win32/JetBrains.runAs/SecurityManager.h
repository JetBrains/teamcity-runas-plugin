#pragma once
#include <list>
#include "Result.h"
#include "Trace.h"
#include <memory>
#include <set>

class Handle;

class SecurityManager
{
	static Result<LUID> LookupPrivilegeValue(const wstring& privilegeName);
	
public:
	void TrySetAllPrivileges(const Handle& token, const bool enablePrivileges);
	Result<bool> SetPrivileges(
		const Handle& token,          // access token handle
		const list<wstring>& privileges,  // names of privileges to enable/disable
		const bool enablePrivileges   // to enable or disable privilege
		);
	Result<shared_ptr<void>> GetTokenInformation(Trace& trace, const Handle& token, _TOKEN_INFORMATION_CLASS tokenInformationClass) const;
	Result<list<SID_AND_ATTRIBUTES>> GetTokenGroups(Trace& trace, const Handle& token) const;
	Result<set<wstring>> GetPrivilegies(Trace& trace, const Handle& token) const;
	Result<bool> IsRunAsAdministrator() const;
};

