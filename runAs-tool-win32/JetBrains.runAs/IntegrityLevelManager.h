#pragma once
#include "IntegrityLevel.h"
#include "Result.h"
class Handle;
class Trace;

class IntegrityLevelManager
{
public:
	static Result<bool> SetIntegrityLevel(const IntegrityLevel& integrityLevel, const Handle& securityToken, Trace& trace);
	Result<const IntegrityLevel> GetIntegrityLevel(Trace& trace, const Handle& token) const;
};

