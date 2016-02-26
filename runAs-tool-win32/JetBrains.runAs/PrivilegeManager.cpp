#include "stdafx.h"
#include "PrivilegeManager.h"
#include "ErrorUtilities.h"
#include "Handle.h"

const list<wstring> AllPrivilegies = {
	SE_CREATE_TOKEN_NAME,
	SE_ASSIGNPRIMARYTOKEN_NAME,
	SE_LOCK_MEMORY_NAME,
	SE_INCREASE_QUOTA_NAME,
	SE_UNSOLICITED_INPUT_NAME,
	SE_MACHINE_ACCOUNT_NAME,
	SE_TCB_NAME,
	SE_SECURITY_NAME,
	SE_TAKE_OWNERSHIP_NAME,
	SE_LOAD_DRIVER_NAME,
	SE_SYSTEM_PROFILE_NAME,
	SE_SYSTEMTIME_NAME,
	SE_PROF_SINGLE_PROCESS_NAME,
	SE_INC_BASE_PRIORITY_NAME,
	SE_CREATE_PAGEFILE_NAME,
	SE_CREATE_PERMANENT_NAME,
	SE_BACKUP_NAME,
	SE_RESTORE_NAME,
	SE_SHUTDOWN_NAME,
	SE_DEBUG_NAME,
	SE_AUDIT_NAME,
	SE_SYSTEM_ENVIRONMENT_NAME,
	SE_CHANGE_NOTIFY_NAME,
	SE_REMOTE_SHUTDOWN_NAME,
	SE_UNDOCK_NAME,
	SE_SYNC_AGENT_NAME,
	SE_ENABLE_DELEGATION_NAME,
	SE_MANAGE_VOLUME_NAME,
	SE_IMPERSONATE_NAME,
	SE_CREATE_GLOBAL_NAME,
	SE_TRUSTED_CREDMAN_ACCESS_NAME,
	SE_RELABEL_NAME,
	SE_INC_WORKING_SET_NAME,
	SE_TIME_ZONE_NAME,
	SE_CREATE_SYMBOLIC_LINK_NAME
};

void PrivilegeManager::TrySetAllPrivileges(const bool enablePrivileges)
{
	Handle token(L"Current process token");
	if (!OpenProcessToken(GetCurrentProcess(), TOKEN_ADJUST_PRIVILEGES | TOKEN_QUERY, &token))
	{
		return;
	}

	for (auto privilegiesIterrator = AllPrivilegies.begin(); privilegiesIterrator != AllPrivilegies.end(); ++privilegiesIterrator)
	{
		SetPrivileges(token, { *privilegiesIterrator }, true);
	}
}

Result<bool> PrivilegeManager::SetPrivileges(const list<wstring>& privileges, const bool enablePrivileges)
{
	Handle token(L"Current process token");
	if (!OpenProcessToken(GetCurrentProcess(), TOKEN_ADJUST_PRIVILEGES | TOKEN_QUERY, &token))
	{
		return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"OpenProcessToken"));
	}
	
	return SetPrivileges(token, privileges, enablePrivileges);
}

Result<bool> PrivilegeManager::SetPrivileges(const Handle& token, const list<wstring>& privileges, const bool enablePrivileges)
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
		tokenPrivileges->Privileges[index].Attributes = enablePrivileges ? SE_PRIVILEGE_ENABLED : 0;
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
		return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"AdjustTokenPrivileges"));
	}

	return true;
}

Result<LUID> PrivilegeManager::LookupPrivilegeValue(const wstring& privilegeName)
{
	LUID luid;
	if (!::LookupPrivilegeValue(
		nullptr,					// lookup privilege on local system
		privilegeName.c_str(),		// privilege to lookup 
		&luid))						// receives LUID of privilege
	{	
		return Result<LUID>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"LookupPrivilegeValue"));
	}

	return luid;
}