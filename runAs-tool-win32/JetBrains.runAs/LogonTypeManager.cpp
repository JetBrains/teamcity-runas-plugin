#include "stdafx.h"
#include "LogonTypeManager.h"

DWORD LogonTypeManager::GetLogonTypeFlag(LogonType logonTypeId) const
{
	if (logonTypeId == LOGON_TYPE_INTERACTIVE)
	{
		return LOGON32_LOGON_INTERACTIVE;
	}

	if (logonTypeId == LOGON_TYPE_NETWORK)
	{
		return LOGON32_LOGON_NETWORK;
	}
	
	return LOGON32_LOGON_INTERACTIVE;
}
