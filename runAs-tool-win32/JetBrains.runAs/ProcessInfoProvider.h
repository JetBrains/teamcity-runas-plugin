#pragma once

class ProcessInfoProvider
{	
	bool IsWow64() const;
	bool Is64OS() const;

public:
	ProcessInfoProvider();
	~ProcessInfoProvider();

	bool IsSuitableOS() const;
};

