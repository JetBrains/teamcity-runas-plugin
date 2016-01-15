#include "stdafx.h"
#include "PrivilegeManager.h"
#include "ErrorUtilities.h"
#include "Handle.h"

PrivilegeManager::PrivilegeManager()
{
}


PrivilegeManager::~PrivilegeManager()
{
}

Result<bool> PrivilegeManager::SetPrivileges(Handle& token, std::list<std::wstring> privileges, bool enablePrivilege)
{
	auto tokenPrivileges = static_cast<PTOKEN_PRIVILEGES>(_alloca(sizeof(TOKEN_PRIVILEGES) + sizeof(LUID_AND_ATTRIBUTES) * (privileges.size() - 1)));
	tokenPrivileges->PrivilegeCount = static_cast<DWORD>(privileges.size());	
	auto index = 0;
	for (auto privilegesIterrator = privileges.begin(); privilegesIterrator != privileges.end(); ++privilegesIterrator)
	{		
		auto privilegeId = LookupPrivilegeValue(*privilegesIterrator);
		if (privilegeId.HasError())
		{
			return Result<bool>(privilegeId.GetErrorCode(), privilegeId.GetErrorDescription());
		}

		tokenPrivileges->Privileges[index].Luid = privilegeId.GetResultValue();
		tokenPrivileges->Privileges[index].Attributes = enablePrivilege ? SE_PRIVILEGE_ENABLED : 0;
		index++;
	}

	if (!AdjustTokenPrivileges(
		token,
		FALSE,
		tokenPrivileges,
		sizeof(TOKEN_PRIVILEGES),
		static_cast<PTOKEN_PRIVILEGES>(nullptr),
		static_cast<PDWORD>(nullptr)))
	{
		auto aa = ErrorUtilities::GetLastErrorMessage(L"AdjustTokenPrivileges");
		return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"AdjustTokenPrivileges"));
	}

	return Result<bool>(true);
}

Result<LUID> PrivilegeManager::LookupPrivilegeValue(std::wstring privilegeName)
{
	LUID luid;
	if (!::LookupPrivilegeValue(
		nullptr,					// lookup privilege on local system
		privilegeName.c_str(),		// privilege to lookup 
		&luid))						// receives LUID of privilege
	{	
		return Result<LUID>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"LookupPrivilegeValue"));
	}

	return Result<LUID>(luid);
}