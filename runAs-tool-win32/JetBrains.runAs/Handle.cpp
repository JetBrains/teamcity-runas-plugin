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
	_refCounter = new int;
	*_refCounter = 0;
	(*_refCounter)++;
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
	(*_refCounter)--;
	if(*_refCounter == 0)
	{
		delete _refCounter;
		Close();
	}
}

Handle::operator HANDLE() const
{
	return _handle;
}

Handle& Handle::operator=(const HANDLE handle)
{
	_handle = handle;
	return *this;
}

Handle& Handle::operator=(const Handle handle)
{
	delete this->_refCounter;
	this->_refCounter = handle._refCounter;
	this->_name = handle._name;
	this->_handle = handle._handle;
	(*_refCounter)+=2;
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