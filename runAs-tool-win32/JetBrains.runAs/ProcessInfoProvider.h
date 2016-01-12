#pragma once
class ProcessInfoProvider
{	
public:
	ProcessInfoProvider();
	~ProcessInfoProvider();

	bool IsServiceProcess() const;
};

