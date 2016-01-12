#pragma once
#include "IProcess.h"
#include "Settings.h"
class Settings;

class ServiceProcess: public IProcess
{	
public:
	virtual int Run(Settings& settings) const override;
};