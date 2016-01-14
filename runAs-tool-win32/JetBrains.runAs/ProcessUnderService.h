#pragma once
#include "IProcess.h"
#include "Settings.h"
class Settings;

class ProcessUnderService: public IProcess
{	
public:
	virtual int Run(Settings& settings) const override;
};