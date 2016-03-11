#include "stdafx.h"
#include "Handle.h"
#include "Result.h"
#include "ErrorUtilities.h"

Handle::Handle()
	:Handle(L"")
{
}

Handle::Handle(const wstring& name)
{
	_name = name;
	_handle = INVALID_HANDLE_VALUE;
}

void Handle::Close()
{
	if (_handle != nullptr && !IsInvalid())
	{
		CloseHandle(_handle);
	}
}

Handle::~Handle()
{
	Close();
}

Handle::operator HANDLE() const
{
	return _handle;
}

Handle& Handle::operator=(const HANDLE handle)
{
	Close();
	_handle = handle;
	return *this;
}

PHANDLE Handle::operator&()
{
	return &_handle;
}

bool Handle::IsInvalid() const
{
	return _handle == INVALID_HANDLE_VALUE;
}

Result<Handle> Handle::Duplicate(const Handle& sourceProcess, const Handle& targetProcess, DWORD desiredAccess, bool inheritHandle, DWORD options) const
{
	Handle targetHandle(L"Duplicated handle");
	if (DuplicateHandle(
		sourceProcess,
		_handle,
		targetProcess,
		&targetHandle,
		desiredAccess,
		inheritHandle,
		options
		))
	{
		return Result<Handle>(ErrorUtilities::GetErrorCode(), ErrorUtilities::GetLastErrorMessage(L"DuplicateTokenEx"));
	}

	return targetHandle;
}

HANDLE Handle::Detach()
{
	auto handle = _handle;
	_handle = INVALID_HANDLE_VALUE;
	return handle;
}
