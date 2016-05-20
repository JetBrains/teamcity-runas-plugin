#include "stdafx.h"
#include "AccountSid.h"

AccountSid::AccountSid(const wstring sid, const wstring name, const wstring domain)
	: _sid(sid), _name(name), _domain(domain)
{
}

wstring AccountSid::GetSid() const
{
	return _sid;
}

wstring AccountSid::GetName() const
{
	return _name;
}

wstring AccountSid::GetDomain() const
{
	return _domain;
}

wstring AccountSid::ToString() const
{
	return GetSid() + L" " + GetDomain() + L"\\" + GetName();
}
