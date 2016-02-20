#include "stdafx.h"
#include "Job.h"
#include "ErrorUtilities.h"

Job::Job()
{
	_handle = CreateJobObject(nullptr, nullptr);
}

Result<bool> Job::SetInformation(JOBOBJECTINFOCLASS infoClass, JOBOBJECT_EXTENDED_LIMIT_INFORMATION& information) const
{		
	if(_handle.IsInvalid())
	{
		return false;
	}

	if (!SetInformationJobObject(_handle, infoClass, &information, sizeof(JOBOBJECT_EXTENDED_LIMIT_INFORMATION)))
	{
		return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"DuplicateTokenEx"));
	}

	return true;
}

Result<JOBOBJECT_EXTENDED_LIMIT_INFORMATION> Job::QueryInformation(JOBOBJECTINFOCLASS infoClass) const
{
	JOBOBJECT_EXTENDED_LIMIT_INFORMATION information{};
	if (_handle.IsInvalid())
	{
		return information;
	}
	
	DWORD lpReturnLength = 0;
	if (!QueryInformationJobObject(_handle, infoClass, &information, sizeof(JOBOBJECT_EXTENDED_LIMIT_INFORMATION), &lpReturnLength))
	{
		return Result<JOBOBJECT_EXTENDED_LIMIT_INFORMATION>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"DuplicateTokenEx"));
	}

	return information;
}

Result<bool> Job::AssignProcessToJob(Handle& process) const
{
	if (_handle.IsInvalid())
	{
		return false;
	}

	if (!AssignProcessToJobObject(_handle, process))
	{
		return Result<bool>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"DuplicateTokenEx"));
	}

	return true;
}
