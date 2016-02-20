#pragma once
#include "Handle.h"
#include "Result.h"
class Handle;

class Job
{
	Handle _handle = Handle(L"Job");
	
public:
	Job();
	Result<bool> SetInformation(JOBOBJECTINFOCLASS infoClass, JOBOBJECT_EXTENDED_LIMIT_INFORMATION& information) const;
	Result<JOBOBJECT_EXTENDED_LIMIT_INFORMATION> QueryInformation(JOBOBJECTINFOCLASS infoClass) const;
	Result<bool> AssignProcessToJob(Handle& process) const;
};

