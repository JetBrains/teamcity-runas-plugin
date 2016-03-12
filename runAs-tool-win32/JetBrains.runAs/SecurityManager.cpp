#include "stdafx.h"
#include "SecurityManager.h"
#include "ErrorUtilities.h"
#include "Handle.h"
#include "StringBuffer.h"
#include <memory>
#include "Trace.h"

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

void SecurityManager::TrySetAllPrivileges(const Handle& token, const bool enablePrivileges)
{
	for (auto privilegiesIterrator = AllPrivilegies.begin(); privilegiesIterrator != AllPrivilegies.end(); ++privilegiesIterrator)
	{
		SetPrivileges(token, { *privilegiesIterrator }, true);
	}
}

Result<bool> SecurityManager::SetPrivileges(const Handle& token, const list<wstring>& privileges, const bool enablePrivileges)
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

Result<LUID> SecurityManager::LookupPrivilegeValue(const wstring& privilegeName)
{
	StringBuffer privilegeNameBuf(privilegeName);
	LUID luid;
	if (!::LookupPrivilegeValue(
		nullptr,						// lookup privilege on local system
		privilegeNameBuf.GetPointer(),	// privilege to lookup 
		&luid))							// receives LUID of privilege
	{	
		return Result<LUID>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"LookupPrivilegeValue"));
	}

	return luid;
}

Result<Handle> SecurityManager::OpenProcessToken(DWORD desiredAccess) const
{
	Handle processToken(L"Process token");
	if (!::OpenProcessToken(GetCurrentProcess(), desiredAccess, &processToken))
	{
		return Result<Handle>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"OpenProcessToken"));
	}

	return processToken;
}

Result<shared_ptr<void>> SecurityManager::GetTokenInformation(Trace& trace, const Handle& token, _TOKEN_INFORMATION_CLASS tokenInformationClass) const
{
	trace < L"SecurityManager::GetTokenInformation - Get the required buffer size and allocate the _TOKEN_INFORMATION_CLASS buffer.";
	LPVOID info = nullptr;
	DWORD length = 0;
	if (!::GetTokenInformation(token, tokenInformationClass, info, 0, &length))
	{
		if (GetLastError() != ERROR_INSUFFICIENT_BUFFER)
		{
			return Result<shared_ptr<void>>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"GetTokenInformation"));
		}
	}

	info = reinterpret_cast<PTOKEN_GROUPS>(new byte[length]);
	memset(info, 0, length);

	trace < L"SecurityManager::GetTokenInformation - Get the token information from the access token.";
	if (!::GetTokenInformation(token, tokenInformationClass, info, length, &length))
	{
		delete info;
		return Result<shared_ptr<void>>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"GetTokenInformation"));
	}

	return shared_ptr<void>(info);
}

Result<list<SID_AND_ATTRIBUTES>> SecurityManager::GetTokenGroups(Trace& trace, const Handle& token) const
{
	auto tokenGroupsResult = GetTokenInformation(trace, token, TokenGroups);
	if (tokenGroupsResult.HasError())
	{
		return Result<list<SID_AND_ATTRIBUTES>>(tokenGroupsResult.GetErrorCode(), tokenGroupsResult.GetErrorDescription());
	}

	list<SID_AND_ATTRIBUTES> result;
	auto groupsInfo = reinterpret_cast<PTOKEN_GROUPS>(tokenGroupsResult.GetResultValue().get());
	for (DWORD index = 0; index < groupsInfo->GroupCount; index++)
	{
		result.push_back(groupsInfo->Groups[index]);
	}

	return result;
}
