#pragma once
#include "IntegrityLevel.h"

class SelfTestStatistic
{
	const bool _hasLogonSid;
	const bool _hasAdministrativePrivileges;
	const bool _hasSeAssignPrimaryTokenPrivilege;
	const IntegrityLevel _integrityLevel;

public:
	SelfTestStatistic();
	SelfTestStatistic(
		const bool hasLogonSID,
		const bool hasAdministrativePrivileges,
		const bool hasSeAssignPrimaryTokenPrivilege,
		const IntegrityLevel integrityLevel);

	bool HasLogonSid() const;
	bool HasAdministrativePrivileges() const;
	bool HasSeAssignPrimaryTokenPrivilege() const;
	IntegrityLevel GetIntegrityLevel() const;
};

