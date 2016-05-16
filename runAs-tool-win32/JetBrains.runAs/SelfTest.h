#pragma once
#include "Result.h"
#include "CommanLineParser.h"
#include "Settings.h"
#include "SecurityManager.h"
#include "Handle.h"

class SecurityManager;
class Trace;

class SelfTest
{
	const SecurityManager _securityManager;

	Result<bool> HasLogonSID(Trace& trace, const Handle& token) const;
	Result<bool> HasAdministrativePrivileges(Trace& trace) const;
	Result<bool> HasSeAssignPrimaryTokenPrivilege(Trace& trace, const Handle& token) const;
	static bool IsWow64();
	static bool Is64OS();	
public:
	SelfTest();
	Result<ExitCode> Run(const Settings& settings) const;	
};

