#pragma once
#include <string>
#include "ErrorCode.h"

template<typename T>
class Result
{
	bool _hasError;
	T _resultValue;
	int _errorCode;
	std::wstring _errorDescription;		

public:
	Result<T>();
	Result<T>(T resultValue);
	Result<T>(int errorCode, std::wstring errorDescription);

	bool HasError() const;
	T GetResultValue() const;
	int GetErrorCode() const;
	std::wstring GetErrorDescription() const;
};

template<typename T>
Result<T>::Result() : _hasError(false), _errorCode(ERROR_CODE_UNKOWN), _errorDescription(L"")
{
}

template<typename T>
Result<T>::Result(T resultValue)
{
	_resultValue = resultValue;
	_hasError = false;
	_errorCode = ERROR_CODE_UNKOWN;
}

template<typename T>
Result<T>::Result(int errorCode, std::wstring errorDescription)
{
	_errorCode = errorCode;
	_errorDescription = errorDescription;
	_hasError = true;
}

template<typename T>
bool Result<T>::HasError() const
{
	return _hasError;
}

template<typename T>
T Result<T>::GetResultValue() const
{
	return _resultValue;
}

template<typename T>
int Result<T>::GetErrorCode() const
{
	return _errorCode;
}

template<typename T>
std::wstring Result<T>::GetErrorDescription() const
{
	return _errorDescription;
}
