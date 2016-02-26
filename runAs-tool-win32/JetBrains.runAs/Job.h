#pragma once
#include "Handle.h"
#include "Result.h"
class Handle;

class Job
{
	Handle _handle = Handle(L"Job");
	
public:
	Job();
	Result<bool> SetInformation(const JOBOBJECTINFOCLASS& infoClass, JOBOBJECT_EXTENDED_LIMIT_INFORMATION& information) const;
	Result<JOBOBJECT_EXTENDED_LIMIT_INFORMATION> QueryInformation(const JOBOBJECTINFOCLASS& infoClass) const;
	Result<bool> AssignProcessToJob(const Handle& process) const;
};

