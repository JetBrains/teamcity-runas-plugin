#include "stdafx.h"
#include "IntegrityLevelManager.h"
#include "Handle.h"
#include "Trace.h"
#include "ErrorUtilities.h"

Result<bool> IntegrityLevelManager::SetIntegrityLevel(const IntegrityLevel& integrityLevelId, const Handle& securityToken, Trace& trace)
{
	if (integrityLevelId != INTEGRITY_LEVEL_AUTO)
	{
		trace < L"ProcessAsUser::Set integrity level to \"";
		trace << integrityLevelId;
		trace << L"\"";

		DWORD integrityLevel = SECURITY_MANDATORY_UNTRUSTED_RID;
		if (integrityLevelId == INTEGRITY_LEVEL_UNTRUSTED)
		{
			integrityLevel = SECURITY_MANDATORY_UNTRUSTED_RID;
		}

		if (integrityLevelId == INTEGRITY_LEVEL_LOW)
		{
			integrityLevel = SECURITY_MANDATORY_LOW_RID;
		}

		if (integrityLevelId == INTEGRITY_LEVEL_MEDIUM)
		{
			integrityLevel = SECURITY_MANDATORY_MEDIUM_RID;
		}

		if (integrityLevelId == INTEGRITY_LEVEL_MEDIUM_PLUS)
		{
			integrityLevel = SECURITY_MANDATORY_MEDIUM_PLUS_RID;
		}

		if (integrityLevelId == INTEGRITY_LEVEL_HIGH)
		{
			integrityLevel = SECURITY_MANDATORY_HIGH_RID;
		}

		SID integrityLevelSid {};
		integrityLevelSid.Revision = SID_REVISION;
		integrityLevelSid.SubAuthorityCount = 1;
		integrityLevelSid.IdentifierAuthority.Value[5] = 16;
		integrityLevelSid.SubAuthority[0] = integrityLevel;

		TOKEN_MANDATORY_LABEL tokenIntegrityLevel = {};
		tokenIntegrityLevel.Label.Attributes = SE_GROUP_INTEGRITY;
		tokenIntegrityLevel.Label.Sid = &integrityLevelSid;

		trace < L"::SetTokenInformation";
		if (!SetTokenInformation(
			securityToken,
			TokenIntegrityLevel,
			&tokenIntegrityLevel,
			sizeof(TOKEN_MANDATORY_LABEL) + GetLengthSid(&integrityLevelSid)))
		{
			return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"SetTokenInformation"));
		}

		return true;
	}

	return false;
}
