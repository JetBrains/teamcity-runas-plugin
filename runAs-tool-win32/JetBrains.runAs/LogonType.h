#pragma once
#include <list>

typedef wstring LogonType;
#define LOGON_TYPE_INTERACTIVE		TEXT("interactive")
#define LOGON_TYPE_NETWORK			TEXT("network")

const std::list<LogonType> LogonTypes = { LOGON_TYPE_INTERACTIVE, LOGON_TYPE_NETWORK };