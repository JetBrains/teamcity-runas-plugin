#include "stdafx.h"
#include "IntegrityLevelManager.h"
#include "Handle.h"
#include "Trace.h"
#include <memory>
#include "SecurityManager.h"

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

		if (integrityLevelId == INTEGRITY_LEVEL_SYSTEM)
		{
			integrityLevel = SECURITY_MANDATORY_SYSTEM_RID;
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
			return Error(L"SetTokenInformation");
		}

		return true;
	}

	return false;
}

Result<const IntegrityLevel> IntegrityLevelManager::GetIntegrityLevel(Trace& trace, const Handle& token) const
{
	SecurityManager securityManager;
	auto tokenInformationResult = securityManager.GetTokenInformation(trace, token, TokenIntegrityLevel);
	if (tokenInformationResult.HasError())
	{
		return tokenInformationResult.GetError();
	}

	auto pTIL = reinterpret_cast<PTOKEN_MANDATORY_LABEL>(tokenInformationResult.GetResultValue().get());
	auto dwIntegrityLevel = *GetSidSubAuthority(pTIL->Label.Sid, static_cast<DWORD>(static_cast<UCHAR>(*GetSidSubAuthorityCount(pTIL->Label.Sid) - 1)));
	switch (dwIntegrityLevel)
	{
		case SECURITY_MANDATORY_UNTRUSTED_RID:
			return Result<const IntegrityLevel>(INTEGRITY_LEVEL_UNTRUSTED);

		case SECURITY_MANDATORY_LOW_RID:
			return Result<const IntegrityLevel>(INTEGRITY_LEVEL_LOW);

		case SECURITY_MANDATORY_MEDIUM_RID:
			return Result<const IntegrityLevel>(INTEGRITY_LEVEL_MEDIUM);

		case SECURITY_MANDATORY_MEDIUM_PLUS_RID:
			return Result<const IntegrityLevel>(INTEGRITY_LEVEL_MEDIUM_PLUS);

		case SECURITY_MANDATORY_HIGH_RID:
			return Result<const IntegrityLevel>(INTEGRITY_LEVEL_HIGH);

		case SECURITY_MANDATORY_SYSTEM_RID:
			return Result<const IntegrityLevel>(INTEGRITY_LEVEL_SYSTEM);
	}

	return Result<const IntegrityLevel>(INTEGRITY_LEVEL_AUTO);
}